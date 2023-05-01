package wmnt;

import binaryFileStorage.*;
import java.io.*;

/**
 * This is the class used for reading the contents of binary files into the table in the side panel.
 * @author Taylor LeBlond
 */
public class WMNTBinaryReader extends BinaryInputStream {
	
	public BinaryHeader bh;
	public BinaryFooter bf;
	public boolean worked;

	public WMNTBinaryReader(String filename) {
		super(new BinaryStore("Binary Store"), null);
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
	
	public void closeReader() {
		closeFile();
	}
}