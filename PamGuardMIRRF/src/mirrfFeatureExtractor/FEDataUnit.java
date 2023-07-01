package mirrfFeatureExtractor;

import PamguardMVC.PamDataUnit;

/**
 * The Feature Extractor's output data unit.
 * Contains feature vector info for each contour stored in an FECallCluster.
 * @author Holly LeBlond
 */
public class FEDataUnit extends PamDataUnit {
	
	protected FEControl feControl;
	protected FECallCluster cluster;
	
	public FEDataUnit(FEControl feControl, FECallCluster cluster) {
		super(cluster.getStartAndEnd()[0], 0, -1, -1);
		this.feControl = feControl;
		this.cluster = cluster;
	}
	
	public FECallCluster getCluster() {
		return cluster;
	}
	
	public void setCluster(FECallCluster inp) {
		cluster = inp;
	}
}