package mirrfLiveClassifier;

import java.awt.Frame;
import java.awt.Window;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

public class LCWaitingDialogThread extends Thread {
	protected LCControl lcControl;
	//protected volatile boolean running;
	protected Window parentFrame;
	protected String message;
	protected volatile LCWaitingDialog dialog;
	
	public LCWaitingDialogThread(Window parentFrame, LCControl lcControl, String message) {
		//this.running = false;
		this.lcControl = lcControl;
		this.parentFrame = parentFrame;
		this.message = message;
	}
		
	@Override
	public void run() {
		//running = true;
		dialog = new LCWaitingDialog(parentFrame, lcControl, message);
		dialog.setVisible(true);
		//while (running) sleep(50);
		//dialog.setVisible(false);
	}
	
	protected void sleep(int ms) {
		try {
			TimeUnit.MILLISECONDS.sleep(ms);
		} catch (Exception e) {
			System.out.println("Sleep exception.");
			e.printStackTrace();
		}
	}
	
	public void halt() {
		//running = false;
		if (dialog != null) dialog.setVisible(false);
	}
}