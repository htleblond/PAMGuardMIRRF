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
	//private KillerThread kt = null;
	private RunnerThread rt = null;
	private ArrayList<String> activePythonThreads;
	private Process pr;
	public volatile ArrayList<String> commandList;
	public PythonInterpreterThread pit = null;
	
	private final int maxThreads = 10; // ADD THIS TO FEPARAMETERS EVENTUALLY
	protected volatile ArrayList<ContourClip> waitList;
	protected volatile ArrayList<String> idList;
	protected volatile ArrayList<ArrayList<ContourClip>> ccList;
	protected volatile int activeThread;
	private volatile ArrayList<ArrayList<String[]>> pythonOutpList;
	private volatile boolean rdbctSignal;
	
	public FEPythonThreadManager(FEControl feControl) {
		this.feControl = feControl;
		this.activePythonThreads = new ArrayList<String>();
		this.printThreadsActive = true;
		this.commandList = new ArrayList<String>();
		this.rdbctSignal = false;
		
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
			setInactive(true);
		}
		
		this.pit = new PythonInterpreterThread();
		pit.start();
	}
	
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
	
	public void resetWaitlists() {
		waitList = new ArrayList<ContourClip>();
		idList = new ArrayList<String>();
		ccList = new ArrayList<ArrayList<ContourClip>>();
		activeThread = -1;
		pythonOutpList = new ArrayList<ArrayList<String[]>>();
	}
	
	public void resetActiveThread() {
		activeThread = -1;
	}
	
	protected class PythonInterpreterThread extends Thread {
		protected PythonInterpreterThread() {
			//this.run();
		}
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
							System.out.println(outpstr);
						}
					}
					if (ebr.ready()) {
						while ((outpstr = ebr.readLine()) != null) {
							System.out.println(outpstr);
						}
					}
					
					System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				} catch (Exception e) {
					System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
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
	
	protected class ContourClip {
		
		public String clusterID;
		public String uid;
		public String nrName;
		public String clipName;
		public String[] extras;
		
		protected ContourClip(String clusterID, String uid, String nrName, String clipName, String[] extras) {
			this.clusterID = clusterID;
			this.uid = uid;
			this.nrName = nrName;
			this.clipName = clipName;
			this.extras = extras;
		}
	}
	
	public void addCommand(String inp) {
		commandList.add(inp);
	}
	
	public void sendContourClipToThread(String clusterID, String uid, String nrName, String clipName, String[] extras) {
		waitList.add(new ContourClip(clusterID, uid, nrName, clipName, extras));
	}
	
	public int clipsLeft() {
		int outp = 0;
		for (int i = 0; i < ccList.size(); i++) {
			outp += ccList.get(i).size();
		}
		return outp;
	}
	
	public int vectorsLeft() {
		int outp = 0;
		for (int i = 0; i < pythonOutpList.size(); i++) {
			outp += pythonOutpList.get(i).size();
		}
		return outp;
	}
	
	public int getWaitlistSize() {
		return waitList.size();
	}
	
	public int commandsLeft() {
		return commandList.size();
	}
	
	public void shutDown() {
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
	
	private void pythonCommand(String command) {
		if (bw != null) {
			try {
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
	
	public void setRDBCTSignal(boolean inp) {
		rdbctSignal = inp;
	}
	
	public boolean getRDBCTSignal() {
		return rdbctSignal;
	}
	
	protected class RunnerThread extends Thread {
		protected RunnerThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				if (waitList.size() > 0) {
					ContourClip cc = waitList.get(0);
					if (cc == null) {
						waitList.remove(0);
					} else if (idList.contains(cc.clusterID)) {
						ccList.get(idList.indexOf(cc.clusterID)).add(cc);
						String command = "thread"+String.format("%02d", idList.indexOf(cc.clusterID))+".addClip(r\""+cc.clipName+"\"";
						for (int i = 0; i < cc.extras.length; i++) {
							command += ","+cc.extras[i];
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
					addVectorToDataBlock();
				}
				if (rdbctSignal) {
					rdbctSignal = false;
					resetActiveThread();
				}
			}
		}
	}
	
	protected boolean processPythonOutput(String inp) {
		String subinp = "";
		if (inp.length() >= 5) {
			subinp = inp.substring(0, 5);
		}
		if (subinp.equals("outp:")) {
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
					feControl.subtractOneFromPendingCounter();
					feControl.addOneToCounter(FEPanel.FAILURE, tokens[1]);
					return false;
				}
			}
			pythonOutpList.get(slot).add(tokens);
			if (params.outputCSVChecked) {
				File f = new File(params.outputCSVName);
				f.setWritable(true, false);
				boolean matchesFeatures = false;
				boolean blankFile = true;
				if (f.exists()) {
					Scanner sc;
					try {
						sc = new Scanner(f);
						if (sc.hasNextLine()) {
							matchesFeatures = true;
							blankFile = false;
							String[] firstLine = sc.nextLine().split(",");
							if (firstLine.length == 6 + params.featureList.length) {
								for (int i = 0; i < params.featureList.length; i++) {
									if (!firstLine[i+6].equals(params.featureList[i][1])) {
										matchesFeatures = false;
									}
								}
							} else {
								matchesFeatures = false;
							}
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
						return false;
					}
					if (!blankFile && !matchesFeatures) {
						try {
							f.delete();
							f.createNewFile();
							blankFile = true;
						} catch (IOException e2) {
							System.out.println("ERROR: Could not delete pre-existing output .csv file.");
							return false;
						}
					}
				} else {
					try {
						f.createNewFile();
					} catch (IOException e2) {
						System.out.println("ERROR: Could not create output .csv file.");
						return false;
					}
				}
				if (blankFile) {
					try {
						String firstLine = "cluster,uid,date,duration,lf,hf";
						for (int i = 0; i < params.featureList.length; i++) {
							firstLine += ","+params.featureList[i][1];
						}
						firstLine += "\n";
						PrintWriter pw = new PrintWriter(f);
						StringBuilder sb = new StringBuilder();
						sb.append(firstLine);
						pw.write(sb.toString());
						pw.flush();
						pw.close();
					} catch (Exception e2) {
						System.out.println("ERROR: Could not write to selected .csv file.");
						return false;
					}
				}
				String outp = "";
				outp += tokens[0].substring(1, tokens[0].length()-1);
				outp += ","+tokens[1];
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date date  = new Date(Long.valueOf(tokens[2]));
				outp += ","+df.format(date);
				for (int i = 3; i < tokens.length; i++) {
					if (tokens[i].length() > 0) {
						if (tokens[i].charAt(0) == '\'') {
							System.out.println("ERROR: "+tokens[1]+" -> Could not process \""+tokens[i]+"\".");
							return false;
						} else if (tokens[i].equals("nan")) {
							System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-3][1]+" = NaN.");
							return false;
						}
						String[] num_tokens = tokens[i].split("e");
						if (num_tokens.length == 1) {
							outp += ","+tokens[i];
						} else if (num_tokens.length == 2) {
							outp += ","+String.valueOf(Double.valueOf(num_tokens[0])*Math.pow(10, Integer.valueOf(num_tokens[1])));
						} else {
							System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-3][1]+" has a non-sequitur value.");
							return false;
						}
					} else {
						System.out.println("ERROR: "+tokens[1]+" -> "+params.featureList[i-3][1]+" has no value.");
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
					System.out.println("ERROR: "+tokens[1]+" -> Could not write row to .csv file.");
					return false;
				}
			}
		}
		return true;
	}
	
	private void addVectorToDataBlock() {
		for (int i = 0; i < pythonOutpList.size(); i++) {
			ArrayList<String[]> currList = new ArrayList<String[]>(pythonOutpList.get(i));
			if (i == activeThread || ccList.get(i).size() > 0 || currList.size() == 0) {
				continue;
			}
			if (currList.get(0).length == 0) {
				pythonOutpList.get(i).clear();
				continue;
			}
			FECallCluster cc = new FECallCluster(currList.size(), feControl.getParams().featureList.length);
			cc.clusterID = currList.get(0)[0];
			for (int j = 0; j < currList.size(); j++) {
				cc.uids[j] = Long.valueOf(currList.get(j)[1]);
				cc.datetimes[j] = Long.valueOf(currList.get(j)[2]);
				cc.durations[j] = (int) Double.valueOf(currList.get(j)[3]).doubleValue();
				cc.lfs[j] = (int) Double.valueOf(currList.get(j)[4]).doubleValue();
				cc.hfs[j] = (int) Double.valueOf(currList.get(j)[5]).doubleValue();
				for (int k = 6; k < currList.get(j).length; k++) {
					cc.featureVector[j][k-6] = Double.valueOf(currList.get(j)[k]);
				}
			}
			if (cc.uids.length > 0) {
				FEDataUnit du = new FEDataUnit(feControl, cc);
				feControl.feProcess.addVectorData(du);
				for (int j = 0; j < cc.uids.length; j++) {
					feControl.subtractOneFromPendingCounter();
					feControl.addOneToCounter(FEPanel.SUCCESS, String.valueOf(cc.uids[j]));
				}
			}
			pythonOutpList.get(i).clear();
		}
	}
	
	public void resetTxtParams() {
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
	
	public void startPrintThreads() {
		printThreadsActive = true;
		ipt = new InputPrintThread();
		ept = new ErrorPrintThread();
		rt = new RunnerThread();
		ipt.start();
		ept.start();
		rt.start();
	}
	
	public void stopPrintThreads() {
		printThreadsActive = false;
		try {
			ipt.join();
			ept.join();
			rt.join();
		} catch(InterruptedException e) {
			// TODO
		}
	}
	
	protected class InputPrintThread extends Thread {
		protected InputPrintThread() {
			//this.run();
		}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					String outpstr = "";
					if (br.ready()) {
						while ((outpstr = br.readLine()) != null) {
							System.out.println("FE IBR: "+outpstr);
							boolean boo = processPythonOutput(outpstr);
							if (outpstr.contains("Error: Could not process ")) {
								//pythonResultsWaitQueue--;
								String uid = outpstr.substring(25);
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
								boolean subd = feControl.subtractOneFromPendingCounter();
								feControl.addOneToCounter(FEPanel.FAILURE, uid);
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
	
	protected class ErrorPrintThread extends Thread {
		protected ErrorPrintThread() {
			//this.run();
		}
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
							if (tokens.length > 0) {
								if (tokens[0].equals(">>>")) {
									break;
								}
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
	
	@Deprecated
	protected class KillerThread extends Thread {
		protected KillerThread() {
			//this.run();
		}
		@Override
		public void run() {
			while(printThreadsActive) {
				pythonCommand("threadList, wavNrList = FEPythonThread.freeThroughList(threadList, wavNrList)");
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
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
	
	public boolean isActive() {
		return active;
	}
	
	public boolean setActive() {
		active = new JarExtractor().extract("src/mirrfFeatureExtractor/FEPythonThread.py",
				feControl.getParams().tempFolder, "FEPythonThread.py", true);
		System.out.println("JarExtractor completed.");
		return active;
	}
	
	public void setInactive(boolean changePanelStatusText) {
		active = false;
	}
}