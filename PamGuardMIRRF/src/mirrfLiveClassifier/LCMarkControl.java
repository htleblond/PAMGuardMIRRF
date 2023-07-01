package mirrfLiveClassifier;

import annotationMark.spectrogram.*;

import java.util.ArrayList;

import javax.swing.JPopupMenu;

import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import annotationMark.MarkDataUnit;
import detectiongrouplocaliser.DetectionGroupSummary;

/**
 * The drag selection tool.
 * Based off of annotationMark.SpectrogramAnnotationModule, but edited to select rows
 * in resultsTable in the LCPanel rather than opening an annotation dialog.
 * @author Holly LeBlond (with some leftover code from whoever made the SpectrogramAnnotationModule)
 */
public class LCMarkControl extends SpectrogramAnnotationModule {
	
	protected NewObserver newObserver;
	
	private LCControl lcControl;
	private LCPanel outputPanel;
	
	public LCMarkControl(LCControl lcControl, LCPanel lcPanel) {
		super("MIRRF Live Classifier call cluster selector");
		this.lcControl = lcControl;
		this.outputPanel = lcPanel;
		
		annotationHandler = new SpectrogramMarkAnnotationHandler(this, getAnnotationDataBlock());
		ArrayList<OverlayMarkObserver> observerlist = OverlayMarkObservers.singleInstance().getMarkObservers();
		OverlayMarkObservers.singleInstance().removeObserver(observerlist.get(observerlist.lastIndexOf(displayObserver)));
		OverlayMarkObservers.singleInstance().addObserver(newObserver = new NewObserver());
	}
	
	/**
	 * This was copied from the DisplayObserver class in SpectrogramAnnotationModule and modified for contour selection purposes.
	 */
	public class NewObserver implements OverlayMarkObserver {

		private final ParameterType[] parameterTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
		private MarkDataUnit existingUnit;

		@Override
		public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
			if (overlayMark == null) {
				return false;
			}
			long t0 = (long) overlayMark.getCoordinate(0).getCoordinate(0);
			double f0 =  overlayMark.getCoordinate(0).getCoordinate(1);
			long t2 = (long) overlayMark.getLastCoordinate().getCoordinate(0);
			double f2 = overlayMark.getLastCoordinate().getCoordinate(1);
			if (mouseEvent.isPopupTrigger() && existingUnit != null) {
				return true;
			}
			if (markStatus == MARK_END) {
				outputPanel.getTable().clearSelection();
				int scrollint = -1;
				ArrayList<String> toHighlight = new ArrayList<String>();
				LCDataBlock db = lcControl.getProcess().resultsDataBlock;
				for (int i = 0; i < db.getUnitsCount(); i++) {
					LCDataUnit du = db.getDataUnit(i, db.REFERENCE_CURRENT);
					LCCallCluster cc = du.getCluster();
					long start = cc.getStartAndEnd()[0];
					long end = cc.getStartAndEnd()[1];
					double low = cc.getFreqLimits()[0];
					double high = cc.getFreqLimits()[1];
					
					if (((t0 <= start && (t2 > start || t2 >= end)) || (t2 >= end && (t0 < end || t0 <= start)) || (t0 >= start && t2 <= end)) &&
						((f0 <= low && (f2 > low || f2 >= high)) || (f2 >= high && (f0 < high || f0 <= low)) || (f0 >= low && f2 <= high))) {
						toHighlight.add(String.valueOf(cc.clusterID)+", "+lcControl.convertLocalLongToUTC(cc.getStartAndEnd()[0]));
					}
				}
				for (int i = 0; i < outputPanel.getTable().getRowCount(); i++) {
					if (toHighlight.contains(outputPanel.getTable().getValueAt(i, 0)+", "+outputPanel.getTable().getValueAt(i, 1))) {
						outputPanel.getTable().addRowSelectionInterval(i, i);
						if (scrollint == -1) {
							scrollint = i;
						}
					}
				}
				if (scrollint != -1) {
					outputPanel.getTable().scrollRectToVisible(outputPanel.getTable().getCellRect(outputPanel.getTable().getRowCount()-1, 0, true));
					outputPanel.getTable().scrollRectToVisible(outputPanel.getTable().getCellRect(scrollint, 0, true));
				}
			}
			return false;
		}

		@Override
		public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
			return null;
		}

		public MarkDataUnit getMarkDataUnit(DetectionGroupSummary markSummaryData) {
			return null;
		}

		@Override
		public ParameterType[] getRequiredParameterTypes() {
			return parameterTypes;
		}

		@Override
		public String getObserverName() {
			return getUnitName();
		}

		@Override
		public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
			return null;
		}

		@Override
		public String getMarkName() {
			return getMarkType();
		}
	}
}