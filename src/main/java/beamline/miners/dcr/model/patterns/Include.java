package beamline.miners.dcr.model.patterns;

import beamline.miners.dcr.annotations.ExposedDcrPattern;
import beamline.miners.dcr.model.relations.UnionRelationSet;

@ExposedDcrPattern(
        name = "Include",
        dependencies = {"Sequence"}
)
public class Include  implements RelationPattern{
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        new ExcludeAndInclude().populateConstraint(unionRelationSet);
    }
}
