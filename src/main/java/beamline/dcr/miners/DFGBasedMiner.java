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
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.DcrModel.RELATION;
import beamline.dcr.model.UnionRelationSet;
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
                type = MinerParameter.Type.CHOICE, defaultValue = "ImmCondition, "),},
        viewParameters = {}
)
public class DFGBasedMiner extends AbstractMiner {

    private Map<String, String> latestActivityInCase = new HashMap<String, String>();
    private Map<String, Integer> indexInCase = new HashMap<String, Integer>();
    private Map<String, Set<String>> observedActivitiesInCase = new HashMap<String, Set<String>>();
    private ExtendedDFG extendedDFG = new ExtendedDFG();

    private Reflections reflections;
    private Set<Class<?>> dcrPatternClasses;


    private String[] dcrPatternList;
    private Set<String> postorderTraversal = new LinkedHashSet<>();

    public DFGBasedMiner(){
        this.reflections = new Reflections("beamline");
        this.dcrPatternClasses = reflections.getTypesAnnotatedWith(ExposedDcrPattern.class);
    }
    @Override
    public void configure(Collection<MinerParameterValue> collection) {
        for(MinerParameterValue v : collection) {
            if (v.getName().equals("DCR Patterns")) {
                this.dcrPatternList = (String[]) v.getValue();
            }
        }
    }

    @Override
    public void consumeEvent(String caseID, String activityName) {
        int currentIndex = 1;
        if (indexInCase.containsKey(caseID)) {
            currentIndex = indexInCase.get(caseID);
        }
        boolean firstOccurrance = true;
        if (observedActivitiesInCase.containsKey(caseID)) {
            if (observedActivitiesInCase.get(caseID).contains(activityName)) {
                firstOccurrance = false;
            } else {
                observedActivitiesInCase.get(caseID).add(activityName);
            }
        } else {
            observedActivitiesInCase.put(caseID, new HashSet<String>(Arrays.asList(activityName)));
        }

        ActivityDecoration activityDecoration = extendedDFG.addActivityIfNeeded(activityName);
        activityDecoration.addNewObservation(currentIndex, firstOccurrance);

        if (latestActivityInCase.containsKey(caseID)) {
            String previousActivity = latestActivityInCase.get(caseID);
            RelationDecoration relationDecoration = extendedDFG.addRelationIfNeeded(previousActivity, activityName);
            relationDecoration.addNewObservation();
        }
        latestActivityInCase.put(caseID, activityName);
        indexInCase.put(caseID, currentIndex + 1);
    }

    @Override
    public List<MinerView> getViews(Collection<MinerParameterValue> collection) {
        List<MinerView> views = new ArrayList<>();

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
            addDependenciesToPostorderSet(originalPattern,unionRelationSet);
        }

        //Post processing
        /*TransitiveReduction transitiveReduction = new TransitiveReduction();


        transitiveReduction.reduce(unionRelationSet,RELATION.CONDITION);
        transitiveReduction.reduce(unionRelationSet,RELATION.RESPONSE);*/

        //project user selected patterns to DCR Model
        for (String dcrPattern : dcrPatternList){
            RELATION enumPattern = RELATION.valueOf(dcrPattern.toUpperCase());
            Set<Triple<String, String, RELATION>> minedPatterns = unionRelationSet.getDcrRelationWithPattern(enumPattern);
            model.addRelations(minedPatterns);
        }

        return model;
    }

    private void addDependenciesToPostorderSet(String root,UnionRelationSet unionRelationSet) throws IllegalAccessException, InstantiationException {
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



}
