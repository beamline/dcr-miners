package beamline.dcr.model.dfg;

public class ActivityDecoration {

	double observations = 0;
	double avgIndex = 0;
	
	double observationsFirstOccurrance = 0;
	double avgFirstOccurrence = 0;

	public void addNewObservation(int currentIndex, boolean isFirstOccurrance) {
		observations++;
		avgIndex = avgIndex + ((currentIndex - avgIndex) / observations);
		
		if (isFirstOccurrance) {
			observationsFirstOccurrance++;
			avgFirstOccurrence = avgFirstOccurrence + ((currentIndex - avgFirstOccurrence) / observationsFirstOccurrance);
		}
	}
	
	public double getAverageIndex() {
		return avgIndex;
	}
	
	public double getAverageFirstOccurrance() {
		return avgFirstOccurrence;
	}
	
	@Override
	public String toString() {
		return "average index: " + avgIndex + " ; average first occurrance: " + avgFirstOccurrence;
	}

}
