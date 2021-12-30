package org.ar.ar_android_tutorial_1to1;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

public class LocalAVInfo {
    public ObservableInt audioBitrate;
    public ObservableInt audioSampleRate;
    public ObservableInt audioChannel;
    public ObservableInt audioVol;
    public ObservableInt fps;
    public ObservableInt videoBitrate;
    public ObservableField<String> resolution;
    public ObservableInt loss;
    public ObservableBoolean showInfo;

    public LocalAVInfo() {
        this.audioBitrate = new ObservableInt(0);
        this.audioSampleRate = new ObservableInt(0);
        this.audioChannel = new ObservableInt(0);
        this.audioVol = new ObservableInt(0);
        this.fps = new ObservableInt(0);
        this.resolution = new ObservableField<>("");
        this.loss = new ObservableInt(0);
        this.videoBitrate = new ObservableInt(0);
        this.showInfo = new ObservableBoolean(false);
    }

    public void reset(){
        audioBitrate.set(0);
        audioSampleRate.set(0);
        audioChannel.set(0);
        audioVol.set(0);
        fps.set(0);
        videoBitrate.set(0);
        resolution.set("");
        loss.set(0);
    }

}
