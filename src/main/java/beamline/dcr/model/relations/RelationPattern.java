package beamline.dcr.model.relations;

import beamline.dcr.model.UnionRelationSet;

public interface RelationPattern {
    public void populateConstraint(UnionRelationSet unionRelationSet);

}
