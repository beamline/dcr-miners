package beamline.dcr.model.patterns;


import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import beamline.dcr.model.dfg.ActivityDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;


public class ExcludeAndInclude implements RelationPattern {
    private List<String> activityList;
    private Set<Pair<String,String>> excludeSet;
    private Set<Pair<String,String>> includeSet;
    private UnionRelationSet unionRelationSet;
    private BitSet[] dfgAdjacencyMatrix;

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        this.activityList = new ArrayList<>(Arrays.asList(unionRelationSet.getUniqueActivities()));
        this.excludeSet = new HashSet<>();
        this.includeSet = new HashSet<>();
        this.unionRelationSet = unionRelationSet;

        Set<Pair<String,String>> dfgRelations = unionRelationSet.getDFGRelations();
        this.dfgAdjacencyMatrix = computeAdjacencyMatrix(dfgRelations);

        //Self-exclusion / at most once
        selfExclusion();

        //precedence
        precedence();

        //notChainSuccession
        //notChainSuccession();

        //removeRedundantExcludes();

        for(Pair<String,String> exclude : excludeSet){
            unionRelationSet.addDcrRelation(Triple.of(exclude.getLeft(), exclude.getRight(), DcrModel.RELATION.EXCLUDE));

        }
        for(Pair<String,String> include : includeSet){
            unionRelationSet.addDcrRelation(Triple.of(include.getLeft(), include.getRight(), DcrModel.RELATION.INCLUDE));

        }
    }

    private void selfExclusion(){
        for (String activity : unionRelationSet.getUniqueActivities()){
            if (unionRelationSet.getActivityDecoration(activity).appearMostOnce()){
                this.excludeSet.add(Pair.of(activity,activity));
            }
        }

    }

    private void precedence(){

        //TODO refactor reused
        String[] listOfActivities = unionRelationSet.getUniqueActivities();

        //check precedence
        for (int i = 0; i<listOfActivities.length; i++){
            for (int j = i+1; j<listOfActivities.length; j++){
                String activity1 = listOfActivities[i];
                ActivityDecoration decoration1 = unionRelationSet.getActivityDecoration(activity1);
                String activity2 = listOfActivities[j];
                ActivityDecoration decoration2 = unionRelationSet.getActivityDecoration(activity2);

                if (decoration1.getAverageFirstOccurrence()<decoration2.getAverageFirstOccurrence() &
                        decoration1.getAverageIndex()<decoration2.getAverageIndex()){
                    //alternate precedence
                    if (decoration1.getNumObservations()==decoration2.getNumObservations()){
                        this.excludeSet.add(Pair.of(activity2,activity2));
                        this.includeSet.add(Pair.of(activity1,activity2));
                    }
                    //precedence not successor
                    if(!excludeSet.contains(Pair.of(activity1,activity1))){
                        this.excludeSet.add(Pair.of(activity2,activity1));
                    }
                }
                else if (decoration2.getAverageFirstOccurrence()<decoration1.getAverageFirstOccurrence() &
                        decoration2.getAverageIndex()<decoration1.getAverageIndex()){
                    //alternate precedence
                    if (decoration1.getNumObservations()==decoration2.getNumObservations()){
                        this.excludeSet.add(Pair.of(activity1,activity1));
                        this.includeSet.add(Pair.of(activity2,activity1));
                    }
                    //precedence not successor
                    if(!excludeSet.contains(Pair.of(activity2,activity2))){
                        this.excludeSet.add(Pair.of(activity1,activity2));
                    }
                }
            }
        }
    }

    private void notChainSuccession(){

        for (int i = 0; i < dfgAdjacencyMatrix.length; i++){
            for (int j = 0; j < dfgAdjacencyMatrix.length; j++){
                if (!dfgAdjacencyMatrix[i].get(j) & i != j){

                    this.excludeSet.add(Pair.of(activityList.get(i),activityList.get(j)));
                    //get events in between i and j
                    Set<String> inBetween = getActivitiesBetween(i,j);

                    for(String activityBetween : inBetween){

                        this.includeSet.add(Pair.of(activityBetween,activityList.get(j)));
                    }
                }
            }
        }

    }

    private BitSet[] computeAdjacencyMatrix(Set<Pair<String,String>>  relationSet){

        BitSet[] matrix = new BitSet[activityList.size()];
        for(int i = 0; i < matrix.length; i++) {
            matrix[i] = new BitSet(activityList.size());
        }
        for(Pair<String,String> relation : relationSet){
            String src = relation.getLeft();
            String tar = relation.getRight();
            int i1 = activityList.indexOf(src);
            int i2 = activityList.indexOf(tar);
            matrix[i1].set(i2);
        }

        return matrix;
    }

    private void computeTransitiveClosure(final BitSet[] matrix) {
        // compute path matrix / transitive closure
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i == j) {
                    continue;
                }
                if (matrix[j].get(i)) {
                    for (int k = 0; k < matrix.length; k++) {
                        if (!matrix[j].get(k)) {
                            matrix[j].set(k, matrix[i].get(k));
                        }
                    }
                }
            }
        }
    }

    private BitSet[] deepCopyBitSet(BitSet[] bitSet){

        BitSet[] newBitSet = new BitSet[bitSet.length];
        for(int i = 0; i < bitSet.length; i++) {
            newBitSet[i] = new BitSet(activityList.size());

            for (int j = 0; j<bitSet.length; j++){
                if (bitSet[i].get(j)) newBitSet[i].set(j);
            }
        }
        return newBitSet;
    }

    private void removeRedundantExcludes(){

        BitSet[] adjacencyMatrixExcludes = computeAdjacencyMatrix(excludeSet);

        for (int i = 0; i<activityList.size(); i++){
            for (int j = 0; j<activityList.size(); j++){
                if (i==j){
                    continue;
                }
                String activity1 = activityList.get(i);
                ActivityDecoration decoration1 = unionRelationSet.getActivityDecoration(activity1);
                String activity2 = activityList.get(j);
                ActivityDecoration decoration2 = unionRelationSet.getActivityDecoration(activity2);

                //if activity 1 always precedes activity 2
                if (decoration1.getAverageFirstOccurrence()<decoration2.getAverageFirstOccurrence() &
                        decoration1.getAverageIndex()<decoration2.getAverageIndex()) {

                    Set<String> activitiesBetween = getActivitiesBetween(i,j);
                    // if exclude(a1,a3)
                    for (int k = 0; k < activityList.size(); k++){
                        if (adjacencyMatrixExcludes[i].get(k) & adjacencyMatrixExcludes[j].get(k)){

                            String sink = activityList.get(k);
                            for (String activityBetween : activitiesBetween){
                                //And there is no include(u,a3) where u is between a1,a2
                                if(!includeSet.contains(Pair.of(activityBetween,sink))){
                                    // exclude(a2,a3) is redundant
                                    excludeSet.remove(Pair.of(activity2,sink));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<String> getActivitiesBetween(int sourceIndex, int sinkIndex){

        final BitSet[] transitiveClosure = deepCopyBitSet(dfgAdjacencyMatrix);
        computeTransitiveClosure(transitiveClosure);
        Set<String> inBetween = new HashSet<>();
        BitSet visited = new BitSet(activityList.size());

        dfs(sourceIndex,sinkIndex, inBetween,transitiveClosure,visited);
        return inBetween;
    }

    private void dfs(int sourceIndex, int sinkIndex, Set<String> inBetween,
                     BitSet[] transitiveClosure,
                     BitSet visited){

        for(int k=0; k < activityList.size(); k++){
            if(k != sourceIndex &
                    k != sinkIndex &
                    dfgAdjacencyMatrix[sourceIndex].get(k) &
                    transitiveClosure[k].get(sinkIndex) &
                    !visited.get(k)
            ){
                String source = activityList.get(k);
                visited.set(sourceIndex);
                inBetween.add(source);

                dfs(k,sinkIndex,inBetween,transitiveClosure,visited);

            }
        }

    }
}
