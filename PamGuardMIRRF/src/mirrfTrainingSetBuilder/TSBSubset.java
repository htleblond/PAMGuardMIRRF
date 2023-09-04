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
	
	/**
	 * Adds a TSBDetection to the corresponding list in validEntriesList.
	 * Also changes start and end dates if the TSBDetection falls outside the range. 
	 * @param inp - The input TSBDetection.
	 * @param selected - If species not previously in classList, adds the corresponding int to selectionArray if true.
	 */
	public void addEntry(TSBDetection inp, boolean selected) {
		if (start.length() == 0 || start.compareTo(inp.datetime) > 0) start = inp.datetime;
		if (end.length() == 0 || end.compareTo(inp.datetime) < 0) end = inp.datetime;
		if (!classList.contains(inp.species)) {
			classList.add(inp.species);
			if (selected) {
				int[] newSelectionArray = new int[selectionArray.length+1];
				for (int i = 0; i < selectionArray.length; i++) newSelectionArray[i] = selectionArray[i];
				newSelectionArray[newSelectionArray.length-1] = classList.indexOf(inp.species);
				this.selectionArray = newSelectionArray;
			}
			validEntriesList.add(new ArrayList<TSBDetection>());
		}
		validEntriesList.get(classList.indexOf(inp.species)).add(inp);
	}
	
}