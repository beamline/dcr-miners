package beamline.miners.dcr;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.reflections.Reflections;

import beamline.miners.dcr.annotations.ExposedDcrPattern;
import beamline.miners.dcr.model.TransitiveReduction;
import beamline.miners.dcr.model.patterns.RelationPattern;
import beamline.miners.dcr.model.relations.DcrModel;
import beamline.miners.dcr.model.relations.DcrModel.RELATION;
import beamline.miners.dcr.model.relations.UnionRelationSet;
import beamline.miners.dcr.model.relations.dfg.ExtendedDFG;
import beamline.miners.dcr.model.streamminers.SlidingWindowStreamMiner;
import beamline.miners.dcr.model.streamminers.StreamMiner;
import beamline.miners.dcr.model.streamminers.UnlimitedStreamMiner;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class DFGBasedMiner extends StreamMiningAlgorithm<XTrace, DcrModel> {
	// Not configured with XML download to beamline
	// XML view only works locally from testrunners

	private Reflections reflections;
	private Set<Class<?>> dcrPatternClasses;

	private StreamMiner streamMiner;
	private UnionRelationSet unionRelationSet;
	private Integer relationsThreshold = 10;
	private String[] transReductionList = new String[] { "Condition", "Response" };

	private String[] dcrPatternList = new String[] { "Condition", "Response", "Exclude", "Include" };
	private String[] dcrConstraintList = new String[] { "Condition", "Response", "Exclude", "Include" };
	private Set<String> postorderTraversal;

	public DFGBasedMiner() {
		this.reflections = new Reflections("beamline");
		this.dcrPatternClasses = reflections.getTypesAnnotatedWith(ExposedDcrPattern.class);
		
		this.streamMiner = new UnlimitedStreamMiner();
	}
	
	public DFGBasedMiner(int maxEvents, int maxTraces) {
		this();
		this.streamMiner = new SlidingWindowStreamMiner(maxEvents, maxTraces);
	}

	@Override
	public DcrModel ingest(XTrace event) {
		String caseID = XConceptExtension.instance().extractName(event);
		String activityName = XConceptExtension.instance().extractName(event.get(0));
		
		this.streamMiner.observeEvent(caseID, activityName);
		return getDcrModel();
	}
	
	public DcrModel convert(ExtendedDFG dfg) throws IllegalAccessException, InstantiationException {
		DcrModel model = new DcrModel();
		this.postorderTraversal = new LinkedHashSet<>();

		this.unionRelationSet = new UnionRelationSet(dfg, relationsThreshold);

		for (String originalPattern : dcrPatternList) {

			minePatternsFromPostOrderDependencies(originalPattern);
		}

		TransitiveReduction transitiveReduction = new TransitiveReduction();

		for (String transReduce : transReductionList) {
			RELATION enumPattern = RELATION.valueOf(transReduce.toUpperCase());
			transitiveReduction.reduce(unionRelationSet, enumPattern);
		}

		model.addActivities(dfg.getActivities());
		// project user selected patterns to DCR Model
		for (String dcrConstraint : dcrConstraintList) {
			RELATION enumConstraint = RELATION.valueOf(dcrConstraint.toUpperCase());
			Set<Triple<String, String, RELATION>> minedConstraints = unionRelationSet
					.getDcrRelationWithConstraint(enumConstraint);
			model.addRelations(minedConstraints);
		}
		return model;

	}

	@SuppressWarnings("rawtypes")
	private void minePatternsFromPostOrderDependencies(String root)
			throws IllegalAccessException, InstantiationException {
		int currentRootIndex = 0;
		Stack<Pair> stack = new Stack<>();

		while (root != null || !stack.isEmpty()) {
			if (root != null) {

				stack.push(Pair.of(root, currentRootIndex));
				currentRootIndex = 0;

				String[] dcrDependencies = getDcrDependencies(root);
				if (dcrDependencies.length >= 1) {
					root = dcrDependencies[0];
				} else {
					root = null;
				}
				continue;
			}

			Pair temp = stack.pop();

			if (!postorderTraversal.contains(temp.getLeft().toString())) {
				minePattern(temp.getLeft().toString());
				postorderTraversal.add(temp.getLeft().toString());

			}

			while (!stack.isEmpty()
					&& (int) temp.getRight() == getDcrDependencies(stack.peek().getLeft().toString()).length - 1) {
				temp = stack.pop();
				if (!postorderTraversal.contains(temp.getLeft().toString())) {
					minePattern(temp.getLeft().toString());
					postorderTraversal.add(temp.getLeft().toString());

				}
			}

			if (!stack.isEmpty()) {
				String[] dependencies = getDcrDependencies(stack.peek().getLeft().toString());
				root = dependencies[(int) temp.getRight() + 1];
				currentRootIndex = (int) temp.getRight() + 1;
			}
		}
	}

	private String[] getDcrDependencies(String dcr) {
		return getExposedPatternClass(dcr).getAnnotation(ExposedDcrPattern.class).dependencies();
	}

	@SuppressWarnings("deprecation")
	private RelationPattern getPatternMinerClass(String patternName)
			throws IllegalAccessException, InstantiationException {
		return (RelationPattern) getExposedPatternClass(patternName).newInstance();
	}

	private Class<?> getExposedPatternClass(String patternName) {
		for (Class<?> exposedPatternClass : dcrPatternClasses) {
			ExposedDcrPattern exposedPattern = exposedPatternClass.getAnnotation(ExposedDcrPattern.class);
			if (exposedPattern.name().equals(patternName)) {
				return exposedPatternClass;
			}
		}
		return null;

	}

	private void minePattern(String patternName) throws InstantiationException, IllegalAccessException {
		RelationPattern patternToMine = getPatternMinerClass(patternName);
		patternToMine.populateConstraint(unionRelationSet);

	}

	public DcrModel getDcrModel() {

		try {
			return convert(streamMiner.getExtendedDFG());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	// For testsoftware

	public ExtendedDFG getExtendedDFG() {
		return streamMiner.getExtendedDFG();
	}

	public void saveCurrentWindowLog(String filePath) throws TransformerException {
		this.streamMiner.saveLog(filePath);
	}

	public int getNumberEventsInWindow() {
		return this.streamMiner.getNumberEventsSaved();
	}

	public UnionRelationSet getUnionRelationSet() {
		return unionRelationSet;
	}

}
