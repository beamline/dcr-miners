package beamline.dcr.miners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import beamline.core.miner.AbstractMiner;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.MinerViewRaw;
import beamline.core.web.miner.models.MinerView.Type;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import beamline.dcr.model.dfg.RelationDecoration;
import beamline.dcr.view.DcrModelView;

@ExposedMiner(
	name = "DFG based DCR miner",
	description = "This miner discovers a DCR model form a DFG",
	configurationParameters = { },
	viewParameters = { }
)
public class DFGBasedMiner extends AbstractMiner {

	private Map<String, String> latestActivityInCase = new HashMap<String, String>();
	private Map<String, Integer> indexInCase = new HashMap<String, Integer>();
	private Map<String, Set<String>> observedActivitiesInCase = new HashMap<String, Set<String>>();
	private ExtendedDFG extendedDFG = new ExtendedDFG();
	
	@Override
	public void configure(Collection<MinerParameterValue> collection) { }

	@Override
	public void consumeEvent(String caseID, String activityName) {
		int currentIndex = 1;
		if (indexInCase.containsKey(caseID)) {
			currentIndex = indexInCase.get(caseID);
		}
		boolean firstOccurrance = true;
		if (observedActivitiesInCase.containsKey(caseID)) {
			if (observedActivitiesInCase.get(caseID).contains(activityName)) {
				firstOccurrance = false;
			} else {
				observedActivitiesInCase.get(caseID).add(activityName);
			}
		} else {
			observedActivitiesInCase.put(caseID, new HashSet<String>(Arrays.asList(activityName)));
		}
		
		ActivityDecoration activityDecoration = extendedDFG.addActivityIfNeeded(activityName);
		activityDecoration.addNewObservation(currentIndex, firstOccurrance);
		
		if (latestActivityInCase.containsKey(caseID)) {
			String previousActivity = latestActivityInCase.get(caseID);
			RelationDecoration relationDecoration = extendedDFG.addRelationIfNeeded(previousActivity, activityName);
			relationDecoration.addNewObservation();
		}
		latestActivityInCase.put(caseID, activityName);
		indexInCase.put(caseID, currentIndex + 1);
	}

	@Override
	public List<MinerView> getViews(Collection<MinerParameterValue> collection) {
		List<MinerView> views = new ArrayList<>();
		
		views.add(new MinerViewRaw("DFG", extendedDFG.toString()));
		views.add(new MinerView("DCR", new DcrModelView(convert(extendedDFG)).toString(), Type.GRAPHVIZ));
		
		return views;
	}
	
	private DcrModel convert(ExtendedDFG dfg) {
		DcrModel model = new DcrModel();
		
		return model;
	}

}
