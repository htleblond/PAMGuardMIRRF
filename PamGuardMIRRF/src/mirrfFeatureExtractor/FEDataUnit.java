package mirrfFeatureExtractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import PamguardMVC.PamDataUnit;
import whistlesAndMoans.AbstractWhistleDataUnit;

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