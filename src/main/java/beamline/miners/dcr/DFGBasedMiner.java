package beamline.miners.dcr;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import beamline.events.BEvent;
import beamline.miners.dcr.annotations.ExposedDcrPattern;
import beamline.miners.dcr.exceptions.PatternUnknownException;
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

public class DFGBasedMiner extends StreamMiningAlgorithm<DcrModel> {

	private static final long serialVersionUID = 4226843984612962011L;
	private Set<Class<?>> dcrPatternClasses;
	private DcrModel cached;

	private StreamMiner streamMiner;
	private UnionRelationSet unionRelationSet;
	private Integer relationsThreshold = 10;
	private RELATION[] transReductionList = new RELATION[] { RELATION.CONDITION, RELATION.RESPONSE };

	private String[] dcrPatternList = new String[] { "Condition", "Response", "Exclude", "Include" };
	private RELATION[] dcrConstraintList = new RELATION[] { RELATION.CONDITION, RELATION.RESPONSE, RELATION.EXCLUDE, RELATION.INCLUDE };
	private Set<String> postorderTraversal;
	
	private int modelRefreshRate = 10;

	public DFGBasedMiner(Set<Class<?>> dcrPatternClasses) {
		this.dcrPatternClasses = dcrPatternClasses;
		this.streamMiner = new UnlimitedStreamMiner();
	}

	public DFGBasedMiner(Set<Class<?>> dcrPatternClasses, int maxEvents, int maxTraces) {
		this(dcrPatternClasses);
		this.streamMiner = new SlidingWindowStreamMiner(maxEvents, maxTraces);
	}

	public DFGBasedMiner setModelRefreshRate(int modelRefreshRate) {
		this.modelRefreshRate = modelRefreshRate;
		return this;
	}

	/**
	 * The patters specified with this methods should be classes annotated with the
	 * {@link ExposedDcrPattern} annotation.
	 * 
	 * @param dcrPatternList
	 * @throws PatternUnknownException
	 */
	public void setDcrPatternsForMining(String... dcrPatternList) throws PatternUnknownException {
		for (String p : dcrPatternList) {
			if (getExposedPatternClass(p) == null) {
				throw new PatternUnknownException();
			}
		}
		this.dcrPatternList = dcrPatternList;

	}

	public void setStreamMinerType(StreamMiner type) {
		this.streamMiner = type;
	}

	public void setTransitiveReductionList(RELATION... transReductionList) {
		this.transReductionList = transReductionList;
	}

	public void setRelationsThreshold(int relationsThreshold) {
		this.relationsThreshold = relationsThreshold;
	}

	public void setDcrConstraintsForVisualization(RELATION... dcrConstraintList) {
		this.dcrConstraintList = dcrConstraintList;
	}

	public void configureSlidingWindowStrategy(
			String[] dcrPatternList,
			int maxTraceSize,
			int maxTraces,
			RELATION[] transitiveReductionList,
			int relationsThreshold,
			RELATION[] dcrConstraints)
			throws PatternUnknownException {
		setDcrPatternsForMining(dcrPatternList);
		setStreamMinerType(new SlidingWindowStreamMiner(maxTraceSize, maxTraces));
		setTransitiveReductionList(transitiveReductionList);
		setRelationsThreshold(relationsThreshold);
		setDcrConstraintsForVisualization(dcrConstraints);
	}

	public void configureUnlimitedMemoryStrategy(
			String[] dcrPatternList,
			RELATION[] transitiveReductionList,
			int relationsThreshold,
			RELATION[] dcrConstraints) throws PatternUnknownException {
		setDcrPatternsForMining(dcrPatternList);
		setStreamMinerType(new UnlimitedStreamMiner());
		setTransitiveReductionList(transitiveReductionList);
		setRelationsThreshold(relationsThreshold);
		setDcrConstraintsForVisualization(dcrConstraints);
	}

	@Override
	public DcrModel ingest(BEvent event) {
		String caseID = event.getTraceName();
		String activityName = event.getEventName();

		this.streamMiner.observeEvent(caseID, activityName);
		if (getProcessedEvents() % modelRefreshRate == 0) {
			return getDcrModel();
		}
		
		return null;
	}

	public DcrModel convert(ExtendedDFG dfg) throws IllegalAccessException, InstantiationException {
		DcrModel model = new DcrModel();
		this.postorderTraversal = new LinkedHashSet<>();

		this.unionRelationSet = new UnionRelationSet(dfg, relationsThreshold);

		for (String originalPattern : dcrPatternList) {

			minePatternsFromPostOrderDependencies(originalPattern);
		}

		TransitiveReduction transitiveReduction = new TransitiveReduction();
		for (RELATION enumPattern : transReductionList) {
			transitiveReduction.reduce(unionRelationSet, enumPattern);
		}

		model.addActivities(dfg.getActivities());
		// project user selected patterns to DCR Model
		for (RELATION enumConstraint : dcrConstraintList) {
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
			cached = convert(streamMiner.getExtendedDFG());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return cached;
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
