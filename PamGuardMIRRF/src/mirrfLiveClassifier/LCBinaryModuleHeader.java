package mirrfLiveClassifier;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleHeader;

/**
 * The Live Classifier's module header for binary files.
 * @author Holly LeBlond
 */
public class LCBinaryModuleHeader extends ModuleHeader implements Serializable, ManagedParameters {
	
	private static final long serialVersionUID = 1L;
	
	public String[] species = new String[0];
	public String[] features = new String[0];

	public LCBinaryModuleHeader(int moduleVersion) {
		super(moduleVersion);
	}

	@Override
	public boolean createHeader(BinaryObjectData binaryObjectData, BinaryHeader binaryHeader) {
		return false;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
}