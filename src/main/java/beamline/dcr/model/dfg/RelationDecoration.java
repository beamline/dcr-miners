package beamline.dcr.model.dfg;

public class RelationDecoration {

	private int frequency = 0;

	public void addNewObservation() {
		frequency++;
	}

	public void decrementFrequency(){frequency--;}
	
	public int getFrequency() {
		return frequency;
	}
	
	@Override
	public String toString() {
		return "frequency: " + frequency;
	}
}
