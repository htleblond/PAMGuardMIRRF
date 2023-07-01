package mirrfFeatureExtractor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * The dialog for choosing which features are in the vector.
 * @author Holly LeBlond
 */
//@SuppressWarnings("serial")
public class FEFeatureDialog extends PamDialog {
	
	protected FEControl feControl;
	protected FESettingsDialog settingsDialog;
	protected DefaultTableModel dtmodel;
	
	protected JPanel p_left;
	protected DefaultListModel<String> dlmodel;
	protected JList<String> flist;
	
	protected JPanel p_right;
	protected JPanel p_cards;
	protected JPanel p_blank;
	protected JPanel p_amp;
	//protected JPanel p_amp_sd; // Not actually possible to retrieve
	//protected JPanel p_ampslope; // Not actually possible to retrieve
	protected JPanel p_duration;
	protected JPanel p_freq_hd;
	//protected JPanel p_freq_sd; // Apparently not possible to retrieve
	protected JPanel p_frange;
	protected JPanel p_fslope_hd;
	//protected JPanel p_fslope_sd; // Apparently not possible to retrieve
	protected JPanel p_mfcc;
	protected JPanel p_poly;
	protected JPanel p_rms;
	protected JPanel p_bandwidth;
	protected JPanel p_centroid;
	protected JPanel p_contrast;
	protected JPanel p_flatness;
	protected JPanel p_flux;
	protected JPanel p_specmag;
	protected JPanel p_rolloff;
	protected JPanel p_yin;
	protected JPanel p_harmonics;
	protected JPanel p_zcr;
	
	protected JButton addButton;
	
	//protected JComboBox amp_sd_box;
	
	//protected JComboBox ampslope_box;
	
	protected JComboBox<String> freq_hd_box;
	
	//protected JComboBox freq_sd_box;
	
	//protected JComboBox fslope_sd_box;
	
	protected JComboBox<String> mfcc_box;
	protected JTextField mfcc_n_field;
	protected JComboBox<String> mfcc_selector_box;
	
	protected JComboBox<String> poly_box;
	protected JTextField poly_order_field;
	protected JComboBox<String> poly_selector_box;
	
	protected JComboBox<String> rms_box;
	
	protected JComboBox<String> bandwidth_box;
	protected JCheckBox bandwidth_normalize_check;
	protected JTextField bandwidth_power_field;
	
	protected JComboBox<String> centroid_box;
	
	protected JComboBox<String> contrast_box;
	protected JTextField contrast_freq_field;
	protected JTextField contrast_bands_field;
	protected JRadioButton contrast_linear_rb;
	protected JRadioButton contrast_log_rb;
	protected ButtonGroup contrast_rbg;
	
	protected JComboBox<String> flatness_box;
	protected JTextField flatness_power_field;
	
	protected JComboBox<String> flux_box;
	
	protected JComboBox<String> specmag_box;
	protected JTextField specmag_min_field;
	protected JTextField specmag_max_field;
	protected JCheckBox specmag_normalize_check;
	
	protected JComboBox<String> rolloff_box;
	protected JTextField rolloff_threshold_field;
	
	protected JComboBox<String> yin_box;
	protected JTextField yin_min_field;
	protected JTextField yin_max_field;
	
	protected JComboBox<String> harmonics_box;
	protected JTextField harmonics_n_field;
	protected JTextField harmonics_buffer_field;
	protected JTextField harmonics_min_field;
	protected JTextField harmonics_max_field;
	protected JCheckBox harmonics_normalize_check;
	
	protected JComboBox<String> zcr_box;
	
	public FEFeatureDialog(Window parentFrame, FEControl feControl, FESettingsDialog settingsDialog, DefaultTableModel dtmodel) {
		super(parentFrame, "MIRRF Feature Extractor", true);
		this.feControl = feControl;
		this.settingsDialog = settingsDialog;
		this.dtmodel = dtmodel;
		
		
		this.getOkButton().setVisible(false);
		this.getCancelButton().setText("Done");
		this.getDefaultButton().setVisible(false);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(""));
		GridBagConstraints b = new PamGridBagContraints();
		b.gridy = 0;
		b.gridx = 0;
		b.fill = GridBagConstraints.BOTH;
		b.anchor = GridBagConstraints.CENTER;
		JPanel subPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		
		p_left = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		//p_left.add(new JLabel("Select a feature to add to vector:"), c);
		//c.gridy++;
		String[] featureNames = new String[] {"Amplitude","Duration","Frequency","Frequency range","Frequency slope",
				"Mel-frequency cepstral coefficients","Polynomial features","Root mean square",
				"Spectral bandwidth","Spectral centroid","Spectral contrast","Spectral flatness","Spectral flux (onset strength)",
				"Spectral magnitude","Spectral rolloff","YIN fundamental frequency","YIN harmonics","Zero-crossing rate"};
		dlmodel = new DefaultListModel<String>();
		for (int i = 0; i < featureNames.length; i++) {
			dlmodel.addElement(featureNames[i]);
		}
		flist = new JList<String>(dlmodel);
		flist.setSize(200, 300);
		flist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		flist.setLayoutOrientation(JList.VERTICAL);
		JScrollPane sp = new JScrollPane(flist);
		sp.setPreferredSize(flist.getSize());
		p_left.add(sp, c);
		subPanel.add(p_left);
		
		p_right = new JPanel(new BorderLayout());
		p_cards = new JPanel(new CardLayout());
		//subPanel.add(p_cards);
		
		//Object[][] test = {{"",""},{""}};
		
		CardLayout cl = (CardLayout) p_cards.getLayout();
		
		p_blank = constructFeaturePanel(makeHTML("Select a feature to add to the vector."),
				new Object[0][0],
				false);
		p_cards.add(p_blank,"");
		cl.show(p_cards,"");
		
		p_amp = constructFeaturePanel(makeHTML("Amplitude value (in dB re SPSL) taken directly from contour header data."),
				new Object[0][0],
				false);
		p_cards.add(p_amp,featureNames[0]);
		
	/*	amp_sd_box = makeOutputBox();
		p_amp_sd = constructFeaturePanel(makeHTML("Parses through amplitude values (in dB re SPSL) from contour slice data."),
				new Object[][] {{"Output:", amp_sd_box}},
				false);
		p_cards.add(p_amp_sd,featureNames[1]); */
		
	/*	ampslope_box = makeOutputBox();
		p_ampslope = constructFeaturePanel(makeHTML("Parses through amplitude values (in dB re SPSL) from contour slice data and calculates "
				+ "slope values for each frame."),
				new Object[][] {{"Output:", ampslope_box}},
				false);
		p_cards.add(p_ampslope,featureNames[2]); */
		
		p_duration = constructFeaturePanel(makeHTML("Duration value (in milliseconds) taken directly from contour header data."),
				new Object[0][0],
				false);
		p_cards.add(p_duration,featureNames[1]);
		
		freq_hd_box = makeOutputBox(new String[]{"Minimum","Maximum"});
		p_freq_hd = constructFeaturePanel(makeHTML("Minimum or maximum frequency value (in Hz) taken directly from contour header data."),
				new Object[][] {{"Output:", freq_hd_box}},
				false);
		p_cards.add(p_freq_hd,featureNames[2]);
		
	/*	freq_sd_box = makeOutputBox();
		p_freq_sd = constructFeaturePanel(makeHTML("Parses through frequency values (in Hz) from contour slice data."),
				new Object[][] {{"Output:", freq_sd_box}},
				false);
		p_cards.add(p_freq_sd,featureNames[3]); */
		
		p_frange = constructFeaturePanel(makeHTML("Difference between maximum and minimum frequency values (in Hz) from contour header data."),
				new Object[0][0],
				false);
		p_cards.add(p_frange,featureNames[3]);
		
		p_fslope_hd = constructFeaturePanel(makeHTML("Maximum frequency value minus minimum frequency value, divided by duration value, "
				+ "all from contour header data (in Hz per second)."),
				new Object[0][0],
				false);
		p_cards.add(p_fslope_hd,featureNames[4]);
		
	/*	fslope_sd_box = makeOutputBox();
		p_fslope_sd = constructFeaturePanel(makeHTML("Parses through frequency values (in Hz) from contour slice data and calculates slope values "
				+ "for each frame."),
				new Object[][] {{"Output:", fslope_sd_box}},
				false);
		p_cards.add(p_fslope_sd,featureNames[6]); */
		
		mfcc_box = makeOutputBox();
		mfcc_n_field = new JTextField(5);
		mfcc_n_field.setDocument(fieldToBoxFilter(mfcc_n_field, "mfcc"));
		mfcc_n_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mfccBoxUpdate();
			}
		});
		// Kudos to this: https://stackoverflow.com/questions/6088571/can-i-catch-the-event-of-exiting-a-jtextfield
		mfcc_n_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	mfccBoxUpdate();
		    }
		});
		mfcc_selector_box = makeOutputBox(new String[] {""}) ;
		mfcc_n_field.setText("12");
		mfccBoxUpdate();
		p_mfcc = constructFeaturePanel(makeHTML("Calculates Mel-frequency cepstral coefficients (MFCCs) from the audio clip. Can produce output value "
				+ "for all coefficients or one specific coefficient."),
				new Object[][] {{"Output:", mfcc_box},{"Number of MFCCs:",mfcc_n_field},{"Selected coefficient:",mfcc_selector_box}},
				true);
		p_cards.add(p_mfcc,featureNames[5]);
		
		poly_box = makeOutputBox();
		poly_order_field = new JTextField(5);
		poly_order_field.setDocument(fieldToBoxFilter(poly_order_field, "poly"));
		poly_order_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				polyBoxUpdate();
			}
		});
		poly_order_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	polyBoxUpdate();
		    }
		});
		poly_selector_box = new JComboBox<String>();
		poly_order_field.setText("1");
		polyBoxUpdate();
		p_poly = constructFeaturePanel(makeHTML("Get coefficients of fitting an nth-order polynomial to the columns of a spectrogram.\n\n"
				+ "(From Librosa documentation: https://librosa.org/doc/main/generated/librosa.feature.poly_features.html)"),
				new Object[][] {{"Output:",poly_box},{"Order:",poly_order_field},{"Selected coefficient:",poly_selector_box}},
				true);
		p_cards.add(p_poly,featureNames[6]);
		
		rms_box = makeOutputBox();
		p_rms = constructFeaturePanel(makeHTML("Calculates root mean square (RMS) values directly from the audio of the clip."),
				new Object[][] {{"Output:", rms_box}},
				false);
		p_cards.add(p_rms,featureNames[7]);
		
		bandwidth_box = makeOutputBox();
		bandwidth_power_field = new JTextField(5);
		bandwidth_power_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(bandwidth_power_field, 1);
			}
		});
		bandwidth_power_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(bandwidth_power_field, 1);
		    }
		});
		bandwidth_power_field.setDocument(JIntFilter());
		bandwidth_power_field.setText("2");
		bandwidth_normalize_check = new JCheckBox();
		bandwidth_normalize_check.setText("Normalize");
		p_bandwidth = constructFeaturePanel(makeHTML("Compute p’th-order spectral bandwidth.\n\n"
				+ "(From Librosa documentation: https://librosa.org/doc/main/generated/librosa.feature.spectral_bandwidth.html)"),
				new Object[][] {{"Output:", bandwidth_box},{"Power:",bandwidth_power_field},{bandwidth_normalize_check}},
				false);
		p_cards.add(p_bandwidth,featureNames[8]);
		
		centroid_box = makeOutputBox();
		p_centroid = constructFeaturePanel(makeHTML("Calculates the \"centre of mass\" of the spectrum of an audio clip."),
				new Object[][] {{"Output:", centroid_box}},
				false);
		p_cards.add(p_centroid,featureNames[9]);
		
		contrast_box = makeOutputBox();
		contrast_freq_field = new JTextField(5);
		contrast_freq_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(contrast_freq_field, 1);
			}
		});
		contrast_freq_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(contrast_freq_field, 1);
		    }
		});
		contrast_freq_field.setDocument(JIntFilter());
		contrast_freq_field.setText("200");
		contrast_bands_field = new JTextField(5);
		contrast_bands_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(contrast_bands_field, 2);
			}
		});
		contrast_bands_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(contrast_bands_field, 2);
		    }
		});
		contrast_bands_field.setDocument(JIntFilter());
		contrast_bands_field.setText("6");
		contrast_linear_rb = new JRadioButton();
		contrast_linear_rb.setText("Linear");
		contrast_log_rb = new JRadioButton();
		contrast_log_rb.setText("Logarithmic");
		contrast_rbg = new ButtonGroup();
		contrast_rbg.add(contrast_linear_rb);
		contrast_rbg.add(contrast_log_rb);
		contrast_rbg.setSelected(contrast_linear_rb.getModel(), true);
		p_contrast = constructFeaturePanel(makeHTML("Calculates the level difference between peaks and valleys in an audio spectrum."),
				new Object[][] {{"Output:", contrast_box},{makeHTML("Frequency cutoff for first bin:")},{"",contrast_freq_field},
				{makeHTML("Number of frequency bands:")},{"",contrast_bands_field},{contrast_linear_rb},{contrast_log_rb}},
				false);
		p_cards.add(p_contrast,featureNames[10]);
		
		flatness_box = makeOutputBox();
		flatness_power_field = new JTextField(5);
		flatness_power_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(flatness_power_field, 1);
			}
		});
		flatness_power_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(flatness_power_field, 1);
		    }
		});
		flatness_power_field.setDocument(JIntFilter());
		flatness_power_field.setText("2");
		p_flatness = constructFeaturePanel(makeHTML("Quantifies how \"noise-like\" or \"tone-like\" frames of an audio clip are."),
				new Object[][] {{"Output:", flatness_box},{"Power:", flatness_power_field}},
				false);
		p_cards.add(p_flatness,featureNames[11]);
		
		flux_box = makeOutputBox();
		p_flux = constructFeaturePanel(makeHTML("Compute a spectral flux onset strength envelope.\n\n"
				+ "(From Librosa documentation: https://librosa.org/doc/main/generated/librosa.onset.onset_strength.html)"),
				new Object[][] {{"Output:", flux_box}},
				false);
		p_cards.add(p_flux,featureNames[12]);
		
		specmag_box = makeOutputBox();
		specmag_min_field = new JTextField(5);
		specmag_max_field = new JTextField(5);
		specmag_min_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(specmag_min_field, 0);
				if (Integer.valueOf(specmag_min_field.getText()) >= Integer.valueOf(specmag_max_field.getText())) {
					specmag_min_field.setText(Integer.toString(Integer.valueOf(specmag_max_field.getText())-1));
				}
			}
		});
		specmag_min_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(specmag_min_field, 0);
				if (Integer.valueOf(specmag_min_field.getText()) >= Integer.valueOf(specmag_max_field.getText())) {
					specmag_min_field.setText(Integer.toString(Integer.valueOf(specmag_max_field.getText())-1));
				}
		    }
		});
		specmag_max_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(specmag_max_field, 1);
				if (Integer.valueOf(specmag_min_field.getText()) >= Integer.valueOf(specmag_max_field.getText())) {
					specmag_max_field.setText(Integer.toString(Integer.valueOf(specmag_min_field.getText())+1));
				}
			}
		});
		specmag_max_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(specmag_max_field, 1);
				if (Integer.valueOf(specmag_min_field.getText()) >= Integer.valueOf(specmag_max_field.getText())) {
					specmag_max_field.setText(Integer.toString(Integer.valueOf(specmag_min_field.getText())+1));
				}
		    }
		});
		specmag_min_field.setDocument(JIntFilter());
		specmag_max_field.setDocument(JIntFilter());
		specmag_min_field.setText("0");
		specmag_max_field.setText("24000");
		specmag_normalize_check = new JCheckBox();
		specmag_normalize_check.setText("Normalize");
		p_specmag = constructFeaturePanel(makeHTML("Retrieves magnitude values from the STFT bins of the audio clip."),
				new Object[][] {{"Output:", specmag_box},{"Minimum frequency (Hz):"},{"", specmag_min_field},
				{"Maximum frequency (Hz):"},{"", specmag_max_field}},
				false);
		p_cards.add(p_specmag,featureNames[13]);
		
		rolloff_box = makeOutputBox();
		rolloff_threshold_field = new JTextField(5);
		rolloff_threshold_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rolloff_threshold_field.getText().length() < 1) {
					rolloff_threshold_field.setText("1");
				} else if (Integer.valueOf(rolloff_threshold_field.getText()) < 1) {
					rolloff_threshold_field.setText("1");
				} else if (Integer.valueOf(rolloff_threshold_field.getText()) > 99) {
					rolloff_threshold_field.setText("99");
				}
			}
		});
		rolloff_threshold_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	if (rolloff_threshold_field.getText().length() < 1) {
					rolloff_threshold_field.setText("1");
				} else if (Integer.valueOf(rolloff_threshold_field.getText()) < 1) {
					rolloff_threshold_field.setText("1");
				} else if (Integer.valueOf(rolloff_threshold_field.getText()) > 99) {
					rolloff_threshold_field.setText("99");
				}
		    }
		});
		rolloff_threshold_field.setDocument(JIntFilter());
		rolloff_threshold_field.setText("85");
		p_rolloff = constructFeaturePanel(makeHTML("Calculates frequency (in Hz) in each frame where [threshold]% of the spectral energy lies below."),
				new Object[][] {{"Output:", rolloff_box},{"Threshold (%):", rolloff_threshold_field}},
				false);
		p_cards.add(p_rolloff,featureNames[14]);
		
		yin_box = makeOutputBox();
		yin_box.setSelectedIndex(1);
		yin_min_field = new JTextField(5);
		yin_max_field = new JTextField(5);
		yin_min_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(yin_min_field, 0);
				if (Integer.valueOf(yin_min_field.getText()) >= Integer.valueOf(yin_max_field.getText())) {
					yin_min_field.setText(Integer.toString(Integer.valueOf(yin_max_field.getText())-1));
				}
			}
		});
		yin_min_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(yin_min_field, 0);
				if (Integer.valueOf(yin_min_field.getText()) >= Integer.valueOf(yin_max_field.getText())) {
					yin_min_field.setText(Integer.toString(Integer.valueOf(yin_max_field.getText())-1));
				}
		    }
		});
		yin_max_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(yin_max_field, 1);
				if (Integer.valueOf(yin_min_field.getText()) >= Integer.valueOf(yin_max_field.getText())) {
					yin_max_field.setText(Integer.toString(Integer.valueOf(yin_min_field.getText())+1));
				}
			}
		});
		yin_max_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(yin_max_field, 1);
				if (Integer.valueOf(yin_min_field.getText()) >= Integer.valueOf(yin_max_field.getText())) {
					yin_max_field.setText(Integer.toString(Integer.valueOf(yin_min_field.getText())+1));
				}
		    }
		});
		yin_min_field.setDocument(JIntFilter());
		yin_max_field.setDocument(JIntFilter());
		yin_min_field.setText("50");
		yin_max_field.setText("10000");
		p_yin = constructFeaturePanel(makeHTML("Estimates fundamental frequency of each frame using the YIN pitch detection algorithm. "
				+ "Not always individually accurate, but certainly useful as a feature. Median recommended over mean."),
				new Object[][] {{"Output:", yin_box},{"Minimum frequency (Hz):"},{"", yin_min_field},{"Maximum frequency (Hz)"},
				{"", yin_max_field}},
				false);
		p_cards.add(p_yin,featureNames[15]);
		
		harmonics_box = makeOutputBox(new String[] {"Sum of harmonic magnitudes","Harmonics-to-background ratio","Harmonic centroid mean",
				"Harmonic centroid standard deviation"});
		harmonics_n_field = new JTextField(5);
		harmonics_n_field.setDocument(JIntFilter());
		harmonics_n_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(harmonics_n_field, 1);
			}
		});
		harmonics_n_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(harmonics_n_field, 1);
		    }
		});
		harmonics_n_field.setText("12");
		harmonics_min_field = new JTextField(5);
		harmonics_max_field = new JTextField(5);
		harmonics_min_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(harmonics_min_field, 0);
				if (Integer.valueOf(harmonics_min_field.getText()) >= Integer.valueOf(harmonics_max_field.getText())) {
					harmonics_min_field.setText(Integer.toString(Integer.valueOf(harmonics_max_field.getText())-1));
				}
			}
		});
		harmonics_min_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(harmonics_min_field, 0);
				if (Integer.valueOf(harmonics_min_field.getText()) >= Integer.valueOf(harmonics_max_field.getText())) {
					harmonics_min_field.setText(Integer.toString(Integer.valueOf(harmonics_max_field.getText())-1));
				}
		    }
		});
		harmonics_max_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textIntUpdate(harmonics_max_field, 1);
				if (Integer.valueOf(harmonics_min_field.getText()) >= Integer.valueOf(harmonics_max_field.getText())) {
					harmonics_max_field.setText(Integer.toString(Integer.valueOf(harmonics_min_field.getText())+1));
				}
			}
		});
		harmonics_max_field.addFocusListener(new FocusAdapter() {
		    public void focusLost(FocusEvent e) {
		    	textIntUpdate(harmonics_max_field, 1);
				if (Integer.valueOf(harmonics_min_field.getText()) >= Integer.valueOf(harmonics_max_field.getText())) {
					harmonics_max_field.setText(Integer.toString(Integer.valueOf(harmonics_min_field.getText())+1));
				}
		    }
		});
		harmonics_min_field.setDocument(JIntFilter());
		harmonics_max_field.setDocument(JIntFilter());
		harmonics_min_field.setText("50");
		harmonics_max_field.setText("10000");
		harmonics_normalize_check = new JCheckBox();
		harmonics_normalize_check.setText("Normalize");
		p_harmonics = constructFeaturePanel(makeHTML("Calculates various harmonics-related features using the YIN pitch detection algorithm."),
				new Object[][] {{"Output:"},{"", harmonics_box},{"Number of harmonics:"},{"",harmonics_n_field},{"Minimum frequency (Hz):"},
				{"",harmonics_min_field},{"Maximum frequency (Hz):"},{"",harmonics_max_field},{harmonics_normalize_check}},
				false);
		p_cards.add(p_harmonics,featureNames[16]);
		
		zcr_box = makeOutputBox();
		p_zcr = constructFeaturePanel(makeHTML("Calculates how often the x-axis is crossed in an audio time series."),
				new Object[][] {{"Output:", zcr_box}},
				false);
		p_cards.add(p_zcr,featureNames[17]);
		
		
		// Kudos to this: https://stackoverflow.com/questions/13800775/find-selected-item-of-a-jlist-and-display-it-in-real-time
		flist.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                	if (flist.getSelectedIndex() == -1) {
                		cl.show(p_cards,"");
                	} else {
                		cl.show(p_cards,flist.getSelectedValue());
                	}
                }
            }
        });
		
		//p_right.add(p_cards, BorderLayout.NORTH);
		p_right.add(p_cards, BorderLayout.CENTER);
		subPanel.add(p_right);
		mainPanel.add(subPanel, b);
		b.gridy++;
		//b.gridx++;
		b.anchor = GridBagConstraints.WEST;
		addButton = new JButton("Add to vector");
		addButton.addActionListener(new AddButtonListener());
		mainPanel.add(addButton, b);
		setDialogComponent(mainPanel);
	}
	
	protected class AddButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String outp = "";
			if (flist.getSelectedIndex() > -1) {
				String selection = flist.getSelectedValue();
				if (selection == "Amplitude") {
					outp = "amplitude";
			/*	} else if (selection == "Amplitude (slice data)") {
					outp = "ampsd_";
					outp += outpAbbr((String) amp_sd_box.getSelectedItem());
				} else if (selection == "Amplitude slope (slice data)") {
					outp = "ampslope_";
					outp += outpAbbr((String) ampslope_box.getSelectedItem()); */
				} else if (selection == "Duration") {
					outp = "duration";
				} else if (selection == "Frequency") {
					outp = "freqhd_";
					outp += outpAbbr((String) freq_hd_box.getSelectedItem());
			/*	} else if (selection == "Frequency (slice data)") {
					outp = "freqsd_";
					outp += outpAbbr((String) freq_sd_box.getSelectedItem()); */
				} else if (selection == "Frequency range") {
					outp = "frange";
				} else if (selection == "Frequency slope") {
					outp = "fslopehd";
			/*	} else if (selection == "Frequency slope (slice data)") {
					outp = "fslopesd_";
					outp += outpAbbr((String) fslope_sd_box.getSelectedItem()); */
				} else if (selection == "Mel-frequency cepstral coefficients") {
					outp = "mfcc_";
					outp += mfcc_n_field.getText() + "_";
					outp += (String) mfcc_selector_box.getSelectedItem() + "_";
					outp += outpAbbr((String) mfcc_box.getSelectedItem());
				} else if (selection == "Polynomial features") {
					outp = "poly_";
					outp += poly_order_field.getText() + "_";
					outp += (String) poly_selector_box.getSelectedItem() + "_";
					outp += outpAbbr((String) poly_box.getSelectedItem());
				} else if (selection == "Root mean square") {
					outp = "rms_";
					outp += outpAbbr((String) rms_box.getSelectedItem());
				} else if (selection == "Spectral bandwidth") {
					outp = "bandwidth_";
					outp += bandwidth_power_field.getText() + "_";
					if (bandwidth_normalize_check.isSelected()) {
						outp += "ny_";
					} else {
						outp += "nn_";
					}
					outp += outpAbbr((String) bandwidth_box.getSelectedItem());
				} else if (selection == "Spectral centroid") {
					outp = "centroid_";
					outp += outpAbbr((String) centroid_box.getSelectedItem());
				} else if (selection == "Spectral contrast") {
					outp = "contrast_";
					outp += contrast_freq_field.getText() + "_";
					outp += contrast_bands_field.getText() + "_";
					if (contrast_linear_rb.isSelected()) {
						outp += "lin_";
					} else {
						outp += "log_";
					}
					outp += outpAbbr((String) contrast_box.getSelectedItem());
				} else if (selection == "Spectral flatness") {
					outp = "flatness_";
					outp += flatness_power_field.getText() + "_";
					outp += outpAbbr((String) flatness_box.getSelectedItem());
				} else if (selection == "Spectral flux (onset strength)") {
					outp = "flux_";
					outp += outpAbbr((String) flux_box.getSelectedItem());
				} else if (selection == "Spectral magnitude") {
					outp = "specmag_";
					outp += specmag_min_field.getText() + "_";
					outp += specmag_max_field.getText() + "_";
					outp += outpAbbr((String) specmag_box.getSelectedItem());
				} else if (selection == "Spectral rolloff") {
					outp = "rolloff_";
					outp += rolloff_threshold_field.getText() + "_";
					outp += outpAbbr((String) rolloff_box.getSelectedItem());
				} else if (selection == "YIN fundamental frequency") {
					outp = "yin_";
					outp += yin_min_field.getText() + "_";
					outp += yin_max_field.getText() + "_";
					outp += outpAbbr((String) yin_box.getSelectedItem());
				} else if (selection == "YIN harmonics") {
					if (harmonics_box.getSelectedIndex() == 0) {
						outp = "harmmags_";
					} else if (harmonics_box.getSelectedIndex() == 1) {
						outp = "hbr_";
					} else if (harmonics_box.getSelectedIndex() == 2) {
						outp = "hcentrmean_";
					} else {
						outp = "hcentrstd_";
					}
					selection = (String) harmonics_box.getSelectedItem();
					outp += harmonics_n_field.getText() + "_";
					outp += harmonics_min_field.getText() + "_";
					outp += harmonics_max_field.getText() + "_";
					if (harmonics_normalize_check.isSelected()) {
						outp += "ny";
					} else {
						outp += "nn";
					}
				} else if (selection == "Zero-crossing rate") {
					outp = "zcr_";
					outp += outpAbbr((String) zcr_box.getSelectedItem());
				}
				if (outp.length() > 0) {
					dtmodel.addRow(new Object[] {selection,outp.toLowerCase()});
				}
			}
		}
	}
	
	/**
	 * Converts full words found in the combo boxes to an abbreviation.
	 */
	protected String outpAbbr(String inp) {
		if (inp == "Mean") {
			return "mean";
		} else if (inp == "Median") {
			return "med";
		} else if (inp == "Standard deviation") {
			return "std";
		} else if (inp == "Minimum") {
			return "min";
		} else if (inp == "Maximum") {
			return "max";
		}
		return "";
	}
	
	// Kudos to this: https://stackoverflow.com/questions/1842223/java-linebreaks-in-jlabels
	public String makeHTML(String inp) {
		int width = 150;
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	}
	
	public String makeHTML(String inp, int width) {
		String outp = "<html><p style=\"width:"+Integer.toString(width)+"px\">"+inp+"</p></html>";
		return outp;
	}
	
	/**
	 * @return A JComboBox containing the values "Mean", "Median",
	 * "Standard deviation", "Minimum" and "Maximum".
	 */
	public JComboBox<String> makeOutputBox() {
		String[] choices = {"Mean","Median","Standard deviation","Minimum","Maximum"};
		JComboBox<String> outp = new JComboBox<String>(choices);
		return outp;
	}
	
	/**
	 * @return A JComboBox containing the input choices.
	 */
	public JComboBox<String> makeOutputBox(String[] choices) {
		JComboBox<String> outp = new JComboBox<String>(choices);
		return outp;
	}
	
	/**
	 * Streamlined means of constructing panels detailing info and
	 * options for each feature.
	 * @param description - The text description.
	 * @param comps - Option components following the description.
	 * Object[]s of size 1 will be added as is with one call of gridy++.
	 * Object[]s of size 2 will also be added with one call of gridy++,
	 * with one call of gridx++ after the first object is added.
	 * @param useHorizontalFill - Whether or not c.fill = GridBagConstraints.HORIZONTAL
	 * for each Object[] in "comps" of size 2.
	 */
	protected JPanel constructFeaturePanel(String description, Object[][] comps, boolean useHorizontalFill) {
		JPanel outp = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		JLabel descLabel = new JLabel();
		descLabel.setText(description);
		outp.add(descLabel, c);
		for (int i = 0; i < comps.length; i++) {
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.NORTHWEST;
			if (comps[i].length >= 1) {
				if (comps[0].length > 0) {
					if (comps[i].length == 1) {
						c.gridwidth = 2;
					}
					if (comps[i][0] instanceof String) {
						outp.add(new JLabel((String)comps[i][0], SwingConstants.LEFT), c);
					} else if (comps[i][0] instanceof Component) {
						outp.add((Component)comps[i][0], c);
					}
				}
			}
			if (comps[i].length == 2) {
				c.gridx++;
				if (useHorizontalFill == true) {
					c.fill = GridBagConstraints.HORIZONTAL;
				}
				c.anchor = GridBagConstraints.NORTHEAST;
				if (comps[i][1] instanceof String) {
					outp.add(new JLabel((String)comps[i][1], SwingConstants.RIGHT), c);
				} else if (comps[i][1] instanceof Component) {
					outp.add((Component)comps[i][1], c);
				}
			}
		}
		return outp;
	}
	
/*	public void fieldToBoxUpdate(JTextField field, JComboBox box) {
		if (field.getText().length() < 1) {
        	field.setText("1");
        } else if (Integer.valueOf(field.getText()) < 1) {
        	field.setText("1");
        }
		box.removeAllItems();
        box.addItem("All");
        for (int i = 1; i <= Integer.valueOf(field.getText()); i++) {
        	box.addItem(Integer.toString(i));
        }
	} */
	
	/**
	 * Used for ensuring a numerical text box has a minimum possible value.
	 */
	protected void textIntUpdate(JTextField field, int lowest) {
		if (field.getText().length() < 1) {
			field.setText(Integer.toString(lowest));
		} else if (Integer.valueOf(field.getText()) < lowest) {
			field.setText(Integer.toString(lowest));
		}
	}
	
	/**
	 * Updates mfcc_selector_box when the number in mfcc_n_field changes.
	 */
	protected void mfccBoxUpdate() {
		if (mfcc_n_field.getText().length() < 1) {
        	mfcc_n_field.setText("1");
        } else if (Integer.valueOf(mfcc_n_field.getText()) < 1) {
        	mfcc_n_field.setText("1");
        }
		mfcc_selector_box.removeAllItems();
		mfcc_selector_box.addItem("All");
        for (int i = 1; i <= Integer.valueOf(mfcc_n_field.getText()); i++) {
        	mfcc_selector_box.addItem(Integer.toString(i));
        }
	}
	
	/**
	 * Updates poly_selector_box when the number in poly_order_field changes.
	 */
	protected void polyBoxUpdate() {
		if (poly_order_field.getText().length() < 1) {
			poly_order_field.setText("1");
        } else if (Integer.valueOf(poly_order_field.getText()) < 1) {
        	poly_order_field.setText("1");
        }
		poly_selector_box.removeAllItems();
		poly_selector_box.addItem("All");
        for (int i = 1; i <= Integer.valueOf(poly_order_field.getText()); i++) {
        	poly_selector_box.addItem(Integer.toString(i));
        }
	}
	
	/**
	 * Limits entry in text field to numbers only.
	 */
	protected PlainDocument JIntFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (c >= '0' && c <= '9') {
	            	super.insertString(offs, str, a);
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Limits entry in text field to numbers and a single decimal point only.
	 */
	protected PlainDocument JDoubleFilter(JTextField field) {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            String fieldText = field.getText();
	            if ((!fieldText.contains(".")) && ((c >= '0' && c <= '9') || (c == '.'))) {
	            	super.insertString(offs, str, a);
		        } else if (fieldText.contains(".") && (c >= '0' && c <= '9')) {
		        	super.insertString(offs, str, a);
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Ensures certain JComboBoxes update when their respective
	 * text fields change values.
	 */
	protected PlainDocument fieldToBoxFilter(JTextField field, String cardName) {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (c >= '0' && c <= '9') {
	            	super.insertString(offs, str, a);
	            	if (field.getText().length() > 0) {
	            		if (Integer.valueOf(field.getText()) > 0) {
	            			if (cardName == "mfcc") {
	            				mfccBoxUpdate();
	            			} else if (cardName == "poly") {
	            				polyBoxUpdate();
	            			}
	            			
	            		}
	            	}
		        }
	        }
		};
		return d;
	}
	
	/**
	 * Returns false, as the feature table in the settings dialog
	 * is already updated by the add button.
	 */
	@Override
	public boolean getParams() {
		return false;
	}

	@Override
	public void cancelButtonPressed() {}

	@Override
	public void restoreDefaultSettings() {}
}