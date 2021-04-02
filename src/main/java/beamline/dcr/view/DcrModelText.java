package beamline.dcr.view;

import beamline.dcr.model.DcrModel;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

public class DcrModelText {


    private DcrModel model;


    public DcrModelText(DcrModel model) {
        this.model = model;

    }




    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();

        b.append("PATTERNS");
        b.append(System.lineSeparator());

        for (Triple<String, String, DcrModel.RELATION> relation : model.getRelations()) {
            b.append(relation.getRight().name()+"(" + relation.getLeft() +
                    "," + relation.getMiddle() + ")");
            b.append(System.lineSeparator());
        }


        return b.toString();
    }

}
