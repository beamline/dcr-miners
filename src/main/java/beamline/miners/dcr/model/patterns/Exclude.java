package beamline.miners.dcr.model.patterns;

import beamline.miners.dcr.annotations.ExposedDcrPattern;
import beamline.miners.dcr.model.relations.UnionRelationSet;

@ExposedDcrPattern(
        name = "Exclude",
        dependencies = {"Sequence"}
)
public class Exclude  implements RelationPattern{
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        new ExcludeAndInclude().populateConstraint(unionRelationSet);
    }
}
