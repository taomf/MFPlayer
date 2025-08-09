package com.ygzy.finance_elec.component.camera.control;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.Log;
import android.view.SurfaceHolder;


import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ygzy.finance_elec.component.camera.ImageMessage;
import com.ygzy.finance_elec.component.camera.utils.FileUtil;
import com.ygzy.finance_elec.utils.SpDataUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

/**
 * 摄像头控制管理类
 * （打开，开启预览）
 * @author michael
 *
 */
public class CameraControlManager {

	/**摄像头对象*/
	private Camera mCamera;


	private static CameraControlManager cameraControlManager;
	/**是否出于预览状态*/
	private boolean isPreviewing;

	private CameraControlManager(){

	}

	public static CameraControlManager getInstance(){
		if(cameraControlManager==null){
			synchronized (CameraControlManager.class) {
				if(cameraControlManager==null){
					cameraControlManager=new CameraControlManager();
				}
			}
		}
		return cameraControlManager;
	}

	/**
	 * 开启摄像头
	 */
	public void doOpenCamera(){
		try{
			int cameraType = SpDataUtils.getCameraType();
			if (cameraType != -1 && Camera.getNumberOfCameras() > 1){
				mCamera= Camera.open(cameraType);
			} else {
				mCamera= Camera.open(Camera.getNumberOfCameras() - 1);
			}

		}catch (Exception e){
			ToastUtils.showLong("打开相机失败失败" + e);
			LogUtils.e("taomf",e.toString());
		}
	}

	/**
	 * 设置参数
	 * @param parameters
	 */
	public void setParameters(Camera.Parameters parameters){
		// 获取所有支持的图片尺寸
		List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
		// 选择一个合适的分辨率，这里以1080p为例
		Camera.Size optimalSize = getOptimalSize(supportedPictureSizes, 1920, 1080);

		// 获取所有支持的预览尺寸
		List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

		if (optimalSize != null) {
			parameters.setPictureSize(optimalSize.width, optimalSize.height);
		}
		// 同样地，设置预览分辨率
		optimalSize = getOptimalSize(supportedPreviewSizes, 1920, 1080);
		if (optimalSize != null) {
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
		}

		mCamera.setParameters(parameters);
	}

	private static Camera.Size getOptimalSize(List<Camera.Size> sizes, int width, int height) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) width / height;
		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = height;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	/**
	 * 开始预览
	 */
	public void startPreView(){
		mCamera.setDisplayOrientation(0);
		mCamera.startPreview();
		isPreviewing=true;
	}

	/**
	 * 获取android.hardware.Camera对象
	 * @return
	 */
	public Camera getCamera(){
		return mCamera;
	}

	/**
	 * 停止相机
	 */
	public void doStopCamera() {
		if(mCamera != null){
			mCamera.stopPreview();
			isPreviewing=false;
			mCamera.release();
			mCamera=null;
		}
	}

	/**
	 * 绑定surface到摄像头
	 * @param holder
	 */
	public void setPreviewDisplay(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			mCamera.release();
		}
	}

	/**
	 * 普通拍照
	 */
	public void doTakePicture(){
		if(isPreviewing&&mCamera!=null){
			try {
				mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
			} catch (Exception e) {
				EventBus.getDefault().post( new ImageMessage("拍照失败，请检查相机状态" + e,ImageMessage.TYPE_ERROR));
				ToastUtils.showLong("拍照失败，请检查相机状态" + e);
			}
		}else {
			EventBus.getDefault().post(new ImageMessage("拍照失败，相机加载失败" + "isPreviewing=" + isPreviewing + "mCamera=" + (mCamera != null) ,ImageMessage.TYPE_ERROR));
			ToastUtils.showLong("拍照失败，相机加载失败");
		}
	}

	int DST_RECT_WIDTH, DST_RECT_HEIGHT,SCREEN_WIDTH,SCREEN_HEIGHT;
	/**
	 * 拍摄指定区域方法
	 * @param w
	 * @param h
	 */
	public void doTakePicture(int w, int h,int screenW,int screenH){
		if(isPreviewing && (mCamera != null)){
			Log.i("szm--", "矩形拍照尺寸:width = " + w + " h = " + h);
			DST_RECT_WIDTH = w;
			DST_RECT_HEIGHT = h;
			SCREEN_WIDTH=screenW;
			SCREEN_HEIGHT=screenH;
			mCamera.takePicture(mShutterCallback, null, mRectJpegPictureCallback);
		}
	}

	/*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
	ShutterCallback mShutterCallback = new ShutterCallback()
			//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	{
		public void onShutter() {
			// 设置快门声
		}
	};

	/**
	 * 常规拍照
	 */
	PictureCallback mJpegPictureCallback = new PictureCallback()
			//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i("szm--", "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
//			FileUtil.saveImageFile(data);
			if(null != data && data.length != 0){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}else{
				ToastUtils.showLong("拍照失败，数据为空");
				EventBus.getDefault().post(new ImageMessage("拍照失败，数据为空",ImageMessage.TYPE_ERROR));
			}
			//保存图片到sdcard
			if(null != b){
				FileUtil.saveBitmap(b);
			}
			//再次进入预览
			mCamera.startPreview();
			isPreviewing = true;

			if(b != null && !b.isRecycled()){
				b.recycle();
			}
		}
	};

	/**
	 * 拍摄指定区域的Rect
	 */
	PictureCallback mRectJpegPictureCallback = new PictureCallback()
			//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i("szm--", "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//保存图片到sdcard
			if(null != b)
			{
				int scalW=DST_RECT_WIDTH*b.getWidth()/SCREEN_WIDTH;
				int scalH=DST_RECT_HEIGHT*b.getHeight()/SCREEN_HEIGHT;
				int x = (b.getWidth() - scalW)/2;
				int y = (b.getHeight()-scalH)/2;
				Log.i("szm--", "---x=="+x+"---y=="+y);
				Log.i("szm--", "b.getWidth() = " + b.getWidth()
						+ " b.getHeight() = " + b.getHeight());
				Bitmap rectBitmap = Bitmap.createBitmap(b, x, y, scalW, scalH);
				FileUtil.saveBitmap(rectBitmap);
				if(b.isRecycled()){
					b.recycle();
					b = null;
				}
				if(rectBitmap.isRecycled()){
					rectBitmap.recycle();
					rectBitmap = null;
				}
			}
			//再次进入预览
			mCamera.startPreview();
			isPreviewing = true;
			if(!b.isRecycled()){
				b.recycle();
				b = null;
			}

		}
	};
}
