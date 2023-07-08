package mirrfFeatureExtractor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;

import org.docx4j.org.apache.poi.poifs.storage.RawDataBlock;

import Acquisition.AcquisitionControl;
import warnings.PamWarning;
import clipgenerator.localisation.ClipDelays;
import fftManager.FFTDataBlock;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import annotation.handler.ManualAnnotationHandler;

/**
 * A modified version of clipGenerator.ClipProcess that creates sound clips
 * when Whistle and Moan Detector contours occur and sends them to the
 * thread manager for extracting feature data via a Python script.
 * @author Holly LeBlond (original code by Doug Gillespie)
 */
public class FEProcess extends PamProcess {
	
	protected ClipBlockProcess[] clipBlockProcesses;
	protected List<ClipRequest> clipRequestQueue;
	protected Object clipRequestSynch = new Object();
	protected PamRawDataBlock rawDataBlock;
	protected long specMouseDowntime;
	protected boolean specMouseDown;
	protected long masterClockTime;
	protected ClipDelays clipDelays;
	protected ManualAnnotationHandler manualAnnotaionHandler;
	protected static PamWarning warningMessage = new PamWarning("Clip Generator", "", 2);
	
	protected FEControl feControl;
	protected double[][] nrData;
	protected PamDataUnit prevDU;
	protected int clusterCountID;
	protected RawDataBlockCheckerThread rdbct;
	protected ArrayList<ClipRequest> csvClipList;
	
	protected FEDataBlock vectorDataBlock;

	public FEProcess(FEControl feControl) {
		//super(feControl);
		super(feControl, null);
		this.feControl = feControl;
		clipRequestQueue = new LinkedList<ClipRequest>();
		
		prevDU = null;
		clusterCountID = 0;
		
		vectorDataBlock = new FEDataBlock(feControl, "MIRRF feature vector data", this, 0);
		addOutputDataBlock(vectorDataBlock);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		super.newData(o, arg);
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL &&
				PamController.getInstance().getRunMode() != PamController.RUN_MIXEDMODE) {
			return;
		}
		processRequestList();
	}

	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		masterClockTime = timeMilliseconds;
	}

	/**
	 * Process the queue of clip request - these are passed straight back
	 * into the ClipBlockProcesses which started them since there is a 
	 * certain amount of bookkeeping which needs to be done at the
	 * individual block level.
	 * @author Doug Gillespie (modified by Holly LeBlond)
	 */
	protected void processRequestList() {
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL &&
				PamController.getInstance().getRunMode() != PamController.RUN_MIXEDMODE) {
			return;
		}
		if (clipRequestQueue.size() == 0) {
			return;
		}
		synchronized(clipRequestSynch) {
			ClipRequest clipRequest;
			ListIterator<ClipRequest> li = clipRequestQueue.listIterator();
			int clipErr;
			while (li.hasNext()) {
				clipRequest = li.next();
				clipErr = clipRequest.clipBlockProcess.processClipRequest(clipRequest);
				if (clipErr == RawDataUnavailableException.DATA_ALREADY_DISCARDED ||
					clipErr == RawDataUnavailableException.INVALID_CHANNEL_LIST) {
					feControl.addOneToCounter(FEPanel.FAILURE, String.valueOf(clipRequest.getUID()));
					li.remove();
				} else if (clipErr == RawDataUnavailableException.DATA_NOT_ARRIVED) {
					continue; // TODO TAYLOR - Find out if doing this doesn't break the CSV stuff.
				} else if (clipErr == 4) {
					feControl.addOneToCounter(FEPanel.IGNORE, String.valueOf(clipRequest.getUID()));
					li.remove();
				} else if (clipErr == 0) {
					feControl.addOneToCounter(FEPanel.PENDING, String.valueOf(clipRequest.getUID()));
					li.remove();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		// TODO TAYLOR - There's probably a better way of communicating this.
		if (feControl.getParams().audioSourceProcessName.length() > 0)
			feControl.getParams().sr = 
				(int) PamController.getInstance().getRawDataBlock(feControl.getParams().audioSourceProcessName).getSampleRate();
		vectorDataBlock.setSampleRate((float) feControl.getParams().sr, false);
		
		subscribeDataBlocks();
	}

	@Override
	public void pamStart() {
		//super.pamStart();
		clipRequestQueue.clear(); // just in case anything hanging around from previously. 
		// if there is it may crash since the ClipblockProcess will probably have been replaced anyway.
		
		vectorDataBlock.setFinished(false);
		
		prevDU = null;
		clusterCountID = 0;
		
		if (feControl.getParams().inputFromCSV) {
			fillClipRequestQueueViaCSV();
		}
		
		rdbct = new RawDataBlockCheckerThread();
		rdbct.start();
	}
	
	@Override
	public void pamStop() {
		System.out.println("Went through pamStop().");
		FEPythonThreadManager threadManager = feControl.getThreadManager();
		int waitNum = 0;
		while (threadManager.getWaitlistSize() > 0 || threadManager.clipsLeft() > 0 || threadManager.vectorsLeft() > 0) {
			if (!(threadManager.getWaitlistSize() > 0 || threadManager.clipsLeft() > 0)) {
				threadManager.resetActiveThread();
				if (threadManager.vectorsLeft() > 0) {
					waitNum++;
				}
			}
		/*	if (waitNum >= 5) {
				threadManager.addVectorToDataBlock(); // Makes sure the occasional straggler gets dealt with.
			} */
			//threadManager.addVectorToDataBlock();
			System.out.println("waitlistSize: "+String.valueOf(threadManager.getWaitlistSize())+
					", clipsLeft: "+String.valueOf(threadManager.clipsLeft())+
					", vectorsLeft: "+String.valueOf(threadManager.vectorsLeft()));
			try {
				TimeUnit.MILLISECONDS.sleep(200);
				//TimeUnit.MILLISECONDS.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		vectorDataBlock.setFinished(true);
		//feControl.getThreadManager().pythonCommand("currThread.runLast()");
		File tempFolder = new File(feControl.getParams().tempFolder);
		// TODO TAYLOR - Create a global list instead, in order for it to not delete everything if there's a second instance of PAMGuard running.
		File[] filesToDelete = tempFolder.listFiles();
		//System.out.println("filesToDelete.length: "+String.valueOf(filesToDelete.length));
		for (int i = 0; i < filesToDelete.length; i++) {
			if (filesToDelete[i].getName().substring(filesToDelete[i].getName().length()-4).equals(".wav")) {
				filesToDelete[i].delete();
			}
		}
	}
	
	/**
	 * Sets up a queue for processing clips when using the CSV input option.
	 * Adds entries to queue if they occur between the start time of the current
	 * audio file and the expected end of the file as specified in the settings.
	 * @author Holly LeBlond
	 */
	protected void fillClipRequestQueueViaCSV() {
		AcquisitionControl ac = null;
		for (int i = 0; i < feControl.getPamController().getNumControlledUnits(); i++) {
			if (feControl.getPamController().getControlledUnit(i).getUnitType().equals("Data Acquisition")) {
				ac = (AcquisitionControl) feControl.getPamController().getControlledUnit(i);
				break;
			}
		}
		if (ac == null) {
			System.out.println("WARNING: No Sound Acquisition process could be found. CSV data cannot be processed.");
			return;
		}
		
		csvClipList = new ArrayList<ClipRequest>();
		
		long start = ac.getDaqProcess().getDaqStatusDataBlock().getLastUnit().getTimeMilliseconds();
		long end = start + 1000*60*feControl.getParams().inputCSVExpectedFileSize;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		ArrayList<String[]> csvEntries = feControl.getParams().inputCSVEntries;
		for (int i = 0; i < csvEntries.size(); i++) {
			String[] curr = csvEntries.get(i);
			long currTime;
			try {
				currTime = df.parse(curr[1]).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}
			if (!(start <= currTime && currTime < end)) continue;
			try {
				FESliceDataUnit newDU = new FESliceDataUnit();
				newDU.setUID(Long.valueOf(curr[0]));
				newDU.setTimeMilliseconds(currTime);
				newDU.setFrequency(new double[] {Double.valueOf(curr[2]), Double.valueOf(curr[3])});
				newDU.setDurationInMilliseconds(Double.valueOf(curr[4]));
				newDU.setMeasuredAmplitude(Double.valueOf(curr[5]));
				newDU.setStartSample((long) (feControl.getParams().sr*(currTime - start))/1000);
				newDU.setSampleDuration((long) (feControl.getParams().sr*(newDU.getDurationInMilliseconds()))/1000);
				newDU.setSliceData(curr);
				csvClipList.add(new ClipRequest(clipBlockProcesses[0], newDU));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(csvClipList, Comparator.comparing(ClipRequest::getEndTimeInDataUnit));
	}
	
	/**
	 * Called at end of setup of after settings dialog to subscribe data blocks. 
	 * @author Doug Gillespie (modified by Holly LeBlond)
	 */
	public synchronized void subscribeDataBlocks() {
		unSubscribeDataBlocks();
		rawDataBlock = PamController.getInstance().getRawDataBlock(feControl.getParams().audioSourceProcessName);
		setParentDataBlock(rawDataBlock, true);
		
		int nBlocks = 1;
		clipBlockProcesses = new ClipBlockProcess[nBlocks];
		PamDataBlock aDataBlock;
		if (feControl.getParams().inputFromCSV) {
			aDataBlock = new PamDataBlock<FESliceDataUnit>(FESliceDataUnit.class, processName, this, 0);
			aDataBlock.setSampleRate(feControl.getParams().sr, false);
		} else {
			aDataBlock = PamController.getInstance().getDetectorDataBlock(feControl.getParams().inputProcessName);
		}
		clipBlockProcesses[0] = new ClipBlockProcess(this, aDataBlock);
	}
	
	/**
	 * Kill off the old ClipBlockProcesses before creating new ones.
	 * @author Doug Gillespie
	 */
	protected void unSubscribeDataBlocks() {
		if (clipBlockProcesses == null) {
			return;
		}
		for (int i = 0; i < clipBlockProcesses.length; i++) {
			if (clipBlockProcesses[i] == null) {
				continue;
			}
			clipBlockProcesses[i].disconnect();
		}
	}
	
	/**
	 * @author Doug Gillespie (modified by Holly LeBlond)
	 */
	public class ClipBlockProcess extends PamObserverAdapter {
		
		private PamDataBlock dataBlock;
		protected FEProcess clipProcess;
		private WavFileWriter wavFile;
		
		/**
		 * @param dataBlock
		 * @param clipGenSetting
		 */
		public ClipBlockProcess(FEProcess clipProcess, PamDataBlock dataBlock) {
			super();
			this.clipProcess = clipProcess;
			this.dataBlock = dataBlock;
			dataBlock.addObserver(this, true);
			
			ConnectedRegionDataBlock crdb = (ConnectedRegionDataBlock) dataBlock;
			ConnectedRegionDataUnit crdu = (ConnectedRegionDataUnit) crdb.getLastUnit();
			// TODO SLICE DATA !!!!!!!!!!!!!!!!
		}
		
		/**
		 * Process a clip request, i.e. make an actual clip from the raw data. This is called back 
		 * from the main thread receiving raw audio data and is called only after any decisions regarding
		 * whether or not a clip should be made have been taken - to get on and make the clip in the
		 * output folder. 
		 * @param clipRequest clip request information
		 * @return 0 if OK or the cause from RawDataUnavailableException if data are not available. 
		 * @author Doug Gillespie (modified by Holly LeBlond)
		 */
		protected int processClipRequest(ClipRequest clipRequest) { 
			PamDataUnit dataUnit = (PamDataUnit) clipRequest.dataUnit;
			long rawStart = dataUnit.getStartSample();
			long rawEnd = rawStart + dataUnit.getSampleDuration();
			int channelMap = decideChannelMap(dataUnit.getChannelBitmap());
			
			FEParameters params = feControl.getParams();
			
			if ((params.miscIgnoreFileStartChecked && 1000*dataUnit.getStartSample()/dataBlock.getSampleRate() < params.miscIgnoreFileStartLength) ||
				(params.miscIgnoreLowFreqChecked && dataUnit.getFrequency()[0] < params.miscIgnoreLowFreq) ||
				(params.miscIgnoreHighFreqChecked && dataUnit.getFrequency()[1] > params.miscIgnoreHighFreq) ||
				(params.miscIgnoreShortDurChecked && dataUnit.getDurationInMilliseconds() < params.miscIgnoreShortDur) ||
				(params.miscIgnoreLongDurChecked && dataUnit.getDurationInMilliseconds() > params.miscIgnoreLongDur) ||
				(params.miscIgnoreQuietAmpChecked && dataUnit.getAmplitudeDB() < params.miscIgnoreQuietAmp) ||
				(params.miscIgnoreLoudAmpChecked && dataUnit.getAmplitudeDB() > params.miscIgnoreLoudAmp)) {
					return 4;
			}
			
			boolean countUp = false;
			if (prevDU == null || !params.miscClusterChecked || (params.audioNRChecked && nrData == null)) countUp = true;
			else if (prevDU.getEndTimeInMilliseconds() + params.miscJoinDistance < dataUnit.getTimeMilliseconds()) countUp = true;
			// TODO TAYLOR - binaryFileBreak stuff (may actually not be necessary)
			
			
			if (params.audioNRChecked && countUp) {
				try {
					nrData = rawDataBlock.getSamples(rawStart-params.audioNRStart, params.audioNRLength, channelMap);
				} catch (RawDataUnavailableException e) {
					System.out.println("Start sample in block: "+String.valueOf(rawDataBlock.getLastUnit().getStartSample()));
					System.out.println("Start sample of NR clip: "+String.valueOf(rawStart-params.audioNRStart));
					e.printStackTrace(); // TODO Remove?
					return e.getDataCause();
				}
				
				if (nrData == null) {
					System.out.println("Null NR clip");
					return RawDataUnavailableException.DATA_ALREADY_DISCARDED;
				}
			}
			
			
			double[][] rawData = null;
			try {
				if (params.audioAutoClipLength) rawData = rawDataBlock.getSamples(rawStart, (int) (rawEnd-rawStart), channelMap);
				else rawData = rawDataBlock.getSamples(rawStart, (int) (rawStart + params.audioClipLength), channelMap);
			}
			catch (RawDataUnavailableException e) {
				System.out.println("Start sample in block: "+String.valueOf(rawDataBlock.getLastUnit().getStartSample()));
				System.out.println("Start sample of raw data: "+String.valueOf(rawStart));
				e.printStackTrace(); // TODO Remove?
				return e.getDataCause();
			}
			if (rawData == null) {
				System.out.println("Null raw data");
				return RawDataUnavailableException.DATA_ALREADY_DISCARDED; // if rawDataBlock.getSamples returns null, assume that the data is already gone and return an error
			}
			
			String nrPath = "";
			if (params.audioNRChecked && countUp) {
				nrPath = createClipPath(dataUnit.getUID(), clusterCountID+1, true);
				AudioFormat af = new Wav16AudioFormat(getSampleRate(), nrData.length);
				wavFile = new WavFileWriter(nrPath, af);
				boolean nrWrote = wavFile.write(nrData);
				wavFile.close();
				if (!nrWrote) {
					System.out.println("Could not write file: "+nrPath);
					return RawDataUnavailableException.DATA_ALREADY_DISCARDED;
				}
			}
			
			String clipPath;
			if (countUp) clipPath = createClipPath(dataUnit.getUID(), clusterCountID+1, false);
			else clipPath = createClipPath(dataUnit.getUID(), clusterCountID, false);
			AudioFormat af = new Wav16AudioFormat(getSampleRate(), rawData.length);
			wavFile = new WavFileWriter(clipPath, af);
			boolean clipWrote = wavFile.write(rawData);
			wavFile.close();
			if (!clipWrote) {
				if (params.audioNRChecked && countUp) {
					File f = new File(nrPath);
					f.delete();
				}
				System.out.println("Could not write file: "+nrPath);
				return RawDataUnavailableException.DATA_ALREADY_DISCARDED;
			}
			
			prevDU = dataUnit;
			if (countUp) clusterCountID++;
			
			long[] sliceStartSamples;
			double[] sliceFreqs;
			if (params.inputFromCSV) {
				FESliceDataUnit sdu = (FESliceDataUnit) dataUnit;
				sliceStartSamples = sdu.sliceStartSamples;
				sliceFreqs = sdu.sliceFreqs;
			} else {
				ConnectedRegionDataUnit crdu = (ConnectedRegionDataUnit) dataUnit;
				long contourStartSample = crdu.getStartSample();
				List<SliceData> sdList = crdu.getConnectedRegion().getSliceData();
				Collections.sort(sdList, Comparator.comparingLong(SliceData::getStartSample));
				sliceStartSamples = new long[sdList.size()];
				sliceFreqs = new double[sdList.size()];
				for (int i = 0; i < sdList.size(); i++) {
					FFTDataBlock fftDB = (FFTDataBlock) crdu.getFFTDataUnits(0).get(0).getParentDataBlock();
					sliceStartSamples[i] = sdList.get(i).getStartSample();
					// For unknown reasons, the slice's start sample isn't always set.
					if (sliceStartSamples[i] == 0) sliceStartSamples[i] = i * fftDB.getFftHop() + contourStartSample;
					sliceFreqs[i] = fftDB.getSampleRate() * sdList.get(i).getPeakInfo()[0][2] / fftDB.getFftLength();
				}
			}
			
			String clusterID = createClusterID(dataUnit.getUID(), clusterCountID, false);
			//String[] extras = new String[8];
			String[] extras = new String[7];
			extras[0] = "uid="+String.valueOf(dataUnit.getUID());
			extras[1] = "datelong="+String.valueOf(dataUnit.getTimeMilliseconds());
			extras[2] = "amplitude="+String.valueOf(dataUnit.getAmplitudeDB());
			extras[3] = "duration="+String.valueOf(dataUnit.getDurationInMilliseconds());
			extras[4] = "freqhd_min="+String.valueOf(dataUnit.getFrequency()[0]);
			extras[5] = "freqhd_max="+String.valueOf(dataUnit.getFrequency()[1]);
			//extras[6] = "frange="+String.valueOf(dataUnit.getFrequency()[1]-dataUnit.getFrequency()[0]);
			//extras[7] = "fslopehd="+String.valueOf((dataUnit.getFrequency()[1]-dataUnit.getFrequency()[0])
			//		/ (dataUnit.getDurationInMilliseconds()/1000));
			extras[6] = "slice_data=[("+String.valueOf(sliceStartSamples[0])+","+String.valueOf(sliceFreqs[0]);
			for (int i = 1; i < sliceFreqs.length; i++)
				extras[6] += "),("+String.valueOf(sliceStartSamples[i])+","+String.valueOf(sliceFreqs[i]);
			extras[6] += ")]";
			feControl.getThreadManager().sendContourClipToThread(clusterID, String.valueOf(dataUnit.getUID()), nrPath, clipPath, extras);
			
			return 0; // no error. 
		}

		/**
		 * @return 1
		 */
		protected int decideChannelMap(int channelBitmap) {
			return 1;
		}

		/**
		 * disconnect from it's data source.
		 * @author Doug Gillespie
		 */
		public void disconnect() {
			dataBlock.deleteObserver(this);
		}
		
		@Override
		public String getObserverName() {
			return clipProcess.getObserverName();
		}
		
		@Override
		public PamObserver getObserverObject() {
			return clipProcess.getObserverObject();
		}
		
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			/**
			 * This one should get updates from the triggering data block. 
			 */
			addClipRequest(new ClipRequest(this, dataUnit));
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {}
	}
	
	/**
	 * Data needed for a clip request. 
	 * @author Doug Gillespie (modified by Holly LeBlond)
	 */
	public class ClipRequest {
		
		protected ClipBlockProcess clipBlockProcess;
		protected PamDataUnit dataUnit;
		
		public ClipRequest(ClipBlockProcess clipBlockProcess,
				PamDataUnit dataUnit) {
			super();
			this.clipBlockProcess = clipBlockProcess;
			this.dataUnit = dataUnit;
		}
		
		/**
		 * @return The UID in the data unit
		 * @author Holly LeBlond
		 */
		public long getUID() {
			return dataUnit.getUID();
		}
		
		/**
		 * @return The start time in the data unit
		 * @author Holly LeBlond
		 */
		public long getTimeInDataUnit() {
			return dataUnit.getTimeMilliseconds();
		}
		
		/**
		 * @return The end time in the data unit
		 * @author Holly LeBlond
		 */
		public long getEndTimeInDataUnit() {
			return dataUnit.getEndTimeInMilliseconds();
		}
	}
	
	/**
	 * Used for signaling the thread manager that the audio has loaded past the current cluster plus the join distance.
	 * Also used for putting CSV-loaded input data into the clipRequestQueue when the audio has loaded far enough.
	 * @author Holly LeBlond
	 */
	public class RawDataBlockCheckerThread extends Thread {
		protected RawDataBlockCheckerThread() {}
		
		@Override
		public void run() {
			while (!vectorDataBlock.isFinished()) {
				try {
					TimeUnit.MILLISECONDS.sleep(20);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (rawDataBlock.getLastUnit() == null) continue;
				
				long currTime = rawDataBlock.getLastUnit().getEndTimeInMilliseconds();
				
				if (feControl.getParams().inputFromCSV) {
					while (csvClipList.size() > 0) {
						// Note that csvClipList should already be sorted by end time.
						ClipRequest cr = csvClipList.get(0);
						if (cr.getEndTimeInDataUnit() <= currTime) {
							addClipRequest(cr);
							csvClipList.remove(0);
						} else break;
					}
				}
				
				FEPythonThreadManager threadManager = feControl.getThreadManager();
				if (threadManager.getRDBCTSignal()) continue;
				if (feControl.getParams().miscClusterChecked) {
					if (prevDU == null) continue;
					if (prevDU.getTimeMilliseconds() + prevDU.getDurationInMilliseconds().longValue() + feControl.getParams().miscJoinDistance
							> currTime) continue;
				}
				threadManager.setRDBCTSignal(true);
			}
		}
	}
	
	/**
	 * Get the output folder, based on time and sub folder options.
	 */
	public String getClipFileFolder() {
		return feControl.getParams().tempFolder; // TODO TAYLOR - MAKE SURE THIS IS CORRECT!!!
	}
	
	/**
	 * Creates a cluster ID String based off of the input UID and cluster count number.
	 * @param uid
	 * @param clusterCount
	 * @param underscore - Whether or not it uses a dash or an underscore.
	 * @author Holly LeBlond
	 */
	public String createClusterID(long uid, int clusterCount, boolean underscore) {
		if (underscore) {
			return String.format("%05d", (uid - (uid % 1000000))/1000000)+"_"+String.format("%05d", clusterCount);
		}
		return String.format("%05d", (uid - (uid % 1000000))/1000000)+"-"+String.format("%05d", clusterCount);
	}
	
	/**
	 * Produces the path for a new sound clip with a cluster ID.
	 * @param uid
	 * @param clusterCount
	 * @param isNR - If the file is supposed to be used for noise removal (true) or if it's the actual contour clip (false).
	 * @author Holly LeBlond
	 */
	public String createClipPath(long uid, int clusterCount, boolean isNR) {
		String fp = getClipFileFolder();
		//if (fp.length() > 0 && !fp.substring(fp.length()-1, fp.length()).equals("/")) fp += "/";
		String cID = createClusterID(uid, clusterCount, true);
		if (isNR) return fp+"NR_"+cID+".wav";
		return fp+"FE_"+cID+"_"+String.valueOf(uid)+".wav";
	}

	/**
	 * @author Doug Gillespie
	 */
	@Override
	public boolean flushDataBlockBuffers(long maxWait) {
		boolean ans = super.flushDataBlockBuffers(maxWait);
		processRequestList(); // one last go at processing the clip request list before stopping.
		return ans;
	}
	
	// TODO TAYLOR - Figure out what this does.
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		long minH = 0;
		if (clipBlockProcesses == null || clipBlockProcesses.length == 0) {
			return 0;
		}
		for (int i = 0; i < clipBlockProcesses.length; i++) {
			if (clipBlockProcesses[i] == null) {
				continue;
			}
			minH = Math.max(minH, clipBlockProcesses[i].getRequiredDataHistory(o, arg));
		}
		minH += Math.max(3000, 192000/(long)getSampleRate());
		if (specMouseDown) {
			minH = Math.max(minH, masterClockTime-specMouseDowntime);
		}
		return minH;
	}
	
	/**
	 * @author Doug Gillespie
	 */
	protected void addClipRequest(ClipRequest clipRequest) {
		synchronized (clipRequestSynch) {
			clipRequestQueue.add(clipRequest);
		}
	}
	
	/**
	 * @return The output data block.
	 * @author Holly LeBlond
	 */
	public FEDataBlock getVectorDataBlock() {
		return vectorDataBlock;
	}
	
	/**
	 * Adds a data unit containing vector data to the output data block.
	 * @author Holly LeBlond
	 */
	public void addVectorData(FEDataUnit du) {
		vectorDataBlock.addPamData(du);
	}
}