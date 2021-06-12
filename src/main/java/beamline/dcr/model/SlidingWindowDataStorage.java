package beamline.dcr.model;

import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.dfg.RelationDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

public class SlidingWindowDataStorage implements DataStorage {
    //based on number of elements in queue

    HashMap<String, List<String>> observedActivitiesInTrace = new HashMap<>();
    private Map<String, String> latestActivityInCase = new HashMap<String, String>();
    private ExtendedDFG extendedDFG = new ExtendedDFG();

    private final int maxSize;

    public SlidingWindowDataStorage(int maxSize) {
        this.maxSize = maxSize;
    }
    @Override
    public void observeEvent(String traceId, String activityName){
        boolean firstOccurrance = true;
        if (observedActivitiesInTrace.containsKey(traceId)) {
            if(observedActivitiesInTrace.get(traceId).size()>=maxSize) removeActivity(traceId,activityName);

            if (observedActivitiesInTrace.get(traceId).contains(activityName)) {
                firstOccurrance = false;
            } else {
                observedActivitiesInTrace.get(traceId).add(activityName);
            }
        } else {
            observedActivitiesInTrace.put(traceId, new ArrayList<>(Arrays.asList(activityName)));
        }
        int currentIndex = observedActivitiesInTrace.get(traceId).size()-1;
        ActivityDecoration activityDecoration = extendedDFG.addActivityIfNeeded(activityName);
        activityDecoration.addNewObservation(currentIndex, firstOccurrance);

        if (latestActivityInCase.containsKey(traceId)) {
            String previousActivity = latestActivityInCase.get(traceId);
            RelationDecoration relationDecoration = extendedDFG.addRelationIfNeeded(previousActivity, activityName);
            relationDecoration.addNewObservation();
        }
        latestActivityInCase.put(traceId, activityName);

    }

    @Override
    public ExtendedDFG getExtendedDFG() {
        return extendedDFG;
    }

    private void removeActivity(String traceId, String activityName){
        List<String> activitiesInTrace = observedActivitiesInTrace.get(traceId);

        //Remove index 0 decoration
        ActivityDecoration activityDecoration = extendedDFG.getActivityDecoration(activityName);
        activityDecoration.removeObservation();
        //decrement dfg Relation
        RelationDecoration relationDecoration = extendedDFG.getRelation(Pair.of(activityName, activitiesInTrace.get(1)));
        relationDecoration.decrementFrequency();
        activitiesInTrace.remove(0);

        Set<String> firstActivityOccurrence = new HashSet<>();

        for(int i = 0; i < activitiesInTrace.size(); i++){
            String activityToAdjust = activitiesInTrace.get(i);
            activityDecoration = extendedDFG.getActivityDecoration(activitiesInTrace.get(i));
            boolean firstOccurrenceOfActivity = firstActivityOccurrence.add(activityToAdjust);
            if (activityName.equals(activityToAdjust) && firstOccurrenceOfActivity){
                activityDecoration.incrementNumFirstObservations();
            }
            activityDecoration.decrementDecorations(firstOccurrenceOfActivity);

        }

    }

    public void convertWindowToLog(String fileName) throws TransformerException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document document = documentBuilder.newDocument();

        Element root = document.createElement("log");
        document.appendChild(root);

        observedActivitiesInTrace.forEach((key, value) -> {
            Element trace = document.createElement("trace");
            trace.setAttribute("id",key);
            for (String activityName : value){
                Element event = document.createElement("event");
                event.setAttribute("id",activityName);
                trace.appendChild(event);
            }
            root.appendChild(trace);
        });
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(fileName + ".xml"));
        transformer.transform(domSource, streamResult);
    }
}
