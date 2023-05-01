package mirrfFeatureExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import Acquisition.RawDataBinaryDataSource;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

import org.apache.commons.lang3.ArrayUtils; // Try something else if this doesn't import properly

//public class FERawDataBlock extends PamRawDataBlock {
@Deprecated
public class FERawDataBlock {
	
	List<RawDataUnit> pamDataUnits;
	PamRawDataBlock oldBlock;
	
	public FERawDataBlock(PamRawDataBlock oldBlock) {
		//super(oldBlock.getDataName(), oldBlock.getParentProcess(), oldBlock.getChannelMap(), oldBlock.getSampleRate());
		this.oldBlock = oldBlock;
		pamDataUnits = new ArrayList<RawDataUnit>();
		//System.out.print("pamDataUnits: ");
		//System.out.println("oldBlock: "+String.valueOf(oldBlock.getUnitsCount()));
		for (int i = 0; i < oldBlock.getUnitsCount(); i++) {
			pamDataUnits.add(oldBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT)); //Try other reference value if this doesn't work
		}
	}
	
	/**
	 * Gets samples of raw data into a pre existing array. If the array is the wrong
	 * size or does not exist, then a new one is created. 
	 * @param startSample
	 * @param duration
	 * @param channelMap
	 * @param wavData
	 * @return double array of raw data
	 */
	//@Override
	synchronized public double[][] getSamples(long startSample, int duration, int channelMap, double[][] wavData) {
		int nChan = PamUtils.getNumChannels(channelMap);
		if (duration < 1) return null;
		if (wavData == null || nChan != wavData.length || duration != wavData[0].length) {
			wavData = new double[nChan][duration];
		}
		wavData = getTheSamples2(startSample, duration, channelMap, wavData);
		if (wavData != null) {
			//System.out.println("WAVDATA 100: "+String.valueOf(wavData[0][100]));
			return wavData;
		}
		System.out.println("Block issue 4");
		return null;
	}
	
	/**
	 * Does the work for the above two functions.
	 * @param startSample
	 * @param duration
	 * @param channelMap
	 * @param waveData
	 * @return copies data into a double array, taking if from multiple raw datablocks
	 * if necessary
	 */
	//synchronized public boolean getTheSamples2(long startSample, int duration, int channelMap, double[][] waveData) {
	synchronized public double[][] getTheSamples2(long startSample, int duration, int channelMap, double[][] waveData) {
		// find the first data block
//		int blockNo = -1;
		ListIterator<RawDataUnit> rawIterator = pamDataUnits.listIterator();
		List<RawDataUnit> currUnits = new ArrayList<RawDataUnit>();
		RawDataUnit unit = null;
		if (pamDataUnits.size() == 0) {
			System.out.println("Block issue 1");
			//return false;
			return null;
		}
		boolean foundStart = false;
		long blockFirstSample = 48000*3600;
		long blockLastSample = -1;
		//System.out.println("rawIterator.hasNext() = "+String.valueOf(rawIterator.hasNext()));
		//System.out.println("Start sample = "+String.valueOf(startSample));
		boolean hasPrintedFirstSample = false;
		while (rawIterator.hasNext()) {
			unit = rawIterator.next();
			if (!hasPrintedFirstSample) {
				//System.out.println("First unit.getStartSample() = "+String.valueOf(unit.getStartSample()));
				hasPrintedFirstSample = true;
			}
			
			//if (unit.getStartSample() <= startSample && unit.getLastSample() <= startSample) {
			if (unit.getStartSample() <= startSample && unit.getLastSample() > startSample) {
				foundStart = true;
			}
			
			if (unit.getStartSample() < blockFirstSample) {
				blockFirstSample = unit.getStartSample();
			}
			if (unit.getLastSample() > blockLastSample) {
				blockLastSample = unit.getLastSample();
			}
			
			if (unit.getLastSample() >= startSample) {
				currUnits.add(unit);
				if (unit.getLastSample() >= startSample+duration) {
					break;
				}
			}
		}
		//System.out.println("Last unit.getStartSample() =  "+String.valueOf(unit.getStartSample()));
		
	/*	if (foundStart == false) {
			System.out.println("Block issue 2");
			//return false;
			return null;
		} */
		int nChan = PamUtils.getNumChannels(channelMap);
		int iChan;
		int outChan;
		double[] unitData;
		int offset;
		int completeChannels = 0;
		int[] channelSamples = new int[nChan]; // will need to keep an eye on
												// how many samples we have for
												// each channel
		double[][] outpData = new double[1][0];
		//System.out.println("CURRUNITS.SIZE: "+String.valueOf(currUnits.size()));
		for (int i = 0; i < currUnits.size(); i++) {
			unitData = currUnits.get(i).getRawData();
			//System.out.println("UNITDATA 10: "+String.valueOf(unitData[10]));
			long begin = 0;
			long end = unitData.length-1;
			if (i == 0) {
				begin = startSample - currUnits.get(i).getStartSample();
			}
			if (i == currUnits.size()-1) {
				end = (startSample+duration) - currUnits.get(i).getStartSample();
			}
			//System.out.println("Begin: "+String.valueOf(currUnits.get(i).getStartSample())+" End: "+String.valueOf(currUnits.get(i).getLastSample()));
			try {
				outpData[0] = ArrayUtils.addAll(outpData[0], Arrays.copyOfRange(unitData, (int) begin, (int) end));
			} catch (Exception e) {
				System.out.println("Block issue 3");
				//outpData[0] = null;
				return null;
			}
		}
		//System.out.println("outpData length: "+String.valueOf(outpData[0].length));
		
		//waveData = outpData;
		//System.out.println("WAVEDATA 0 10: "+String.valueOf(waveData[0][10]));
		return outpData;
	}
	
	synchronized public long[] getStartSampleLimits() {
		long[] outp = new long[] {-1,-1};
		ListIterator<RawDataUnit> rawIterator = pamDataUnits.listIterator();
		RawDataUnit unit = null;
		if (pamDataUnits.size() == 0) {
			//System.out.println("\ttest: pamDataUnits has size 0.");
			return outp;
		}
		boolean hasPrintedFirstSample = false;
		while (rawIterator.hasNext()) {
			unit = rawIterator.next();
			if (!hasPrintedFirstSample) {
				//System.out.println("\\ttest: First unit.getStartSample() = "+String.valueOf(unit.getStartSample()));
				outp[0] = unit.getStartSample();
				hasPrintedFirstSample = true;
			}
		}
		//System.out.println("\\ttest: Last unit.getStartSample() =  "+String.valueOf(unit.getLastSample()));
		if (unit != null) {
			outp[1] = unit.getLastSample();
		}
		return outp;
	}
	
	public double getMax(double[][] inp) {
		double max = 0.0;
		for (int i = 0; i < inp.length; i++) {
			for (int j = 0; j < inp[0].length; j++) {
				if (inp[i][j] > max) {
					max = inp[i][j];
				}
			}
		}
		return max;
	}
	
	/**
	 * Check the data block integrity - that is that all units are
	 * in order and that the sample numbers increase correctly.
	 * <p>This is used when loading data offline. 
	 * @return
	 */
/*	synchronized private boolean checkIntegrity() {
		int nChannels = PamUtils.getNumChannels(getChannelMap());
		int errors = 0;
		int[] channelList = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			channelList[i] = PamUtils.getNthChannel(i, getChannelMap());
		}
		int expectedChannel = channelList[0];
		ListIterator<RawDataUnit> iterator = getListIterator(0);
		RawDataUnit dataUnit;
		long[] expectedSample = new long[PamConstants.MAX_CHANNELS];
		int singleChannel;
		int channelIndex = 0;
		int unitIndex = 0;
		while (iterator.hasNext()) {
			expectedChannel = channelList[channelIndex];
			dataUnit = iterator.next();
			// check it's the expected channel. 
			singleChannel = PamUtils.getSingleChannel(dataUnit.getChannelBitmap());
			if (singleChannel != expectedChannel) {
				reportProblem(++errors, unitIndex, String.format("Got channel %d, expected %d", singleChannel, expectedChannel)
						, dataUnit);
			}
			
			// check the sample number
			if (expectedSample[channelIndex] > 0) {
				if (expectedSample[channelIndex] != dataUnit.getStartSample()) {
					reportProblem(++errors, unitIndex, String.format("Got sample %d, expected %d", 
							dataUnit.getStartSample(), expectedSample[channelIndex]), dataUnit);
				}
			}
			
			// check the length
			if (dataUnit.getSampleDuration() != dataUnit.getRawData().length) {
				reportProblem(++errors, unitIndex, String.format("Have %d samples, expected %d", 
						dataUnit.getSampleDuration(), dataUnit.getRawData().length), dataUnit);
			}
			
			// move expectations.
			expectedSample[channelIndex] = dataUnit.getStartSample() + dataUnit.getSampleDuration();
			if (++channelIndex >= nChannels) {
				channelIndex = 0;
			}
			unitIndex++;
		}
		
		return errors == 0;
	} */
	
	public PamRawDataBlock getOldBlock() {
		return oldBlock;
	}
	
/*	private void reportProblem(int nErrors, int index, String str, RawDataUnit unit) {
		System.out.println(String.format("Error %d in RawDataBlock item %d of %d: %s", 
				nErrors, index, getUnitsCount(), str));
		System.out.println(unit.toString());
	} */
}