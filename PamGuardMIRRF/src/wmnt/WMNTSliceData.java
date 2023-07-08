package wmnt;

import fftManager.FFTDataUnit;
import whistlesAndMoans.SliceData;

/**
 * Only exists to override SliceData's protected constructors.
 */
public class WMNTSliceData extends SliceData {

	public WMNTSliceData(int sliceNumber, int sliceLength, FFTDataUnit fftDataUnit) {
		super(sliceNumber, sliceLength, fftDataUnit);
	}
	
	public WMNTSliceData(SliceData oldSlice, int peakToSteal) {
		super(oldSlice, peakToSteal);
	}
	
	public WMNTSliceData(int sliceNumber, long startSample, int[][] peakInfo){
		super(sliceNumber, startSample, peakInfo);
	}

}