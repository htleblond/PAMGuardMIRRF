package mirrfFeatureExtractor;

import java.awt.event.MouseEvent;
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

import Acquisition.AcquisitionControl;
import warnings.PamWarning;
import clipgenerator.ClipDataUnit;
import clipgenerator.localisation.ClipDelays;
import dataPlotsFX.layout.TDGraphFX;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkProcess;
import annotation.handler.ManualAnnotationHandler;

/**
 * Process for making short clips of audio data. 
 * <br> separate subscriber processes for each triggering data block, but these all send clip requests
 * back into the main observer of the actual raw data - so that all clips are made from the 
 * same central thread. 
 * <br> Let the request queue trigger off the main clock signal. 
 *  
 * @author Doug Gillespie
 *
 */
public class FEProcess extends SpectrogramMarkProcess {

	//private ClipControl clipControl;
	private PamDataBlock[] dataSources;
	private ClipBlockProcess[] clipBlockProcesses;
	private List<ClipRequest> clipRequestQueue;
	private Object clipRequestSynch = new Object();
	private PamRawDataBlock rawDataBlock;
	//private ClipDataBlock clipDataBlock;
	private long specMouseDowntime;
	private boolean specMouseDown;
	private long masterClockTime;
	private ClipDelays clipDelays;
	//private BuoyLocaliserManager buoyLocaliserManager;
	//private ClipSpectrogramMarkDataBlock clipSpectrogramMarkDataBlock;
	private ManualAnnotationHandler manualAnnotaionHandler;
	private static PamWarning warningMessage = new PamWarning("Clip Generator", "", 2);
	
	protected FEControl feControl;
	protected double[][] nrData;
	protected PamDataUnit prevDU;
	protected int clusterCountID;
	protected RawDataBlockCheckerThread rdbct;
	protected ArrayList<ClipRequest> csvClipList;
	
	protected FEDataBlock vectorDataBlock;

	public FEProcess(FEControl feControl) {
		super(feControl);
		this.feControl = feControl;
		//this.clipControl = clipControl;
		clipRequestQueue = new LinkedList<ClipRequest>();
		//clipSpectrogramMarkDataBlock = new ClipSpectrogramMarkDataBlock(this, 0);
		//clipDataBlock = new ClipDataBlock(clipControl.getUnitName() + " Clips", this, 0);
		//clipDataBlock.setBinaryDataSource(new ClipBinaryDataSource(clipControl, clipDataBlock));
		//ClipOverlayGraphics cog = new ClipOverlayGraphics(clipControl, clipDataBlock);
		//clipDataBlock.setOverlayDraw(cog);
		//StandardSymbolManager symbolManager = new ClipSymbolManager(clipDataBlock, ClipOverlayGraphics.defSymbol, true);
		//symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		//clipDataBlock.setPamSymbolManager(symbolManager);
		//addOutputDataBlock(clipDataBlock);
		//clipDelays = new ClipDelays(clipControl);
		//buoyLocaliserManager = new BuoyLocaliserManager();
		//manualAnnotaionHandler = new ManualAnnotationHandler(clipControl, clipDataBlock);
		//clipDataBlock.setAnnotationHandler(manualAnnotaionHandler);
		
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
				
			/*	switch (clipErr) {
				case 0: // no error - clip should have been created. 
				case RawDataUnavailableException.DATA_ALREADY_DISCARDED:
				case RawDataUnavailableException.INVALID_CHANNEL_LIST:
					//					System.out.println("Clip error : " + clipErr);
					li.remove();
				case RawDataUnavailableException.DATA_NOT_ARRIVED:
					continue; // hopefully, will get this next time !
				} */
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		/*
		 * Work out which hydrophones are in use and create an appropriate bearing
		 * localiser. 
		 */
		
		// TODO TAYLOR - There's probably a better way of communicating this.
		if (feControl.getParams().audioSourceProcessName.length() > 0)
			feControl.getParams().sr = 
				(int) PamController.getInstance().getRawDataBlock(feControl.getParams().audioSourceProcessName).getSampleRate();
		vectorDataBlock.setSampleRate((float) feControl.getParams().sr, false);
		
		subscribeDataBlocks();
	}

	@Override
	public void pamStart() {
		super.pamStart();
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
		//feControl.getThreadManager().pamStopped = true;
		//checkerThreadOn = false;
		//csvThreadActive = false;
		//if (feParams.inputCSVEntries.size() > 0) {
		//	feControl.getParams().inputCSVIndexes = new ArrayList<Integer>(csvIndexesCopy);
		//	feControl.getSidePanel().getFEPanel().getReloadCSVButton().setEnabled(true);
		//}
		//while (feControl.getThreadManager().pythonResultsWaitQueue > 0) {
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
				FEDummyDataUnit newDU = new FEDummyDataUnit();
				newDU.setUID(Long.valueOf(curr[0]));
				newDU.setTimeMilliseconds(currTime);
				newDU.setFrequency(new double[] {Double.valueOf(curr[2]), Double.valueOf(curr[3])});
				newDU.setDurationInMilliseconds(Double.valueOf(curr[4]));
				newDU.setMeasuredAmplitude(Double.valueOf(curr[5]));
				newDU.setStartSample((long) (feControl.getParams().sr*(currTime - start))/1000);
				newDU.setSampleDuration((long) (feControl.getParams().sr*(newDU.getDurationInMilliseconds()))/1000);
				csvClipList.add(new ClipRequest(clipBlockProcesses[0], newDU));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(csvClipList, Comparator.comparing(ClipRequest::getEndTimeInDataUnit));
	}
	
	/**
	 * Called at end of setup of after settings dialog to subscribe data blocks. 
	 */
	public synchronized void subscribeDataBlocks() {
		unSubscribeDataBlocks();
		//rawDataBlock = PamController.getInstance().getRawDataBlock(clipControl.clipSettings.dataSourceName);
		rawDataBlock = PamController.getInstance().getRawDataBlock(feControl.getParams().audioSourceProcessName);
		setParentDataBlock(rawDataBlock, true);
		
		//int nBlocks = clipControl.clipSettings.getNumClipGenerators();
		int nBlocks = 1;
		clipBlockProcesses = new ClipBlockProcess[nBlocks];
		PamDataBlock aDataBlock;
		//ClipGenSetting clipGenSetting;
	/*	for (int i = 0; i < nBlocks; i++) {
			
			clipGenSetting = clipControl.clipSettings.getClipGenSetting(i);

			if (clipGenSetting.enable == false) {
				continue;
			}
			if (i == 0) {
				aDataBlock = this.clipSpectrogramMarkDataBlock;
			}
			else {
				aDataBlock = PamController.getInstance().getDetectorDataBlock(clipGenSetting.dataName); 

			}
			if (aDataBlock == null) {
				continue;
			}
			clipBlockProcesses[i] = new ClipBlockProcess(this, aDataBlock, clipGenSetting);
		} */
		if (feControl.getParams().inputFromCSV) {
			aDataBlock = new PamDataBlock<FEDummyDataUnit>(FEDummyDataUnit.class, processName, this, 0);
			aDataBlock.setSampleRate(feControl.getParams().sr, false);
		} else {
			aDataBlock = PamController.getInstance().getDetectorDataBlock(feControl.getParams().inputProcessName);
		}
		clipBlockProcesses[0] = new ClipBlockProcess(this, aDataBlock);
	}
	
	/**
	 * Kill off the old ClipBlockProcesses before creating new ones. 
	 */
	private void unSubscribeDataBlocks() {
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
	
	public class ClipBlockProcess extends PamObserverAdapter {
		
		private PamDataBlock dataBlock;
		//protected ClipGenSetting clipGenSetting;
		protected FEProcess clipProcess;
		//private ClipDataUnit lastClipDataUnit;
		private WavFileWriter wavFile;
		//private StandardClipBudgetMaker clipBudgetMaker;
		//private BearingLocaliser bearingLocaliser;
		//private int hydrophoneMap;
		
		/**
		 * @param dataBlock
		 * @param clipGenSetting
		 */
		//public ClipBlockProcess(FEProcessNew clipProcess, PamDataBlock dataBlock,
		//		ClipGenSetting clipGenSetting) {
		public ClipBlockProcess(FEProcess clipProcess, PamDataBlock dataBlock) {
			super();
			this.clipProcess = clipProcess;
			this.dataBlock = dataBlock;
			//this.clipGenSetting = clipGenSetting;
			//clipBudgetMaker = new StandardClipBudgetMaker(this);
			dataBlock.addObserver(this, true);
			

		/*	if (rawDataBlock != null) {
				int chanMap = decideChannelMap(rawDataBlock.getChannelMap());
				hydrophoneMap = rawDataBlock.getChannelListManager().channelIndexesToPhones(chanMap);
				double timingError = Correlations.defaultTimingError(getSampleRate());
				bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(hydrophoneMap, timingError); 
			} */
		}
		
		/**
		 * Process a clip request, i.e. make an actual clip from the raw data. This is called back 
		 * from the main thread receiving raw audio data and is called only after any decisions regarding
		 * whether or not a clip should be made have been taken - to get on and make the clip in the
		 * output folder. 
		 * @param clipRequest clip request information
		 * @return 0 if OK or the cause from RawDataUnavailableException if data are not available. 
		 */
		private int processClipRequest(ClipRequest clipRequest) {
//			System.out.println("Process clip request:? " +clipRequest.dataUnit); 
			PamDataUnit dataUnit = (PamDataUnit) clipRequest.dataUnit;
			long rawStart = dataUnit.getStartSample();
			long rawEnd = rawStart + dataUnit.getSampleDuration();
//			rawStart -= (clipGenSetting.preSeconds * getSampleRate());
			//rawStart = (long) Math.max(rawStart-clipGenSetting.preSeconds * getSampleRate(),0); // prevent negative numbers, just start at the beginning if detection is near start of file
			//rawEnd += (clipGenSetting.postSeconds * getSampleRate());
			int channelMap = decideChannelMap(dataUnit.getChannelBitmap());
			
			//boolean append = false;
//			if (lastClipDataUnit != null) {
//				if (rawStart < (lastClipDataUnit.getStartSample()+lastClipDataUnit.getSampleDuration()) &&
//						channelMap == lastClipDataUnit.getChannelBitmap()) {
//					append = true;
//					rawStart = lastClipDataUnit.getStartSample()+lastClipDataUnit.getSampleDuration();
//					if (rawEnd < rawStart) {
//						return 0; // nothing to do !
//					}
//				}
//			}
			
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
			
			String clusterID = createClusterID(dataUnit.getUID(), clusterCountID, false);
			String[] extras = new String[8];
			extras[0] = "uid="+String.valueOf(dataUnit.getUID());
			extras[1] = "datelong="+String.valueOf(dataUnit.getTimeMilliseconds());
			extras[2] = "amplitude="+String.valueOf(dataUnit.getAmplitudeDB());
			extras[3] = "duration="+String.valueOf(dataUnit.getDurationInMilliseconds());
			extras[4] = "freqhd_min="+String.valueOf(dataUnit.getFrequency()[0]);
			extras[5] = "freqhd_max="+String.valueOf(dataUnit.getFrequency()[1]);
			extras[6] = "frange="+String.valueOf(dataUnit.getFrequency()[1]-dataUnit.getFrequency()[0]);
			extras[7] = "fslopehd="+String.valueOf((dataUnit.getFrequency()[1]-dataUnit.getFrequency()[0])
					/ (dataUnit.getDurationInMilliseconds()/1000));
			feControl.getThreadManager().sendContourClipToThread(clusterID, String.valueOf(dataUnit.getUID()), nrPath, clipPath, extras);
			
		/*	if (append && clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
				wavFile.append(rawData);
				lastClipDataUnit.setSampleDuration(rawEnd-lastClipDataUnit.getStartSample());
				clipDataBlock.updatePamData(lastClipDataUnit, dataUnit.getTimeMilliseconds());
//				System.out.println(String.format("%d samples added to file", rawData[0].length));
			} */
		/*	else {
				ClipDataUnit clipDataUnit = null;
				//long startMillis = dataUnit.getTimeMilliseconds() - (long) (clipGenSetting.preSeconds*1000.);
				long startMillis = dataUnit.getTimeMilliseconds();
				String fileName = "";
				
				if ((clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) 
						|| (clipControl.clipSettings.storageOption == ClipSettings.STORE_BOTH)) {
					String folderName = getClipFileFolder(dataUnit.getTimeMilliseconds(), true);
					fileName = getClipFileName(startMillis);
					AudioFormat af = new Wav16AudioFormat(getSampleRate(), rawData.length);
					wavFile = new WavFileWriter(folderName+fileName, af);
					wavFile.write(rawData);
					wavFile.close();
					// make a data unit to go with it. 
					clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
							(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
				}
				if ((clipControl.clipSettings.storageOption == ClipSettings.STORE_BINARY) 
						|| (clipControl.clipSettings.storageOption == ClipSettings.STORE_BOTH)) {
					clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
							(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
				}
				clipDataUnit.setTriggerDataUnit(dataUnit);
				clipDataUnit.setFrequency(dataUnit.getFrequency());
				lastClipDataUnit = clipDataUnit;
				if (bearingLocaliser != null) {
					localiseClip(clipDataUnit, bearingLocaliser, hydrophoneMap);
				}				
				clipDataBlock.addPamData(clipDataUnit);
				
				
			} */
			
			return 0; // no error. 
		}

	/*	private String getClipFileName(long timeStamp) {
			return PamCalendar.createFileNameMillis(timeStamp, clipGenSetting.clipPrefix, ".wav");
		} */
	

		/**
		 * Decide which channels should actually be used. 
		 * @param channelBitmap
		 * @return
		 */
		protected int decideChannelMap(int channelBitmap) {
		/*	switch (clipGenSetting.channelSelection) {
			case ClipGenSetting.ALL_CHANNELS:
				return rawDataBlock.getChannelMap();
			case ClipGenSetting.DETECTION_CHANNELS_ONLY:
				return channelBitmap;
			case ClipGenSetting.FIRST_DETECTION_CHANNEL_ONLY:
				int overlap = channelBitmap & rawDataBlock.getChannelMap();
				int first = PamUtils.getLowestChannel(overlap);
				return 1<<first;
			} */
			//return 0;
			return 1;
		}

		/**
		 * disconnect from it's data source. 
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
		
	/*	@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) ((clipGenSetting.preSeconds+clipGenSetting.postSeconds) * 1000.);
		} */
		
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			/**
			 * This one should get updates from the triggering data block. 
			 */
//			System.out.printf("Clip request: " + dataUnit.toString());
		/*	if (shouldMakeClip((PamDataUnit) dataUnit)) {
//				System.out.printf(": Clip requested\n");
				addClipRequest(new ClipRequest(this, dataUnit));
			} */
//			else {
//				System.out.printf(": Clip request refused\n");
//			}
			addClipRequest(new ClipRequest(this, dataUnit));
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub
		}

		/**
		 * Function to decide whether or not a clip should be made. 
		 * Might be set to all clips, might be working to a budget. 
		 * Will ultimately be calling into quite a long winded decision
		 * making process. 
		 * @param arg
		 * @return true if a clip should be made, false otherwsie. 
		 */
		private boolean shouldMakeClip(PamDataUnit dataUnit) {
			//return clipBudgetMaker.shouldStore(dataUnit);
			return true;
		}
		
	}
	
	/**
	 * Data needed for a clip request. 
	 * @author Doug Gillespie
	 *
	 */
	public class ClipRequest {
		
		protected ClipBlockProcess clipBlockProcess;
		protected PamDataUnit dataUnit;
		
		/**
		 * @param clipBlockProcess
		 * @param dataUnit
		 */
		public ClipRequest(ClipBlockProcess clipBlockProcess,
				PamDataUnit dataUnit) {
			super();
			this.clipBlockProcess = clipBlockProcess;
			this.dataUnit = dataUnit;
		}
		
		/**
		 * @return The UID in the data unit
		 * @author Taylor LeBlond
		 */
		public long getUID() {
			return dataUnit.getUID();
		}
		
		/**
		 * @return The start time in the data unit
		 * @author Taylor LeBlond
		 */
		public long getTimeInDataUnit() {
			return dataUnit.getTimeMilliseconds();
		}
		
		/**
		 * @return The end time in the data unit
		 * @author Taylor LeBlond
		 */
		public long getEndTimeInDataUnit() {
			return dataUnit.getEndTimeInMilliseconds();
		}
	}
	
	/**
	 * Used for signaling the thread manager that the audio has loaded past the current cluster plus the join distance.
	 * Also used for putting CSV-loaded input data into the clipRequestQueue when the audio has loaded far enough.
	 * @author Taylor LeBlond
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
	 * Find the wav file to go with a particular clip
	 * @param clipDataUnit data unit to find the file for. 
	 * @return file, or null if not found. 
	 */
	@Deprecated
	public File findClipFile(ClipDataUnit clipDataUnit) {
		//String path = getClipFileFolder(clipDataUnit.getTimeMilliseconds(), true);
		String path = getClipFileFolder();
		path += clipDataUnit.fileName;
		File aFile = new File(path);
		if (aFile.exists() == false) {
			return null;
		}
		return aFile;
	}
	
	/**
	 * Get the output folder, based on time and sub folder options. 
	 * @param timeStamp
	 * @param addSeparator
	 * @return
	 */
	public String getClipFileFolder() {
	/*	String fileSep = FileParts.getFileSeparator();
		if (clipControl.clipSettings.outputFolder == null) return null;
		String folderName = new String(clipControl.clipSettings.outputFolder);
		if (clipControl.clipSettings.datedSubFolders) {
			folderName += fileSep + PamCalendar.formatFileDate(timeStamp);

			// now check that that folder exists. 
			File folder = FileFunctions.createNonIndexedFolder(folderName);
			if (folder == null || folder.exists() == false) {
				return null;
			}
		}
		if (addSeparator) {
			folderName += fileSep;
		}
		return folderName; */
		return feControl.getParams().tempFolder; // TODO TAYLOR - MAKE SURE THIS IS CORRECT!!!
	}
	
	/**
	 * 
	 * @param uid
	 * @param clusterCount
	 * @param underscore
	 * @return
	 * @author Taylor LeBlond
	 */
	public String createClusterID(long uid, int clusterCount, boolean underscore) {
		if (underscore) {
			return String.format("%05d", (uid - (uid % 1000000))/1000000)+"_"+String.format("%05d", clusterCount);
		}
		return String.format("%05d", (uid - (uid % 1000000))/1000000)+"-"+String.format("%05d", clusterCount);
	}
	
	/**
	 * 
	 * @param uid
	 * @param clusterCount
	 * @param isNR
	 * @return
	 * @author Taylor LeBlond
	 */
	public String createClipPath(long uid, int clusterCount, boolean isNR) {
		String fp = getClipFileFolder();
		//if (fp.length() > 0 && !fp.substring(fp.length()-1, fp.length()).equals("/")) fp += "/";
		String cID = createClusterID(uid, clusterCount, true);
		if (isNR) return fp+"NR_"+cID+".wav";
		return fp+"FE_"+cID+"_"+String.valueOf(uid)+".wav";
	}

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

	protected void addClipRequest(ClipRequest clipRequest) {
		synchronized (clipRequestSynch) {
			clipRequestQueue.add(clipRequest);
		}
	}
	
	public FEDataBlock getVectorDataBlock() {
		return vectorDataBlock;
	}
	
	public void addVectorData(FEDataUnit du) {
		vectorDataBlock.addPamData(du);
	}
	
	@Override
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, int downUp, int channel, 
			long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay) {
		return false;
	}

	@Override
	public boolean canMark() {
		return false;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the manualAnnotaionHandler
	 */
	public ManualAnnotationHandler getManualAnnotaionHandler() {
		return manualAnnotaionHandler;
	}
}