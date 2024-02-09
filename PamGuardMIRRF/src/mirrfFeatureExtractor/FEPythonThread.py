# FEPythonThread.py (for MIRRF Classifier plugin for PAMGuard)
# By Holly LeBlond

import numpy as np
import librosa
from librosa import display
from librosa import core
import parselmouth
import soundfile as sf
import matplotlib.pyplot as plt
import scipy
import scipy.signal as signal
from scipy import fft
import os
import sys
import IPython.display as ipd

from datetime import datetime
from datetime import timedelta
import pytz
import traceback

import matplotlib.markers

import logging
import threading
#import multiprocessing
import time
import warnings
from builtins import FileNotFoundError

# Basically, an instance of this should be created in the Python interpreter
# for each cluster, and sound clips should be fed into it via addClip.
class FEThread():
    def __init__(self, y_nr_name: str, y_nr: np.ndarray, txtParams: list):
        #print("In thread __init__.")
        self.active = True
        self.clipList = []
        self.y_nr = y_nr
        self.y_nr_name = y_nr_name
        #self.txtParams = txtParams
        self.sr = int(txtParams[0])
        self.audioAutoClipLength = txtParams[1]
        self.audioClipLength = txtParams[2]
        self.audioSTFTLength = txtParams[3]
        self.audioHopSize = txtParams[4]
        self.audioWindowFunction = txtParams[5]
        self.audioNormalizeChecked = txtParams[6]
        self.audioHPFChecked = txtParams[7]
        self.audioHPFThreshold = txtParams[8]
        self.audioHPFMagnitude = txtParams[9]
        self.audioLPFChecked = txtParams[10]
        self.audioLPFThreshold = txtParams[11]
        self.audioLPFMagnitude = txtParams[12]
        self.audioNRChecked = txtParams[13]
        self.audioNRStart = txtParams[14]
        self.audioNRLength = txtParams[15]
        self.audioNRScalar = txtParams[16]
        self.features = txtParams[17]
        self.featureProcessList, self.featureProcessIndexes = self.featuresToProcess(self.features)
        if self.audioNRChecked and len(y_nr) > 0:
            self.y_nr_stft = librosa.stft(y_nr, n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                          win_length=self.audioSTFTLength, window='rectangular')
            self.y_nr_avg = [np.mean(x)*self.audioNRScalar for x in np.abs(self.y_nr_stft)]
        #print("About to start thread.")
        #thread = threading.Thread(target=self.threadFunc(), args=(1,))
        #thread.start()
    
    # Loads a clip for processing.
    def addClip(self, fn:str, uid, datelong, amplitude, duration, freqhd_min, freqhd_max, slice_data, \
                pe_cluster_id="", pe_location="", pe_label="", pe_header_features={}): # "pe" stands for "pre-existing", implying it came from a pre-existing .mirrfts file
        #print("Reached addClip().")
        self.clipList.append(fn)
        headerData = HeaderData(uid, datelong, amplitude, duration, freqhd_min, freqhd_max, slice_data, \
                                pe_cluster_id, pe_location, pe_label, pe_header_features)
        thread = threading.Thread(target=self.threadFunc, args=(fn,headerData,))
        #thread = multiprocessing.Process(target=self.threadFunc, args=(fn,headerData,))
        thread.start()
        #print("Thread "+str(uid)+" has finished.", flush=True)
    
    # The processing thread's function.
    def threadFunc(self, fn: str, headerData):
        #print("Reached threadFunc().")
        if self.audioNRChecked and len(self.y_nr) == 0:
            print("Error: Could not process "+str(headerData.uid), flush=True, file=sys.stderr)
            #if os.path.exists(fn):
            #    os.remove(fn)
            return
        try:
            outp = self.extractFeatures(fn, headerData)
            print("outp: "+str(outp), flush=True)
            self.clipList.pop(0);
        except Exception:
            #print("Error: Could not process "+str(extras[0]), file=sys.stderr)
            print("Error: Could not process "+str(headerData.uid), flush=True, file=sys.stderr)
            print(traceback.format_exc(), flush=True, file=sys.stderr)
       # if os.path.exists(fn):
       #     os.remove(fn)
    
    # Loads and manipulates the audio before sending it to the functions
    # that perform the feature extraction.
    def extractFeatures(self, fn: str, headerData):
        self.sr = librosa.core.get_samplerate(fn)
        y = loadAudio(fn, self.sr)
        y_orig = y
        y_stft = librosa.stft(y, n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength, \
                              window=self.getWindowName(self.audioWindowFunction))
        maxval = np.max(np.abs(y_stft))
        if self.audioNRChecked:
            for i in np.arange(len(y_stft)):
                for j in np.arange(len(y_stft[i])):
                    if self.y_nr_avg[i] > np.abs(y_stft[i][j]):
                        y_stft[i][j] *= 0
                    elif self.y_nr_avg[i] > 0 and np.abs(y_stft[i][j]) > 0:
                        y_stft[i][j] *= (np.abs(y_stft[i][j]) - self.y_nr_avg[i])/np.abs(y_stft[i][j])
        if self.audioNormalizeChecked and np.max(np.abs(y_stft)) > 0:
            y_stft *= 1/np.max(np.abs(y_stft))
        y = librosa.istft(y_stft, hop_length=self.audioHopSize, win_length=self.audioSTFTLength, \
                          window=self.getWindowName(self.audioWindowFunction))
        if self.audioHPFChecked:
            hpf = signal.butter(self.audioHPFMagnitude, self.audioHPFThreshold, 'highpass', fs=self.sr, output='sos')
            y = signal.sosfilt(hpf, y)
        if self.audioLPFChecked:
            lpf = signal.butter(self.audioLPFMagnitude, self.audioLPFThreshold, 'lowpass', fs=self.sr, output='sos')
            y = signal.sosfilt(lpf, y)
        if self.audioHPFChecked or self.audioLPFChecked:
            y_stft = librosa.stft(y, n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength, \
                                  window=self.getWindowName(self.audioWindowFunction))
            if self.audioNormalizeChecked and np.max(np.abs(y_stft)) > 0:
                y_stft *= 1/np.max(np.abs(y_stft))
        
        #TEST CODE - DELETE AFTERWARDS
        #newFN = str(headerData.uid)
        #sf.write("C:/Users/wtleb/Desktop/MIRRF export (delete)/cliptest/"+newFN+"_NR.wav", self.y_nr, self.sr, 'PCM_24')
        #sf.write("C:/Users/wtleb/Desktop/MIRRF export (delete)/cliptest/"+newFN+"_Orig.wav", y_orig, self.sr, 'PCM_24')
        #sf.write("C:/Users/wtleb/Desktop/MIRRF export (delete)/cliptest/"+newFN+"_New.wav", y, self.sr, 'PCM_24')
        
        #y_mag = librosa.amplitude_to_db(np.abs(y_stft), ref=np.max)
        preCalcFeatures = []
        for feature in self.featureProcessList:
            preCalcFeatures.append(self.preCalculateFeature(feature, y, y_stft))
        outp = []
        if headerData.from_mirrfts:
            outp = [headerData.pe_cluster_id, headerData.uid, headerData.pe_location, headerData.datelong, headerData.duration, \
                    headerData.freqhd_min, headerData.freqhd_max, headerData.pe_label]
        if not headerData.from_mirrfts:
            idTokens = self.clipList[0].split("_")
            outp = [idTokens[1]+"-"+idTokens[2], headerData.uid, headerData.datelong, headerData.duration, headerData.freqhd_min, headerData.freqhd_max]
        for i in np.arange(len(self.features)):
            if self.featureProcessIndexes[i] == -1:
                outp.append(self.extractIndividualFeature(self.features[i], [], headerData))
            else:
                outp.append(self.extractIndividualFeature(self.features[i], preCalcFeatures[self.featureProcessIndexes[i]], headerData))
        return outp
    
    # Parses through input settings and creates a list of what to process, while making sure it doesn't process the same thing if used by the
    # same feature with marginally different settings.
    def featuresToProcess(self, features: list):
        outp = []
        outpIndexes = []
        for i in np.arange(len(features)):
            curr = ""
            tokens = features[i].split("_")
            if tokens[0] in ["amplitude","duration","freqhd","frange","fslopehd", \
                             "freqsd","freqsdd1","freqsdd2","freqsdelbow","freqsdslope"]:
                # These are just calculated from input header data.
                pass
            elif tokens[0] in ["rms","centroid","flux","zcr"]:
                curr = tokens[0]
            elif tokens[0] in ["mfcc","poly","flatness","rolloff"]:
                curr = tokens[0]+"_"+tokens[1]
            elif tokens[0] in ["bandwidth","specmag","praat"]:
                curr = tokens[0]+"_"+tokens[1]+"_"+tokens[2]
            elif tokens[0] in ["formantfreq","formantcount","formantdiff","contrast"]:
                curr = tokens[0]+"_"+tokens[1]+"_"+tokens[2]+"_"+tokens[3]
            elif tokens[0] in ["thd","hbr","hcentroid","hfr"]:
                curr = "harms_"+tokens[1]+"_"+tokens[2]+"_"+tokens[3]
                if tokens[0] == "hfr":
                    for prev in outp:
                        ptokens = prev.split("_")
                        if len(ptokens) >= 4 and ptokens[0] == "harms" and ptokens[2] == tokens[2] \
                        and ptokens[3] == tokens[3] and ptokens[1] >= tokens[1]:
                            curr = prev
                            break
            if len(curr) == 0:
                outpIndexes.append(-1)
            elif curr not in outp:
                outp.append(curr)
                outpIndexes.append(len(outp)-1)
            else:
                outpIndexes.append(outp.index(curr))
        return outp, outpIndexes
    
    # Performs the actual feature extraction. Designed such that instances where
    # the actual processing is the same will use the same output without having
    # to calculate it more than once.
    def preCalculateFeature(self, feature: str, y, y_stft):
        tokens = feature.split("_")
        if tokens[0] in ["formantfreq","formantcount","formantdiff"]:
            # Kudos: https://www.mathworks.com/help/signal/ug/formant-estimation-with-lpc-coefficients.html
            # and also: https://support.ircam.fr/docs/AudioSculpt/3.0/co/LPC_1.html
            order = int(np.ceil(self.sr/(int(tokens[1])*0.25)))
            #lpccs = [x for x in librosa.lpc(y, order=order) if not np.isinf(x) and not np.isnan(x)]
            windowed = y * librosa.filters.get_window('hamming', len(y))
            filtered = scipy.signal.lfilter([1], [1, 0.63], windowed)
            lpccs = [x for x in librosa.lpc(filtered, order=order)]
            for x in lpccs:
                if np.isnan(x) or np.isinf(x):
                    return [] # End result should be zero in every case if this happens
            roots = [x for x in np.roots(lpccs) if np.imag(x) >= 0]
            freqs = [x*(self.sr/(2*np.pi)) for x in np.arctan2(np.imag(roots),np.real(roots))]
            stuff = [[freqs[i],roots[i]] for i in np.arange(len(freqs))]
            stuff.sort()
            #bandwidths = [-1/2*(self.sr/(2*np.pi))*np.log(np.abs(x[1])) for x in stuff]
            bandwidths = []
            for x in stuff:
                if np.abs(x[1]) > 0:
                    bandwidths.append(-0.5*(self.sr/(2*np.pi))*np.log(np.abs(x[1])))
                else:
                    bandwidths.append(float(tokens[3])) # discounts it in case of log 0
            return [stuff[i][0] for i in np.arange(len(stuff)) if stuff[i][0] > float(tokens[2]) and bandwidths[i] < float(tokens[3])]
        elif tokens[0] == "mfcc":
            #return librosa.feature.mfcc(y=y, sr=sr, n_mfcc=int(tokens[1]), n_fft=audioSTFTLength)
            return librosa.feature.mfcc(y=y, sr=self.sr, n_mfcc=int(tokens[1]))
        elif tokens[0] == "poly":
            return librosa.feature.poly_features(y=y, sr=self.sr, order=int(tokens[1]), n_fft=self.audioSTFTLength, hop_length=self.audioHopSize)
        elif tokens[0] == "rms":
            return librosa.feature.rms(y=y)
        elif tokens[0] == "bandwidth":
            if tokens[2] == "ny":
                return librosa.feature.spectral_bandwidth(y=y, sr=self.sr, p=float(tokens[1]), norm=True, n_fft=self.audioSTFTLength, \
                                                          hop_length=self.audioHopSize)
            else:
                return librosa.feature.spectral_bandwidth(y=y, sr=self.sr, p=float(tokens[1]), norm=False, n_fft=self.audioSTFTLength, \
                                                          hop_length=self.audioHopSize)
        elif tokens[0] == "centroid":
            return librosa.feature.spectral_centroid(y=y, sr=self.sr, n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                                     window=self.getWindowName(self.audioWindowFunction))
        elif tokens[0] == "contrast":
            if tokens[3] == "lin":
                return librosa.feature.spectral_contrast(y=y, sr=self.sr, fmin=float(tokens[1]), n_bands=int(tokens[2]), linear=True, \
                                                     n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                                     window=self.getWindowName(self.audioWindowFunction))
            else:
                return librosa.feature.spectral_contrast(y=y, sr=self.sr, fmin=float(tokens[1]), n_bands=int(tokens[2]), linear=False, \
                                                     n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                                     window=self.getWindowName(self.audioWindowFunction))
        elif tokens[0] == "flatness":
            return librosa.feature.spectral_flatness(y=y, power=float(tokens[1]), \
                                                     n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                                     window=self.getWindowName(self.audioWindowFunction))
        elif tokens[0] == "flux":
            return librosa.onset.onset_strength(y=y, sr=self.sr)
        elif tokens[0] == "specmag":
            return np.abs(y_stft)[int(self.audioSTFTLength*(int(tokens[1])/self.sr)):int(self.audioSTFTLength*(int(tokens[2])/self.sr))]
        elif tokens[0] == "rolloff":
            return librosa.feature.spectral_rolloff(y=y, sr=self.sr, roll_percent=float(tokens[1])/100,\
                                                     n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, \
                                                     window=self.getWindowName(self.audioWindowFunction))
        elif tokens[0] == "praat":
            #yin = librosa.yin(y, fmin=int(tokens[1]), fmax=int(tokens[2]), sr=self.sr, \
            #                   frame_length=2*self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength)
            snd = parselmouth.Sound(y, sampling_frequency=self.sr)
            praat = snd.to_pitch(time_step=self.audioHopSize/self.sr, pitch_floor=int(tokens[1]), pitch_ceiling=int(tokens[2]))
            outp = [x[0] for x in praat.to_array()[0] if x[0] > 0.0]
            if len(outp) == 0:
                return [0.0]
            return outp
        elif tokens[0] == "harms":
            #yin_orig = librosa.yin(y, fmin=int(tokens[2]), fmax=int(tokens[3]), sr=self.sr, \
            #                   frame_length=2*self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength)
            snd = parselmouth.Sound(y, sampling_frequency=self.sr)
            time_step = self.audioHopSize/self.sr
            praat = snd.to_pitch(time_step=time_step, pitch_floor=int(tokens[2]), pitch_ceiling=int(tokens[3]))
            praat = [x[0] for x in praat.to_array()[0]]
            nh = int(tokens[1])
            y_fft_arr = []
            j = 0
            # Not sure what the window size is, so it assumes it's the same as the time step.
            while (j+1)*int(self.sr*time_step) <= len(y):
                y_fft_arr.append(fft.fft(y[j*int(self.sr*time_step):(j+1)*int(self.sr*time_step)], n=self.sr)[:int(self.sr/2)])
                j += 1
            freqs_mags = []
            fft_mag_outp = []
            for i in np.arange(len(praat)):
                if praat[i] == 0.0:
                    continue
                frame = []
                for j in np.arange(nh)+1:
                    if int(np.round(praat[i]*j)) < self.sr/2:
                        frame.append([praat[i]*j, np.abs(y_fft_arr[i][int(np.round(praat[i]*j))])])
                    else:
                        frame.append([praat[i]*j, 0.0])
                freqs_mags.append(frame)
                fft_mag_outp.append(np.abs(y_fft_arr[i]))
            return [freqs_mags, fft_mag_outp]
        elif tokens[0] == "zcr":
            return librosa.feature.zero_crossing_rate(y+0.0001)
        return []
            
    # Goes through the array of pre-calculated data and runs calculateData on it.
    def extractIndividualFeature(self, feature: str, featureArray, headerData):
        tokens = feature.split("_")
        header_features = ["amplitude","duration","freqhd_min","freqhd_max"]
        if feature == "amplitude":
            if headerData.from_mirrfts:
                if "amplitude" in headerData.pe_header_features:
                    return headerData.pe_header_features["amplitude"]
                return np.nan
            return headerData.amplitude
        elif feature == "duration":
            return headerData.duration
        elif feature == "freqhd_min":
            return headerData.freqhd_min
        elif feature == "freqhd_max":
            return headerData.freqhd_max
        elif feature == "frange":
            return headerData.freqhd_max - headerData.freqhd_min
        elif feature == "fslopehd":
            return (headerData.freqhd_max - headerData.freqhd_min) / (headerData.duration / 1000)
        elif feature == "freqsdelbow":
            if headerData.from_mirrfts and feature in headerData.pe_header_features:
                return headerData.pe_header_features[feature]
            if headerData.slice_data[0] == -1 or (headerData.from_mirrfts and not feature in headerData.pe_header_features):
                return np.nan
            deriv2abs = np.abs(self.calculateFreqSD2ndDerivative(headerData.slice_data))
            elbow = headerData.slice_data[np.argmax(deriv2abs)+1]
            start = (1000 * (headerData.slice_data[0][0] - elbow[0]) / self.sr, headerData.slice_data[0][1] - elbow[1])
            end = (1000 * (headerData.slice_data[len(headerData.slice_data)-1][0] - elbow[0]) / self.sr, \
                   headerData.slice_data[len(headerData.slice_data)-1][1] - elbow[1])
            dot = np.dot(start, end)
            mag = np.sqrt(np.power(start[0],2)+np.power(start[1],2)) * np.sqrt(np.power(end[0],2)+np.power(end[1],2))
            if mag == 0 or np.abs(dot/mag) > 1:
                return 180.0
            return 180.0 * np.arccos(dot/mag) / np.pi
        elif feature == "freqsdslope" or tokens[0] in ["freqsd","freqsdd1","freqsdd2"]:
            if headerData.from_mirrfts and feature in headerData.pe_header_features:
                return headerData.pe_header_features[feature]
            if headerData.slice_data[0] == -1 or (headerData.from_mirrfts and not feature in headerData.pe_header_features):
                return np.nan
        if feature == "freqsdslope":
            return (headerData.slice_data[len(headerData.slice_data)-1][1] - headerData.slice_data[0][1]) / (headerData.duration / 1000)
        elif tokens[0] == "freqsd":
            return self.calculateUnit([x[1] for x in headerData.slice_data], tokens[len(tokens)-1])
        elif tokens[0] == "freqsdd1":
            return self.calculateUnit(self.calculateFreqSD1stDerivative(headerData.slice_data), tokens[len(tokens)-1])
        elif tokens[0] == "freqsdd2":
            return self.calculateUnit(self.calculateFreqSD2ndDerivative(headerData.slice_data), tokens[len(tokens)-1])
        elif tokens[0] in ["rms","bandwidth","centroid","contrast","flatness","flux","specmag","rolloff","praat","zcr"]:
            return self.calculateUnit(featureArray, tokens[len(tokens)-1])
        elif tokens[0] == "formantfreq":
            if len(featureArray) >= int(tokens[4]):
                return featureArray[int(tokens[4])-1]
            else:
                return 0.0
        elif tokens[0] == "formantcount":
            return len(featureArray)
        elif tokens[0] == "formantdiff":
            num = int(tokens[4])-1
            if num+1 < len(featureArray):
                return featureArray[num+1] - featureArray[num]
            return 0.0
        elif tokens[0] in ["mfcc","poly"]:
            if tokens[2] == "all":
                return self.calculateUnit(featureArray, tokens[len(tokens)-1])
            else:
                return self.calculateUnit(featureArray[int(tokens[2])-1], tokens[len(tokens)-1])
        #elif tokens[0] == "harmmags":
            #if np.max(featureArray[0]) == 0:
            #    return 1.0
            #outp = np.sum([x/np.max(featureArray[0]) for x in featureArray[0]])
            #if np.isnan(outp):
            #    return 0.0
            #return outp
        elif tokens[0] == "thd":
            # Kudos: https://www.analog.com/media/en/training-seminars/design-handbooks/Practical-Analog-Design-Techniques/Section8.pdf
            # and also: https://www.youtube.com/watch?v=s_cVP5gu4SY for deriving THD out of FFT magnitudes
            if len(featureArray[0]) == 0 or len(featureArray[0][0]) < 2:
                return 0.0
            thd = []
            for i in np.arange(len(featureArray[0])):
                frame = featureArray[0][i]
                if (frame[0][1] > 0.0):
                    #thd.append(np.sqrt(np.sum([np.power(frame[x][1], 2) for x in np.arange(len(frame)-1)+1]))/frame[0][1])
                    harmonic_power_ratios = []
                    for j in np.arange(len(frame)-1)+1:
                        dbc = frame[j][1] - frame[0][1]
                        power_ratio = np.power(10, dbc/10)
                        harmonic_power_ratios.append(power_ratio)
                    thd.append(100*np.sqrt(np.sum(harmonic_power_ratios)))
            thd = [x for x in thd if str(x) != 'nan']
            return self.calculateUnit(thd, tokens[len(tokens)-1])
        elif tokens[0] == "hbr": # harmonic-mean divided by frame-median FFT magnitude
            if len(featureArray[0]) == 0:
                return 0.0
            ratios = []
            for i in np.arange(len(featureArray[0])):
                frame_t = np.transpose(featureArray[0][i])
                ratios.append(np.mean(frame_t[1])/np.median(featureArray[1][i]))
            ratios = [x for x in ratios if str(x) != 'nan']
            return self.calculateUnit(ratios, tokens[len(tokens)-1])
        #elif tokens[0] in ["hcentrmean","hcentrstd"]:
        elif tokens[0] == "hcentroid":
            if len(featureArray[0]) == 0:
                return self.calculateUnit([1.0], tokens[len(tokens)-1])
            centroids = []
            for i in np.arange(len(featureArray[0])):
                frame_t = np.transpose(featureArray[0][i])
                harmonics_100 = []
                if np.max(frame_t[1]) == 0.0:
                    harmonics_100 = [1.0]
                else:
                    harmonics_100 = [100*x/np.max(frame_t[1]) for x in frame_t[1]]
                    #harmonics_100 = [x for x in harmonics_100 if str(x) != 'nan']
                if len(harmonics_100) == 0:
                    harmonics_100 = [1.0]
                harmonics_histo = []
                for j in np.arange(len(harmonics_100)):
                    for k in np.arange(int(harmonics_100[j])):
                        harmonics_histo.append(j+1)
                if tokens[len(tokens)-2] == "mean":
                    centroids.append(np.mean(harmonics_histo))
                elif tokens[len(tokens)-2] == "median":
                    centroids.append(np.median(harmonics_histo))
                elif tokens[len(tokens)-2] == "std":
                    centroids.append(np.std(harmonics_histo))
                elif tokens[len(tokens)-2] == "mode":
                    centroids.append(np.array(harmonics_100).argmax()+1)
                else:
                    return feature
            return self.calculateUnit(centroids, tokens[len(tokens)-1])
        elif tokens[0] == "hfr":
            hn = int(tokens[1])-1
            max_ratio = float(tokens[4])
            ratios = []
            for i in np.arange(len(featureArray[0])):
                frame = featureArray[0][i]
                # Harmonic frequency must be below half of sampling rate.
                if 0.0 < frame[hn][0] < self.sr/2:
                    if frame[hn][1] == 0.0:
                        ratios.append(0.0)
                    elif frame[0][1] > 0.0:
                        ratio = frame[hn][1]/frame[0][1]
                        if ratio < max_ratio:
                            ratios.append(ratio)
                        else:
                            ratios.append(max_ratio)
                    else: # Fundamental has a magnitude of zero
                        ratios.append(max_ratio)
            return self.calculateUnit(ratios, tokens[len(tokens)-1])
        return feature
     
    # Extracts the mean, median, standard deviation, maximum or minimum from pre-calculated data.
    def calculateUnit(self, featureArray, function):
        if len(featureArray) == 0:
            return 0.0
        if function == "mean":
            return np.mean(featureArray)
        elif function == "med":
            return np.median(featureArray)
        elif function == "std":
            return np.std(featureArray)
        elif function == "max":
            return np.max(featureArray)
        elif function == "min":
            return np.min(featureArray)
        elif function == "rng":
            return np.max(featureArray) - np.min(featureArray)
        return 0.0;
    
    # Returns shorthands for window functions.
    def getWindowName(self, winname):
        if winname == "Bartlett-Hann":
            return "barthann"
        elif winname == "Blackman-Harris":
            return "blackmanharris"
        elif winname == "Flat top":
            return "flattop"
        elif winname == "Triangular":
            return "triang"
        return winname.lower()
    
    def calculateFreqSD1stDerivative(self, slice_data: list):
        if len(slice_data) <= 1:
            return [0.0]
        ms = [1000*(x[0]-slice_data[0][0])/self.sr for x in slice_data]
        freqs = [x[1] for x in slice_data]
        return [(freqs[i+1] - freqs[i])/(ms[i+1] - ms[i]) for i in np.arange(len(freqs)-1)]
    
    def calculateFreqSD2ndDerivative(self, slice_data: list):
        deriv = self.calculateFreqSD1stDerivative(slice_data)
        if len(deriv) <= 1:
            return [0.0]
        ms = [1000*(x[0]-slice_data[0][0])/self.sr for x in slice_data]
        return [(deriv[i+1] - deriv[i])/(ms[i+1] - ms[i]) for i in np.arange(len(deriv)-1)]

class HeaderData:
    def __init__(self, uid, datelong, amplitude, duration, freqhd_min, freqhd_max, slice_data, pe_cluster_id, pe_location, pe_label, pe_header_features):
        self.uid = uid
        self.datelong = datelong
        self.amplitude = amplitude
        self.duration = duration
        self.freqhd_min = freqhd_min
        self.freqhd_max = freqhd_max
        self.slice_data = slice_data
        self.pe_cluster_id = pe_cluster_id
        self.pe_location = pe_location
        self.pe_label = pe_label
        self.pe_header_features = pe_header_features
        self.from_mirrfts = len(pe_cluster_id) > 0

# Loads clip into a variable and then deletes the file, as it is no longer needed.
def loadAudio(fn: str, sr: int):
    warnings.simplefilter(action='ignore', category=FutureWarning)
    try:
        y, newsr = librosa.load(fn, sr=sr)
        #os.remove(fn)
        return y
    except (Exception, RuntimeError, FileNotFoundError) as e:
        print("loadAudio exception")
        print(traceback.format_exc(), file=sys.stderr)
    return []
