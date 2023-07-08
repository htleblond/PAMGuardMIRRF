package wmnt;

import binaryFileStorage.*;
import fftManager.FFTDataBlock;
import whistlesAndMoans.ConnectedRegion;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;
import whistlesAndMoans.WhistleBearingInfo;
import whistlesAndMoans.WhistleToneConnectProcess;
import whistlesAndMoans.WhistleToneConnectProcess.ShapeConnector;

import java.io.*;

import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamProcess;

/**
 * This is the class used for reading the contents of binary files into the table in the side panel.
 * @author Holly LeBlond
 */
public class WMNTBinaryReader extends BinaryInputStream {
	
	protected WMNTControl wmntControl;
	protected WhistleToneConnectProcess wmDetector;
	
	public BinaryHeader bh;
	public BinaryFooter bf;
	public boolean worked;

	public WMNTBinaryReader(WMNTControl wmntControl, String filename, WhistleToneConnectProcess wmDetector) {
		super(new BinaryStore("Binary Store"), null);
		this.wmntControl = wmntControl;
		this.wmDetector = wmDetector;
		File inpfile = new File(filename);
		boolean opened = openFile(inpfile);
		this.worked = opened;
		if (opened == true){
			this.bh = readHeader();
			this.bf = getBinaryFooter();
		} else {
			this.bh = null;
			this.bf = null;
		}
	}
	
	public BinaryObjectData nextData(int fileFormat) {
		return readNextObject(fileFormat);
	}
	
	/**
	 * Literally just a slightly-modified copy of WhistleBinaryDataSource.sinkData.
	 * @author Doug Gillespie (with slight modifications from Holly LeBlond)
	 */
	public ConnectedRegionDataUnit getDataAsDataUnit(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		ConnectedRegionDataUnit crdu = null;
		ConnectedRegion cr = null;
		WMNTSliceData sliceData;
		
		ConnectedRegionDataBlock wmDataBlock = wmDetector.getOutputData();
		int fftHop = wmDataBlock.getFftHop();
		int fftLength = wmDataBlock.getFftLength();
		float sampleRate = wmDataBlock.getSampleRate();
		int fileVersion = binaryObjectData.getVersionNumber();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
//		long intLength;
		//if (delayScale == 0) {
		//	delayScale = wmDetector.getDelayScale();
		//}
		long startSample;
		int channelMap; 
		int singleChannel;
		short nSlices;
		int sliceNum;
		int nPeaks;
		double amplitude = 0;
		double[] delays = null;
		int nDelays;
		long firstSliceSample;
		int[][] peakInfo;
		int firstSliceNum = 0;
		int[] timeBins;
		int[] peakFreqsBins;
		try {
//			intLength = dis.readInt(); // should always be dataLength-4 !
//			firstSliceSample = (long) ((double)(binaryObjectData.getTimeMillis() - bh.getDataDate()) * sampleRate / 1000.);
//			firstSliceSample = binary

			/**
			 * Bit of mess sorted out on 15/5/2020. Was working because module version went from 1 to 2 at same time 
			 * as file version went from 3 to 4. May have been some middly stuff where file version and module 
			 * There is some FV 3 with MV 1, in which case data were probably duplicated. 
			 */
			if (fileVersion > 3) { // basic data now in standard format. 
				firstSliceSample = startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
				
				// if the DataUnitBaseData contains a sequence map, use it in place of the channel map
				if (binaryObjectData.getDataUnitBaseData().getSequenceBitmap()!=null) {
					channelMap = binaryObjectData.getDataUnitBaseData().getSequenceBitmap();
				} else {
					channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
				}
			}
			else { // old stuff which should only be 
				firstSliceSample = startSample = dis.readLong();
				channelMap = dis.readInt();
				binaryObjectData.getDataUnitBaseData().setChannelBitmap(channelMap);
			}

//			if (channelMap != 1) {
//				System.out.println("Channel map = " + channelMap);
//			}
			singleChannel = PamUtils.getLowestChannel(channelMap);
			nSlices = dis.readShort();
			if (moduleVersion >= 1) {
				amplitude = (double) dis.readShort() / 100.;
			}
			if (fileVersion >= 4) {
				// As of FILE version 4, the time delays are now stored in the DataUnitBaseData object.
				// If there are no time delays yet, this method would return null.  In previous versions
				// however, if there were no time delays an empty array would be created.  Therefore,
				// to maintain compatibility with the rest of the code, create an empty array if the
				// method returns a null value
				// PAMGuard file that have FV4 and MV2 will end up in this part of the conditional statement
				// leaving the conditional reading of floats for Network data which is still in the older format. 
				delays = binaryObjectData.getDataUnitBaseData().getTimeDelaysSeconds();
				if (delays==null) {
					delays=new double[0];
				}
			}
			else if (moduleVersion <2)  { // old files with FV<4, delays in a messed up integer format. 
				nDelays = dis.readByte();
				//					if (nDelays > 1) {
				//						System.out.println("Bad number of delays : " + nDelays);
				//					}

				delays = new double[nDelays];
				//					if (moduleVersion == 1) {
				for (int i = 0; i < nDelays; i++) {
					//delays[i] = (double) dis.readShort() / delayScale / sampleRate;
					delays[i] = (double) dis.readShort() / wmDetector.getDelayScale() / sampleRate;
				}
			} 
			else if (moduleVersion == 2) { // still only for FV<4, so network data only. 
				nDelays = dis.readByte();
				delays = new double[nDelays];
				for (int i = 0; i < nDelays; i++) {
					delays[i] = (double) dis.readFloat() / sampleRate;
				}
			}
		
			/*
			 * sliceDataList = cr.getSliceData();
			for (int i = 0; i < nSlices; i++) {
				sliceData = sliceDataList.get(i);
				dos.writeInt(sliceData.sliceNumber);
				dos.writeByte(sliceData.nPeaks);
				peakInfo = sliceData.peakInfo;
				for (int j = 0; j < sliceData.nPeaks; j++) {
					for (int k = 0; k < 4; k++) {
						dos.writeShort(peakInfo[j][k]);
					}
				}
			}
			 */
			
			timeBins = new int[nSlices];
			peakFreqsBins = new int[nSlices];
			for (int i = 0; i < nSlices; i++) {
				sliceNum = dis.readInt();
				nPeaks = dis.readByte();
				if (nPeaks < 0) {
					System.out.println("Negative number of peaks: " + nPeaks);
				}
				if (i == 0) {
					firstSliceNum = sliceNum;
					FFTDataBlock fftDB = (FFTDataBlock) wmDetector.getParentDataBlock();
					cr = new ConnectedRegion(singleChannel, sliceNum, 0, fftDB.getFftLength());
				}
				peakInfo = new int[nPeaks][4];
				for (int j = 0; j < nPeaks; j++) {
					for (int k = 0; k < 4; k++) {
						peakInfo[j][k] = dis.readShort();
					}
				}
				sliceData = new WMNTSliceData(sliceNum, firstSliceSample + 
						fftHop * (sliceNum-firstSliceNum), peakInfo);
				cr.addOfflineSlice(sliceData);
				timeBins[i] = sliceData.getSliceNumber();
				peakFreqsBins[i] = sliceData.getPeakBin();
			}			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		cr.cleanFragmentedFragment();
//		cr.sett
//		cr.addOfflineSlice(sliceData);
//		cr.condenseInfo();
		crdu = new ConnectedRegionDataUnit(binaryObjectData.getDataUnitBaseData(), cr, wmDetector);
//		crdu.setTimeMilliseconds(binaryObjectData.getTimeMilliseconds());
//		crdu.setTimeDelaysSeconds(delays);
//		crdu.setCalculatedAmlitudeDB(amplitude);
//		crdu.setSampleDuration((long) ((nSlices+1) * fftHop));
//		crdu.setSequenceBitmap(binaryObjectData.getDataUnitBaseData().getSequenceBitmap());
//		crdu.setChannelBitmap(binaryObjectData.getDataUnitBaseData().getChannelBitmap());
		/*
		 *  now also need to recalculate bearings using the appropriate bearing localiser.
		 *  These are hidden away in the sub processes and may be different for different 
		 *  hydrophone groups. 
		 *  Only do this here if we're in viewer mode, not network receive mode. 
		 *  If we're n network receive mode, we can't do this until 
		 *  channel numbers have been reassigned.   
		 */
		//if ((runMode == PamController.RUN_PAMVIEW || runMode == PamController.RUN_NOTHING) && delays != null) {
		if (delays != null) {
			ShapeConnector shapeConnector = wmDetector.findShapeConnector(channelMap);
			if (shapeConnector != null) {
				BearingLocaliser bl = shapeConnector.getBearingLocaliser();
				if (bl != null) {
					double[][] angles = bl.localise(delays, crdu.getTimeMilliseconds());
					WhistleBearingInfo newLoc = new WhistleBearingInfo(crdu, bl, 
							shapeConnector.getGroupChannels(), angles);
					newLoc.setArrayAxis(bl.getArrayAxis());
					newLoc.setSubArrayType(bl.getArrayType());
					crdu.setTimeDelaysSeconds(delays);
					crdu.setLocalisation(newLoc);
				}
			}
		}
		
		try {
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return crdu;
	}
	
	public void closeReader() {
		closeFile();
	}
}