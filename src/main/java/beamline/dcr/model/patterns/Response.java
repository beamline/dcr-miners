package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Response",
        dependencies = {}
)
public class Response implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        String[] activities = unionRelationSet.getUniqueActivities();
        Set<Triple<String,String, DcrModel.RELATION>> directLoops =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.DIRECTLOOP);
        for (int i = 0; i < activities.length ; i++){
            Double avgInd =
                    unionRelationSet.getActivityDecoration(activities[i]).getAverageIndex();
            for (int j = i + 1; j < activities.length ; j++){
                Double avgIndOther =
                        unionRelationSet.getActivityDecoration(activities[j]).getAverageIndex();
                //First check if direct loop appears between events
                if (!directLoops.contains(Triple.of(activities[i], activities[j], DcrModel.RELATION.DIRECTLOOP)) &
                        !directLoops.contains(Triple.of(activities[j], activities[i], DcrModel.RELATION.DIRECTLOOP))){
                    if (avgInd < avgIndOther){
                        unionRelationSet.addDcrRelation(Triple.of(activities[i], activities[j], DcrModel.RELATION.RESPONSE));
                    }else if (avgInd > avgIndOther){
                        unionRelationSet.addDcrRelation(Triple.of(activities[j], activities[i], DcrModel.RELATION.RESPONSE));
                    }
                }

            }
        }
    }
}
