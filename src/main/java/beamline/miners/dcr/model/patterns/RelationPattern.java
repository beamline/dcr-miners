package beamline.miners.dcr.model.patterns;

import beamline.miners.dcr.model.relations.UnionRelationSet;

public interface RelationPattern {
    public void populateConstraint(UnionRelationSet unionRelationSet);

}
