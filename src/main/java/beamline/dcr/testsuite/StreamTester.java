package beamline.dcr.testsuite;

import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.Stream;
import beamline.dcr.miners.DFGBasedMiner;
import mqttxes.lib.XesMqttConsumer;
import mqttxes.lib.XesMqttEvent;
import mqttxes.lib.XesMqttEventCallback;

import java.util.ArrayList;
import java.util.Collection;

public class StreamTester {
    public static void main(String[] args) throws Exception {


        String rootPath = System.getProperty("user.dir");
        String currentPath = rootPath + "/src/main/java/beamline/dcr/testsuite";



        DFGBasedMiner sc = new DFGBasedMiner();
        Collection<MinerParameterValue> coll = new ArrayList<>();

        String[] patternList = {"Condition","Response"};
        MinerParameterValue confParam = new MinerParameterValue("DCR Patterns", patternList);
        coll.add(confParam);

        String fileName = currentPath + "/minedpatterns/online_mining"+ "_" + java.time.LocalDate.now();
        MinerParameterValue fileParam = new MinerParameterValue("filename", fileName);
        coll.add(fileParam);
        sc.configure(coll);

        Stream stream = new Stream("Hospital log", "broker.hivemq.com", "pmcep");
        sc.setStream(stream);
        sc.start();

        XesMqttConsumer client = new XesMqttConsumer(stream.getBrokerHost(), stream.getTopicBase());
        client.subscribe(stream.getProcessName(), new XesMqttEventCallback() {
            @Override
            public void accept(XesMqttEvent e) {
                //consumeEvent(e.getCaseId(), e.getActivityName());
                System.out.println(e.getCaseId());
            }
        });

        client.connect();
        Thread.sleep(10000);
        client.disconnect();
        sc.stop();
        System.exit(0);
    }
}
