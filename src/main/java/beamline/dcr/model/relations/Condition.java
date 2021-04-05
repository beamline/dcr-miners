package beamline.dcr.model.relations;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import beamline.dcr.model.dfg.ActivityDecoration;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Condition",
        latticeLevel = 1
        )
public class Condition implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        String[] activities = unionRelationSet.getUniqueActivities();

        for (int i = 0; i < activities.length ; i++){
             Double avgFO =
                    unionRelationSet.getActivityDecoration(activities[i]).getAverageFirstOccurrance();
             for (int j = i + 1; j < activities.length ; j++){
                 Double avgFOOther =
                         unionRelationSet.getActivityDecoration(activities[j]).getAverageFirstOccurrance();

                 if (avgFO < avgFOOther){
                     unionRelationSet.addDcrRelation(Triple.of(activities[i], activities[j], DcrModel.RELATION.CONDITION));
                 }else if (avgFO > avgFOOther){
                     unionRelationSet.addDcrRelation(Triple.of(activities[j], activities[i], DcrModel.RELATION.CONDITION));
                 }
             }
        }

    }
}
