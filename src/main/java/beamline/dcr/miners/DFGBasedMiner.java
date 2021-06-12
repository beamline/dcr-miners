package beamline.dcr.miners;

import java.util.*;

import beamline.core.miner.AbstractMiner;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.annotations.ExposedMinerParameter;
import beamline.core.web.miner.models.MinerParameter;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.MinerViewRaw;
import beamline.core.web.miner.models.MinerView.Type;
import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.*;
import beamline.dcr.model.DcrModel.RELATION;
import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.dfg.RelationDecoration;
import beamline.dcr.model.patterns.RelationPattern;
import beamline.dcr.view.DcrModelText;
import beamline.dcr.view.DcrModelXML;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.reflections.Reflections;


@ExposedMiner(
        name = "DFG based DCR miner",
        description = "This miner discovers a DCR model form a DFG",
        //TODO: Get all RELATION values in defaultValue
        configurationParameters = {@ExposedMinerParameter(name = "DCR Patterns",
                type = MinerParameter.Type.CHOICE, defaultValue = "Condition"),
                @ExposedMinerParameter(name = "Data Storage",
                        type = MinerParameter.Type.CHOICE, defaultValue = "Sliding Window"),
                @ExposedMinerParameter(name = "Max Size Window",
                        type = MinerParameter.Type.INTEGER, defaultValue = "20")
        },
        viewParameters = {}
)
public class DFGBasedMiner extends AbstractMiner {


    private Reflections reflections;
    private Set<Class<?>> dcrPatternClasses;
    private DataStorage dataStorage;


    private String[] dcrPatternList;
    private Set<String> postorderTraversal = new LinkedHashSet<>();

    public DFGBasedMiner(){
        this.reflections = new Reflections("beamline");
        this.dcrPatternClasses = reflections.getTypesAnnotatedWith(ExposedDcrPattern.class);
    }
    @Override
    public void configure(Collection<MinerParameterValue> collection) {
        String dataStorageString = "";
        Integer windowMax = null;
        for(MinerParameterValue v : collection) {
            if (v.getName().equals("DCR Patterns")) {
                this.dcrPatternList = (String[]) v.getValue();
            }else if (v.getName().equals("Data Storage")){
                dataStorageString = (String) v.getValue();
            }else if (v.getName().equals("Max Size Window")){
                windowMax = (Integer) v.getValue();
            }

        }
        switch (dataStorageString){
            case "Sliding Window":
                this.dataStorage = new SlidingWindowDataStorage(windowMax);
            default:
                this.dataStorage = new UnlimitedDataStorage();
        }
    }

    @Override
    public void consumeEvent(String caseID, String activityName) {
        this.dataStorage.observeEvent(caseID,activityName);

    }

    @Override
    public List<MinerView> getViews(Collection<MinerParameterValue> collection) {
        List<MinerView> views = new ArrayList<>();
        ExtendedDFG extendedDFG = dataStorage.getExtendedDFG();
        views.add(new MinerViewRaw("DFG", extendedDFG.toString()));

        //views.add(new MinerView("DCR", new DcrModelView(convert(extendedDFG)).toString(), Type.GRAPHVIZ));
        try {
            //set of SESE's
            //each SESE
            DcrModel dcrModelConverted = convert(extendedDFG);
            views.add(new MinerView("DCR", new DcrModelText(dcrModelConverted).toString(), Type.RAW));
            //write Json
            for(MinerParameterValue v : collection) {
                if (v.getName().equals("filename")) {
                    //new DcrModelJson(dcrModelConverted).toFile(v.getValue().toString());
                    new DcrModelXML(dcrModelConverted).toFile(v.getValue().toString());
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return views;
    }


    private DcrModel convert(ExtendedDFG dfg) throws IllegalAccessException, InstantiationException {
        DcrModel model = new DcrModel();

        int DFGThreshold = 0;  //Can be input in convert

        UnionRelationSet unionRelationSet = new UnionRelationSet(dfg,DFGThreshold);

        //Create set of patterns to mine
        for (String originalPattern : dcrPatternList){
            minePatternsFromPostOrderDependencies(originalPattern,unionRelationSet);
        }

        //Post processing
        TransitiveReduction transitiveReduction = new TransitiveReduction();


        transitiveReduction.reduce(unionRelationSet,RELATION.CONDITION);
        transitiveReduction.reduce(unionRelationSet,RELATION.RESPONSE);

        //project user selected patterns to DCR Model
        for (String dcrPattern : dcrPatternList){
            RELATION enumPattern = RELATION.valueOf(dcrPattern.toUpperCase());
            Set<Triple<String, String, RELATION>> minedPatterns = unionRelationSet.getDcrRelationWithPattern(enumPattern);
            model.addRelations(minedPatterns);
        }

        return model;
    }
    private void minePatternsFromPostOrderDependencies(String root, UnionRelationSet unionRelationSet) throws IllegalAccessException, InstantiationException {
        int currentRootIndex = 0;
        Stack<Pair> stack = new Stack<>();

        while (root != null || !stack.isEmpty()) {
            if (root != null) {

                stack.push(Pair.of(root, currentRootIndex));
                currentRootIndex = 0;

                String[] dcrDependencies = getDcrDependencies(root);
                if (dcrDependencies.length>=1) {
                    root = dcrDependencies[0];
                }
                else {
                    root = null;
                }
                continue;
            }

            Pair temp = stack.pop();
            if (!postorderTraversal.contains(temp.getLeft().toString())){
                minePattern(temp.getLeft().toString(),unionRelationSet);
                postorderTraversal.add(temp.getLeft().toString());

            }
            //postorderTraversal.add(temp.getLeft().toString());

            while (!stack.isEmpty() && (int) temp.getRight() ==
                    getDcrDependencies(stack.peek().getLeft().toString()).length - 1){
                temp = stack.pop();
                if (!postorderTraversal.contains(temp.getLeft().toString())){
                    minePattern(temp.getLeft().toString(),unionRelationSet);
                    postorderTraversal.add(temp.getLeft().toString());

                }
            }

            if (!stack.isEmpty()) {
                String[] dependencies = getDcrDependencies(stack.peek().getLeft().toString());
                root = dependencies[
                        (int) temp.getRight() + 1];
                currentRootIndex = (int) temp.getRight() + 1;
            }
        }
    }
    private String[] getDcrDependencies(String dcr){
        return getExposedPatternClass(dcr).getAnnotation(ExposedDcrPattern.class).dependencies();
    }
    private RelationPattern getPatternMinerClass (String patternName) throws IllegalAccessException, InstantiationException {
        return (RelationPattern) getExposedPatternClass(patternName).newInstance();
    }
    private Class<?> getExposedPatternClass(String patternName){
        for (Class<?> exposedPatternClass : dcrPatternClasses) {
            ExposedDcrPattern exposedPattern = exposedPatternClass.getAnnotation(ExposedDcrPattern.class);
            if (exposedPattern.name().equals(patternName)) {
                return exposedPatternClass;
            }
        }
        return null;

    }
    private void minePattern(String patternName,UnionRelationSet unionRelationSet) throws InstantiationException, IllegalAccessException {
        RelationPattern patternToMine = getPatternMinerClass(patternName);
        patternToMine.populateConstraint(unionRelationSet);
    }

    //For testsuite

    public ExtendedDFG getExtendedDFG() {
        return dataStorage.getExtendedDFG();
    }

}
