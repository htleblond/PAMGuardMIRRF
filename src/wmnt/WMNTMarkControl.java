package wmnt;

import annotationMark.spectrogram.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.text.*;
import java.util.Date;

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
 * The WMNT's drag selection tool. Dragging the box over contours in the spectrogram
 * will select their corresponding rows in the table.
 * @author Taylor LeBlond
 */
public class WMNTMarkControl extends SpectrogramAnnotationModule {
	
	protected NewObserver newObserver;
	
	private WMNTPanel outputPanel;
	
	public WMNTMarkControl(String unitName, WMNTPanel wmntPanel) {
		super(unitName);
		outputPanel = wmntPanel;
		
		annotationHandler = new SpectrogramMarkAnnotationHandler(this, getAnnotationDataBlock());
		ArrayList<OverlayMarkObserver> observerlist = OverlayMarkObservers.singleInstance().getMarkObservers();
		OverlayMarkObservers.singleInstance().removeObserver(observerlist.get(observerlist.lastIndexOf(displayObserver)));
		OverlayMarkObservers.singleInstance().addObserver(newObserver = new NewObserver());
	}

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
				outputPanel.ttable.clearSelection();
				int scrollint = -1;
				long ms = 0;
				int numrows = outputPanel.ttable.getRowCount();
				SimpleDateFormat currdateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSS");
				currdateformat.setTimeZone(TimeZone.getTimeZone("UTC"));
				for (int i = 0; i < numrows; i++) {
					String stamp = String.valueOf(outputPanel.ttable.getValueAt(i, 1));
					try {
						Date d = currdateformat.parse(stamp);
						ms = d.getTime();
						if (t0 < ms && ms < t2) {
							if (outputPanel.getCurrFormat() >= 4) {
								int lf = Integer.parseInt(String.valueOf(outputPanel.ttable.getValueAt(i, 2)));
								int hf = Integer.parseInt(String.valueOf(outputPanel.ttable.getValueAt(i, 3)));
								if (f0 < lf && hf < f2) {
									outputPanel.ttable.addRowSelectionInterval(i, i);
									if (scrollint == -1) {
										scrollint = i;
									}
								}
							} else {
								outputPanel.ttable.addRowSelectionInterval(i, i);
								if (scrollint == -1) {
									scrollint = i;
								}
							}
						}
					} catch (ParseException e2) {
						System.out.println(e2);
					}
				}
				if (scrollint != -1) {
					outputPanel.ttable.scrollRectToVisible(outputPanel.ttable.getCellRect(numrows-1, 0, true));
					outputPanel.ttable.scrollRectToVisible(outputPanel.ttable.getCellRect(scrollint, 0, true));
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