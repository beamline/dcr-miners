package beamline.dcr.model;

import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.dfg.RelationDecoration;

import java.util.*;

public class UnlimitedDataStorage implements DataStorage{
    private Map<String, String> latestActivityInTrace = new HashMap<String, String>();
    private Map<String, Integer> indexInCase = new HashMap<String, Integer>();
    private Map<String, Set<String>> observedActivitiesInTrace = new HashMap<String, Set<String>>();
    private ExtendedDFG extendedDFG = new ExtendedDFG();


    @Override
    public void observeEvent(String traceId, String activityName) {
        int currentIndex = 1;
        if (indexInCase.containsKey(traceId)) {
            currentIndex = indexInCase.get(traceId);
        }
        boolean firstOccurrance = true;
        if (observedActivitiesInTrace.containsKey(traceId)) {
            if (observedActivitiesInTrace.get(traceId).contains(activityName)) {
                firstOccurrance = false;
            } else {
                observedActivitiesInTrace.get(traceId).add(activityName);
            }
        } else {
            observedActivitiesInTrace.put(traceId, new HashSet<String>(Arrays.asList(activityName)));
        }

        ActivityDecoration activityDecoration = extendedDFG.addActivityIfNeeded(activityName);
        activityDecoration.addNewObservation(currentIndex, firstOccurrance);

        if (latestActivityInTrace.containsKey(traceId)) {
            String previousActivity = latestActivityInTrace.get(traceId);
            RelationDecoration relationDecoration = extendedDFG.addRelationIfNeeded(previousActivity, activityName);
            relationDecoration.addNewObservation();
        }
        latestActivityInTrace.put(traceId, activityName);
        indexInCase.put(traceId, currentIndex + 1);
    }

    @Override
    public ExtendedDFG getExtendedDFG() {
        return extendedDFG;
    }
}
