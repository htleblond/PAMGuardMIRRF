package mirrfTestClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import mirrfLiveClassifier.LCBestFeaturesDialog;
import mirrfLiveClassifier.LCPythonThreadManager;
import mirrfLiveClassifier.LCTrainingSetInfo;

public class TCPythonThreadManager extends LCPythonThreadManager {
	
	protected volatile boolean running;
	protected volatile boolean waitingOnModel;
	protected volatile boolean lastInitFailed;
	
	protected volatile int totalClusters;
	protected volatile int totalProcessed;
	
	public TCPythonThreadManager(TCControl tcControl) {
		super(tcControl);
		//this.scriptClassName = "TCPythonScript"; // Likely not even necessary.
		this.running = false;
		this.waitingOnModel = false;
		this.lastInitFailed = false;
		this.totalClusters = -1;
		this.totalProcessed = -1;
	}
	
	public boolean initializeTrainingSets() {
		running = true;
		TCParameters params = getControl().getParams();
		if (getControl().getTrainingSetInfo() == null ||
			(params.validation.contains("labelled") && getControl().getTestingSetInfo() == null)) {
			// TODO Error message
			return false;
		}
		getStartButton().setText("Stop");
		getStartButton().setEnabled(true);
		String pyParams = params.outputPythonParamsToText();
        if (getControl().getFeatureList().size() > 0) {
        	pyParams += "\""+getControl().getFeatureList().get(0)+"\"";
			for (int i = 1; i < getControl().getFeatureList().size(); i++) {
				pyParams += ",\""+getControl().getFeatureList().get(i)+"\"";
			}
		}
        pyParams += "]";
        pyParams += "]";
        getLoadingBar().setValue(0);
		if (params.validation.equals("leaveoneout")) {
			ArrayList<String> idList = new ArrayList<String>();
			Iterator<String> it = getControl().getTrainingSetInfo().subsetCounts.keySet().iterator();
			while (it.hasNext()) idList.add(it.next());
			idList.sort(Comparator.naturalOrder());
			getLoadingBar().setString("Fitting classifier models 0/"+String.valueOf(idList.size())+" (0.0%)");
			for (int i = 0; i < idList.size(); i++) {
				String initCommand = "tcm"+idList.get(i)+" = LCPythonScript.TCModel(r\""+getControl().getTrainPath()+"\","
						+pyParams+",[\""+idList.get(i)+"\"],[])";
				initializeModel(initCommand);
				if (lastInitFailed || !running) break;
				double loadInt = 100 * (double) (i+1) / idList.size();
				getLoadingBar().setValue((int) Math.floor(loadInt));
				getLoadingBar().setString("Fitting classifier models "+String.valueOf(i+1)+"/"+String.valueOf(idList.size())+
						" ("+String.format("%.1f", (float) loadInt)+"%)");
			}
		} else if (params.validation.equals("kfold")) {
			getLoadingBar().setString("Fitting classifier models 0/"+String.valueOf(params.kNum)+" (0.0%)");
			for (int i = 0; i < params.kNum; i++) {
				String initCommand = "tcm"+String.valueOf(i)+" = LCPythonScript.TCModel(r\""+getControl().getTrainPath()+"\","
						+pyParams+",[],[\""+String.valueOf(i)+"\"])";
				initializeModel(initCommand);
				if (lastInitFailed || !running) break;
				double loadInt = 100 * (double) (i+1) / params.kNum;
				getLoadingBar().setValue((int) Math.floor(loadInt));
				getLoadingBar().setString("Fitting classifier models "+String.valueOf(i+1)+"/"+String.valueOf(params.kNum)+
						" ("+String.format("%.1f", (float) loadInt)+"%)");
			}
		} else {
			getLoadingBar().setString("Fitting classifier models 0/1 (0.0%)");
			String initCommand = "tcm = LCPythonScript.TCModel(r\""+getControl().getTrainPath()+"\","+pyParams+",[";
			if (params.validation.equals("testsubset")) {
				//System.out.println(params.testSubset);
				if (params.testSubset.length() == 1) {
					ArrayList<String> subsetList = getControl().getTrainingSetInfo().getSortedSubsetList();
					boolean firstAdded = false;
					for (int i = 0; i < subsetList.size(); i++) {
						if (subsetList.get(i).substring(0, 1).equals(params.testSubset)) {
							if (firstAdded) initCommand += ",";
							initCommand += "\""+subsetList.get(i)+"\"";
							firstAdded = true;
						}
					}
				} else initCommand += params.testSubset;
			}
			initCommand += "],[])";
			initializeModel(initCommand);
			if (!lastInitFailed && running) {
				getLoadingBar().setValue(100);
				getLoadingBar().setString("Fitting classifier models 1/1 (100.0%)");
			}
		}
		if (lastInitFailed || !running) {
			if (lastInitFailed) getControl().SimpleErrorDialog("Error occured while attempting to fit classifier models in Python. "
					+ "See console for details.", 250);
			getStartButton().setText("Start");
			getStartButton().setEnabled(true);
			lastInitFailed = false;
			running = false;
			return false;
		}
		return true;
	}
	
	protected void initializeModel(String initCommand) {
		waitingOnModel = true;
		addCommand(initCommand);
		while (waitingOnModel && running) {
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean initializeBestFeaturesSet() {
		if (running) {
			getControl().SimpleErrorDialog("This action cannot be performed during processing.", 250);
			return false;
		}
		TCParameters params = getControl().getParams();
		String pyParams = params.outputPythonParamsToText();
        if (getControl().getFeatureList().size() > 0) {
        	pyParams += "\""+getControl().getFeatureList().get(0)+"\"";
			for (int i = 1; i < getControl().getFeatureList().size(); i++) {
				pyParams += ",\""+getControl().getFeatureList().get(i)+"\"";
			}
		}
        pyParams += "]";
        pyParams += "]";
        running = true;
        String initCommand = "tcmBest = LCPythonScript.TCModel(r\""+getControl().getTrainPath()+"\","+pyParams+",[],[])";
		initializeModel(initCommand);
		if (lastInitFailed || !running) {
			if (lastInitFailed) getControl().SimpleErrorDialog("Error occured while attempting to fit classifier models in Python. "
					+ "See console for details.", 250);
			lastInitFailed = false;
			running = false;
			return false;
		}
		running = false;
		return true;
	}
	
	public void startPredictions(int maxMapSize) {
		TCParameters params = getControl().getParams();
		LCTrainingSetInfo setInfo = getControl().getTrainingSetInfo();
		if (params.validation.contains("labelled")) setInfo = getControl().getTestingSetInfo();
		if (setInfo.compare(new LCTrainingSetInfo("")) || setInfo.compare(new LCTrainingSetInfo(null))) {
			getControl().SimpleErrorDialog("Set containing values to be tested is invalid.", maxMapSize);
			return;
		}
		File f = new File(setInfo.pathName);
		if (!f.exists()) {
			getControl().SimpleErrorDialog("Set containing values to be tested no longer exists.", maxMapSize);
			return;
		}
		ArrayList<String> clusterList = new ArrayList<String>();
		Scanner sc = null;
		String[] nextLine = null;
		try {
			sc = new Scanner(f);
			if (sc.hasNextLine()) nextLine = sc.nextLine().split(",");
			else {
				getControl().SimpleErrorDialog("Set containing values to be tested is apparently now empty.", maxMapSize);
				sc.close();
				return;
			}
			while (sc.hasNextLine()) {
				nextLine = sc.nextLine().split(",");
				if (params.validation.equals("testsubset")) {
					if (params.testSubset.length() == 1 && !nextLine[0].substring(0, 1).equals(params.testSubset)) continue;
					if (params.testSubset.length() != 1 && !nextLine[0].substring(0, 2).equals(params.testSubset)) continue;
				}
				if (!clusterList.contains(nextLine[0])) clusterList.add(nextLine[0]);
			}
			clusterList.sort(Comparator.naturalOrder());
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (sc != null) sc.close();
			getControl().SimpleErrorDialog("Error scanning set for testing.", maxMapSize);
			return;
		}
		totalClusters = clusterList.size();
		totalProcessed = 0;
		getLoadingBar().setValue(0);
		getLoadingBar().setString("0/"+String.valueOf(totalClusters)+" (0.0%)");
		for (int i = 0; i*maxMapSize < totalClusters; i++) {
			HashMap<String, ArrayList<TCDetection>> clusterMap = new HashMap<String, ArrayList<TCDetection>>();
			for (int j = i*maxMapSize; j < i*maxMapSize+maxMapSize && j < totalClusters; j++)
				clusterMap.put(clusterList.get(j), new ArrayList<TCDetection>());
			try {
				sc = new Scanner(f);
				nextLine = sc.nextLine().split(","); // Skips first line.
				while (sc.hasNextLine()) {
					nextLine = sc.nextLine().split(",");
					if (nextLine.length < getControl().getTrainingSetInfo().featureList.size() + 8 ||
							!clusterMap.containsKey(nextLine[0])) continue;
					try {
						TCDetection newDetection = new TCDetection(nextLine);
						clusterMap.get(nextLine[0]).add(newDetection);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO
			}
			for (int j = i*maxMapSize; j < i*maxMapSize+maxMapSize && j < totalClusters; j++) {
				if (!running) return;
				ArrayList<TCDetection> currList = clusterMap.get(clusterList.get(j));
				String outp = "tcm";
				if (params.validation.equals("leaveoneout")) outp += clusterList.get(j).substring(0,2);
				else if (params.validation.equals("kfold"))
					outp += String.valueOf((int) Math.floor(params.kNum * (double) j / totalClusters));
				outp += ".predictCluster([";
				for (int k = 0; k < currList.size(); k++) {
					TCDetection curr = currList.get(k);
					outp += "[\""+curr.clusterID+"\",";
					outp += String.valueOf(curr.uid)+",";
					outp += String.valueOf(getControl().convertDateStringToLong(curr.datetime))+",";
					outp += String.valueOf(curr.duration)+",";
					outp += String.valueOf(curr.lf)+",";
					outp += String.valueOf(curr.hf)+",";
					outp += "\""+curr.species+"\",";
					outp += "[";
					for (int l = 0; l < curr.featureVector.length; l++) {
						outp += String.valueOf(curr.featureVector[l]);
						if (l < curr.featureVector.length-1) outp += ",";
					}
					if (k < currList.size()-1) outp += "]],";
					else outp += "]]])";
				}
				if (!running) return;
				if (currList.size() > 0) lcControl.getThreadManager().addCommand(outp);
			}
		}
		running = false;
	}
	
	@Override
	protected void parseIPTOutput(String outpstr, boolean print) {
		if (print) System.out.println("LC IBR: "+outpstr);
		if (outpstr.equals("Initialization succeeded")) {
			//lcControl.setTrainingSetStatus(true);
			//lcControl.setModelFittingStatus(true);
			waitingOnModel = false;
		} else if (outpstr.equals("Initialization failed")) {
			//lcControl.setTrainingSetStatus(false);
			//lcControl.setModelFittingStatus(true);
			lastInitFailed = true;
			waitingOnModel = false;
		}
		if (outpstr.substring(0,6).equals("RESULT")) {
			getControl().getProcess().addResultsData(outpstr.substring(10));
			addOneToLoadingBar();
		}
		if (outpstr.contains("Error encountered while attempting to process cluster")) {
			addOneToLoadingBar();
			getSidePanelPanel().addOneToErrorCounter();
		}
		if (outpstr.contains("Cluster ignored due to settings")) {
			addOneToLoadingBar();
			getSidePanelPanel().addOneToIgnoreCounter();
		}
		if (outpstr.equals("RUNLAST")) {
			finished = true;
		}
		if (outpstr.contains("BESTFEATUREORDER")) {
			if (getControl().getTabPanel().getPanel().getWaitingDialogThread() != null)
				getControl().getTabPanel().getPanel().getWaitingDialogThread().halt();
			LCBestFeaturesDialog dialog = new LCBestFeaturesDialog(lcControl.getGuiFrame(), lcControl, outpstr.substring(18));
			dialog.setVisible(true);
		}
	}
	
	protected void addOneToLoadingBar() {
		totalProcessed++;
		if (totalClusters <= 0) return;
		double value = 100 * (double) totalProcessed / totalClusters;
		getLoadingBar().setValue((int) Math.floor(value));
		getLoadingBar().setString(String.valueOf(totalProcessed)+"/"+String.valueOf(totalClusters)+" ("+
				String.format("%.1f", (float) value)+"%)");
	}
	
	protected TCSidePanelPanel getSidePanelPanel() {
		return getControl().getSidePanel().getTCSidePanelPanel();
	}
	
	protected JButton getStartButton() {
		return getSidePanelPanel().startButton;
	}
	
	protected JProgressBar getLoadingBar() {
		return getSidePanelPanel().loadingBar;
	}
	
	@Override
	protected TCControl getControl() {
		return (TCControl) lcControl;
	}
	
	public void stop() {
		running = false;
	}
	
}