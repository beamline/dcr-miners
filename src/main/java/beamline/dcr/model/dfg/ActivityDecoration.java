package beamline.dcr.model.dfg;

public class ActivityDecoration {

	double observations = 0;
	double avgIndex = 0;
	int currentIndex = 0;
	
	double observationsFirstOccurrance = 0;
	double avgFirstOccurrence = 0;


	public void addNewObservation(int currentIndex, boolean isFirstOccurrance) {
		this.currentIndex = currentIndex;

		observations++;
		avgIndex = avgIndex + ((currentIndex - avgIndex) / observations);
		
		if (isFirstOccurrance) {
			observationsFirstOccurrance++;
			avgFirstOccurrence = avgFirstOccurrence + ((currentIndex - avgFirstOccurrence) / observationsFirstOccurrance);
		}
	}
	public double getNumObservations() {
		return observations;
	}
	public double getAverageIndex() {
		return avgIndex;
	}
	public boolean appearMostOnce(){ return 1==observations/observationsFirstOccurrance;}
	public double getTraceAppearances(){ return observationsFirstOccurrance;}
	public double getAverageFirstOccurrence() {
		return avgFirstOccurrence;
	}
	
	@Override
	public String toString() {
		return "average index: " + avgIndex + " ; average first occurrence: " + avgFirstOccurrence;
	}

}
