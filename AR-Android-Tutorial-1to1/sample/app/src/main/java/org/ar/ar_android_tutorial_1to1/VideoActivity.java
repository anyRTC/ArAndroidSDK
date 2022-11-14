package org.ar.ar_android_tutorial_1to1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.viewbinding.ViewBinding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.ar.ar_android_tutorial_1to1.databinding.ActivityMainBinding;
import org.ar.rtc.Constants;
import org.ar.rtc.IRtcEngineEventHandler;
import org.ar.rtc.RtcEngine;
import org.ar.rtc.VideoEncoderConfiguration;
import org.ar.rtc.video.VideoCanvas;
import org.ar.uikit.logger.LoggerRecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.TextureViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoActivity extends AppCompatActivity {

    private String userId = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private String remoteId = "";
    ActivityMainBinding binding;
    private LocalAVInfo localAVInfo = new LocalAVInfo();
    private RemoteAVInfo remoteAVInfo = new RemoteAVInfo();
    private long remoteJoinTime = 0;
    private long audioStartSubTime = 0;
    private long videoStartSubTime = 0;
    private long audioSubSuccessTime = 0;
    private long videoSubSuccessTime = 0;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private HashMap<String,TextureView> renderers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setMLocal(localAVInfo);
        binding.setRemote(remoteAVInfo);

        if (getString(R.string.show_avInfo).equals("1")){
            localAVInfo.showInfo.set(true);
        }

        binding.btnCall.setOnClickListener(v -> {
            if (mCallEnd) {
                startCall();
                mCallEnd = false;
                binding.btnCall.setImageResource(R.drawable.img_leave);
            } else {
                endCall();
                mCallEnd = true;
                binding.btnCall.setImageResource(R.drawable.img_join);
            }
            showButtons(!mCallEnd);
        });
        binding.btnVideoMute.setOnClickListener(v -> {
            binding.btnVideoMute.setSelected(!binding.btnVideoMute.isSelected());
            mRtcEngine.muteLocalVideoStream(binding.btnVideoMute.isSelected());
        });
        binding.btnSwitchCamera.setOnClickListener(v -> {
            mRtcEngine.switchCamera();
        });
        binding.btnAudioMute.setOnClickListener(v -> {
            binding.btnAudioMute.setSelected(!binding.btnAudioMute.isSelected());
            mRtcEngine.muteLocalAudioStream(binding.btnAudioMute.isSelected());
        });

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }


    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getResources().getString(R.string.app_id), mRtcEventHandler);
            //启用视频模块
            mRtcEngine.enableVideo();
            mRtcEngine.startPreview();
            JSONObject json =new JSONObject();
            try {
                json.put("Cmd","ConfPriCloudAddr");
                json.put("ServerAdd","0.0.0.0");
                json.put("Port",6080);
//                mRtcEngine.setParameters(json.toString());//私有云设置
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }


    private void joinChannel() {
        mRtcEngine.joinChannel("", getResources().getString(R.string.channel), "Extra Optional Data", userId);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, final String uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.logRecyclerView.logI("加入频道成功");
                    showButtons(true);
                }
            });
        }

        @Override
        public void onUserJoined(final String uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.logRecyclerView.logI(uid + "加入频道");
                    remoteJoinTime = 0;
                    audioStartSubTime = 0;
                    videoStartSubTime = 0;
                    remoteJoinTime = System.currentTimeMillis();
                }
            });
        }

        @Override
        public void onLocalVideoStats(LocalVideoStats stats) {
            super.onLocalVideoStats(stats);
            runOnUiThread(() -> {
                localAVInfo.videoBitrate.set(stats.sentBitrate);
                localAVInfo.resolution.set(stats.encodedFrameHeight + "x" + stats.encodedFrameWidth);
                localAVInfo.fps.set(stats.sentFrameRate);
                localAVInfo.loss.set(stats.txPacketLossRate);
            });
        }

        @Override
        public void onLocalAudioStats(LocalAudioStats stats) {
            super.onLocalAudioStats(stats);
            runOnUiThread(() -> {
                localAVInfo.audioBitrate.set(stats.sentBitrate);
                localAVInfo.audioSampleRate.set(stats.sentSampleRate);
                localAVInfo.audioChannel.set(stats.numChannels);

            });
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
            runOnUiThread(() -> {
                for (AudioVolumeInfo info : speakers
                ) {
                    if (info.uid.equals("0")) {
                        localAVInfo.audioVol.set(info.volume);
                    }else if (info.uid.equals(remoteId)){
                        remoteAVInfo.audioVol.set(info.volume);
                    }
                }
            });
        }

        // SDK 接收到第一帧远端视频并成功解码时，会触发该回调。
        // 可以在该回调中调用 setupRemoteVideo 方法设置远端视图。
        @Override
        public void onFirstRemoteVideoDecoded(String uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoteAVInfo.subVideoSuccessToFirstFrameTime.set(System.currentTimeMillis()-videoSubSuccessTime);
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onFirstRemoteAudioDecoded(String uid, int elapsed) {
            super.onFirstRemoteAudioDecoded(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoteAVInfo.subAudioSuccessToFirstFrameTime.set(System.currentTimeMillis()-audioSubSuccessTime);
                }
            });
        }

        @Override
        public void onRemoteVideoStats(RemoteVideoStats stats) {
            super.onRemoteVideoStats(stats);
            runOnUiThread(()->{
                remoteAVInfo.videoBitrate.set(stats.receivedBitrate);
                remoteAVInfo.fps.set(stats.decoderOutputFrameRate);
                remoteAVInfo.loss.set(stats.packetLossRate);
                remoteAVInfo.resolution.set(stats.height+"x"+stats.width);
            });

        }

        @Override
        public void onRemoteAudioStats(RemoteAudioStats stats) {
            super.onRemoteAudioStats(stats);
            runOnUiThread(()->{
                remoteAVInfo.audioBitrate.set(stats.receivedBitrate);
                remoteAVInfo.audioSampleRate.set(stats.receivedSampleRate);
                remoteAVInfo.audioChannel.set(stats.numChannels);
            });
        }

        @Override
        public void onAudioSubscribeStateChanged(String channel, String uid, int oldState, int newState, int elapseSinceLastState) {
            super.onAudioSubscribeStateChanged(channel, uid, oldState, newState, elapseSinceLastState);
            runOnUiThread(() -> {
                if (newState==2){//正在订阅
                    audioStartSubTime = System.currentTimeMillis();
                    remoteAVInfo.onlineToSubAudioTime.set(audioStartSubTime-remoteJoinTime);
                }else if (newState ==3){
                    remoteAVInfo.subAudioToSubSuccessTime.set(System.currentTimeMillis()-audioStartSubTime);
                    audioSubSuccessTime = System.currentTimeMillis();
                }
            });
        }

        @Override
        public void onVideoSubscribeStateChanged(String channel, String uid, int oldState, int newState, int elapseSinceLastState) {
            super.onVideoSubscribeStateChanged(channel, uid, oldState, newState, elapseSinceLastState);
            runOnUiThread(() -> {
                if (newState==2){//正在订阅
                    videoStartSubTime = System.currentTimeMillis();
                    remoteAVInfo.onlineToSubVideoTime.set(videoStartSubTime-remoteJoinTime);
                }else if (newState ==3){
                    remoteAVInfo.subVideoToSubSuccessTime.set(System.currentTimeMillis()-videoStartSubTime);
                    videoSubSuccessTime = System.currentTimeMillis();
                }
            });
        }

        @Override
        public void onUserOffline(final String uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.logRecyclerView.logI(uid + "离开频道");
                    removeRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onError(int err) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.logRecyclerView.logI("onError" + err);
                }
            });
        }
    };

    private void setupLocalVideo() {

        //创建TextureView对象
        TextureView mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        if (binding.rlLocalVideo != null) {
            binding.rlLocalVideo.removeAllViews();
        }
        binding.rlLocalVideo.addView(mLocalView);
        renderers.put("local",mLocalView);
        //设置本地视图
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, userId));
    }


    private void setupRemoteVideo(String uid) {
        if (binding.rlRemoteVideo != null) {
            binding.rlRemoteVideo.removeAllViews();
        }
        remoteId = uid;
        TextureView mRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        binding.rlRemoteVideo.addView(mRemoteView);
        renderers.put(uid, mRemoteView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(mRemoteView, Constants.RENDER_MODE_HIDDEN, getResources().getString(R.string.channel), uid, Constants.VIDEO_MIRROR_MODE_DISABLED));
    }


    private void removeRemoteVideo(String uid) {
        if (binding.rlRemoteVideo != null) {
            if (renderers.containsKey(uid)) {
                binding.rlRemoteVideo.removeAllViews();
                ((TextureViewRenderer)renderers.get(uid)).release();
                remoteAVInfo.reset();
                remoteJoinTime = 0;
                videoStartSubTime=0;
                audioStartSubTime=0;
                audioSubSuccessTime = 0;
                videoSubSuccessTime = 0;
            }
        }
    }

    private void removeLocalVideo() {
        if (binding.rlLocalVideo != null) {
            binding.rlLocalVideo.removeAllViews();
            if (renderers.containsKey("local")){
                ((TextureViewRenderer)renderers.get("local")).release();
            }
            localAVInfo.reset();
        }

    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo(remoteId);
        leaveChannel();
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        binding.btnVideoMute.setVisibility(visibility);
        binding.btnSwitchCamera.setVisibility(visibility);
        binding.btnAudioMute.setVisibility(visibility);
    }


    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                showToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                finish();
                return;
            }
            initEngineAndJoinChannel();
        }
    }

    private void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mCallEnd) {
                endCall();
                finish();
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RtcEngine.destroy();
    }
}