package beamline.dcr.miners;

import java.util.*;
import java.util.stream.Collectors;

import beamline.core.miner.AbstractMiner;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.MinerViewRaw;
import beamline.core.web.miner.models.MinerView.Type;
import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.dfg.RelationDecoration;
import beamline.dcr.model.relations.RelationPattern;
import beamline.dcr.view.DcrModelText;
import org.apache.commons.lang3.tuple.Triple;
import org.reflections.Reflections;

@ExposedMiner(
        name = "DFG based DCR miner",
        description = "This miner discovers a DCR model form a DFG",
        configurationParameters = {},
        viewParameters = {}
)
public class DFGBasedMiner extends AbstractMiner {

    private Map<String, String> latestActivityInCase = new HashMap<String, String>();
    private Map<String, Integer> indexInCase = new HashMap<String, Integer>();
    private Map<String, Set<String>> observedActivitiesInCase = new HashMap<String, Set<String>>();
    private ExtendedDFG extendedDFG = new ExtendedDFG();

    @Override
    public void configure(Collection<MinerParameterValue> collection) {
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
            views.add(new MinerView("DCR", new DcrModelText(convert(extendedDFG)).toString(), Type.RAW));
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


        Set<Class<RelationPattern>> dcrPatternRelations = getPreorderDcrPatterns();

        for (Class<RelationPattern> relationPattern : dcrPatternRelations){
            RelationPattern dcrPattern = relationPattern.newInstance();
            dcrPattern.populateConstraint(unionRelationSet);
        }

        //Add dcr patterns to DCR Model
        //TODO Chance model.add to take input Triple
        for (Triple<String, String, DcrModel.RELATION> pattern : unionRelationSet.getDcrRelations()){
            model.addRelation(pattern.getLeft(),pattern.getMiddle(),pattern.getRight());
        }

        return model;
    }

    private Set<Class<RelationPattern>> getPreorderDcrPatterns() {

        Reflections reflections = new Reflections("beamline");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ExposedDcrPattern.class);

        Set<Class<RelationPattern>> prioritizedDcrSet = annotated.stream()
                .sorted(Comparator.comparing(
                        aClass -> aClass.getAnnotation(ExposedDcrPattern.class).latticeLevel())
                )
                .map(aClass -> (Class<RelationPattern>) aClass)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return prioritizedDcrSet;
    }

}
