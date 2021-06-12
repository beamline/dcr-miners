package beamline.dcr.testsuite;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import java.io.File;
import java.util.*;

public class ConformanceTesting {

    private final String eventLogPath;
    private Double fitness;
    private Double precision;
    private Set<String> illegalTraces;

    TransitionSystem transitionSystem;

    public ConformanceTesting(String eventLogPath, TransitionSystem transitionSystem) {
        this.illegalTraces = new HashSet<>();
        this.eventLogPath = eventLogPath;
        this.transitionSystem = transitionSystem;
    }
    public void checkConformance() throws Exception {

        File xesFile = new File(eventLogPath);
        XesXmlParser xesParser = new XesXmlParser();

        List<XLog> parsedXesFile = xesParser.parse(xesFile);

        int illegalActions;
        int counter=1;
        double worstCaseCost = 0.0;
        double totalCost = 0.0;
        double sumActivitiesExecuted = 0.0;
        double sumExecutableActivities = 0.0;
        for (XLog traces : parsedXesFile){
            for (XTrace trace : traces){
                String traceId = trace.getAttributes().get("concept:name").toString();
                Set<Pair<String,BitSet[]>> markingSet = new HashSet<>();
                illegalActions = 0;
                boolean traceIslegal = true;
                for (XEvent event : trace ){
                    String activity = event.getAttributes().get("concept:name").toString();
                    //Check if event can be executed
                    if(!transitionSystem.executeEvent(activity)){
                        traceIslegal=false;
                        illegalActions++;
                    }
                    if(markingSet.add(Pair.of(activity,transitionSystem.getMarking()))){
                        sumActivitiesExecuted += transitionSystem.getExecutedOfEnabled();
                    }

                    sumExecutableActivities += transitionSystem.getEnabledEvents().size();
                    worstCaseCost ++;
                }

                /*if (transitionSystem.anyPendingEvents()){

                    traceIslegal = false;
                }*/

                /*XAttribute xAttribute = new XAttributeBooleanImpl("pdc:isPos",traceIslegal);
                XAttributeMap xMap =trace.getAttributes();
                xMap.put("mapKey",xAttribute);
                trace.setAttributes(xMap);*/
                totalCost+=illegalActions;

                if (!traceIslegal){
                    this.illegalTraces.add(traceId);
                }
                transitionSystem.resetMarking();
                counter++;

            }
            this.precision = sumActivitiesExecuted/sumExecutableActivities;
            this.fitness = 1-(totalCost/worstCaseCost);

            //outputSerializer.serialize(traces,fileOut); //uncomment to save xes with transition analysis (forbidden/not)
        }
    }

    public Double getFitness() {
        return fitness;
    }

    public Double getPrecision() {
        return precision;
    }

    public Set<String> getIllegalTraces(){
        return illegalTraces;
    }
}
