package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "ImmResponse",
        dependencies = {"Response"}
)
public class ImmediateResponse implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {

        Set<Triple<String, String, DcrModel.RELATION>> dcrReponses =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.RESPONSE);

        Set<Pair<String, String>> DFGRelations = unionRelationSet.getDFGRelations();
        for (Triple<String, String, DcrModel.RELATION> dcrResponse : dcrReponses){
            String src = dcrResponse.getLeft();
            String tar = dcrResponse.getMiddle();
            if (DFGRelations.contains(Pair.of(src,tar))){
                unionRelationSet.replaceDCRRelation(
                        Triple.of(src,tar, DcrModel.RELATION.IMMRESPONSE),
                        dcrResponse);
            }
        }
    }

}
