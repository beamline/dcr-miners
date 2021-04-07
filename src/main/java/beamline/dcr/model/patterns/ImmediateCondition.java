package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.DcrModel;
import beamline.dcr.model.UnionRelationSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import java.util.Set;

@ExposedDcrPattern(
        name = "ImmCondition",
        dependencies = {"Condition"}
)
public class ImmediateCondition implements RelationPattern {

    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {

        Set<Triple<String, String, DcrModel.RELATION>> dcrConditions =
                unionRelationSet.getDcrRelationWithPattern(DcrModel.RELATION.CONDITION);

        Set<Pair<String, String>> DFGRelations = unionRelationSet.getDFGRelations();
        for (Triple<String, String, DcrModel.RELATION> dcrCondition : dcrConditions){
            String src = dcrCondition.getLeft();
            String tar = dcrCondition.getMiddle();
            if (DFGRelations.contains(Pair.of(src,tar))){
                unionRelationSet.replaceDCRRelation(
                        Triple.of(src,tar, DcrModel.RELATION.IMMCONDITION),
                        dcrCondition);
            }
        }

    }
}
