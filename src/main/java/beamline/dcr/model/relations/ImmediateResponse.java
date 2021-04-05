package beamline.dcr.model.relations;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "ImmResp",
        latticeLevel = 3
)
public class ImmediateResponse implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {

        Set<Pair<String, String>> dcrResponses =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.RESPONSE);

        Set<Pair<String, String>> DFGRelations = unionRelationSet.getDFGRelations();
        for (Pair<String, String> dcrResponse : dcrResponses){
            if (DFGRelations.contains(dcrResponse)){
                unionRelationSet.replaceDCRRelation(
                        Triple.of(dcrResponse.getLeft(),dcrResponse.getRight(), DcrModel.RELATION.IMMRESPONSE),
                        Triple.of(dcrResponse.getLeft(),dcrResponse.getRight(), DcrModel.RELATION.RESPONSE));
            }
        }

        /*Set<Pair<String, String>> DFGRelations = unionRelationSet.getDFGRelations();

        for (Pair<String, String> relation : DFGRelations) {
            String sourceActivity = relation.getLeft();
            double sourceAvgInd = unionRelationSet.getActivityDecoration(sourceActivity).getAverageIndex();
            String targetActivity = relation.getRight();
            double targetAvgInd = unionRelationSet.getActivityDecoration(targetActivity).getAverageIndex();
            if (sourceAvgInd < targetAvgInd) {
                unionRelationSet.addDcrRelation(Triple.of(sourceActivity, targetActivity, DcrModel.RELATION.IMMRESPONSE));
            }
        }*/
    }

}
