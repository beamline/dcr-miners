package beamline.dcr.miners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import beamline.core.miner.AbstractMiner;
import beamline.core.web.annotations.ExposedMiner;
import beamline.core.web.miner.models.MinerParameterValue;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.MinerView.Type;
import beamline.dcr.model.DcrModel;
import beamline.dcr.view.DcrModelView;

@ExposedMiner(
	name = "DCR visualization tester",
	description = "This miner discovers a DCR model",
	configurationParameters = { },
	viewParameters = { }
)
public class DcrVisualizerTester extends AbstractMiner {

	@Override
	public void configure(Collection<MinerParameterValue> collection) {

	}

	@Override
	public void consumeEvent(String caseID, String activityName) {
		
	}

	@Override
	public List<MinerView> getViews(Collection<MinerParameterValue> collection) {
		List<MinerView> views = new ArrayList<>();
		views.add(new MinerView("Graphical ", new DcrModelView(getModel()).toString(), Type.GRAPHVIZ));
		return views;
	}

	public DcrModel getModel() {
		DcrModel model = new DcrModel();
		
		model.addRelation("Start", "Activity 3", DcrModel.RELATION.CONDITION);
		model.addRelation("Start", "Activity 2", DcrModel.RELATION.RESPONSE);
		model.addRelation("Activity 3", "Activity 2", DcrModel.RELATION.RESPONSE);
		model.addRelation("Activity 2", "Activity 4", DcrModel.RELATION.INCLUDE);
		model.addRelation("Activity 4", "Activity 4", DcrModel.RELATION.EXCLUDE);
		model.addRelation("Start", "Activity 4", DcrModel.RELATION.EXCLUDE);
		model.addRelation("Activity 4", "Activity 5", DcrModel.RELATION.SPAWN);
		model.addRelation("Activity 4", "Activity 6", DcrModel.RELATION.MILESTONE);

		model.addRelation("D: Medical examination", "B: Prescribe medicine", DcrModel.RELATION.RESPONSE);
		model.addRelation("C: Further examination needed", "A: 2nd medical examination", DcrModel.RELATION.RESPONSE);
		model.addRelation("C: Further examination needed", "A: 2nd medical examination", DcrModel.RELATION.CONDITION);
		model.addRelation("D: Medical examination", "B: Prescribe medicine", DcrModel.RELATION.CONDITION);
		model.addRelation("D: Medical examination", "C: Further examination needed", DcrModel.RELATION.CONDITION);
		model.addRelation("A: 2nd medical examination", "B: Prescribe medicine", DcrModel.RELATION.MILESTONE);
		
		return model;
	}
}
