package beamline.miners.dcr.model.streamminers;

import java.io.Serializable;

import beamline.miners.dcr.model.relations.dfg.ExtendedDFG;

public interface StreamMiner extends Serializable {

	public void observeEvent(String traceId, String activityName);

	public ExtendedDFG getExtendedDFG();

	public void saveLog(String filePath);

	public int getNumberEventsSaved();

}
