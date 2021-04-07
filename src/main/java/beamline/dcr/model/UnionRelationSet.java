package beamline.dcr.model;

import beamline.dcr.model.dfg.ActivityDecoration;
import beamline.dcr.model.dfg.ExtendedDFG;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UnionRelationSet {

    private final Integer threshold;
    private ExtendedDFG extendedDFG;
    private Set<Triple<String,String, DcrModel.RELATION>> DcrRelations =  new HashSet<>();

    public UnionRelationSet(ExtendedDFG extendedDFG, Integer threshold){
        this.extendedDFG = extendedDFG;
        this.threshold = threshold;
    }
    public Set<Pair<String, String>> getDFGRelations(){
        return extendedDFG.getRelationsAboveThreshold(threshold);
    }

    public ActivityDecoration getActivityDecoration(String activity){
        return extendedDFG.getActivityDecoration(activity);
    }

    public Set<Triple<String, String, DcrModel.RELATION>> getDcrRelations() {
        return DcrRelations;
    }

    public void addDcrRelation(Triple<String,String, DcrModel.RELATION> relation){
        DcrRelations.add(relation);
    }

    public void removeDCRRelation(Triple<String,String, DcrModel.RELATION> relation){
        DcrRelations.remove(relation);
    }

    public void replaceDCRRelation(Triple<String,String, DcrModel.RELATION> newRelation,Triple<String,String, DcrModel.RELATION> oldRelation){
        removeDCRRelation(oldRelation);
        addDcrRelation(newRelation);
    }
    public boolean DCRRelationsContains(Triple<String,String, DcrModel.RELATION> relation){
        return DcrRelations.contains(relation);
    }

    public Set<Triple<String, String, DcrModel.RELATION>> getDcrRelationWithPattern(DcrModel.RELATION pattern){
        return DcrRelations.stream()
                .filter(entry -> entry.getRight() == pattern)
                .collect(Collectors.toSet());
    }

    public String[] getUniqueActivities(){
        return extendedDFG.getActivities().toArray(new String[0]);
    }
}
