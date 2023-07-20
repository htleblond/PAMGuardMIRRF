# Music Information Retrieval Random Forest (MIRRF) Classifier for PAMGuard

This is an external PAMGuard plugin for classifying Whistle and Moan Detector (WMD) contours using music information retrieval (MIR) techniques and contour header data in conjunction with an ensemble classifier model. While the primary goal of this project is to create a classifier that can discriminate between killer whale, humpback whale and vessel noise in high-traffic areas of the Salish Sea, the classifier should theoretically work with any sound source that produces WMD detections.

## Includes the following modules:

Under "Classifiers":
- **Feature Extractor** - Extracts feature vector data from sound clips where WMD detections occur.
- **Training Set Builder** - Tool for combining feature vector data and annotation data to create and customize training sets.
- **Live Classifier** - Classifies feature vector data directly output by the Feature Extractor.
- **Test Classifier** - Performs cross-validation on pre-existing training sets.

Under "Utilities" (Viewer-mode only):
- **Whistle and Moan Navigation Tool (WMNT)** - Tool for annotating WMD detections and for easier navigation of the spectrogram after processing.

## Installation
1. Install Python 3 and ensure that pip works.
2. Download the most recent MIRRF release.
3. Run the .bat file in the downloaded folder. (Note to Python developers: Check the .bat file first in case it creates any conflicts!)
4. Place the .jar file in the downloaded folder into your versions of PAMGuard's "plugins" folder.
