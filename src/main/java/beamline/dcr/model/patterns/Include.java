package beamline.dcr.model.patterns;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.UnionRelationSet;

@ExposedDcrPattern(
        name = "Include",
        dependencies = {"Condition","Response"}
)
public class Include  implements RelationPattern{
    @Override
    public void populateConstraint(UnionRelationSet unionRelationSet) {
        new ExcludeAndInclude().populateConstraint(unionRelationSet);
    }
}
