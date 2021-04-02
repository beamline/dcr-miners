package beamline.dcr.model.relations;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;
@ExposedDcrPattern(
        name="ImmCond",
        latticeLevel = 2
)
public class ImmediateCondition implements RelationPattern {
    private String type = "ImmCond";
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        Set<Pair<String, String>> DFGRelations = unionRelationSet.getDFGRelations();

        for (Pair<String, String> relation : DFGRelations) {
            String sourceActivity = relation.getLeft();
            double sourceAvgFO = unionRelationSet.getActivityDecoration(sourceActivity).getAverageFirstOccurrance();
            String targetActivity = relation.getRight();
            double targetAvgFO = unionRelationSet.getActivityDecoration(targetActivity).getAverageFirstOccurrance();
            if (sourceAvgFO < targetAvgFO) {

                unionRelationSet.addDcrRelation(Triple.of(sourceActivity, targetActivity, DcrModel.RELATION.IMMCONDITION));
            }
        }
    }

    @Override
    public String getType() {
        return type;
    }

}
