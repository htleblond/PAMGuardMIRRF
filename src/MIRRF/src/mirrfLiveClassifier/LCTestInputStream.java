package mirrfLiveClassifier;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryInputStream;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.CountingInputStream;

public class LCTestInputStream extends BinaryInputStream {
	
	public File curr;
	public DataInputStream dis;
	
	public LCTestInputStream(BinaryStore binaryStore, PamDataBlock pamDataBlock) {
		super(binaryStore, pamDataBlock);
		System.out.println("TZ: "+PamCalendar.defaultTimeZone.getDisplayName());
	}
	
	protected boolean openFile(File inputFile) {
		curr = inputFile;
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			CountingInputStream cis = new CountingInputStream(new BufferedInputStream(fis));
			dis = new DataInputStream(cis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		int unitsRead = 0;

		return true;
	}
	
	public boolean testReadHeader() {
		// first work out the total number of bytes in the header. 
//		int headLen = 12 + pamguard.length() + pamguardVersion.length() + 
//		pamguardBranch.length() + 8 + 8 + 
//		2 + moduleType.length() +
//		2 + moduleName.length() + 
//		2 + streamName.length() +
//		4;
//		if (extraInfo != null) {
//			headLen += extraInfo.length;
//		}
		
		int headLen = 0;
		int headId = 0;
		String PamString;
		String pamguard = "PAMGUARDDATA";
		byte[] nameBytes = new byte[pamguard.getBytes().length];
		int nNameBytes = 0;
		int extraInfoLen = 0;
		
		byte[] bytes;
		try {
			headLen = dis.readInt();
			headId = dis.readInt();
			int headerFormat = dis.readInt();
			nNameBytes = dis.read(nameBytes);
			String pamguardVersion = dis.readUTF();
			String pamguardBranch = dis.readUTF();
			long dataDate = dis.readLong();
			long analysisDate = dis.readLong();
			long fileStartSample = dis.readLong();
			String moduleType = dis.readUTF();
			String moduleName = dis.readUTF();
			String streamName = dis.readUTF();
			extraInfoLen = dis.readInt();
			dis.skip(extraInfoLen);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}