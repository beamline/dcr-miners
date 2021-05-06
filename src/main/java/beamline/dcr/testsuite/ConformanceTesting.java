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
    String graphId;
    String eventLogPath;
    Integer illegalActions = 0;

    public ConformanceTesting(String graphId, String eventLogPath) {
        this.graphId = graphId;
        this.eventLogPath = eventLogPath;
    }
    public void checkConformance() throws Exception {

        File xesFile = new File(eventLogPath);
        XesXmlParser xesParser = new XesXmlParser();

        List<XLog> parsedXesFile = xesParser.parse(xesFile);

        FileOutputStream fileOut = new FileOutputStream(eventLogPath+"_withComformance.xes");
        XesXmlSerializer outputSerializer = new XesXmlSerializer();
        for (XLog traces : parsedXesFile){
            for (XTrace trace : traces){

                String traceId = trace.getAttributes().get("concept:name").toString();
                boolean traceIslegal = true;
                for (XEvent event : trace ){
                    String activity = event.getAttributes().get("concept:name").toString();
                    //Check rest if event is ok

                }


                XAttribute xAttribute = new XAttributeBooleanImpl("pdc:isPos",true);
                XAttributeMap xMap =trace.getAttributes();
                xMap.put("mapKey",xAttribute);
                trace.setAttributes(xMap);



            }
            outputSerializer.serialize(traces,fileOut);
        }
    }


    private String makeRequest(String url) throws IOException {
        URL dcrItu = new URL(url);
        HttpURLConnection con = (HttpURLConnection) dcrItu.openConnection();

        con.setRequestMethod("GET");

        String responseMessage = con.getResponseMessage();
        con.disconnect();

        return responseMessage;
    }


}
