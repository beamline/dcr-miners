package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Condition",
        dependencies = {}
        )
public class Condition implements RelationPattern {

    /*@Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        String[] activities = unionRelationSet.getUniqueActivities();

        Set<Triple<String,String, DcrModel.RELATION>> directLoops =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.DIRECTLOOP);
        for (int i = 0; i < activities.length ; i++){
             Double avgFO =
                    unionRelationSet.getActivityDecoration(activities[i]).getAverageFirstOccurrence();
             for (int j = i + 1; j < activities.length ; j++){
                 Double avgFOOther =
                         unionRelationSet.getActivityDecoration(activities[j]).getAverageFirstOccurrence();
                //First check if direct loop appears between events
                 if (!directLoops.contains(Triple.of(activities[i], activities[j], DcrModel.RELATION.DIRECTLOOP)) &
                         !directLoops.contains(Triple.of(activities[j], activities[i], DcrModel.RELATION.DIRECTLOOP))){
                     if (avgFO < avgFOOther){
                         unionRelationSet.addDcrRelation(Triple.of(activities[i], activities[j], DcrModel.RELATION.CONDITION));
                     }else if (avgFO > avgFOOther){
                         unionRelationSet.addDcrRelation(Triple.of(activities[j], activities[i], DcrModel.RELATION.CONDITION));
                     }
                 }

             }
        }
    }*/
    //IMM CONDITION
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        Set<Pair<String,String>> dfgRelations = unionRelationSet.getDFGRelations();

        for (Pair<String,String> relation : dfgRelations){

            String source = relation.getLeft();
            double avgFOSource =
                    unionRelationSet.getActivityDecoration(source).getAverageFirstOccurrence();
            double numTraceAppearancesSource = unionRelationSet.getActivityDecoration(source).getTraceAppearances();
            String target = relation.getRight();
            double avgFOTarget =
                    unionRelationSet.getActivityDecoration(target).getAverageFirstOccurrence();
            double numTraceAppearancesTarget = unionRelationSet.getActivityDecoration(target).getTraceAppearances();

            if(avgFOSource<avgFOTarget & numTraceAppearancesSource >= numTraceAppearancesTarget){
                unionRelationSet.addDcrRelation(Triple.of(source, target, DcrModel.RELATION.CONDITION));
            }

        }
    }

    public void populateConstraint(UnionRelationSet unionRelationSet,Set<Integer> parameterCombination) {
        Set<Pair<String,String>> dfgRelations = unionRelationSet.getDFGRelations();

        for (Pair<String,String> relation : dfgRelations){

            String source = relation.getLeft();
            double avgFOSource =
                    unionRelationSet.getActivityDecoration(source).getAverageFirstOccurrence();
            double numTraceAppearancesSource = unionRelationSet.getActivityDecoration(source).getTraceAppearances();
            String target = relation.getRight();
            double avgFOTarget =
                    unionRelationSet.getActivityDecoration(target).getAverageFirstOccurrence();
            double numTraceAppearancesTarget = unionRelationSet.getActivityDecoration(target).getTraceAppearances();

            boolean isCondition = true;

            if(parameterCombination.contains(1) && isCondition){
                isCondition = avgFOSource<avgFOTarget;
            }
            if(parameterCombination.contains(2) && isCondition){
                isCondition = numTraceAppearancesSource >= numTraceAppearancesTarget;
            }
            if(isCondition){
                unionRelationSet.addDcrRelation(Triple.of(source, target, DcrModel.RELATION.CONDITION));
            }

        }
    }
}
