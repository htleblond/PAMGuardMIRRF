package mirrfTrainingSetBuilder;

import java.util.ArrayList;

/**
 * Data object for storing subset data.
 * @author Holly LeBlond
 */
public class TSBSubset {
	
	public String featurePath;
	public String wmntPath;
	public String id;
	public String location;
	public String start;
	public String end;
	public ArrayList<String> classList;
	public int[] selectionArray;
	public ArrayList<ArrayList<TSBDetection>> validEntriesList;
	
	public TSBSubset() {
		this.featurePath = "";
		this.wmntPath = "";
		this.id = "";
		this.location = "";
		this.start = "";
		this.end = "";
		this.classList = new ArrayList<String>();
		this.selectionArray = new int[0];
		this.validEntriesList = new ArrayList<ArrayList<TSBDetection>>();
	}
	
}