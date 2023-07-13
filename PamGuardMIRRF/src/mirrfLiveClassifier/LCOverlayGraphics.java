package mirrfLiveClassifier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import PamController.PamControlledUnit;
import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.GeneralProjector.ParameterType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import mirrfFeatureExtractor.FEDataBlock;
import whistlesAndMoans.WhistleMoanControl;

/**
 * The Live Classifier's overlay graphics.
 * Partially copied from whistleAndMoans.CROverlayGraphics.
 * @author Holly LeBlond
 */
public class LCOverlayGraphics extends PamDetectionOverlayGraphics {

	protected LCControl lcControl;
	protected LCDataBlock dataBlock;
	protected boolean isViewer;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 10, 10, true,
			Color.RED, Color.RED); // FIGURE OUT WHAT THIS DOES

	public LCOverlayGraphics(LCControl lcControl, LCDataBlock dataBlock) {
		super(dataBlock, new PamSymbol(defaultSymbol));
		this.lcControl = lcControl;
		this.dataBlock = dataBlock;
		isViewer = lcControl.isViewer();
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (lcControl.getParams().printJava)
			System.out.println("REACHED drawDataUnit");
		return drawOnSpectrogram(g, pamDataUnit, generalProjector);
	}
	
	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (lcControl.getParams().printJava)
			System.out.println("REACHED drawOnSpectrogram");
		LCDataUnit lcDataUnit = (LCDataUnit) pamDataUnit;
//		setLineColor(Color.RED);
		// use the super class to draw a rectangle. 
		Rectangle r = null;// super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		drawShape(g, lcDataUnit, generalProjector);
		return r;
	}
	
	/**
	 * @return The FFT data block from the source Feature Extractor's source.
	 */
	protected FFTDataBlock retrieveFFTDataBlock() {
		FFTDataBlock fftDB = null;
		FEDataBlock vectorDB = (FEDataBlock) lcControl.getProcess().getParentDataBlock();
		WhistleMoanControl wmc = null;
		boolean breakAll = false;
		try {
			for (int i = 0; i < lcControl.getPamController().getNumControlledUnits(); i++) {
				PamControlledUnit pcu = lcControl.getPamController().getControlledUnit(i);
				for (int j = 0; j < pcu.getNumPamProcesses(); j++) {
					PamProcess pp = pcu.getPamProcess(j);
					for (int k = 0; k < pp.getNumOutputDataBlocks(); k++) {
						//System.out.println(pp.getOutputDataBlock(k).getLongDataName()+" -> "+vectorDB.getInputDataName());
						if (pp.getOutputDataBlock(k).getLongDataName().equals(vectorDB.getInputDataName())) {
							wmc = (WhistleMoanControl) pcu;
							for (int l = 0; l < lcControl.getPamController().getFFTDataBlocks().size(); l++) {
								FFTDataBlock fdb = (FFTDataBlock) lcControl.getPamController().getFFTDataBlock(l);
								//System.out.println("\t"+fdb.getLongDataName()+" -> "+wmc.getWhistleToneParameters().getDataSource());
								if (fdb.getLongDataName().equals(wmc.getWhistleToneParameters().getDataSource())) {
									fftDB = fdb;
									break;
								}
							}
							breakAll = true;
							break;
						}
					}
					if (breakAll) break;
				}
				if (breakAll) break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fftDB;
	}
	
	protected Rectangle drawShape(Graphics g, LCDataUnit dataUnit, GeneralProjector generalProjector) {
		if (lcControl.getParams().printJava)
			System.out.println("REACHED drawShape");
		try {
			LCCallCluster cc = dataUnit.getCluster();
			
			long first = cc.getStartAndEnd()[0];
			long last = cc.getStartAndEnd()[1];
			int lowest = cc.getFreqLimits()[0];
			int highest = cc.getFreqLimits()[1];
			
			FFTDataBlock fftDB = retrieveFFTDataBlock();
			
			if (fftDB == null) {
				System.out.println("MIRRF Live Classifier: Could not find associated FFT data block.");
				return null;
			}
			
			float sr = fftDB.getSampleRate();
			int fftLength = fftDB.getFftLength();
			int fftHop = fftDB.getFftHop();
			double binStep = fftHop / sr * 1000;
			Coordinate3d c3d = generalProjector.getCoord3d(first-(Integer) fftHop/5.12, lowest-(Integer) fftLength/10.24, 0);
			Point p1 = c3d.getXYPoint();
			c3d = generalProjector.getCoord3d(last+(Integer) fftHop/5.12, highest+(Integer) fftLength/10.24, 0);
			Point p2 = c3d.getXYPoint();
			
			// Kudos to Edwin Buck @ https://stackoverflow.com/questions/16995308/can-you-increase-line-thickness-when-using-java-graphics-for-an-applet-i-dont
			Graphics2D g2 = (Graphics2D) g;
		    g2.setStroke(new BasicStroke(3));
			g2.setColor(Color.GRAY);
			if (lcControl.getParams().labelColours.containsKey(cc.getPredictedSpeciesString())) {
				g2.setColor(lcControl.getParams().labelColours.get(cc.getPredictedSpeciesString()));
			}
			
			c3d = generalProjector.getCoord3d(first-(Integer) fftHop/5.12, highest+(Integer) fftLength/2.56, 0);
			Point p3 = c3d.getXYPoint();
			c3d = generalProjector.getCoord3d(first-(Integer) fftHop/5.12, highest+(Integer) fftLength/5.12, 0);
			Point p4 = c3d.getXYPoint();
			if (!isViewer) {
				g2.drawString(cc.getPredictedSpeciesString(), p4.x, p4.y);
			} else {
				String actualSpecies = cc.getActualSpeciesString();
				if (actualSpecies.endsWith(" *")) {
					actualSpecies = actualSpecies.substring(0, actualSpecies.length()-2);
				}
				if (actualSpecies.equals("Other")) {
					g2.setColor(Color.GRAY);
					g2.drawString(cc.getActualSpeciesString()+" "+new String(Character.toChars(0x2192))+" "+cc.getPredictedSpeciesString(), p4.x, p4.y);
				} else if (actualSpecies.equals("Unlabelled")) {
					g2.setColor(Color.GRAY);
					g2.drawString("? "+new String(Character.toChars(0x2192))+" "+cc.getPredictedSpeciesString(), p4.x, p4.y);
				} else if (actualSpecies.equals(cc.getPredictedSpeciesString())) {
					g2.setColor(Color.GREEN);
					g2.drawString(new String(Character.toChars(0x221A)), p3.x, p3.y);
					g2.setColor(lcControl.getParams().labelColours.get(actualSpecies));
					g2.drawString(cc.getActualSpeciesString()+" "+new String(Character.toChars(0x2192))+" "+cc.getPredictedSpeciesString(), p4.x, p4.y);
				} else {
					g2.setColor(Color.RED);
					g2.drawString("X", p3.x, p3.y);
					g2.setColor(lcControl.getParams().labelColours.get(actualSpecies));
					g2.drawString(cc.getActualSpeciesString()+" "+new String(Character.toChars(0x2192))+" "+cc.getPredictedSpeciesString(), p4.x, p4.y);
				}
			}
			g2.setColor(Color.GRAY);
			if (lcControl.getParams().labelColours.containsKey(cc.getPredictedSpeciesString())) {
				g2.setColor(lcControl.getParams().labelColours.get(cc.getPredictedSpeciesString()));
			}
			g2.drawLine(p1.x, p1.y, p1.x, p2.y);
			g2.drawLine(p1.x, p2.y, p2.x, p2.y);
			g2.drawLine(p2.x, p2.y, p2.x, p1.y);
			g2.drawLine(p2.x, p1.y, p1.x, p1.y);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; //???????????
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		String str = dataUnit.getSummaryString();
		return str;
	}
	
	@Override
	public boolean canDraw(GeneralProjector generalProjector) {
		boolean outp = false;
		if (generalProjector.getParameterTypes()[0] == ParameterType.TIME && generalProjector.getParameterTypes()[1] == ParameterType.FREQUENCY) {
			outp = true;
		}
		return outp;
	}
}