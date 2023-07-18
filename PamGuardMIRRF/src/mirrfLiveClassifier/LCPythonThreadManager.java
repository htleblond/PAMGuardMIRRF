package mirrfLiveClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.docx4j.wml.Br;

import mirrf.MIRRFJarExtractor;

/**
 * Creates an instance of a Python interpreter, sends clips to the
 * Python script for processing, and manages communication between
 * Java and Python.
 * @author Holly LeBlond
 */
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
	
	/**
	 * Creates the Python interpreter and immediately begins importing libraries.
	 */
	protected void initializePython() throws Exception {
		try {
			commandList = new ArrayList<String>();
			LCParameters lcParams = lcControl.getParams();
			ProcessBuilder pb = new ProcessBuilder("python", "-i");
			pr = pb.start();
		    br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		    ebr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
	        bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
	        
	        startPrintThreads();
	        
	        pythonCommand("import os", getControl().getParams().printInput);
	        pythonCommand("os.chdir(r\""+pathname+"\")", getControl().getParams().printInput);
	        pythonCommand("os.getcwd()", getControl().getParams().printInput);
	        pythonCommand("import numpy as np", getControl().getParams().printInput);
	        String pyParams = lcControl.getParams().outputPythonParamsToText();
	        if (lcParams.getFeatureList().size() > 0) {
	        	pyParams += "\""+lcParams.getFeatureList().get(0)+"\"";
				for (int i = 1; i < lcParams.getFeatureList().size(); i++) {
					pyParams += ",\""+lcParams.getFeatureList().get(i)+"\"";
				}
			}
	        pyParams += "]";
	        pyParams += "]";
	        if (pyParams.length() > 0) {
	            pythonCommand("txtParams = "+pyParams, getControl().getParams().printInput);
	        } else {
	            pythonCommand("txtParams = []", getControl().getParams().printInput);
	        }
	        pythonCommand("import sys", getControl().getParams().printInput);
	        pythonCommand("import gc", getControl().getParams().printInput);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Iterates through and empties the command queue and sends commands to the Python interpreter.
	 */
	protected class PythonInterpreterThread extends Thread {
		public PythonInterpreterThread() {}
		@Override
		public void run() {
			try {
				// Kudos to this: https://stackoverflow.com/questions/25041529/how-to-run-the-python-interpreter-and-get-its-output-using-java
				if (setActive()) {
					initializePython();
			        pythonCommand("import "+scriptClassName, getControl().getParams().printInput);
			        
			        while (active || commandList.size() > 0) {
			        	if (commandList.size() > 0) {
			        		pythonCommand(commandList.get(0), getControl().getParams().printInput);
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
				e.printStackTrace(System.out);
			}
		}
	}
	
	/**
	 * Adds a command to the queue.
	 */
	public void addCommand(String inp) {
		commandList.add(inp);
	}
	
	/**
	 * Actually sends the command to the interpreter.
	 * @param command
	 * @param toPrint - If true, prints the command to the console
	 */
	public void pythonCommand(String command, boolean toPrint) {
		if (bw != null) {
			try {
				if (command != null) {
					if (toPrint) System.out.println("COMMAND: "+command);
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
	
	/**
	 * Starts the print threads.
	 */
	public void startPrintThreads() {
		printThreadsActive = true;
		ipt = new InputPrintThread();
		ept = new ErrorPrintThread();
		ipt.start();
		ept.start();
	}
	
	/**
	 * Parses output from the InputPrintThread (which should be non-error output from the Python interpreter).
	 * @param outpstr - The output string
	 * @param print - If true, prints the output string to the console
	 */
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
	
	/**
	 * Passes along non-error output from the Python interpreter.
	 */
	protected class InputPrintThread extends Thread {
		protected InputPrintThread() {}
		@Override
		public void run() {
			while(printThreadsActive) {
				try {
					//System.out.println("Still in loop: "+String.valueOf(Math.random())); // (For testing if loop gets stuck.)
					String outpstr = "";
					if (br.ready()) {
						while (br.ready() && printThreadsActive && (outpstr = br.readLine()) != null) {
							if (outpstr != null) {
								parseIPTOutput(outpstr, getControl().getParams().printOutput);
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
	
	/**
	 * Parses output from the ErrorPrintThread. Always prints it to the console.
	 * @param outpstr - The output string
	 */
	protected void parseEPTOutput(String outpstr) {
		System.out.println("LC EBR: "+outpstr);
	}
	
	/**
	 * Passes along error output from the Python interpreter.
	 */
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
	
	/**
	 * @return Whether or not all commands have finished processing in the Python interpreter.
	 * (Assuming the "finished" boolean has been correctly set.)
	 */
	public boolean getFinished() {
		return finished;
	}
	
	/**
	 * Sets the "finished" boolean.
	 * If true, other classes will assume that the interpreter has finished processing everything.
	 */
	public void setFinished(boolean inp) {
		finished = inp;
	}
	
	protected LCControl getControl() {
		return lcControl;
	}
	
	/**
	 * Extracts the Python script from the plugin's .jar file and turns the thread manager "on".
	 * @return True if the script was successfully extracted. Otherwise, false.
	 */
	public boolean setActive() {
		active = new MIRRFJarExtractor().extract("src/mirrfLiveClassifier/LCPythonScript.py",
				lcControl.getParams().tempFolder, "LCPythonScript.py", true);
		if(getControl().getParams().printJava)
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