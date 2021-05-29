package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Response",
        dependencies = {"DirectLoop"}
)
public class Response implements RelationPattern {

    /*@Override
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
    }*/
    //IMM RESPONSE
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        Set<Pair<String, String>> dfgRelations = unionRelationSet.getDFGRelations();
        Set<Triple<String,String, DcrModel.RELATION>> directLoops =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.DIRECTLOOP);
        for (Pair<String, String> relation : dfgRelations) {
            String source = relation.getLeft();
            double avgIndexSource =
                    unionRelationSet.getActivityDecoration(source).getAverageIndex();
            double traceAppearanceSource = unionRelationSet.getActivityDecoration(source).getTraceAppearances();
            String target = relation.getRight();
            double avgIndexTarget =
                    unionRelationSet.getActivityDecoration(target).getAverageIndex();
            double traceAppearancesTarget = unionRelationSet.getActivityDecoration(target).getTraceAppearances();
            if (avgIndexSource < avgIndexTarget ) {
                unionRelationSet.addDcrRelation(Triple.of(source, target, DcrModel.RELATION.RESPONSE));
            }

        }
    }
}
