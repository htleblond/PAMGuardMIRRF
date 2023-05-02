import numpy as np
import librosa
from librosa import display
from librosa import core
import soundfile as sf
import matplotlib.pyplot as plt
import scipy.signal as signal
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
import time
import warnings
from builtins import FileNotFoundError

print("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")

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
        
    def addClip(self, fn:str, uid, datelong, amplitude, duration, freqhd_min, freqhd_max, frange, fslopehd):
        #print("Reached addClip().")
        self.clipList.append(fn)
        extras = [uid, datelong, amplitude, duration, freqhd_min, freqhd_max, frange, fslopehd]
        #thread = threading.Thread(target=self.threadFunc(fn, extras), args=(1,))
        thread = threading.Thread(target=self.threadFunc, args=(fn,extras,))
        thread.start()
        print("Thread "+str(uid)+" has finished.", flush=True)
    
    def threadFunc(self, fn: str, extras):
        #print("Reached threadFunc().")
        if self.audioNRChecked and len(self.y_nr) == 0:
            print("Error: Could not process "+str(extras[0]), flush=True)
            if os.path.exists(fn):
                os.remove(fn)
            return
        try:
            outp = self.extractFeatures(fn, extras)
            print("outp: "+str(outp), flush=True)
            self.clipList.pop(0);
        except Exception:
            #print("Error: Could not process "+str(extras[0]), file=sys.stderr)
            print("Error: Could not process "+str(extras[0]), flush=True)
            print(traceback.format_exc(), flush=True, file=sys.stderr)
        if os.path.exists(fn):
            os.remove(fn)
        
    def killAfterCompletion(self):
        self.active = False
        
    def checkIfFreeable(self):
        if not self.active and len(self.clipList) == 0:
            return True
        else:
            return False
    
    def extractFeatures(self, fn: str, extras):
        #print(self.y_nr_name[3:len(self.y_nr_name)-4]+", "+str(np.mean(self.y_nr)))
        self.sr = librosa.core.get_samplerate(fn)
        y, sr = librosa.load(fn, sr=self.sr)
        y_stft = librosa.stft(y, n_fft=self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength, \
                              window=self.getWindowName(self.audioWindowFunction))
        #print(y[100:200])
        maxval = np.max(np.abs(y_stft))
        if self.audioNRChecked:
            for i in np.arange(len(y_stft)):
                for j in np.arange(len(y_stft[i])):
                    if self.y_nr_avg[i] > np.abs(y_stft[i][j]):
                        y_stft[i][j] *= 0
                    elif self.y_nr_avg[i] > 0 and np.abs(y_stft[i][j]) > 0:
                        y_stft[i][j] *= (np.abs(y_stft[i][j]) - self.y_nr_avg[i])/np.abs(y_stft[i][j])
                        #y_stft[i][j] *= np.abs(self.y_nr_avg[i] - np.abs(y_stft[i][j]))
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
        #y_mag = librosa.amplitude_to_db(np.abs(y_stft), ref=np.max)
        preCalcFeatures = []
        for feature in self.featureProcessList:
            preCalcFeatures.append(self.preCalculateFeature(feature, y, y_stft))
        #idTokens = self.y_nr_name.split("_")
        idTokens = self.clipList[0].split("_")
        outp = [idTokens[1]+"-"+idTokens[2], extras[0], extras[1], extras[3], extras[4], extras[5]]
        for i in np.arange(len(self.features)):
            outp.append(self.extractIndividualFeature(self.features[i], preCalcFeatures[self.featureProcessIndexes[i]], extras))
        return outp

    def preCalculateFeature(self, feature: str, y, y_stft):
        tokens = feature.split("_")
        if tokens[0] == "mfcc":
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
        elif tokens[0] == "yin":
            yin = librosa.yin(y, fmin=int(tokens[1]), fmax=int(tokens[2]), sr=self.sr, \
                               frame_length=2*self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength)
            outp = []
            for i in np.arange(len(yin)):
                if yin[i] < self.sr/4:
                    outp.append(yin[i])
                else:
                    outp.append(0.0)
            #if len(outp) == 0:
            #    return [0.0]
            return outp
        elif tokens[0] == "harms":
            yin_orig = librosa.yin(y, fmin=int(tokens[2]), fmax=int(tokens[3]), sr=self.sr, \
                               frame_length=2*self.audioSTFTLength, hop_length=self.audioHopSize, win_length=self.audioSTFTLength)
            yin = []
            for i in np.arange(len(yin_orig)):
                if yin_orig[i] < self.sr/4:
                    yin.append(yin_orig[i])
                else:
                    yin.append(0.0)
            #if len(yin) == 0:
            #    yin = [0.0]
            nh = int(tokens[1])
            harmonics_arr = [[] for x in np.arange(nh)]
            y_amp_t = np.transpose(np.abs(y_stft))
            for j in np.arange(len(y_amp_t)):
                for k in np.arange(nh):
                    if yin[j] > 0.0:
                        floor = [int(np.floor((yin[j]*(k+1)-0.5)/((self.sr/2)/(self.audioSTFTLength/2)))), \
                                 np.mod((yin[j]*(k+1)-0.5)/((self.sr/2)/(self.audioSTFTLength/2)),1)]
                        ceiling = [int(np.ceil((yin[j]*(k+1)-0.5)/((self.sr/2)/(self.audioSTFTLength/2)))), \
                                   1-np.mod((yin[j]*(k+1)-0.5)/((self.sr/2)/(self.audioSTFTLength/2)),1)]
                        #print(str(yin[j])+" : "+str(floor[0]))
                        if yin[j] >= int(tokens[2]) and yin[j] <= int(tokens[3]):
                            if ceiling[0] < self.audioSTFTLength/2:
                                harmonics_arr[k].append(y_amp_t[j][floor[0]]*floor[1] + y_amp_t[j][int(ceiling[0])]*ceiling[1])
                            else:
                                harmonics_arr[k].append(0.0)
            harmonics = []
            if len(harmonics_arr[0]) > 0:
                harmonics = [np.mean(x) for x in harmonics_arr]
            else:
                harmonics = [[0.0] for x in harmonics_arr]
            return [harmonics, harmonics_arr, y_amp_t]
        elif tokens[0] == "zcr":
            return librosa.feature.zero_crossing_rate(y+0.0001)
        return []
            
        
    def extractIndividualFeature(self, feature: str, featureArray, extras):
        tokens = feature.split("_")
        header_features = ["amplitude","duration","freqhd_min","freqhd_max","frange","fslopehd"]
        if feature in header_features:
            return extras[header_features.index(feature)+2]
        elif tokens[0] in ["rms","bandwidth","centroid","contrast","flatness","flux","specmag","rolloff","yin","zcr"]:
            return self.calculateUnit(featureArray, tokens[len(tokens)-1])
        elif tokens[0] in ["mfcc","poly"]:
            if tokens[2] == "all":
                return self.calculateUnit(featureArray, tokens[len(tokens)-1])
            else:
                return self.calculateUnit(featureArray[int(tokens[2])-1], tokens[len(tokens)-1])
        elif tokens[0] == "harmmags":
            if np.max(featureArray[0]) == 0:
                return 1.0
            outp = np.sum([x/np.max(featureArray[0]) for x in featureArray[0]])
            if np.isnan(outp):
                return 0.0
            return outp
        elif tokens[0] == "hbr":
            if np.sum(featureArray[2]) == 0:
                return 0.0
            outp = np.sum(featureArray[1])/np.sum(featureArray[2])
            if np.isnan(outp):
                return 0.0
            return outp
        elif tokens[0] in ["hcentrmean","hcentrstd"]:
            if not np.max(featureArray[0]) > 0:
                if tokens[0] == "hcentrmean":
                    return 1.0
                elif tokens[0] == "hcentrstd":
                    return 0.0
            harmonics_1000 = []
            try:
                harmonics_1000 = [1000*x/np.max(featureArray[0]) for x in featureArray[0]]
            except Exception:
                pass
            harmonics_histo = []
            if len(harmonics_1000) > 0:
                for j in np.arange(int(tokens[1])):
                    for k in np.arange(int(harmonics_1000[j])):
                        harmonics_histo.append(j)
            else:
                if tokens[0] == "hcentrmean":
                    return 1.0
                elif tokens[0] == "hcentrstd":
                    return 0.0
            if tokens[0] == "hcentrmean":
                if len(harmonics_histo) > 0:
                    return np.mean(harmonics_histo)+1
                else:
                    return 1.0
            elif tokens[0] == "hcentrstd":
                if len(harmonics_histo) > 0:
                    return np.std(harmonics_histo)
                else:
                    return 0.0
        return feature
     
    def calculateUnit(self, featureArray, function):
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
        return 0;
    
    def featuresToProcess(self, features: list):
        outp = []
        outpIndexes = []
        for i in np.arange(len(features)):
            curr = ""
            tokens = features[i].split("_")
            if tokens[0] in ["amplitude","duration","freqhd","freqsd","frange","fslopehd","fslopesd"]:
                # These involve contour header or slice data, so they will be dealt with in Java.
                pass
            elif tokens[0] in ["rms","centroid","flux","zcr"]:
                curr = tokens[0]
            elif tokens[0] in ["mfcc","poly","flatness","rolloff"]:
                curr = tokens[0]+"_"+tokens[1]
            elif tokens[0] in ["bandwidth","specmag","yin"]:
                curr = tokens[0]+"_"+tokens[1]+"_"+tokens[2]
            elif tokens[0] in ["contrast"]:
                curr = tokens[0]+"_"+tokens[1]+"_"+tokens[2]+"_"+tokens[3]
            elif tokens[0] in ["harmmags","hbr","hcentrmean","hcentrstd"]:
                curr = "harms_"+tokens[1]+"_"+tokens[2]+"_"+tokens[3]
                
            if len(curr) == 0:
                outpIndexes.append(-1)
            elif curr not in outp:
                outp.append(curr)
                outpIndexes.append(len(outp)-1)
            else:
                outpIndexes.append(outp.index(curr))
                
        return outp, outpIndexes
    
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
    
    def runLast(self):
        print("RUNLAST")
    
def freeThroughList(threadList: list, wavNrList: list):
    #print("listlens: "+str(len(threadList))+", "+str(len(wavNrList)))
    if len(threadList) == len(wavNrList):
        i = 0
        while i < len(threadList):
            if freeIfComplete(threadList[i], wavNrList[i]):
                threadList.pop(i)
                wavNrList.pop(i)
            else:
                i += 1
    return threadList, wavNrList
    
def freeIfComplete(inpThread: FEThread, inpWav):
    if inpThread.checkIfFreeable():
        print("freed: "+inpThread.y_nr_name[:len(inpThread.y_nr_name)-4])
        #inpThread = None
        #inpWav = None
        return True
    return False

def loadAudio(fn: str, sr: int):
    warnings.simplefilter(action='ignore', category=FutureWarning)
    try:
        y, newsr = librosa.load(fn, sr)
        return y
    except (Exception, RuntimeError, FileNotFoundError) as e:
        print("loadAudio exception")
        print(traceback.format_exc(), file=sys.stderr)
    return []
