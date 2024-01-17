package mirrfFeatureExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import mirrf.MIRRFJarExtractor;

/**
 * Creates an instance of a Python interpreter, sends clips to the
 * Python script for processing, and manages communication between
 * Java and Python.
 * @author Holly LeBlond
 */
public class FEPythonThreadManager {
	
	private FEControl feControl;
	private boolean active;
	private boolean printThreadsActive;
	private String pathname;
	private BufferedWriter bw = null;
	private BufferedReader br = null;
	private BufferedReader ebr = null;
	private InputPrintThread ipt = null;
	private ErrorPrintThread ept = null;
	private RunnerThread rt = null;
	//private ArrayList<String> activePythonThreads;
	private Process pr;
	public volatile ArrayList<String> commandList;
	public PythonInterpreterThread pit = null;
	
	private final int maxThreads = 2; // TODO ADD THIS TO FEPARAMETERS EVENTUALLY
	private final int maxClipsAtOnce = 25; // TODO ADD THIS TO FEPARAMETERS EVENTUALLY
	protected volatile ArrayList<ContourClip> waitList;
	protected volatile ArrayList<String> idList;
	protected volatile ArrayList<ArrayList<ContourClip>> ccList;
	protected volatile int activeThread;
	private volatile ArrayList<ArrayList<String[]>> pythonOutpList;
	private volatile boolean rdbctSignal;
	
	public volatile ArrayList<String> remainingUIDs; // TODO This is only for testing.
	
	public FEPythonThreadManager(FEControl feControl) {
		this.feControl = feControl;
		//this.activePythonThreads = new ArrayList<String>();
		this.printThreadsActive = true;
		this.commandList = new ArrayList<String>();
		this.rdbctSignal = false;
		this.remainingUIDs = new ArrayList<String>();
		
		String defpathname = feControl.getParams().tempFolder;
		this.pathname = "";
		for (int i = 0; i < defpathname.length(); i++) {
			if (!defpathname.substring(i, i+1).equals("\\")) {
				this.pathname += defpathname.substring(i, i+1);
			} else {
				this.pathname += "/";
			}
		}
		
		if (feControl.getParams().tempFolder.length() > 0) {
			setActive();
		} else {
			setInactive();
		}
		
		this.pit = new PythonInterpreterThread();
		pit.start();
	}
	
	/**
	 * Checks if threads are still running and restarts them if necessary.
	 */
	public void checkThreads() {
		if (!pit.isAlive() || active == false) {
			pit = new PythonInterpreterThread();
			pit.start();
			active = true;
		}
		if (!printThreadsActive) {
			printThreadsActive = true;
			rt = new RunnerThread();
			rt.start();
			ipt = new InputPrintThread();
			ipt.start();
			ept = new ErrorPrintThread();
			ept.start();
		}
	}
	
	/**
	 * Clears waitList, idList, ccList and pythonOutpList, and calls resetActiveThread().
	 */
	public void resetWaitlists() {
		waitList = new ArrayList<ContourClip>();
		idList = new ArrayList<String>();
		ccList = new ArrayList<ArrayList<ContourClip>>();
		resetActiveThread();
		pythonOutpList = new ArrayList<ArrayList<String[]>>();
	}
	
	/**
	 * Sets activeThread to -1. This means that addVectorToDataBlock() won't
	 * skip over any slots in pythonOutpList. Don't call this unless FEProcess
	 * has finished with its current cluster.
	 */
	public void resetActiveThread() {
		activeThread = -1;
	}
	
	/**
	 * Thread that initializes and sends commands to the Python interpreter.
	 */
	protected class PythonInterpreterThread extends Thread {
		protected PythonInterpreterThread() {}
		@Override
		public void run() {
			if (active) {
				try {
					resetWaitlists();
					
					// Kudos to this: https://stackoverflow.com/questions/25041529/how-to-run-the-python-interpreter-and-get-its-output-using-java
					
					ProcessBuilder pb = new ProcessBuilder("python", "-i");
					pr = pb.start();
			        br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			        ebr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		            bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
		            pythonCommand("import os");
		            pythonCommand("os.chdir(r\""+pathname+"\")");
		            pythonCommand("os.getcwd()");
		            pythonCommand("import numpy as np");
		            String pyParams = feControl.getParams().outputPythonParamsToText();
		            if (pyParams.length() > 0) {
		            	pythonCommand("txtParams = "+pyParams);
		            } else {
		            	pythonCommand("txtParams = []");
		            }
		            pythonCommand("import librosa");
		            pythonCommand("import librosa");
		            pythonCommand("import sys");
		            pythonCommand("import gc");
		            pythonCommand("import FEPythonThread");
		            
					String outpstr = null;
					if (br.ready()) {
						while ((outpstr = br.readLine()) != null) {
							if (feControl.getParams().miscPrintJavaChecked)
								System.out.println(outpstr);
						}
					}
					if (ebr.ready()) {
						while ((outpstr = ebr.readLine()) != null) {
							if (feControl.getParams().miscPrintJavaChecked)
								System.out.println(outpstr);
						}
					}
					
					System.out.println("Python Interpreter Thread initialization successful.");
				} catch (Exception e) {
					System.out.println("Python Interpreter Thread initialization failed.");
					e.printStackTrace(System.out);
				}
			}
			startPrintThreads();
			while (active || commandList.size() > 0) {
				if (commandList.size() > 0) {
					pythonCommand(commandList.get(0));
					commandList.remove(0);
				}
				if (commandList.size() == 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(50);
					} catch (Exception e) {
						System.out.println("Sleep exception.");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * A simple data structure for storing info to be passed to the Python script.
	 */
	protected class ContourClip {
		
		public String clusterID;
		public String uid;
		public String nrName;
		public String clipName;
		public String[] headerData;
		
		protected ContourClip(String clusterID, String uid, String nrName, String clipName, String[] headerData) {
			this.clusterID = clusterID;
			this.uid = uid;
			this.nrName = nrName;
			this.clipName = clipName;
			this.headerData = headerData;
		}
	}
	
	/**
	 * Adds a Python command to the queue.
	 */
	public void addCommand(String inp) {
		commandList.add(inp);
	}
	
	/**
	 * Adds a contour clip to the waitList.
	 * "headerData" should include contour header data (see use in FEProcess for details).
	 */
	public void sendContourClipToThread(String clusterID, String uid, String nrName, String clipName, String[] headerData) {
		remainingUIDs.add(uid);
		ContourClip cc = new ContourClip(clusterID, uid, nrName, clipName, headerData);
		this.waitList.add(cc);
	}
	
	/**
	 * @return How many Python commands are still in the queue.
	 */
	public int commandsLeft() {
		return commandList.size();
	}
	
	/**
	 * @return How many clips there are that haven't been sent to the Python script yet.
	 */
	public int getWaitlistSize() {
		return waitList.size();
	}
	
	/**
	 * @return How many clips there are that are currently being processed.
	 */
	public int clipsLeft() {
		ArrayList<ArrayList<ContourClip>> ccClone = new ArrayList<ArrayList<ContourClip>>(ccList);
		int outp = 0;
		for (int i = 0; i < ccClone.size(); i++) {
			outp += ccClone.get(i).size();
		}
		return outp;
	}
	
	/**
	 * @return How many instances of Python output there are that haven't been added to the data block yet.
	 */
	public int vectorsLeft() {
		ArrayList<ArrayList<String[]>> pythonOutpClone = new ArrayList<ArrayList<String[]>>(pythonOutpList);
		int outp = 0;
		for (int i = 0; i < pythonOutpClone.size(); i++) {
			outp += pythonOutpClone.get(i).size();
		}
		return outp;
	}
	
	/**
	 * Shuts down the Python interpreter.
	 * Probably useless and would likely cause a tonne of exceptions.
	 */
	@Deprecated
	protected void shutDown() {
		if (bw != null) {
			try {
				bw.write("quit()");
				bw.newLine();
				bw.flush();
				bw.close();
			} catch (IOException e) {
				System.out.println("IOException in shutDown().");
			}
		}
	}
	
	/**
	 * Directly sends a Python command to the interpreter.
	 */
	private void pythonCommand(String command) {
		if (bw != null) {
			try {
				if (feControl.getParams().miscPrintInputChecked)
					System.out.println("FE COMMAND: "+command);
				if (command != null) {
					bw.write(command);
					bw.newLine();
					bw.flush();
				}
			} catch (IOException e) {
				System.out.println("IOException in pythonCommand().");
			}
		}
	}
	
	/**
	 * Used by the RawDataBlockCheckerThread in FEProcess to signal that the current cluster has been passed.
	 */
	public void setRDBCTSignal(boolean inp) {
		rdbctSignal = inp;
	}
	
	/**
	 * Checks if the RawDataBlockCheckerThread in FEProcess is finished with the current cluster.
	 */
	public boolean getRDBCTSignal() {
		return rdbctSignal;
	}
	
	/**
	 * Thread that passes ContourClip objects from the waitList to the ccList and
	 * sends Python commands in the queue to the interpreter.
	 */
	protected class RunnerThread extends Thread {
		protected RunnerThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				if (waitList.size() > 0 && clipsLeft() < maxClipsAtOnce) {
					ContourClip cc = waitList.get(0);
					if (idList.contains(cc.clusterID)) {
						ccList.get(idList.indexOf(cc.clusterID)).add(cc);
						String command = "thread"+String.format("%02d", idList.indexOf(cc.clusterID))+".addClip(r\""+cc.clipName+"\"";
						for (int i = 0; i < cc.headerData.length; i++) {
							command += ","+cc.headerData[i];
						}
						command += ")";
						commandList.add(command);
						waitList.remove(0);
					} else {
						int index = -1;
						if (idList.size() < maxThreads) {
							idList.add(cc.clusterID);
							ccList.add(new ArrayList<ContourClip>());
							pythonOutpList.add(new ArrayList<String[]>());
							index = idList.size()-1;
						} else {
							for (int i = 0; i < ccList.size(); i++) {
								if (ccList.get(i).size() == 0 && pythonOutpList.get(i).size() == 0) {
									index = i;
									idList.set(index, cc.clusterID);
									pythonOutpList.set(index, new ArrayList<String[]>());
									break;
								}
							}
						}
						if (index != -1) {
							if (feControl.getParams().audioNRChecked) {
								//commandList.add("nr"+String.format("%02d", index)+
								//		" = librosa.load(r\""+cc.nrName+"\", sr="+String.valueOf(feControl.getParams().sr)+")[0]");
								commandList.add("nr"+String.format("%02d", index)+
										" = FEPythonThread.loadAudio(fn=r\""+cc.nrName+"\", sr="+String.valueOf(feControl.getParams().sr)+")");
								commandList.add("thread"+String.format("%02d", idList.indexOf(cc.clusterID))+
										" = FEPythonThread.FEThread(r\""+cc.nrName+"\", nr"+String.format("%02d", idList.indexOf(cc.clusterID))+", txtParams)");
							} else {
								commandList.add("thread"+String.format("%02d", idList.indexOf(cc.clusterID))+
										" = FEPythonThread.FEThread(\"\", [], txtParams)");
							}
							activeThread = index;
						}
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (Exception e) {
							System.out.println("Sleep exception.");
							e.printStackTrace();
						}
					}
				} else {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (Exception e) {
						System.out.println("Sleep exception.");
						e.printStackTrace();
					}
				}
				if (vectorsLeft() > 0) {
					pushVectorsToDataBlock();
				}
				if (rdbctSignal) {
					rdbctSignal = false;
					resetActiveThread();
				}
			}
		}
	}
	
	/**
	 * Processes output from the InputPrintThread.
	 */
	protected boolean processPythonOutput(String inp) {
		if (inp.startsWith("outp:")) {
			//System.out.println(inp);
			FEParameters params = feControl.getParams();
			
			String[] tokens = inp.substring(7,inp.length()-1).split(", ");
			boolean breakLoop = false;
			for (int i = 0; i < ccList.size(); i++) {
				for (int j = 0; j < ccList.get(i).size(); j++) {
					if (ccList.get(i).get(j).uid.equals(tokens[1])) {
						ccList.get(i).remove(j);
						breakLoop = true;
						break;
					}
				}
				if (breakLoop) {
					break;
				}
			}
			int slot = idList.indexOf(tokens[0].substring(1,tokens[0].length()-1));
			if (slot < 0) {
				if (idList.size() < maxThreads) {
					slot = idList.size();
					idList.add(tokens[0].substring(1,tokens[0].length()-1));
					ccList.add(new ArrayList<ContourClip>());
					pythonOutpList.add(new ArrayList<String[]>());
				} else {
					for (int i = 0; i < pythonOutpList.size(); i++) {
						if (pythonOutpList.get(i).size() == 0 && ccList.get(i).size() == 0) {
							slot = i;
							idList.set(i, tokens[0].substring(1,tokens[0].length()-1));
							break;
						}
					}
				}
				if (slot < 0) {
					remainingUIDs.remove(tokens[1]); // TODO
					feControl.subtractOneFromPendingCounter();
					feControl.addOneToCounter(FEPanel.FAILURE, tokens[1]);
					return false;
				}
			}
			pythonOutpList.get(slot).add(tokens);
			// The blankfile/matchesfeatures checks are pretty much already taken care of in FESettingsDialog now.
			if (params.outputDataOption > 0) {
				try {
					File f = new File(params.outputDataName);
					f.setWritable(true, false);
					
					String outp = "";
					outp += tokens[0].substring(1, tokens[0].length()-1); // 0 - cluster
					outp += ","+tokens[1]; // 1 - uid
					int index = 2;
					if (params.outputDataOption == params.OUTPUT_MIRRFTS) {
						String location = tokens[index++]; // 2 (.mirrfts) - location
						outp += ","+location.substring(1, location.length()-1);
					}
					//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
					//df.setTimeZone(TimeZone.getTimeZone("UTC"));
					//Date date  = new Date(Long.valueOf(tokens[index++])); 
					outp += ","+FEControl.convertDateLongToString(Long.valueOf(tokens[index++])); // 2 (.mirrffe), 3 (.mirrfts) - date
					outp += ","+tokens[index++]; // 3 (.mirrffe), 4 (.mirrfts) - duration
					outp += ","+tokens[index++]; // 4 (.mirrffe), 5 (.mirrfts) - lf
					outp += ","+tokens[index++]; // 5 (.mirrffe), 6 (.mirrfts) - hf
					if (params.outputDataOption == params.OUTPUT_MIRRFTS) {
						String label = tokens[index++]; // 7 (.mirrfts) - label
						outp += ","+label.substring(1, label.length()-1);
					}
					for (int i = index; i < tokens.length; i++) {
						if (tokens[i].length() == 0) {
							System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-index][1]+" has no value.");
							return false;
						} else if (tokens[i].equals("nan")) {
							System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-index][1]+" = NaN.");
							return false;
						}
						String[] num_tokens = tokens[i].split("e");
						if (num_tokens.length == 1) outp += ","+tokens[i];
						else if (num_tokens.length == 2)
							outp += ","+String.valueOf(Double.valueOf(num_tokens[0])*Math.pow(10, Integer.valueOf(num_tokens[1])));
						else {
							System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-3][1]+" has a non-sequitur value.");
							return false;
						}
					}
					outp += "\n";
					try {
						PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
						StringBuilder sb = new StringBuilder();
						sb.append(outp);
						pw.write(sb.toString());
						pw.flush();
						pw.close();
					} catch (Exception e2) {
						System.out.println("ERROR: "+tokens[1]+" -> Could not write row to output file.");
						return false;
					}
				} catch (Exception e3) {
					System.out.println("ERROR: "+tokens[1]+" -> Python output not formatted correctly.");
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Parses through pythonOutpList and sends any feature vector data that's ready to go to the data block.
	 */
	private void pushVectorsToDataBlock() {
		for (int i = 0; i < pythonOutpList.size(); i++) {
			if (feControl.feProcess.getVectorDataBlock().isFinished() && getWaitlistSize() == 0 && clipsLeft() == 0)
				resetActiveThread();
			ArrayList<String[]> currList = new ArrayList<String[]>(pythonOutpList.get(i));
			if (i == activeThread || ccList.get(i).size() > 0 || currList.size() == 0) {
				continue;
			}
			if (currList.get(0).length == 0) {
				pythonOutpList.get(i).clear();
				continue;
			}
			FECallCluster cc = new FECallCluster(currList.size(), feControl.getParams().featureList.length);
			try {
				cc.clusterID = currList.get(0)[0];
				for (int j = 0; j < currList.size(); j++) {
					cc.uids[j] = Long.valueOf(currList.get(j)[1]);
					int index = 2;
					if (feControl.getParams().inputFilesAreMIRRFTS()) {
						String location = currList.get(j)[index++];
						cc.locations[j] = location.substring(1, location.length()-1);
					}
					cc.datetimes[j] = Long.valueOf(currList.get(j)[index++]);
					cc.durations[j] = (int) Double.valueOf(currList.get(j)[index++]).doubleValue();
					cc.lfs[j] = (int) Double.valueOf(currList.get(j)[index++]).doubleValue();
					cc.hfs[j] = (int) Double.valueOf(currList.get(j)[index++]).doubleValue();
					if (feControl.getParams().inputFilesAreMIRRFTS()) {
						String label = currList.get(j)[index++];
						cc.labels[j] = label.substring(1, label.length()-1);
					}
					for (int k = index; k < currList.get(j).length; k++) {
						cc.featureVector[j][k-index] = Double.valueOf(currList.get(j)[k]);
					}
				}
				if (cc.uids.length > 0) {
					FEDataUnit du = new FEDataUnit(feControl, cc);
					feControl.feProcess.addVectorData(du);
					for (int j = 0; j < cc.uids.length; j++) {
						remainingUIDs.remove(String.valueOf(cc.uids[j])); // TODO
						feControl.subtractOneFromPendingCounter();
						feControl.addOneToCounter(FEPanel.SUCCESS, String.valueOf(cc.uids[j]));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				for (int j = 0; j < currList.size(); j++) {
					remainingUIDs.remove(currList.get(j)[1]); // TODO
					feControl.subtractOneFromPendingCounter();
					feControl.addOneToCounter(FEPanel.FAILURE, currList.get(j)[1]);
				}
			}
			deleteFilesAfterProcessing(cc);
			pythonOutpList.get(i).clear();
		}
	/*	String outp = "Remaining:";
		ArrayList<String> remainingClone = new ArrayList<String>(remainingUIDs);
		for (int i = 0; i < remainingClone.size(); i++) outp += " "+remainingClone.get(i);
		outp += ", vectorsLeft: "+String.valueOf(this.vectorsLeft());
		System.out.println(outp); */
	}
	
	protected void deleteFilesAfterProcessing(FECallCluster cc) {
		new File(feControl.getParams().tempFolder+"NR_"+cc.clusterID.replace("-", "_")+".wav").delete();
		String toDelete = feControl.getParams().tempFolder+"FE_"+cc.clusterID.replace("-", "_")+"_";
		for (int i = 0; i < cc.getSize(); i++) {
			try { // In case UIDs were not parsed correctly for whatever reason.
				new File(toDelete+cc.uids[i]+".wav").delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Re-initializes the "txtParams" variable, generally under the assumption that the
	 * Feature Extractor's settings have changed.
	 */
	protected void resetTxtParams() {
		try {
			String pyParams = feControl.getParams().outputPythonParamsToText();
	        if (pyParams.length() > 0) {
	        	pythonCommand("txtParams = "+pyParams);
	        } else {
	        	pythonCommand("txtParams = []");
	        }
		} catch (Exception e) {
			System.out.println("Exception in resetTxtParams().");
		}
	}
	
	/**
	 * Forces the print threads and runner thread to (re)start.
	 */
	public void startPrintThreads() {
		printThreadsActive = true;
		ipt = new InputPrintThread();
		ept = new ErrorPrintThread();
		rt = new RunnerThread();
		ipt.start();
		ept.start();
		rt.start();
	}
	
	/**
	 * Doesn't work. Needs to be fixed at some point.
	 */
	@Deprecated
	public void stopPrintThreads() {
		printThreadsActive = false;
		try {
			ipt.join();
			ept.join();
			rt.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void signalPythonError(String outpstr) {
		String uid = outpstr.substring(outpstr.indexOf("Error: Could not process ")+25);
		boolean breakLoop = false;
		for (int i = 0; i < ccList.size(); i++) {
			for (int j = 0; j < ccList.get(i).size(); j++) {
				if (ccList.get(i).get(j).uid.equals(uid)) {
					ccList.get(i).remove(j);
					breakLoop = true;
					break;
				}
			}
			if (breakLoop) {
				break;
			}
		}
		if (breakLoop) {
			remainingUIDs.remove(uid); // TODO
			boolean subd = feControl.subtractOneFromPendingCounter();
			feControl.addOneToCounter(FEPanel.FAILURE, uid);
		}
	}
	
	/**
	 * Passes non-error Python output back to Java.
	 */
	protected class InputPrintThread extends Thread {
		protected InputPrintThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					String outpstr = "";
					if (br.ready()) {
						while ((outpstr = br.readLine()) != null) {
							if (feControl.getParams().miscPrintOutputChecked)
								System.out.println("FE IBR: "+outpstr);
							boolean boo = processPythonOutput(outpstr);
							if (outpstr.contains("Error: Could not process ")) {
								signalPythonError(outpstr);
							}
						}
					}
				} catch (IOException e) {
					// TODO
				}
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
					System.out.println("Sleep exception.");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Passes Python error output back to Java.
	 */
	protected class ErrorPrintThread extends Thread {
		protected ErrorPrintThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					String outpstr = "";
					if (ebr.ready()) {
						while (ebr.ready() && (outpstr = ebr.readLine()) != null) {
							System.out.println("FE EBR: "+outpstr);
							if (outpstr.contains("NameError: name ")) {
								printThreadsActive = false;
								active = false;
								break;
							}
							String[] tokens = outpstr.split(" ");
						/*	if (tokens.length > 0) {
								if (tokens[0].equals(">>>")) {
									break;
								}
							} */
							if (outpstr.contains("Error: Could not process ")) {
								signalPythonError(outpstr);
							}
						}
					}
				} catch (IOException e) {
					// TODO
				}
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
					System.out.println("Sleep exception.");
					e.printStackTrace();
				}
			}
		}
	}
	
	public FEControl getControl() {
		return feControl;
	}
	
	/**
	 * Checks if the thread manager is running or not. Generally should return true.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Attempts to extract the Python script from the .jar file and sets the thread manager to active if it worked.
	 * @return Whether or not the JarExtractor succeeded.
	 */
	public boolean setActive() {
		active = new MIRRFJarExtractor().extract("src/mirrfFeatureExtractor/FEPythonThread.py",
				feControl.getParams().tempFolder, "FEPythonThread.py", true);
		if (feControl.getParams().miscPrintJavaChecked)
			System.out.println("JarExtractor completed.");
		return active;
	}
	
	/**
	 * Deactivates the threads. Exercise caution.
	 */
	public void setInactive() {
		active = false;
	}
}