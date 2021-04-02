package beamline.dcr.model;

import beamline.dcr.annotations.ExposedDcrPattern;
import beamline.dcr.model.relations.RelationPattern;

public class DcrPattern {
    private String name;
    private int latticeLevel;
    private Class<RelationPattern> relationPatternClass;

    public DcrPattern(ExposedDcrPattern annotation, Class<RelationPattern> aClass){
        this.name = annotation.name();
        this.latticeLevel = annotation.latticeLevel();

        this.relationPatternClass = aClass;


    }
}
