package com.hijiyam_koubou.recoverybrain;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BmpSaver implements Runnable {        //static ?必要？
	public CS_Util UTIL;
	private Activity activity;
	private Context context;
	/**
	 * The JPEG image
	 */
	private static Bitmap shotBitmap;
	/**
	 * The file we save the image into.
	 */
	private File mFile;
	private String saveFileName;
	public boolean isSaveEnd = true;

	public BmpSaver(Context _context , Activity _activity , Bitmap bitmap , String _saveFileName  ) {                //static
		final String TAG = "BmpSaver[BS]";
		String dbMsg = "";
		try {
			isSaveEnd = true;
			activity = _activity;
			context = _context;
			shotBitmap = bitmap;
			saveFileName = _saveFileName;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void run() {
		final String TAG = "BmpSaver.run[BS]";
		String dbMsg = "";
		try {
			int width = shotBitmap.getWidth();
			int height = shotBitmap.getHeight();
			int byteCount = shotBitmap.getByteCount();
			dbMsg += ",shotBitmap[" + width + "×" + height + "]=" + byteCount + "バイト";
			if ( 0 < byteCount ) {
				FileOutputStream output = null;
				try {
					dbMsg += ",isSaveEnd=" + isSaveEnd ;
					if (isSaveEnd ) {
						isSaveEnd = false;
						dbMsg += ",saveFileName=" + saveFileName;
						mFile = new File(saveFileName);                 //getActivity().getExternalFilesDir(null)
						if ( mFile.exists() ) {
							mFile.delete();
						}
						dbMsg += ",mFile=" + mFile.toString();
						output = new FileOutputStream(mFile);
						shotBitmap.compress(Bitmap.CompressFormat.PNG , 100 , output);//画像をJPEG形式として保存
						if ( UTIL == null ) {
							UTIL = new CS_Util();
						}
						UTIL.setContentValues( activity, "image/png", saveFileName);
					}
				} catch (IOException er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				} finally {
//					shotBitmap.recycle();
					if ( null != output ) {
						try {

							output.close();
						} catch (IOException er) {
							myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
						}
						dbMsg += ",output破棄"  ;

					}
				}
			}
			isSaveEnd = true;
//			dbMsg += ",isSaveEnd=" + isSaveEnd ;

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}



	///////////////////////////////////////////////////////////////////////////////////
//	public void messageShow(String titolStr , String mggStr) {
//		if ( UTIL == null ) {
//			UTIL = new CS_Util();
//		}
//		UTIL.messageShow(titolStr , mggStr , MainActivity.this);
//	}

	public void myLog(String TAG , String dbMsg) {
		if ( UTIL == null ) {
			UTIL = new CS_Util();
		}
		UTIL.myLog(TAG , dbMsg);
	}

	public void myErrorLog(String TAG , String dbMsg) {
		isSaveEnd = true;
		if ( UTIL == null ) {
			UTIL = new CS_Util();
		}
		UTIL.myErrorLog(TAG , dbMsg);
	}

}