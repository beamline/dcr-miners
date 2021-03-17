package beamline.dcr;

import java.util.Arrays;
import java.util.List;

import beamline.core.miner.exceptions.MinerException;
import beamline.core.web.miner.models.MinerView;
import beamline.core.web.miner.models.Stream;
import beamline.dcr.miners.DFGBasedMiner;

public class Tester {

	public static void main(String[] args) throws MinerException {
		DFGBasedMiner sc = new DFGBasedMiner();
		sc.setStream(new Stream("test", "localhost", ""));
		sc.start();
		
		List<List<String>> traces = Arrays.asList(
				Arrays.asList("A", "B"),
				Arrays.asList("A", "B", "D"),
				Arrays.asList("A", "B", "C", "B")
		);
		
		int i = 0;
		for (List<String> trace : traces) {
			for(String activity : trace) {
				sc.consumeEvent("Case " + i, activity);
			}
			i++;
		}
		
		List<MinerView> views = sc.getViews(null);
		
		System.out.println("\n\n++++++++++++++++++++++++++++++++");
		for (MinerView v : views) {
			System.out.println("=== " + v.getName().toUpperCase() + " ===");
			System.out.println("");
			System.out.println(v.getValue());
			System.out.println("\n");
		}
		System.out.println("++++++++++++++++++++++++++++++++");
		
		sc.stop();
		System.exit(0);
	}
}
