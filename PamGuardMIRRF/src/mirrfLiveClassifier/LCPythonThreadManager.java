package mirrfLiveClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import mirrfFeatureExtractor.JarExtractor;

public class LCPythonThreadManager{
	protected LCControl lcControl;
	protected String scriptClassName = "LCPythonScript";
	protected String pathname;
	protected volatile boolean printThreadsActive;
	protected BufferedWriter bw = null;
	protected BufferedReader br = null;
	protected BufferedReader ebr = null;
	protected InputPrintThread ipt = null;
	protected ErrorPrintThread ept = null;
	protected volatile ArrayList<String> commandList;
	protected Process pr;
	protected volatile boolean active;
	protected PythonInterpreterThread pit;
	protected volatile boolean finished;
	
	public LCPythonThreadManager(LCControl lcControl) {
		this.lcControl = lcControl;
		init();
	}
	
	protected void init() {
		this.printThreadsActive = false;
		this.finished = true;
		this.commandList = new ArrayList<String>();
		String defpathname = lcControl.getParams().tempFolder;
		this.pathname = "";
		for (int i = 0; i < defpathname.length(); i++) {
			if (!defpathname.substring(i, i+1).equals("\\")) {
				this.pathname += defpathname.substring(i, i+1);
			} else {
				this.pathname += "/";
			}
		}
		
		this.pit = new PythonInterpreterThread();
		this.pit.start();
	}
	
	protected void initializePython() throws Exception {
		try {
			ProcessBuilder pb = new ProcessBuilder("python", "-i");
			pr = pb.start();
		    br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		    ebr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
	        bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
	        
	        startPrintThreads();
	        
	        pythonCommand("import os");
	        pythonCommand("os.chdir(r\""+pathname+"\")");
	        pythonCommand("os.getcwd()");
	        pythonCommand("import numpy as np");
	        String pyParams = lcControl.getParams().outputPythonParamsToText();
	        if (lcControl.getFeatureList().size() > 0) {
	        	pyParams += "\""+lcControl.getFeatureList().get(0)+"\"";
				for (int i = 1; i < lcControl.getFeatureList().size(); i++) {
					pyParams += ",\""+lcControl.getFeatureList().get(i)+"\"";
				}
			}
	        pyParams += "]";
	        pyParams += "]";
	        if (pyParams.length() > 0) {
	            pythonCommand("txtParams = "+pyParams);
	        } else {
	            pythonCommand("txtParams = []");
	        }
	        pythonCommand("import sys");
	        pythonCommand("import gc");
		} catch (Exception e) {
			throw e;
		}
	}
	
	protected class PythonInterpreterThread extends Thread {
		protected PythonInterpreterThread() {}
		@Override
		public void run() {
			try {
				// Kudos to this: https://stackoverflow.com/questions/25041529/how-to-run-the-python-interpreter-and-get-its-output-using-java
				if (setActive()) {
					initializePython();
			        pythonCommand("import "+scriptClassName);
			        
			        while (active || commandList.size() > 0) {
			        	if (commandList.size() > 0) {
			        		pythonCommand(commandList.get(0));
				            commandList.remove(0);
			        	}
			        	try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (Exception e) {
							System.out.println("Sleep exception.");
							e.printStackTrace();
						}
			        }
				}
			} catch (Exception e) {
				System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
				e.printStackTrace(System.out);
			}
		}
	}
	
	public void addCommand(String inp) {
		commandList.add(inp);
	}
	
	public void pythonCommand(String command) {
		if (bw != null) {
			try {
				System.out.println("COMMAND: "+command);
				if (command != null) {
					bw.write(command);
					bw.newLine();
					bw.flush();
				}
			} catch (IOException e) {
				System.out.println("IOException in pythonCommand().");
			}
		} else {
			System.out.println("ERROR: BufferedWriter is null.");
		}
	}
	
	public void startPrintThreads() {
		printThreadsActive = true;
		ipt = new InputPrintThread();
		ept = new ErrorPrintThread();
		ipt.start();
		ept.start();
	}
	
	protected void parseIPTOutput(String outpstr, boolean print) {
		if (print) System.out.println("LC IBR: "+outpstr);
		if (outpstr.equals("Initialization succeeded")) {
			lcControl.setTrainingSetStatus(true);
			lcControl.setModelFittingStatus(true);
		} else if (outpstr.equals("Initialization failed")) {
			lcControl.setTrainingSetStatus(false);
			lcControl.setModelFittingStatus(true);
		}
		if (outpstr.substring(0,6).equals("RESULT")) {
			lcControl.getProcess().addResultsData(outpstr.substring(10));
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
	
	protected class InputPrintThread extends Thread {
		protected InputPrintThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					String outpstr = "";
					if (br.ready()) {
						while (br.ready() && printThreadsActive && (outpstr = br.readLine()) != null) {
							if (outpstr != null) {
								parseIPTOutput(outpstr, true); // TODO Set to false after testing.
								try {
									TimeUnit.MILLISECONDS.sleep(50);
								} catch (Exception e) {
									System.out.println("Sleep exception.");
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					// TODO
				}
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (Exception e) {
					System.out.println("Sleep exception.");
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void parseEPTOutput(String outpstr) {
		System.out.println("LC EBR: "+outpstr);
		// TODO
	}
	
	protected class ErrorPrintThread extends Thread {
		protected ErrorPrintThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					String outpstr = "";
					if (ebr.ready()) {
						while (ebr.ready() && printThreadsActive && (outpstr = ebr.readLine()) != null) {
							if (outpstr != null) {
								parseEPTOutput(outpstr);
								try {
									TimeUnit.MILLISECONDS.sleep(50);
								} catch (Exception e) {
									System.out.println("Sleep exception.");
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					// TODO
				}
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (Exception e) {
					System.out.println("Sleep exception.");
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean getFinished() {
		return finished;
	}
	
	public void setFinished(boolean inp) {
		finished = inp;
	}
	
	protected LCControl getControl() {
		return lcControl;
	}
	
	public boolean setActive() {
		active = new JarExtractor().extract("src/mirrfLiveClassifier/LCPythonScript.py",
				lcControl.getParams().tempFolder, "LCPythonScript.py", true);
		System.out.println("JarExtractor completed.");
		return active;
	}
	
	// I'm not really sure if this is even useful.
	@Deprecated
	public void halt() {
		commandList.clear();
		active = false;
		printThreadsActive = false;
	}
}