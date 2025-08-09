package com.ygzy.finance_elec.component.camera.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.ygzy.finance_elec.FinanceHooks;
import com.ygzy.finance_elec.component.CameraPreView;
import com.ygzy.finance_elec.component.camera.ImageMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	private static final String TAG = "szm--";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";

	/**初始化保存路径
	 * @return
	 */
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}

	/**保存Bitmap到sdcard
	 * @param b
	 */
	public static void saveBitmap(Bitmap b){

//		String path = initPath();

		File dir = new File(FinanceHooks.photoPath,"MyCameraFolder");
//		File dir = new File(initPath());
		if (!dir.exists()) dir.mkdirs();
		File file = new File(dir,System.currentTimeMillis() + ".jpg");

//		String jpegName = path + "/" + DateUtils.getNowTimeSend()  +".jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + file.getAbsolutePath());
		try {
			FileOutputStream fout = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 50, bos);
			bos.flush();
			bos.close();
			Log.i(TAG, "saveBitmap成功");
			EventBus.getDefault().postSticky(new ImageMessage(file.getAbsolutePath(),ImageMessage.TYPE_IMAGE));
		} catch (Exception e) {
			ToastUtils.showLong("图片保存失败:" + e);
			EventBus.getDefault().postSticky(new ImageMessage("图片保存失败:" + e,ImageMessage.TYPE_ERROR));
			Log.i(TAG, "saveBitmap:失败" + e);
		}

	}


}
