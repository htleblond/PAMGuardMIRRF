package mirrfFeatureExtractor;

import java.util.ArrayList;
import java.util.Comparator;

import PamguardMVC.PamDataUnit;

/**
 * A data unit for storing slice data from .wmnt files for
 * when the ClipBlockProcess needs it.
 * @author Holly LeBlond
 */
//@SuppressWarnings("rawtypes")
public class FESliceDataUnit extends PamDataUnit {
	
	public long[] sliceStartSamples;
	public double[] sliceFreqs;
	
	public FESliceDataUnit() {
		super(0, 0, -1, -1);
	}
	
	public void setSliceData(String[] inputCSVEntry) {
		ArrayList<String[]> splitList = new ArrayList<String[]>();
		for (int i = 9; i < inputCSVEntry.length; i++) {
			try {
				String[] split = inputCSVEntry[i].split(">");
				Long.valueOf(split[0]);
				Double.valueOf(split[1]);
				splitList.add(split);
			} catch (Exception e) {
				continue;
			}
		}
		splitList.sort(Comparator.comparingLong(a -> Long.valueOf(a[0])));
		int arrSize = splitList.size();
		//if (arrSize > 5) arrSize = 5; // FOR TESTING, REMOVE IF IT'S NOT THE PROBLEM
		sliceStartSamples = new long[arrSize];
		sliceFreqs = new double[arrSize];
		for (int i = 0; i < arrSize; i++) {
			sliceStartSamples[i] = Long.valueOf(splitList.get(i)[0]);
			sliceFreqs[i] = Double.valueOf(splitList.get(i)[1]);
		}
	}
	
}