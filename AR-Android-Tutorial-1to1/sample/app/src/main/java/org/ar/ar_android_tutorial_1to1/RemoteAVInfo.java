package org.ar.ar_android_tutorial_1to1;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableLong;

public class RemoteAVInfo {
    public ObservableLong onlineToSubAudioTime;
    public ObservableLong subAudioToSubSuccessTime;
    public ObservableLong onlineToSubVideoTime;
    public ObservableLong subVideoToSubSuccessTime;
    public ObservableLong subVideoSuccessToFirstFrameTime;
    public ObservableLong subAudioSuccessToFirstFrameTime;

    public ObservableInt audioBitrate;
    public ObservableInt audioSampleRate;
    public ObservableInt audioChannel;
    public ObservableInt audioVol;
    public ObservableInt fps;
    public ObservableInt videoBitrate;
    public ObservableField<String> resolution;
    public ObservableInt loss;

    public RemoteAVInfo() {
        this.audioBitrate = new ObservableInt(0);
        this.audioSampleRate = new ObservableInt(0);
        this.audioChannel = new ObservableInt(0);
        this.audioVol = new ObservableInt(0);
        this.fps = new ObservableInt(0);
        this.resolution = new ObservableField<>("");
        this.loss = new ObservableInt(0);
        this.videoBitrate = new ObservableInt(0);

        this.onlineToSubAudioTime = new ObservableLong(0);
        this.subAudioToSubSuccessTime = new ObservableLong(0);
        this.onlineToSubVideoTime = new ObservableLong(0);
        this.subVideoToSubSuccessTime = new ObservableLong(0);
        this.subVideoSuccessToFirstFrameTime = new ObservableLong(0);
        this.subAudioSuccessToFirstFrameTime = new ObservableLong(0);
    }

    public void reset(){
        onlineToSubAudioTime.set(0);
        subAudioToSubSuccessTime.set(0);
        onlineToSubVideoTime.set(0);
        subVideoToSubSuccessTime.set(0);
        audioBitrate.set(0);
        audioSampleRate.set(0);
        audioChannel.set(0);
        audioVol.set(0);
        fps.set(0);
        videoBitrate.set(0);
        resolution.set("");
        subVideoSuccessToFirstFrameTime.set(0);
        subAudioSuccessToFirstFrameTime.set(0);
        loss.set(0);
    }
}
