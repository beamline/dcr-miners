package beamline.dcr.testsuite;

import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.Stream;
import beamline.dcr.miners.DFGBasedMiner;
import beamline.dcr.model.DcrModel;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Tester {

	public static void main(String[] args) throws Exception {
		/*String rootPath = System.getProperty("user.dir");
		String currentPath = rootPath + "/src/main/java/beamline/dcr/testsuite";

		DFGBasedMiner sc = new DFGBasedMiner();

		Collection<MinerParameterValue> coll = new ArrayList<>();

		String[] patternList = {"Response","Condition","Include","Exclude"};
		MinerParameterValue confParam = new MinerParameterValue("DCR Patterns", patternList);
		coll.add(confParam);

		String fileName = currentPath + "/minedpatterns/dcr_all_relations" + java.time.LocalDate.now();
		MinerParameterValue fileParam = new MinerParameterValue("filename", fileName);
		coll.add(fileParam);
		sc.configure(coll);
		sc.setStream(new Stream("test", "localhost", ""));
		sc.start();


		String eventLog = "/eventlogs/TrainingLog.xes";


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
		/*
		String baselineFilePath ="src/main/java/beamline/dcr/testsuite/minedpatterns/BaseLineRelations.JSON";
		String baselineFileWithoutExcl = "src/main/java/beamline/dcr/testsuite/minedpatterns/BaseLineRelations_wo_exclude.JSON";

		PerformanceStatistics performanceStatistics = new PerformanceStatistics();
		System.out.println("With exclusion in baseline and with transitive reduction");
		System.out.println(performanceStatistics.getPrecisionRecallString(baselineFilePath,jsonFileName));
		System.out.println("Without exclusion in baseline and with transitive reduction");
		System.out.println(performanceStatistics.getPrecisionRecallString(baselineFileWithoutExcl,jsonFileName));

		ConformanceTesting conformanceTesting = new ConformanceTesting("graphId",currentPath + eventLog);
		conformanceTesting.checkConformance();


		sc.stop();
		System.exit(0);*/
		DcrApiCommunicator dcrApi = new DcrApiCommunicator("1002026");
		dcrApi.instantiateModel();
		dcrApi.getEnabledEvents();
		dcrApi.executeEvent("t41");
	}




}
