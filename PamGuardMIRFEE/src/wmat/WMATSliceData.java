package wmat;

import fftManager.FFTDataUnit;
import whistlesAndMoans.SliceData;

/**
 * Only exists to override SliceData's protected constructors.
 */
public class WMATSliceData extends SliceData {

	public WMATSliceData(int sliceNumber, int sliceLength, FFTDataUnit fftDataUnit) {
		super(sliceNumber, sliceLength, fftDataUnit);
	}
	
	public WMATSliceData(SliceData oldSlice, int peakToSteal) {
		super(oldSlice, peakToSteal);
	}
	
	public WMATSliceData(int sliceNumber, long startSample, int[][] peakInfo){
		super(sliceNumber, startSample, peakInfo);
	}

}