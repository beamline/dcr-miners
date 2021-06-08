package beamline.dcr.testsuite;

import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.Stream;
import beamline.dcr.miners.DFGBasedMiner;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.TransitiveReduction;
import beamline.dcr.model.UnionRelationSet;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.patterns.Condition;
import beamline.dcr.model.patterns.ExcludeAndInclude;
import beamline.dcr.model.patterns.RelationPattern;
import beamline.dcr.model.patterns.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Tester {

	public static void main(String[] args) throws Exception {
		String eventlogNumber = "2";
		String rootPath = System.getProperty("user.dir");
		String currentPath = rootPath + "/src/main/java/beamline/dcr/testsuite";

		DFGBasedMiner sc = new DFGBasedMiner();

		Collection<MinerParameterValue> coll = new ArrayList<>();

		String[] patternList = {"Condition","Response","Exclude"};
		MinerParameterValue confParam = new MinerParameterValue("DCR Patterns", patternList);
		coll.add(confParam);

		String fileName = currentPath + "/minedpatterns/mined_model_"+eventlogNumber+ "_" + java.time.LocalDate.now();
		MinerParameterValue fileParam = new MinerParameterValue("filename", fileName);
		coll.add(fileParam);
		sc.configure(coll);
		sc.setStream(new Stream("test", "localhost", ""));
		sc.start();

		//String eventLog = "/eventlogs/test2005/Graph"+eventlogNumber+"/eventlog_graph25.xes";
		String eventLog = "/eventlogs/eventlog_graph"+eventlogNumber+ ".xes";
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

		/*System.out.println("\n\n++++++++++++++++++++++++++++++++");
		for (MinerView v : views) {
			System.out.println("=== " + v.getName().toUpperCase() + " ===");
			System.out.println("");
			System.out.println(v.getValue());
			System.out.println("\n");
		}
		System.out.println("++++++++++++++++++++++++++++++++");*/

		// For testing
		ExtendedDFG extendedDFG = sc.getExtendedDFG();

		Set<Set<String>> relationPatternSet = new LinkedHashSet<>();
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Condition")));
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Response")));
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Condition","Response")));
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Condition","ExcludeAndInclude")));
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Response","ExcludeAndInclude")));
		relationPatternSet.add(new LinkedHashSet<>(Arrays.asList("Condition","Response","ExcludeAndInclude")));

		Set<Set<Integer>> conditionParameters = new LinkedHashSet<>();
		conditionParameters.add(new LinkedHashSet<>(Arrays.asList(1)));
		conditionParameters.add(new LinkedHashSet<>(Arrays.asList(2)));
		conditionParameters.add(new LinkedHashSet<>(Arrays.asList(1,2)));

		Set<Set<Integer>> excludeIncludeParameters = new LinkedHashSet<>();
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(2)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(3)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,2)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,3)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(2,3)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(2,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(3,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,2,3)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,2,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,3,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(2,3,4)));
		excludeIncludeParameters.add(new LinkedHashSet<>(Arrays.asList(1,2,3,4)));

		Set<Set<Integer>> transitiveReductionParameters = new LinkedHashSet<>();
		transitiveReductionParameters.add(new LinkedHashSet<>());
		transitiveReductionParameters.add(new LinkedHashSet<>(Arrays.asList(1)));
		transitiveReductionParameters.add(new LinkedHashSet<>(Arrays.asList(2)));
		transitiveReductionParameters.add(new LinkedHashSet<>(Arrays.asList(1,2)));

		//For best performaning values
		List<Pair<String,Double>> highestPerformanceList = new ArrayList<>();
		int amountPerformanceMetrics = 5;
		for(int j = 0; j < amountPerformanceMetrics; j++){

			highestPerformanceList.add(Pair.of("",0.0));
		}


		String groundTruthModelPath = currentPath + "/groundtruthmodels/Process" + eventlogNumber +".xml";
		String discoverModelPath = currentPath + "/discovermodels/DCR_graph" + eventlogNumber +".xml";

		String outputDirectoryPath = currentPath + "/evaluations/"+ eventlogNumber;
		File outputDirectoryObject = new File(outputDirectoryPath);
		if (!outputDirectoryObject.exists()){
			outputDirectoryObject.mkdirs();
		}
		String outputFile = outputDirectoryPath +"/performance_test.csv";
		StringBuilder csvText = new StringBuilder();
		File myObj = new File(outputFile);

		myObj.createNewFile();
		String title = "constraints,model_jaccard,model_precision,model_recall,log_fitness,log_precision\n";
		writeMetricsToFile(outputFile,title);

		Set<String> checkedCombinations = new HashSet<>();

		UnionRelationSet unionRelationSet;
		DcrModel dcrModel;
		StringBuilder patternCombination;
		boolean containsCondition = false;
		boolean containsResponse = false;
		for(Set<String> relationPatterns : relationPatternSet){
			for (Set<Integer> transitiveReductionParameter : transitiveReductionParameters){
				for (Set<Integer> conditionParameter : conditionParameters){
					for(Set<Integer> excludeIncludeParameter : excludeIncludeParameters) {
						patternCombination = new StringBuilder();
						unionRelationSet = new UnionRelationSet(extendedDFG, 0);
						containsCondition = false;
						containsResponse = false;
						for (String pattern : relationPatterns) {

							patternCombination.append(pattern);
							switch (pattern) {
								case "Condition":
									new Condition().populateConstraint(unionRelationSet, conditionParameter);
									for (int parameterSetting : conditionParameter) {
										patternCombination.append(parameterSetting).append("-");
									}
									containsCondition=true;
									break;
								case "Response":
									new Response().populateConstraint(unionRelationSet);
									containsResponse=true;
									break;
								case "ExcludeAndInclude":
									for (int parameterSetting : excludeIncludeParameter) {
										patternCombination.append(parameterSetting).append("-");
									}
									new ExcludeAndInclude().populateConstraint(unionRelationSet, excludeIncludeParameter);
									break;
							}
							patternCombination.append(";");

						}
						if (transitiveReductionParameter.size() > 0) {
							StringBuilder transitiveString =new StringBuilder("TransitiveReduction");

							TransitiveReduction transitiveReduction = new TransitiveReduction();
							if (transitiveReductionParameter.contains(1) && containsCondition) {
								transitiveReduction.reduce(unionRelationSet, DcrModel.RELATION.CONDITION);
								transitiveString.append(1);
							}
							if (transitiveReductionParameter.contains(2) && containsResponse) {
								transitiveReduction.reduce(unionRelationSet, DcrModel.RELATION.RESPONSE);
								transitiveString.append("-").append(2);
							}

							if(transitiveString.length()!= 19){
								patternCombination.append(transitiveString);
							}
						}

						if (!checkedCombinations.contains(patternCombination.toString())) {
							checkedCombinations.add(patternCombination.toString());
							dcrModel = new DcrModel();
							dcrModel.addRelations(unionRelationSet.getDcrRelations());

							List<Double> performanceList = getImperativeEvaluationString(dcrModel, unionRelationSet, discoverModelPath,
									currentPath + eventLog);
							StringBuilder performanceString = new StringBuilder();
							for (int i = 0; i < performanceList.size(); i++){
								double performanceValue = performanceList.get(i);
								//First check if highest value of metric seen
								if(highestPerformanceList.get(i).getRight()<performanceValue ||
										(highestPerformanceList.get(i).getRight().equals(performanceValue)
												&& highestPerformanceList.get(i).getLeft().length() > patternCombination.length())){
									highestPerformanceList.set(i, Pair.of(patternCombination.toString(),performanceValue));
								}

								//Add to file
								performanceString.append(performanceValue).append(",");

							}
							String finaleString = patternCombination + "," + performanceString + "\n";
							csvText.append(finaleString);
						}
					}
				}
			}

		}

		writeMetricsToFile(outputFile,csvText.toString());

		PlotResults plotResults = new PlotResults(outputFile);

		plotResults.saveScatterPlot(outputDirectoryPath);
		printBestPerformingCombination(highestPerformanceList);
		sc.stop();
		System.exit(0);


	}

	private static List<Double> getImperativeEvaluationString
			(DcrModel dcrModel, UnionRelationSet unionRelationSet,
			 String groundTruthFilePath, String eventLogFilePath) throws Exception {
		//model_jaccard,model_precision,model_recall, Log_fitness,Log_precision

		TransitionSystem transitionSystem = new TransitionSystem(unionRelationSet);

		ModelComparison modelComparison = new ModelComparison(dcrModel);
		modelComparison.loadComparativeModel(groundTruthFilePath);

		ConformanceTesting conformanceTesting = new ConformanceTesting(eventLogFilePath, transitionSystem);
		conformanceTesting.checkConformance();
		List<Double> outputList = new ArrayList<>(List.of(modelComparison.getJaccardSimilarity(), modelComparison.getPrecision(),
				modelComparison.getRecall(), conformanceTesting.getFitness(),
				conformanceTesting.getPrecision()));




		return outputList;

	}
	private static void writeMetricsToFile(String filePath,String textString){
		try {
			FileWriter myWriter = new FileWriter(filePath,true);
			myWriter.write(textString);
			myWriter.close();

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	private static void printBestPerformingCombination(List<Pair<String,Double>> performanceList){
		List<String> metrics = new ArrayList<>(List.of("Jaccard", "Model Precision", "Model Recall",
				"Log Fitness", "Log Precision"));
		for (int i = 0; i < performanceList.size(); i++){
			System.out.println(metrics.get(i) + " - Combination:" +performanceList.get(i).getLeft() +
					" - value:" + performanceList.get(i).getRight());
		}


	}

}
