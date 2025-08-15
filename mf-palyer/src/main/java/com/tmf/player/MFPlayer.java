package com.tmf.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.jzvd.JZDataSource;
import io.dcloud.feature.uniapp.UniSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.ui.action.AbsComponentData;
import io.dcloud.feature.uniapp.ui.component.AbsVContainer;
import io.dcloud.feature.uniapp.ui.component.UniComponent;
import io.dcloud.feature.uniapp.ui.component.UniComponentProp;

/**
 * @author : taomf
 * Date    : 2025/8/9 009/10:44
 * Desc    : 播放器
 */
public class MFPlayer extends UniComponent<AGVideo> implements AGVideo.JzVideoListener ,VideoSpeedPopup.SpeedChangeListener{

    public MFPlayer(UniSDKInstance instance, AbsVContainer parent, AbsComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    public MFPlayer(UniSDKInstance uniSDKInstance, AbsVContainer absVContainer, int i, AbsComponentData absComponentData) {
        super(uniSDKInstance, absVContainer, i, absComponentData);
    }

    private AGVideo mPlayer;

    private JZDataSource mJzDataSource;

    private VideoSpeedPopup videoSpeedPopup;



    @Override
    protected AGVideo initComponentHostView(Context context) {
        mPlayer = new AGVideo(context);
        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.height = 200;

        mPlayer.setLayoutParams(params);
        mPlayer.setJzVideoListener(this);

        return mPlayer;
    }


    @UniComponentProp(name = "tel")
    public void setTel(String telNumber) {
    }



    @UniJSMethod
    public void play() {
        ToastUtils.showShort("播放");
        LogUtils.d("taomf==", mPlayer == null );
        LogUtils.d("taomf==", mPlayer.mediaInterface == null );
        if (mPlayer.mediaInterface == null){
            mPlayer.startVideo();
            return;
        }

        if (!mPlayer.mediaInterface.isPlaying()){
            mPlayer.mediaInterface.start();
        }
    }

    @UniJSMethod
    public void pause() {
        ToastUtils.showShort("暂停");
        mPlayer.mediaInterface.pause();
    }

    @SuppressLint("CheckResult")
    @UniJSMethod
    public void initPlayer(JSONObject options) {
        LogUtils.d("taomf==", options.toJSONString());
        JSONObject video = options.getJSONObject("video");

        LinkedHashMap<String, String> map = JSONObject.parseObject(video.toJSONString(),LinkedHashMap.class);
        String logoUrlString =  options.getString("logoUrlString");

        Glide.with(mUniSDKInstance.getContext()).load(logoUrlString).into((ImageView)mPlayer.findViewById(R.id.screen));

        mJzDataSource = new JZDataSource(map);
        mPlayer.setUp(mJzDataSource,0);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
    }

    @Override
    public void nextClick() {}

    @Override
    public void backClick() {
        ToastUtils.showShort("返回");
        if (mPlayer.screen == mPlayer.SCREEN_FULLSCREEN) {
            dismissSpeedPopAndEpisodePop();
            AGVideo.backPress();
        } else {
//            finish();
            fireEvent("exitPlayer");
        }
    }

    /**
     * 关闭倍速播放弹窗和选集弹窗
     */
    private void dismissSpeedPopAndEpisodePop() {
        if (videoSpeedPopup != null) {
            videoSpeedPopup.dismiss();
        }
    }

    /**
     * 跳转
     */
    @Override
    public void throwingScreenClick() {}

    /**
     * 选择集
     */
    @Override
    public void selectPartsClick() {}

    @Override
    public void speedClick() {
        if (videoSpeedPopup == null) {
            videoSpeedPopup = new VideoSpeedPopup(mUniSDKInstance.getContext());
            videoSpeedPopup.setSpeedChangeListener(this);
        }
        videoSpeedPopup.showAtLocation(ActivityUtils.getActivityByContext(mUniSDKInstance.getContext()).getWindow().getDecorView(), Gravity.RIGHT, 0, 0);
    }

    @Override
    public void completion() {
        ToastUtils.showShort("播放完成");

        fireEvent("completion");
    }

    @Override
    public void onProgress(int progress, long position, long duration) {
//        LogUtils.d("taomf==", "progress: " + progress + "\r\n position: " + position + "\r\n duration: " + duration);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> number = new HashMap<>();
        number.put("progress", progress);
        number.put("position", position);
        number.put("duration", duration);
        params.put("detail", number);
        fireEvent("onProgress", params);
    }

    @Override
    public void speedChange(float speed) {
        Object[] object = { speed };
        mPlayer.mediaInterface.setSpeed(speed);
        mJzDataSource.objects[0] = object;
        ToastUtils.showShort("正在以" + speed + "X倍速播放");
        mPlayer.speedChange(speed);
    }

}
