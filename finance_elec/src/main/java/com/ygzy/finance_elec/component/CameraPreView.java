package com.ygzy.finance_elec.component;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.ygzy.finance_elec.Constant;
import com.ygzy.finance_elec.R;
import com.ygzy.finance_elec.component.camera.ImageMessage;
import com.ygzy.finance_elec.component.camera.control.CameraControlManager;
import com.ygzy.finance_elec.component.camera.view.CameraSurfaceView;
import com.ygzy.finance_elec.utils.SpDataUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import io.dcloud.feature.uniapp.UniSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.ui.action.AbsComponentData;
import io.dcloud.feature.uniapp.ui.component.AbsVContainer;
import io.dcloud.feature.uniapp.ui.component.UniComponent;

/**
 * @author : taomf
 * Date    : 2024/11/18/14:02
 * Desc    : 相机预览
 */
public class CameraPreView extends UniComponent<FrameLayout> {
    private final String TAG = "CameraPreView";
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private int cameraType = CameraSelector.LENS_FACING_FRONT;
    private String[] cameraIdList;
    TextView textView;

    private long firstTime= 0;
    private ArrayList<Long> hits = new ArrayList<>();

    public CameraPreView(UniSDKInstance uniSDKInstance, AbsVContainer absVContainer, int i, AbsComponentData absComponentData) {
        super(uniSDKInstance, absVContainer, i, absComponentData);
    }

    public CameraPreView(UniSDKInstance uniSDKInstance, AbsVContainer absVContainer, AbsComponentData absComponentData) {
        super(uniSDKInstance, absVContainer, absComponentData);
    }

    private CameraSurfaceView cameraSurfaceView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable errorRunnable =  () -> {
        imgPath(createImages("拍照失败，请检查相机状态"));
    };

    /**
     * 是否拍照中
     */
    private boolean isTakePhoto = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected FrameLayout initComponentHostView(Context context) {
        if (Build.MODEL.equals(Constant.newScale2) || Build.MODEL.equals(Constant.newScale)  || Build.MODEL.equals(Constant.oldScale1)){
            EventBus.getDefault().register(this);

            FrameLayout frame =  new FrameLayout(context);

            cameraSurfaceView = new CameraSurfaceView(context);
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);

            frame.addView(cameraSurfaceView, params);
            frame.addView(changeCameraText(context,0));
            return frame;
        }

        FrameLayout frameLayout =  new FrameLayout(context);
        previewView = new PreviewView(context);

        frameLayout.addView(previewView);
        frameLayout.addView(changeCameraText(context,1));
        return frameLayout;
    }

    /**
     * @return 切换摄像头文本
     * @param context 上下文
     * @param type 0:camera 1:cameraX
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private TextView changeCameraText(Context context,int type){
        textView = new TextView(context);
        textView.setText("翻转镜头");
        textView.setTextSize(20);
        textView.setAlpha(0f);
        textView.setTextColor(context.getColor(R.color.color_ic_launcher));
        textView.setTypeface(null, Typeface.BOLD); // 设置字体样式
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, // 设置宽度为 WRAP_CONTENT
                FrameLayout.LayoutParams.WRAP_CONTENT // 设置高度为 WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 0); // 设置外边距，例如右边距为 10dp
        layoutParams.gravity = Gravity.TOP | Gravity.END; // 设置对齐方式
        textView.setPadding(30,30,30,30);
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(v -> {
            if (textView.getAlpha() == 1f && System.currentTimeMillis() - firstTime > 1000){
                firstTime = System.currentTimeMillis();
                if (type == 0){
                   switchCamera();
                }else {
                   changeCamera();
                }
            }else {
                long hitTime = System.currentTimeMillis();
                if (hitTime - firstTime > 500){
                    hits.clear();
                }
                firstTime = hitTime;
                hits.add(hitTime);
                if (hits.size() == 3){
                    if((hits.get(2) - hits.get(0)) < 1500){
                        if (Camera.getNumberOfCameras() == 2){
                            textView.setAlpha(1f);
                        }
                        hits.clear();
                    }
                }
            }
        });
        return  textView;
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
    }
    /**
     * 启动相机，开机相机取景预览
     */
    @UniJSMethod
    public void startCamera() {
        LogUtils.d(TAG, "onCreate: " + Build.MODEL);
        if (Build.MODEL.equals(Constant.newScale2) || Build.MODEL.equals(Constant.newScale)) return;
        openCamera();
    }

    @UniJSMethod
    public void hideNavBar() {
        BarUtils.setNavBarVisibility(((Activity) mUniSDKInstance.getContext()).getWindow(),false);
    }

    @UniJSMethod
    public void showNavBar() {
        BarUtils.setNavBarVisibility(((Activity) mUniSDKInstance.getContext()).getWindow(),true);
    }

    @UniJSMethod
    public void closeCamera() {
        LogUtils.d(TAG, "close摄像头");
        if(cameraProvider != null){
            cameraProvider.unbindAll();
        }
    }

    @UniJSMethod
    public void changeCamera() {
        if (cameraIdList.length == 2){
            cameraProvider.unbindAll();
            LifecycleOwner lifecycleOwner = (LifecycleOwner) mUniSDKInstance.getContext();
            CameraSelector cameraSelector;
            if(cameraType == CameraSelector.LENS_FACING_FRONT){
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraType = CameraSelector.LENS_FACING_BACK;
            }else {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraType = CameraSelector.LENS_FACING_FRONT;
            }
            SpDataUtils.saveCameraType(cameraType);
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);
        }
    }

    private void switchCamera() {
        if(Camera.getNumberOfCameras() == 2){
            if (SpDataUtils.getCameraType() == 0){
                SpDataUtils.saveCameraType(1);
            } else {
                SpDataUtils.saveCameraType(0);
            }
            cameraSurfaceView.switchCamera();
        }
    }

    /**
     * 打开相机
     */
    public void openCamera() {
        LogUtils.d(TAG, "open摄像头");
//        BarUtils.setNavBarVisibility(((Activity) mUniSDKInstance.getContext()).getWindow(),false);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mUniSDKInstance.getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                LogUtils.d(TAG, "open摄像头2");

                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraManager cameraManager=(CameraManager) mUniSDKInstance.getContext().getSystemService(Context.CAMERA_SERVICE);
                cameraIdList = cameraManager.getCameraIdList();

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector;
                if(cameraIdList.length > 1){
                    int type = SpDataUtils.getCameraType();
                    if (type == CameraSelector.LENS_FACING_FRONT){
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                        cameraType = CameraSelector.LENS_FACING_FRONT;
                    }else if (type == CameraSelector.LENS_FACING_BACK){
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                        cameraType = CameraSelector.LENS_FACING_BACK;
                    }else {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                        cameraType = CameraSelector.LENS_FACING_FRONT;
                    }
                }else {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraType = CameraSelector.LENS_FACING_BACK;
                }

                cameraProvider.unbindAll();
                LifecycleOwner lifecycleOwner = (LifecycleOwner) mUniSDKInstance.getContext();
                //绑定生命周期，防止内存泄露
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                LogUtils.d(TAG, e.toString());
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mUniSDKInstance.getContext()));
    }

    /**
     * 拍照
     */
    @UniJSMethod
    public void takePhoto() {
        if (Build.MODEL.equals(Constant.newScale2) || Build.MODEL.equals(Constant.newScale)  || Build.MODEL.equals(Constant.oldScale1)){
            CameraControlManager cameraControlManager = cameraSurfaceView.getCameraControlManager();
            if ( cameraControlManager != null && cameraControlManager.getCamera() != null){
                if (!isTakePhoto){
                    isTakePhoto = true;
                    handler.postDelayed(errorRunnable,5000);

                    takeRectPicture();
                }
            }else {
                ToastUtils.showLong("相机打开失败 camera = null");
                imgPath(createImages("相机打开失败 camera = null"));
            }
            return;
        }

        if (imageCapture == null){
            ToastUtils.showShort("相机初始化中...");
            imgPath("");
            return;
        }
        File dir = new File(mUniSDKInstance.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyCameraFolder");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir,System.currentTimeMillis() + ".jpg");
//        File file = new File(initPhotoPath(), TimeUtils.getNowMills() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this.getContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                ToastUtils.showShort("拍照成功");
                if (textView != null){
                    textView.setAlpha(0f);
                }
                imgPath(file.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                ToastUtils.showShort("拍照出错" + exception.getCause() + exception.getImageCaptureError());
                imgPath(createImages("拍照出错" + exception.getCause() + exception.getImageCaptureError()));
            }
        });
    }

    public String initPhotoPath() {
        File directory = Environment.getExternalStorageDirectory();
        String path = directory.getAbsolutePath() + "/PlayCameraS";
        File file = new File(path);
        file.mkdirs();
        return path;
    }

    private void imgPath(String path) {
        isTakePhoto = false;
        handler.removeCallbacks(errorRunnable);

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> number = new HashMap<>();
        number.put("path", path);
        //目前uni限制 参数需要放入到"detail"中 否则会被清理
        params.put("detail", number);
        fireEvent("onTel", params);
        LogUtils.d(TAG, "imgPath: " + path);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSaveFilePath(ImageMessage message) {
        if (textView != null){
            textView.setAlpha(0f);
        }
        if (message.getType() == ImageMessage.TYPE_IMAGE){
            imgPath(message.getMessage());
        }else {
            String createImage = createImages(message.getMessage());
            imgPath(createImage);
        }
    }

    /**
     * 拍摄(true 指定区域 false 全局)
     *
     */
    public void takeRectPicture(){
        CameraControlManager.getInstance().doTakePicture();
    }

    @UniJSMethod
    public void stopCamera(){
        Log.i("szm--", "停止相机");
        CameraControlManager.getInstance().doStopCamera();
    }

    /**
     * 创建图片
     * @param text 绘制的文字
     * @return 图片路径
     */
    private String createImages(String text) {
        // 1. 创建空白 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        // 2. 绑定 Canvas
        Canvas canvas = new Canvas(bitmap);
        // 3. 绘制背景色（可选）
        canvas.drawColor(Color.GRAY);
        // 4. 配置画笔
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)); // 粗体

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40); // 单位：像素
        textPaint.setAntiAlias(true);

        StaticLayout staticLayout  = new StaticLayout(
                text,
                textPaint,
                1600,
                Layout.Alignment.ALIGN_NORMAL, // 左对齐
                1.0f, // 行间距倍数（1.0倍字体高度）
                0.0f, // 额外行间距
                true // 是否包含内边距
        );

        // 5. 计算基线位置（垂直居中）
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baselineY = (float) bitmap.getHeight() / 2 - (fm.ascent + fm.descent) / 2;

        // 5. 绘制文本到Canvas
        canvas.save();
        canvas.translate(50, baselineY); // 设置文本起始位置
        staticLayout .draw(canvas);
        canvas.restore();

        // 7. 保存图片
        try {
            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) 系统图片目录
            File dir = new File(mUniSDKInstance.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyCameraFolder");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir,System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            ToastUtils.showLong("保存失败");
        } finally {
            bitmap.recycle();
        }
        return "";
    }

}
