package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

@ExposedDcrPattern(
        name = "Sequence",
        dependencies = {"ImmCondition","ImmResponse"}
)
public class Sequence implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {

        Set<Triple<String, String, DcrModel.RELATION>> dcrImmConditions =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.IMMCONDITION);

        Set<Triple<String, String, DcrModel.RELATION>> dcrImmResponse =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.IMMRESPONSE);

        for (Triple<String, String, DcrModel.RELATION> immCondRelation : dcrImmConditions) {
            String src = immCondRelation.getLeft();
            String tar = immCondRelation.getMiddle();
            Triple<String, String, DcrModel.RELATION> respImmRespRelation =
                    Triple.of(src, tar, DcrModel.RELATION.IMMRESPONSE);
            if (dcrImmResponse.contains(respImmRespRelation)) {

                unionRelationSet.addDcrRelation(Triple.of(src, tar, DcrModel.RELATION.SEQUENCE));

                unionRelationSet.removeDCRRelation(immCondRelation);
                unionRelationSet.removeDCRRelation(respImmRespRelation);
            }
        }
    }

}
