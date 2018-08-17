package com.hijiyam_koubou.recoverybrain;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRActivity extends AppCompatActivity {
	private LinearLayout scam_ll;
	private DecoratedBarcodeView qrReaderView;
	private ImageButton release_bt;
	private LinearLayout coment_ll;

	private LinearLayout prevew_ll;
	//	private ImageView preveiw_iv;
	private Button acsess_bt;
	private Button rescan_bt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[QRA]";
		String dbMsg = "";
		try {
			setContentView(R.layout.activity_qr);
			scam_ll = ( LinearLayout ) findViewById(R.id.scam_ll);
			qrReaderView = ( DecoratedBarcodeView ) findViewById(R.id.decoratedBarcodeView);
			release_bt = ( ImageButton ) findViewById(R.id.release_bt);
			coment_ll = ( LinearLayout ) findViewById(R.id.coment_ll);

			prevew_ll = ( LinearLayout ) findViewById(R.id.prevew_ll);
//			preveiw_iv = (ImageView ) findViewById(R.id.preveiw_iv);
			acsess_bt = ( Button ) findViewById(R.id.acsess_bt);
			rescan_bt = ( Button ) findViewById(R.id.rescan_bt);
			prevew_ll.setVisibility(View.GONE);
			laterCreate();
			//onActivityResult で結果を受け取るパターン
//					new IntentIntegrator(MainActivity.this).initiateScan();   				//これだけでもQRコードアクティビティを形成して、onActivityResultで内容を取得できる

//		DecoratedBarcodeView qrReaderView = findViewById(R.id.decoratedBarcodeView);
//		startCapture()
//		new IntentIntegrator(QRActivity.this).initiateScan();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String TAG = "onResume[QRA]";
		String dbMsg = "";
		try {
			qrReaderView.resume();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onPause() {
		final String TAG = "onPause[QRA]";
		String dbMsg = "";
		try {
			qrReaderView.pause();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode , int resultCode , Intent data) {
		final String TAG = "onActivityResult[QRA]";
		String dbMsg = "requestCode=" + requestCode + ",resultCode=" + resultCode;
		try {
//			IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//			if(result != null) {
//				String dataURI  = result.getContents();
//				dbMsg = ",dataURI="+dataURI;
//				Intent webIntent = new Intent(this , CS_Web_Activity.class);
//				webIntent.putExtra("dataURI" , dataURI);
//				startActivity(webIntent);
//			} else {
//				super.onActivityResult(requestCode, resultCode, data);
//			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		final String TAG = "onKeyDown[QRA]";
		String dbMsg = "開始";
		try {
			dbMsg = "keyCode=" + keyCode;//+",getDisplayLabel="+String.valueOf(MyEvent.getDisplayLabel())+",getAction="+MyEvent.getAction();////////////////////////////////
			myLog(TAG , dbMsg);
			switch ( keyCode ) {    //キーにデフォルト以外の動作を与えるもののみを記述★KEYCODE_MENUをここに書くとメニュー表示されない
				case KeyEvent.KEYCODE_HOME:            //3
				case KeyEvent.KEYCODE_BACK:            //4KEYCODE_BACK :keyCode；09SH: keyCode；4,MyEvent=KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
					callQuit();
					return true;
				default:
					return false;
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			return false;
		}
	}


	public void laterCreate() {
		final String TAG = "laterCreate[QRA]";
		String dbMsg = "";
		try {
			int orientation = getResources().getConfiguration().orientation;
			dbMsg += "orientation=" + orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
				dbMsg += "=横向き";
			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
				dbMsg += "=縦向き";
			}

			release_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {                    // ボタンがクリックされた時に呼び出されます
					final String TAG = "release_bt[MA]";
					String dbMsg = "";
					try {
//						Button button = (Button) v;
						startCapture();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			acsess_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {    // ボタンがクリックされた時に呼び出されます
					final String TAG = "acsess_bt[MA]";
					String dbMsg = "";
					try {
						Button button = ( Button ) v;
						String dataURI = ( String ) button.getText();
//				String dataURI = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";					//extras.getString("dataURI");                        //最初に表示するページのパス
//					String dataURI = "http://192.168.3.14:3080";
						dbMsg = ",dataURI=" + dataURI;
						Intent webIntent = new Intent(QRActivity.this , CS_Web_Activity.class);
						webIntent.putExtra("dataURI" , dataURI);
						startActivity(webIntent);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			rescan_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {                    // ボタンがクリックされた時に呼び出されます
					final String TAG = "rescan_bt[MA]";
					String dbMsg = "";
					try {
						reStart();    //☆再スキャンできないのでリスタート
//						startCapture();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
			release_bt.setVisibility(View.GONE);
			startCapture();
//			navi_head_sub_tv.setText(rootUrlStr);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void callQuit() {
		final String TAG = "callQuit[MA]";
		String dbMsg = "";
		try {
//			sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);            //	getActivity().getBaseContext()
//			myEditor = sharedPref.edit();
////			myEditor.putString("peer_id_key" , "");      //使用した
////			boolean kakikomi = myEditor.commit();
			this.finish();
//			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
//				finishAndRemoveTask();                      //アプリケーションのタスクを消去する事でデバッガーも停止する。
//			} else {
//				moveTaskToBack(true);                       //ホームボタン相当でアプリケーション全体が中断状態
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void reStart() {
		final String TAG = "reStart[MA}";
		String dbMsg = "";
		try {
			Intent intent = new Intent();
			intent.setClass(this , this.getClass());
			this.startActivity(intent);
			this.finish();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	private void startCapture() {
		final String TAG = "startCapture[MA]";                        //
		String dbMsg = "";
		try {
			prevew_ll.setVisibility(View.GONE);
			coment_ll.setVisibility(View.VISIBLE);
//						release_bt.setVisibility(View.VISIBLE);
			qrReaderView.decodeSingle(new BarcodeCallback() {
				@Override
				public void barcodeResult(BarcodeResult barcodeResult) {
					final String TAG = "barcodeResult[MA]";
					String dbMsg = "";
					try {
						String dataURI = barcodeResult.getText();
						dbMsg = ",dataURI=" + dataURI;
						if ( dataURI.startsWith("http") ) {
							coment_ll.setVisibility(View.GONE);
							release_bt.setVisibility(View.GONE);
//						acsess_bt.setVisibility(View.VISIBLE);
//						scam_ll.setVisibility(View.GONE);
							prevew_ll.setVisibility(View.VISIBLE);
//					preveiw_iv = (ImageView ) findViewById(R.id.preveiw_iv);
							acsess_bt.setText(dataURI);

//						Bitmap bitmap = barcodeEncoder.encodeBitmap(data,BarcodeFormat.QR_CODE, size, size);

//						navi_head_sub_tv.setText(dataURI);
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);

					}

				}

				@Override
				public void possibleResultPoints(List< ResultPoint > list) {
					final String TAG = "possibleResultPoints[MA]";
					String dbMsg = "";
					try {
//						dbMsg += "list = " + list.size() + "件";
//						for (Object  rStr: list){
//							dbMsg += "\n = " + rStr;
//						}
//						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
//		qrReaderView?.decodeSingle(object : BarcodeCallback {
//			override fun barcodeResult(result: BarcodeResult?) {
//				stopCapture()
//				if (result == null) {
//					// no result
//					Log.w(TAG, "No result")
//					return
//				}
//				Log.i(TAG, "QRCode Result: ${result.text}")
//				val bytes = result.resultMetadata[ResultMetadataType.BYTE_SEGMENTS] as? List<*>
//				val data = bytes?.get(0) as? ByteArray ?: return
//
//																  // print result
//																  val resultString = StringBuffer()
//				data.map { byte ->
//					resultString.append(String.format("0x%02X,", byte))
//				}
//				Log.i(TAG, resultString.toString())
//			}
//			override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
//		})
//		qrReaderView?.resume()

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , QRActivity.this);
	}

	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}
}


/**
 * AndroidアプリでQRコードの読取、生成をする     https://qiita.com/11Kirby/items/0f496fe80df84875c132
 **/


