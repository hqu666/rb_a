package com.hijiyam_koubou.recoverybrain;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class RecoveryBrainActivity extends AppCompatActivity   implements NavigationView.OnNavigationItemSelectedListener {
	com.hijiyam_koubou.recoverybrain.CS_CanvasView sa_disp_v ;        //表示側
	com.hijiyam_koubou.recoverybrain.CS_CanvasView sa_pad_v ;        //操作側

	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;
	public String rootUrlStr = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";					//	String dataURI = "http://192.168.3.14:3080";	//自宅
	public boolean isReadPref = false;
	public boolean isRecoveryBrain = false;
	public boolean isNotSet = true;

	/**
	 * このアプリケーションの設定ファイル読出し
	 **/
	public void readPref() {
		final String TAG = "readPref[RBS]";
		String dbMsg = "許諾済み";//////////////////
		try {
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                //(初回起動で)全パーミッションの許諾を取る
				dbMsg = "許諾確認";
				String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.INTERNET , Manifest.permission.CAMERA };
//				Manifest.permission.ACCESS_NETWORK_STATE , Manifest.permission.ACCESS_WIFI_STATE ,
// , Manifest.permission.MODIFY_AUDIO_SETTINGS , Manifest.permission.RECORD_AUDIO ,  Manifest.permission.MODIFY_AUDIO_SETTINGS,
				boolean isNeedParmissionReqest = false;
				for ( String permissionName : PERMISSIONS ) {
					dbMsg += "," + permissionName;
					int checkResalt = checkSelfPermission(permissionName);
					dbMsg += "=" + checkResalt;
					if ( checkResalt != PackageManager.PERMISSION_GRANTED ) {
						isNeedParmissionReqest = true;
					}
				}
				if ( isNeedParmissionReqest ) {
					dbMsg += "許諾処理へ";
					requestPermissions(PERMISSIONS , REQUEST_PREF);
					return;
				}
			}		dbMsg += ",isReadPref=" + isReadPref;
			MyPreferenceFragment prefs = new MyPreferenceFragment();
			prefs.readPref(this);
			rootUrlStr = prefs.rootUrlStr;
			dbMsg += ",rootUrlStr=" + rootUrlStr;

			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);            //	getActivity().getBaseContext()
			myEditor = sharedPref.edit();

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final String TAG = "onCreate[RBS]";
		String dbMsg = "";
		try {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);        //タスクバーを 非表示
			requestWindowFeature(Window.FEATURE_NO_TITLE);                            //タイトルバーを非表示
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);        //ローディングをタイトルバーのアイコンとして表示☆リソースを読み込む前にセットする
			readPref();
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_rb);

			LinearLayout sa_disp_ll = ( LinearLayout ) findViewById(R.id.sa_disp_ll);
			sa_disp_v = new  com.hijiyam_koubou.recoverybrain.CS_CanvasView(this,true);        //表示(受信)側
			sa_disp_ll.addView(sa_disp_v);
			LinearLayout sa_pad_ll = ( LinearLayout ) findViewById(R.id.sa_pad_ll);
			sa_pad_v = new  com.hijiyam_koubou.recoverybrain.CS_CanvasView(this,false);        //表示側
			sa_pad_ll.addView(sa_pad_v);

			sa_pad_v.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					final String TAG = "sa_pad_v[RBS]";
					String dbMsg = "";
					boolean retBool = true; //「TouchEventを消化」したものとしてこのビューに送らない (パッドに線を書かせない)
					try {
						float xPoint = event.getX();       //view上の座標
						float yPoint = event.getY();
						int action = event.getAction();
						dbMsg += "(" + xPoint + "×" + yPoint + ")action=" + action;
						sa_disp_v.onTouchEvent(event);               //イベントを送信；padのトレースでveiw側に描画
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
					return retBool;
				}
			});

			initDrawer();   //ここで取らないとonPostCreateでNullPointerException
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	/**
	 * 全リソースの読み込みが終わってフォーカスが当てられた時
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		final String TAG = "onStart[RBS]";
		String dbMsg = "hasFocus=" + hasFocus;
		try {
			if(isNotSet){
				laterCreate();
				isNotSet = false;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	@Override
	protected void onStart() {
		super.onStart();
		final String TAG = "onStart[RBS]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * この時点では_peer.on.OPENに至っていない
	 */
	@Override
	protected void onResume() {
		super.onResume();
		final String TAG = "onResume[RBS]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onPause() {
		final String TAG = "onPause[RBS]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		final String TAG = "onStop[RBS]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	///ハンバーガーメニュー//////////////////////////////////////////////////////////////////////////////////////////////////////////
	private Toolbar toolbar;
	public ActionBarDrawerToggle abdToggle;        //アニメーションインジケータ
	public NavigationView navigationView;
	private DrawerLayout drawer;
	public ImageView navi_head_iv;
	public TextView nave_head_main_tv;
	public TextView navi_head_sub_tv;
	@Override
	public void onBackPressed() {
		drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		if ( drawer.isDrawerOpen(GravityCompat.START) ) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		final String TAG = "onPostCreate[MA}";
		String dbMsg = "";
		try {
			abdToggle.syncState();    //NaviIconの回転アニメーションなど   Attempt to invoke virtual method 'void android.support.v7.app.ActionBarDrawerToggle.syncState()' on a null object reference
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG, dbMsg + "で" + e.toString());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		final String TAG = "onConfigurationChanged[MA}";
		String dbMsg = "";
		try {
			abdToggle.onConfigurationChanged(newConfig);
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG, dbMsg + "で" + e.toString());
		}
	}

	/**
	 * NaviViewの初期設定
	 * 開閉のイベント設定
	 * https://woshidan.hatenablog.com/entry/2016/09/07/223301
	 **/
	public void initDrawer() {            //http://qiita.com/androhi/items/f12b566730d9f951b8ec
		final String TAG = "initDrawer[RBS]";
		String dbMsg = "";
		try {
			toolbar = ( Toolbar ) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
//					nvh_img = ( ImageView ) findViewById(R.id.nvh_img);                //NaviViewヘッダーのアイコン
			drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
			abdToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			abdToggle.setDrawerIndicatorEnabled(true);
			drawer.setDrawerListener(abdToggle);    //Attempt to invoke virtual method 'void android.support.v4.widget.DrawerLayout.setDrawerListener(android.support.v4.widget.DrawerLayout$DrawerListener)' on a null object reference
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);            //左矢印←アイコンになる
			getSupportActionBar().setDisplayShowHomeEnabled(true);

			navi_head_iv = (ImageView ) findViewById(R.id.navi_head_iv);
			nave_head_main_tv = (TextView ) findViewById(R.id.nave_head_main_tv);
			navi_head_sub_tv = (TextView ) findViewById(R.id.navi_head_sub_tv);

			navigationView = ( NavigationView ) findViewById(R.id.nav_view);
			navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					final String TAG = "onNavigationItemSelected[initDrawer]";
					String dbMsg = "MenuItem" + menuItem.toString();/////////////////////////////////////////////////
					boolean retBool = false;
					try {
						retBool = funcSelected(menuItem);
						RecoveryBrainActivity.this.drawer.closeDrawers();
					} catch (Exception e) {
						myLog(TAG, dbMsg + "で" + e.toString());
						return false;
					}
					return retBool;
				}
			});
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG, dbMsg + "で" + e.toString());
		}
	}                                                                    //NaviViewの初期設定
	///toolbarへメニュー追加///////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rb_cont , menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		funcSelected(item);

		return super.onOptionsItemSelected(item);
	}


	@SuppressWarnings ( "StatementWithEmptyBody" )
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		final String TAG = "onNavigationItemSelected[WabA]";
		String dbMsg = "MenuItem" + item.toString();/////////////////////////////////////////////////
		try {
			int id = item.getItemId();
			dbMsg = "id=" + id;
//			switch ( id ) {
//				case R.id.wd_web_menu:
//					dbMsg = "wd_web_menu=";
//					navigationView.getMenu().clear();
//					navigationView.inflateMenu(R.menu.wev_cont);
//					break;
//				default:
					funcSelected(item);
//					//		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
//					break;
//			}
//			drawer.closeDrawer(GravityCompat.START);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
		return true;
	}

	/**
	 * MainActivityのメニュー
	 * ドロワーと共通になるので関数化
	 */
	public boolean funcSelected(MenuItem item) {
		final String TAG = "funcSelected[WabA]";
		String dbMsg = "MenuItem" + item.toString();/////////////////////////////////////////////////
		try {
			Bundle bundle = new Bundle();
			int id = item.getItemId();
			dbMsg = "id=" + id;
			switch ( id ) {
				case R.id.rbm_common_back:     //戻す
					sa_disp_v.canvasBack();
					break;


				case R.id.md_qr_read:     //QRコードから接続
					Intent qra = new Intent(this , QRActivity.class);
					startActivity(qra);
					break;
				case R.id.md_call_main:          //web
				case R.id.mm_call_main:          //web
//					Uri uri = Uri.parse("http://ec2-52-197-173-40.ap-northeast-1.compute.amazonaws.com:3080/");
//					Intent webIntent = new Intent(Intent.ACTION_VIEW,uri);
//					startActivity(webIntent);
					Intent webIntent = new Intent(this , CS_Web_Activity.class);
					String dataURI = rootUrlStr;
					dbMsg += "dataURI=" + dataURI;
//					final Date date = new Date(System.currentTimeMillis());
//					final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//					dataURI += "/?room=" +df.format(date);
////					CS_Util UTIL = new CS_Util();
////					dataURI += "/?room=" + UTIL.retDateStr(date , "yyyyMMddhhmmss");
//					dbMsg += ">>" + dataURI;
					webIntent.putExtra("dataURI" , dataURI);                        //最初に表示するページのパス
//					baseUrl = "file://"+extras.getString("baseUrl");				//最初に表示するページを受け取る
//					fType = extras.getString("fType");							//データタイプ
					startActivity(webIntent);
					break;
				case R.id.md_prefarence:      //設定
				case R.id.mm_prefarence:      //設定
					Intent settingsIntent = new Intent(RecoveryBrainActivity.this , MyPreferencesActivty.class);
					startActivityForResult(settingsIntent , REQUEST_PREF);//		StartActivity(intent);
					break;
				case R.id.md_quit:
				case R.id.mm_quit:
					callQuit();
					break;
				case R.id.close_web_menu:
					navigationView.getMenu().clear();
					navigationView.inflateMenu(R.menu.activity_web_drawer);
//					drawer.openDrawer(GravityCompat.START);
					break;
				default:
					onContextItemSelected(item);
					break;
			}
//			if (drawer.isDrawerOpen(GravityCompat.START)) {
//				// ドロワーメニューが開いた際に閉じる画像を非表示にする
//				drawer.closeDrawer(GravityCompat.START);
//			} else {
//				drawer.openDrawer(GravityCompat.START);
//				// ドロワーメニューが開いた際に閉じる画像を表示する
//			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
		return false;
	}                                        //メニューとDrowerからの画面/機能選択

	static final int REQUEST_PREF = 100;                          //Prefarensからの戻り
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final String TAG = "onActivityResult[MA]";
		String dbMsg = "requestCode=" + requestCode + ",resultCode=" + resultCode;
		try {
			switch ( requestCode ) {
				case REQUEST_PREF:                                //Prefarensからの戻り
					readPref();
					break;
			}
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

	/**
	 * Cameraパーミッションが通った時点でstartLocalStream
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode , String permissions[] , int[] grantResults) {
		final String TAG = "onRequestPermissionsResult[MA]";
		String dbMsg = "";
		try {
			dbMsg = "requestCode=" + requestCode;
			switch ( requestCode ) {
				case REQUEST_PREF:
					readPref();        //ループする？
					break;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event) {
//		final String TAG = "dispatchTouchEvent[RBS]";
//		String dbMsg = "";
//		boolean retBool = false; //trueに設定すると「TouchEventを消化」したものとして他に送らない
//		try {
//			float xPoint = event.getX();   //画面上での座標
//			float yPoint = event.getY();
//			int action = event.getAction();
//			dbMsg += "(" + xPoint + "×" + yPoint + ")action=" + action;
////		if (ev.getAction() == MotionEvent.ACTION_UP) {
////			if (listener != null) {
////				post(new Runnable() {
////					@Override
////					public void run() {
////						listener.onClick(CustomButtonView.this);
////					}
////				});
////			}
////		}
//			retBool = true;
//			myLog(TAG , dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
//		}
////		return super.dispatchTouchEvent(event);
//		return retBool;
//	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		final String TAG = "onKeyDown";
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
	///////////////////////////////////////////
	/**
	 * onCreateに有ったイベントなどの処理パート
	 * onCreateは終了処理後のonDestroyの後でも再度、呼び出されるので実データの割り付けなどを分離する
	 */
	public void laterCreate() {
		final String TAG = "laterCreate[RBS]";
		String dbMsg = "";
		try {
			int orientation = getResources().getConfiguration().orientation;
			dbMsg += "orientation=" + orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
				dbMsg += "=横向き";
			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
				dbMsg += "=縦向き";
			}

			int canvasWidth =sa_disp_v.getWidth() ;
			int canvasHeight =sa_disp_v.getHeight() ;
			dbMsg += "canvas[" + canvasWidth + "×" + canvasHeight + "]";
			sa_disp_v.addBitMap(readFIle( "st001.png") ,canvasWidth ,canvasHeight );

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public Bitmap readFIle(String fileName) {
		final String TAG = "readFIle[RBS]";
		String dbMsg = "";
		Bitmap bm = null;
		try {
			AssetManager as = getResources().getAssets();
			dbMsg += "fileName="+fileName;
			InputStream is = as.open(fileName);
			bm = BitmapFactory.decodeStream(is);
			is.close();
			//decodeStream(InputStream,padding.options);
			dbMsg += "　=　"+bm.getByteCount() + "バイト";
			myLog(TAG , dbMsg);
		} catch (IOException er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return  bm;
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
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
				finishAndRemoveTask();                      //アプリケーションのタスクを消去する事でデバッガーも停止する。
			} else {
				moveTaskToBack(true);                       //ホームボタン相当でアプリケーション全体が中断状態
			}
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


	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , RecoveryBrainActivity.this);
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
 課題
 	・起動Activtyの入れ替え
 	 	・単独実行Actvtyから開始
  	 ・canvas間通信
		 ・padのトレースでveiw側に描画

8/20 ・定例パターン読込み
 		・パターン再作成（droｗで端部をきれいに、反転で作った定型の差替え；web更新）
8/21 ・pixcel配列読込み
	 ・評価実装
		 ・消し込みPaintプロパティ送信
		 ・消し込み評価
		 ・padのtachiendで自動評価
		 ・toolbarへの書き込み
		 ・もう一度
	 ・変形
		 ・右へ90度回転
		 ・左へ90度回転
		 ・180度回転
		 ・上下反転
		 ・左右反転
		 ・オリジナル（に戻す）
	 ・動作設定
		 ・鏡面動作
		 ・上下
		 ・左右
		 ・トレース後に自動判定
 	・toolBarカスタマイズ
 		・もう一度ボタン
 		・変形ボタンリスト
 		・手動評価ボタン
 	・配布
 		・GoogleDrive登録
 		・取説web
 			・リンク
 			・概要紹介
 	・iOS移植
 		・フレームワーク
 			・ライフサイクル
 			・コールバック
 			・GUI
 			・デバイスアクセス
 * **/