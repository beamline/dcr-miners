package beamline.dcr.testsuite;

import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.Stream;
import beamline.dcr.miners.DFGBasedMiner;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import java.io.File;
import java.util.*;

public class Tester {

	public static void main(String[] args) throws Exception {
		String rootPath = System.getProperty("user.dir");
		String currentPath = rootPath + "/src/main/java/beamline/dcr/testsuite";

		DFGBasedMiner sc = new DFGBasedMiner();

		Collection<MinerParameterValue> coll = new ArrayList<>();

		String[] patternList = {"Condition","Response","Exclude"};
		MinerParameterValue confParam = new MinerParameterValue("DCR Patterns", patternList);
		coll.add(confParam);

		String fileName = currentPath + "/minedpatterns/cond_resp_25_illegal_" + java.time.LocalDate.now();
		MinerParameterValue fileParam = new MinerParameterValue("filename", fileName);
		coll.add(fileParam);
		sc.configure(coll);
		sc.setStream(new Stream("test", "localhost", ""));
		sc.start();

		String eventLog = "/eventlogs/test2005/Graph25/eventlog_characteristic.xes";

		File xesFile = new File(currentPath + eventLog);
		XesXmlParser xesParser = new XesXmlParser();

		List<XLog> parsedXesFile = xesParser.parse(xesFile);

		for (XLog traces : parsedXesFile){
			for (XTrace trace : traces){
				String traceId = trace.getAttributes().get("concept:name").toString();
				for (XEvent event : trace ){
					String activity = event.getAttributes().get("concept:name").toString();
					sc.consumeEvent(traceId,activity);
				}

			}
		}

		
		List<MinerView> views = sc.getViews(coll);

		System.out.println("\n\n++++++++++++++++++++++++++++++++");
		for (MinerView v : views) {
			System.out.println("=== " + v.getName().toUpperCase() + " ===");
			System.out.println("");
			System.out.println(v.getValue());
			System.out.println("\n");
		}
		System.out.println("++++++++++++++++++++++++++++++++");

		// For performance testing

		/*String baselineFilePath ="src/main/java/beamline/dcr/testsuite/minedpatterns/BaseLineRelations.JSON";
		String baselineFileWithoutExcl = "src/main/java/beamline/dcr/testsuite/minedpatterns/BaseLineRelations_wo_exclude.JSON";

		PerformanceStatistics performanceStatistics = new PerformanceStatistics();
		System.out.println("With exclusion in baseline and with transitive reduction");
		System.out.println(performanceStatistics.getPrecisionRecallString(baselineFilePath,jsonFileName));
		System.out.println("Without exclusion in baseline and with transitive reduction");
		System.out.println(performanceStatistics.getPrecisionRecallString(baselineFileWithoutExcl,jsonFileName));*/



		TransitionSystem transitionSystem = new TransitionSystem(fileName + ".xml");
		//TransitionSystem transitionSystem = new TransitionSystem("src/main/java/beamline/dcr/testsuite/eventlogs/test2005/Graph13/dcrmined13.xml");
		ConformanceTesting conformanceTesting = new ConformanceTesting(currentPath + eventLog, transitionSystem);
		conformanceTesting.checkConformance();
		sc.stop();
		System.exit(0);


	}



}
