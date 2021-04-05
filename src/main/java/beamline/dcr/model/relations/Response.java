package beamline.dcr.model.relations;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import beamline.dcr.model.dfg.ActivityDecoration;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Response",
        latticeLevel = 1
)
public class Response implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        String[] activities = unionRelationSet.getUniqueActivities();

        for (int i = 0; i < activities.length ; i++){
            Double avgInd =
                    unionRelationSet.getActivityDecoration(activities[i]).getAverageIndex();
            for (int j = i + 1; j < activities.length ; j++){
                Double avgIndOther =
                        unionRelationSet.getActivityDecoration(activities[j]).getAverageIndex();

                if (avgInd < avgIndOther){
                    unionRelationSet.addDcrRelation(Triple.of(activities[i], activities[j], DcrModel.RELATION.RESPONSE));
                }else if (avgInd > avgIndOther){
                    unionRelationSet.addDcrRelation(Triple.of(activities[j], activities[i], DcrModel.RELATION.RESPONSE));
                }
            }
        }
    }
}
