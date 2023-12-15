package mirrfFeatureExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import PamguardMVC.PamDataUnit;

/**
 * A data unit for storing pre-existing training data from .mirrfts files for
 * when the ClipBlockProcess needs it.
 * @author Holly LeBlond
 */
//@SuppressWarnings("rawtypes")
public class FETrainingDataUnit extends PamDataUnit {
	
	public String clusterID;
	public String location;
	public String label;
	public HashMap<String, Double> problematicFeatures;
	
	public FETrainingDataUnit(FEInputDataObject inp, long startSample, long sampleDuration) {
		super(0, 0, -1, -1);
		
		this.setUID(inp.uid);
		long longtime = FEControl.convertDateStringToLong(inp.datetime);
		this.setTimeMilliseconds(longtime);
		this.setFrequency(new double[] {Double.valueOf(inp.lf), Double.valueOf(inp.hf)});
		this.setDurationInMilliseconds(Double.valueOf(inp.duration));
		this.setMeasuredAmplitude(-1);
		this.setStartSample(startSample);
		this.setSampleDuration(sampleDuration);
		
		this.clusterID = inp.clusterID;
		this.location = inp.location;
		this.label = inp.label;
		this.problematicFeatures = inp.problematicFeatures;
	}
	
}