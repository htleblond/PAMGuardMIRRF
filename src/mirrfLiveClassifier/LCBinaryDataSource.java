package mirrfLiveClassifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryInputStream;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import mirrfFeatureExtractor.FEDataBlock;
import whistlesAndMoans.SliceData;
import whistlesAndMoans.WhistleBinaryModuleHeader;

public class LCBinaryDataSource extends BinaryDataSource {
	
	private LCControl lcControl;
	private String streamName;
	
	private static final int currentVersion = 6;
	private static final int moduleID = 1901;
	
	private LCBinaryModuleHeader currMH;
	
	public LCBinaryDataSource(LCControl lcControl, LCDataBlock sisterDataBlock, String streamName) {
		super(sisterDataBlock);
		this.lcControl = lcControl;
		this.streamName = streamName;
	}
	
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	private DataOutputStream headerOutputStream;
	private ByteArrayOutputStream headerBytes;
	
	private String currentBinaryDataFolder = "";
	
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		//System.out.println("REACHED getPackedData");
		LCDataUnit du = (LCDataUnit) pamDataUnit;
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		
		//this.getBinaryStorageStream().writeModuleHeader(getModuleHeaderData());
		
		LCCallCluster cc = du.getCluster();
		try {
			dos.writeUTF(cc.clusterID);
			dos.writeInt(cc.getSize());
			for (int i = 0; i < cc.getSize(); i++) {
				dos.writeLong(cc.uids[i]);
				dos.writeLong(cc.datetimes[i]);
				dos.writeFloat((float) cc.durations[i]);
				dos.writeFloat((float) cc.lfs[i]);
				dos.writeFloat((float) cc.hfs[i]);
				for (int j = 0; j < cc.probaList[i].length; j++) {
					dos.writeFloat((float) cc.probaList[i][j]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BinaryObjectData pbo = new BinaryObjectData(moduleID, bos.toByteArray());
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	/*	System.out.println("Original: "+String.valueOf(pbo.getDataUnitBaseData().getTimeMilliseconds()));
		System.out.println("Changed:  "+String.valueOf(cc.getStartAndEnd()[0]));
		pbo.getDataUnitBaseData().setTimeMilliseconds(cc.getStartAndEnd()[0]);
		pbo.getDataUnitBaseData().setTimeNanoseconds(null);
		//pbo.getDataUnitBaseData().getChannelBitmap(null); // not actually optional
		//pbo.getDataUnitBaseData().setUID(null); // not actually optional
		pbo.getDataUnitBaseData().setStartSample(null);
		pbo.getDataUnitBaseData().setSampleDuration(null);
		pbo.getDataUnitBaseData().setFrequency(new double[] {cc.getFreqLimits()[0], cc.getFreqLimits()[1]});
		pbo.getDataUnitBaseData().setMillisecondDuration((double) (cc.getStartAndEnd()[1]-cc.getStartAndEnd()[0]));
		pbo.getDataUnitBaseData().setTimeDelaysSeconds(null); */
		
		//this.getBinaryStorageStream().storeData(moduleID, pbo.getDataUnitBaseData(), pbo);
		
		return pbo;
	}

	@Override
	public String getStreamName() {
		return streamName;
	}

	@Override
	public int getStreamVersion() {
		return 0;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		//System.out.println("REACHED getModuleHeaderData");
		if (headerOutputStream == null) {
			headerOutputStream = new DataOutputStream(headerBytes = new ByteArrayOutputStream());
		}
		headerBytes.reset();
		try {
			headerOutputStream.writeShort(lcControl.getParams().labelOrder.length);
			for (int i = 0; i < lcControl.getParams().labelOrder.length; i++) {
				headerOutputStream.writeUTF(lcControl.getParams().labelOrder[i]);
			}
			if (lcControl.isViewer()) {
				headerOutputStream.writeShort(currMH.features.length);
				for (int i = 0; i < currMH.features.length; i++) {
					headerOutputStream.writeUTF(currMH.features[i]);
				}
			} else {
				FEDataBlock vectorBlock = (FEDataBlock) lcControl.getProcess().getParentDataBlock();
				headerOutputStream.writeShort(vectorBlock.getFeatureNames().length);
				for (int i = 0; i < vectorBlock.getFeatureNames().length; i++) {
					headerOutputStream.writeUTF(vectorBlock.getFeatureNames()[i]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return headerBytes.toByteArray();
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		//System.out.println("REACHED sinkModuleHeader");
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData());
		DataInputStream dis = new DataInputStream(bis);
		LCBinaryModuleHeader mh = new LCBinaryModuleHeader(binaryObjectData.getVersionNumber());
		try {
			mh.species = new String[dis.readShort()];
			for (int i = 0; i < mh.species.length; i++) {
				mh.species[i] = dis.readUTF();
			}
			mh.features = new String[dis.readShort()];
			for (int i = 0; i < mh.features.length; i++) {
				mh.features[i] = dis.readUTF();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		currMH = mh;
		return mh;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		//System.out.println("REACHED sinkData");
		
		// This doesn't seem to do anything.
	/*	BinaryStore bs = BinaryStore.findBinaryStoreControl();
		if (bs.getBinaryStoreSettings().getStoreLocation() == null) {
			currentBinaryDataFolder = "";
			lcControl.getTabPanel().getPanel().getTableModel().setRowCount(0);
			lcControl.getProcess().resultsDataBlock.clearAll();
		} else if (!bs.getBinaryStoreSettings().getStoreLocation().equals(currentBinaryDataFolder)) {
			currentBinaryDataFolder = bs.getBinaryStoreSettings().getStoreLocation();
			lcControl.getTabPanel().getPanel().getTableModel().setRowCount(0);
			lcControl.getProcess().resultsDataBlock.clearAll();
		} */
		
		if (!lcControl.isViewer()) {
			currMH = (LCBinaryModuleHeader) sinkModuleHeader(binaryObjectData, bh);
		}
		LCDataUnit du = null;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		try {
			String clusterID = dis.readUTF();
			int nDetections = dis.readInt();
			LCCallCluster cc = new LCCallCluster(lcControl.getParams().labelOrder, nDetections);
			cc.clusterID = clusterID;
			if (currMH.species.length != cc.labelList.size()) {
				System.out.println("LCBinaryDataSource.sinkData: Cluster at "+String.valueOf(lcControl.convertLocalLongToUTC(bh.getDataDate()))+
						" does not contain the same species as those specified in the parameters.");
				return null;
			}
			for (int i = 0; i < currMH.species.length; i++) {
				if (!cc.labelList.contains(currMH.species[i])) {
					System.out.println("LCBinaryDataSource.sinkData: Cluster at "+String.valueOf(lcControl.convertLocalLongToUTC(bh.getDataDate()))+
							" does not contain the same species as those specified in the parameters.");
					return null;
				}
			}
			for (int i = 0; i < nDetections; i++) {
				cc.uids[i] = dis.readLong();
				cc.datetimes[i] = dis.readLong();
				cc.durations[i] = (int) dis.readFloat();
				cc.lfs[i] = (int) dis.readFloat();
				cc.hfs[i] = (int) dis.readFloat();
				for (int j = 0; j < currMH.species.length; j++) {
					// This ensures that you can use binary files where the label order may be different, as long as the exact same species are present.
					cc.probaList[i][cc.labelList.indexOf(currMH.species[j])] = dis.readFloat();
				}
			}
			du = new LCDataUnit(lcControl, cc);
			//System.out.println("checkMemory: "+String.valueOf(checkMemory(this.getSisterDataBlock(), 10000000L)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return du;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		//System.out.println("REACHED sinkModuleFooter");
		//System.out.println("checkMemory: "+String.valueOf(checkMemory(this.getSisterDataBlock(), 10000000L)));
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		
	}
	
	// stolen from BinaryStore for testing
/*	private boolean checkMemory(PamDataBlock dataBlock, long minAmount) {
		Runtime r = Runtime.getRuntime();
		long totalMemory = r.totalMemory();
		long maxMemory = r.maxMemory();
		long freeMemory = r.freeMemory();
		if (freeMemory > minAmount) {
			return true;
		}
		else if (freeMemory + totalMemory < maxMemory - minAmount) {
			return true;
		}
		// run the garbage collector and try again ...
		r.gc();
		
		totalMemory = r.totalMemory();
		maxMemory = r.maxMemory();
		freeMemory = r.freeMemory();
		if (freeMemory > minAmount) {
			return true;
		}
		else if (freeMemory + totalMemory < maxMemory - minAmount) {
			return true;
		}
		
		// not enoughmemory, so throw a warning. 
		JOptionPane.showMessageDialog(null, "System memory is getting low and no more " +
				"\ndata can be loaded. Select a shorter load time for offline data", 
				dataBlock.getDataName(), JOptionPane.ERROR_MESSAGE);

		return false;
	} */
}