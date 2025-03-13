package wmat;

import java.util.List;

import whistlesAndMoans.SliceData;

/**
 * Performs various calculations on an input list of SliceData.
 * Kinda pointless now as all these calculations are done in the Feature Extractor's Python script and not here.
 * @author Holly LeBlond
 */
@Deprecated
public class WMATSliceDataCalculator {
	
	//public List<SliceData> sliceDataList;
	public SliceData[] sliceDataArr;
	public int sr;
	public int fftLen;
	
	public WMATSliceDataCalculator(List<SliceData> sliceDataList, int sr, int fftLen) {
		//this.sliceDataList = sliceDataList;
		this.sr = sr;
		this.fftLen = fftLen;
		
		this.sliceDataArr = new SliceData[sliceDataList.size()];
		for (int i = 0; i < sliceDataList.size(); i++) {
			SliceData sd = sliceDataList.get(i);
			this.sliceDataArr[i] = sd;
			//System.out.println(sd.getSliceNumber());
		}
	}
	
	public double[] getFreqs() {
		double[] outp = new double[sliceDataArr.length];
		for (int i = 0; i < outp.length; i++)
			//outp[i] = (double) sr * sliceDataArr[i].getPeakInfo()[0][2] / fftLen; // getPeakBin doesn't work offline.
			outp[i] = (double) sr * sliceDataArr[i].getPeakInfo()[0][2] / fftLen;
		return outp;
	}
	
	public long[] getTimestamps() {
		long[] outp = new long[sliceDataArr.length];
		for (int i = 0; i < outp.length; i++)
			outp[i] = sliceDataArr[i].getStartSample();
		return outp;
	}
	
	public double[] getTimestampsInSeconds() {
		long[] longs = getTimestamps();
		double[] outp = new double[longs.length];
		for (int i = 0; i < longs.length; i++) {
			outp[i] = (longs[i] - longs[0])/1000;
		}
		return outp;
	}
	
/*	public double[] getAmps() {
		double[] outp = new double[sliceDataArr.length];
		for (int i = 0; i < outp.length; i++)
			outp[i] = sliceDataArr[i].am;
		return outp;
	} */
	
	public double calculateFreqElbowAngle() {
		double[] deriv2 = calculate2ndDerivative(getFreqs());
		if (deriv2.length <= 1) return 180.0;
		int maxIndex = 0;
		for (int i = 1; i < deriv2.length; i++) {
			if (Math.abs(deriv2[i]) > Math.abs(deriv2[maxIndex]))
				maxIndex = i;
		}
		//if (maxIndex == 0) return 180.0;
		SliceData elbow = sliceDataArr[maxIndex+1];
		SliceData start = sliceDataArr[0];
		SliceData end = sliceDataArr[sliceDataArr.length-1];
		double x1 = (double) 1000 * (start.getStartSample() - elbow.getStartSample()) / sr;
		double y1 = (double) sr * (start.getPeakInfo()[0][2] - elbow.getPeakInfo()[0][2]) / fftLen;
		double x2 = (double) 1000 * (end.getStartSample() - elbow.getStartSample()) / sr;
		double y2 = (double) sr * (end.getPeakInfo()[0][2] - elbow.getPeakInfo()[0][2]) / fftLen;
		//System.out.println(String.valueOf(maxIndex+1)+"/"+String.valueOf(sliceDataArr.length));
		//System.out.println("("+String.valueOf(x1)+", "+String.valueOf(y1)+") ("+String.valueOf(x2)+", "+String.valueOf(y2)+")");
		double dot = x1*x2 + y1*y2;
		double mag = Math.sqrt(Math.pow(x1,2)+Math.pow(y1,2)) * Math.sqrt(Math.pow(x2,2)+Math.pow(y2,2));
		if (mag == 0.0) return 180.0;
		//System.out.println(180 * Math.acos(dot/mag) / Math.PI);
		return 180 * Math.acos(dot/mag) / Math.PI;
	}
	
	public static double[] calculateDerivative(double[] arr) {
		if (arr.length <= 1) return new double[] {0};
		double[] outp = new double[arr.length-1];
		for (int i = 0; i < outp.length; i++)
			outp[i] = arr[i+1] - arr[i];
		return outp;
	}
	
	public static double[] calculate2ndDerivative(double[] arr) {
		if (arr.length <= 1) return new double[] {0};
		double[] deriv = calculateDerivative(arr);
		double[] outp = new double[deriv.length-1];
		for (int i = 0; i < outp.length; i++)
			outp[i] = deriv[i+1] - deriv[i];
		return outp;
	}
	
	public static double calculateRange(double[] arr) {
		return calculateMax(arr) - calculateMin(arr);
	}
	
	public static double calculateMean(double[] arr) {
		double sum = 0.0;
		for (int i = 0; i < arr.length; i++) sum += arr[i];
		return sum / arr.length;
	}
	
	public static double calculateStandardDeviation(double[] arr) {
		double mean = calculateMean(arr);
		double var = 0;
		for (int i = 0; i < arr.length; i++)
			var += Math.pow(arr[i]-mean, 2);
		var /= arr.length;
		return Math.sqrt(var);
	}
	
	public static double calculateMin(double[] arr) {
		double outp = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (outp > arr[i]) outp = arr[i];
		}
		return outp;
	}
	
	public static double calculateMax(double[] arr) {
		double outp = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (outp < arr[i]) outp = arr[i];
		}
		return outp;
	}
	
}