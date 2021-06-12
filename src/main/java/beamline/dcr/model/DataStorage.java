package beamline.dcr.model;

import beamline.dcr.model.dfg.ExtendedDFG;

import java.util.HashMap;
import java.util.Map;

public interface DataStorage {


    public void observeEvent(String traceId,String activityName);

    public ExtendedDFG getExtendedDFG();

}
