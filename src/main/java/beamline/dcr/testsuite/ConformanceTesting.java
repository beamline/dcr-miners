package beamline.dcr.testsuite;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeMapLazyImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.out.XesXmlSerializer;
import org.deckfour.xes.util.XAttributeUtils;

import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConformanceTesting {

    private String eventLogPath;
    private Double fitness;
    private Double precision;

    TransitionSystem transitionSystem;

    public ConformanceTesting(String eventLogPath, TransitionSystem transitionSystem) {
        this.eventLogPath = eventLogPath;
        this.transitionSystem = transitionSystem;
    }
    public void checkConformance() throws Exception {

        File xesFile = new File(eventLogPath);
        XesXmlParser xesParser = new XesXmlParser();

        List<XLog> parsedXesFile = xesParser.parse(xesFile);

        FileOutputStream fileOut = new FileOutputStream(eventLogPath+"_withComformance.xes");
        XesXmlSerializer outputSerializer = new XesXmlSerializer();
        int illegalActions;
        int counter=1;
        int worstCaseCost = 0;
        int totalCost=0;

        int sumActivitiesExecuted = 0;
        int sumExecutableActivities = 0;
        for (XLog traces : parsedXesFile){
            for (XTrace trace : traces){

                illegalActions = 0;
                String traceId = trace.getAttributes().get("concept:name").toString();
                boolean traceIslegal = true;
                for (XEvent event : trace ){
                    String activity = event.getAttributes().get("concept:name").toString();
                    //Check if event can be executed
                    if(!transitionSystem.executeEvent(activity)){
                        traceIslegal=false;
                        illegalActions++;
                    }
                    sumActivitiesExecuted += transitionSystem.getExecutedOfEnabled();
                    sumExecutableActivities += transitionSystem.getEnabledEvents().size();
                    worstCaseCost ++;
                }
                if (transitionSystem.anyPendingEvents()){
                    traceIslegal = false;
                }

                XAttribute xAttribute = new XAttributeBooleanImpl("pdc:isPos",traceIslegal);
                XAttributeMap xMap =trace.getAttributes();
                xMap.put("mapKey",xAttribute);
                trace.setAttributes(xMap);

                System.out.println("Trace: " + counter + "/" + traces.size() + " - illegal actions: " + illegalActions + "/" + trace.size() +
                "- Pending state at completion: " + String.valueOf(transitionSystem.anyPendingEvents()));
                transitionSystem.resetMarking();
                counter++;

            }
            this.precision = (double) sumActivitiesExecuted/sumExecutableActivities;
            this.fitness = (double) 1-(totalCost/worstCaseCost);
            outputSerializer.serialize(traces,fileOut);
        }
    }

    public Double getFitness() {
        return fitness;
    }

    public Double getPrecision() {
        return precision;
    }
}
