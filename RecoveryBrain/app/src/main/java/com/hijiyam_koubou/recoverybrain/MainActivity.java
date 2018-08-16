package com.hijiyam_koubou.recoverybrain;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private Toolbar toolbar;
	public ActionBarDrawerToggle abdToggle;        //アニメーションインジケータ
	public NavigationView navigationView;
	private DrawerLayout drawer;
	public ImageView navi_head_iv;
	public TextView nave_head_main_tv;
	public TextView navi_head_sub_tv;

	private LinearLayout scam_ll ;
	private DecoratedBarcodeView qrReaderView;
	private ImageButton release_bt ;
	private LinearLayout prevew_ll;
//	private ImageView preveiw_iv;
	private Button acsess_bt;
	private Button rescan_bt;

	private Camera mCamera;
	
	//プリファレンス設定
	SharedPreferences myNFV_S_Pref;
	SharedPreferences.Editor pNFVeditor;

	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;
	public String rootUrlStr = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";					//	String dataURI = "http://192.168.3.14:3080";	//自宅
	public boolean isReadPref = false;

	/**
	 * このアプリケーションの設定ファイル読出し
	 **/
	public void readPref() {
		final String TAG = "readPref[MA]";
		String dbMsg = "許諾済み";//////////////////
		try {
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                //(初回起動で)全パーミッションの許諾を取る
				dbMsg = "許諾確認";
				String[] PERMISSIONS = { Manifest.permission.INTERNET , Manifest.permission.CAMERA};
//				Manifest.permission.ACCESS_NETWORK_STATE , Manifest.permission.ACCESS_WIFI_STATE ,   , Manifest.permission.MODIFY_AUDIO_SETTINGS , Manifest.permission.RECORD_AUDIO , Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.MODIFY_AUDIO_SETTINGS
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
			}
			dbMsg += ",isReadPref=" + isReadPref;
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

	/**
	 * アプリ起動時の入り口；終了処理後のonDestroyの後でも再度、呼び出されるので構成パーツのID取得に留める
	 * ①new Peer(this , option);でサーバに接続
	 * ② PeerEventEnum.OPENでstartLocalStream()へ				// _peer.on.OPENに入らない場合は SkywayにApicationを追加する時；権限(APIキー認証を利用する)はOFFに
	 **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[MA]";
		String dbMsg = "";
		try {
			readPref();
//			int orientation = getResources().getConfiguration().orientation;
//			dbMsg += "orientation=" + orientation;
//			if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
//				dbMsg += "=横向き";
//			} else if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
//				dbMsg += "=縦向き";
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);      //横向きに修正
//			}
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);        //タスクバーを 非表示
			requestWindowFeature(Window.FEATURE_NO_TITLE);                            //タイトルバーを非表示
			setContentView(R.layout.activity_main);
			toolbar = ( Toolbar ) findViewById(R.id.toolbar);

			drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
			navi_head_iv = (ImageView ) findViewById(R.id.navi_head_iv);
			nave_head_main_tv = (TextView ) findViewById(R.id.nave_head_main_tv);
			navi_head_sub_tv = (TextView ) findViewById(R.id.navi_head_sub_tv);

			scam_ll = (LinearLayout ) findViewById(R.id.scam_ll);
			qrReaderView = (DecoratedBarcodeView) findViewById(R.id.decoratedBarcodeView);
			release_bt = (ImageButton) findViewById(R.id.release_bt);

			prevew_ll = (LinearLayout ) findViewById(R.id.prevew_ll);
//			preveiw_iv = (ImageView ) findViewById(R.id.preveiw_iv);
			acsess_bt = (Button ) findViewById(R.id.acsess_bt);
			rescan_bt = (Button ) findViewById(R.id.rescan_bt);
			prevew_ll.setVisibility(View.GONE);
//					new IntentIntegrator(MainActivity.this).initiateScan();   				//これだけでもQRコードアクティビティを形成して、onActivityResultで内容を取得できる

			setSupportActionBar(toolbar);
			initDrawer();

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
		final String TAG = "onStart[MA]";
		String dbMsg = "hasFocus=" + hasFocus;
		try {
			laterCreate();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String TAG = "onStart[MA]";
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
		final String TAG = "onResume[MA]";
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
		final String TAG = "onPause[MA]";
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
	protected void onStop() {
		final String TAG = "onStop[MA]";
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

	static final int REQUEST_PREF = 100;                          //Prefarensからの戻り
	static final int REQUEST_SWOPEN = REQUEST_PREF + 1;        //skyway接続開始

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final String TAG = "onActivityResult[MA]";
		String dbMsg = "requestCode=" + requestCode + ",resultCode=" + resultCode;
		try {
			switch ( requestCode ) {
				case REQUEST_PREF:                                //Prefarensからの戻り
//					readPref();
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

	///ハンバーガーメニュー//////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onBackPressed() {
//		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
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
	 **/
	public void initDrawer() {            //http://qiita.com/androhi/items/f12b566730d9f951b8ec
		final String TAG = "initDrawer[MA}";
		String dbMsg = "";
		try {
			//		nvh_img = ( ImageView ) findViewById(R.id.nvh_img);                //NaviViewヘッダーのアイコン
			drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
			myLog(TAG, dbMsg);
			abdToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			abdToggle.setDrawerIndicatorEnabled(true);
			drawer.setDrawerListener(abdToggle);    //Attempt to invoke virtual method 'void android.support.v4.widget.DrawerLayout.setDrawerListener(android.support.v4.widget.DrawerLayout$DrawerListener)' on a null object reference
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);            //左矢印←アイコンになる
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			myLog(TAG, dbMsg);
			navigationView = ( NavigationView ) findViewById(R.id.nav_view);
			navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					final String TAG = "onNavigationItemSelected[MyActivity.initDrawer]";
					String dbMsg = "MenuItem" + menuItem.toString();/////////////////////////////////////////////////
					boolean retBool = false;
					try {
						retBool = funcSelected(menuItem);
						MainActivity.this.drawer.closeDrawers();
					} catch (Exception e) {
						myLog(TAG, dbMsg + "で" + e.toString());
						return false;
					}
					return retBool;
				}
			});
		} catch (Exception e) {
			myErrorLog(TAG, dbMsg + "で" + e.toString());
		}
	}                                                                    //NaviViewの初期設定

	///メイン画面へメニュー追加///////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main , menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		funcSelected(item);

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings ( "StatementWithEmptyBody" )
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
//		int id = item.getItemId();
		funcSelected(item);
//		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	///メニュー///////////////////////////////////////////////////////////////////////////
	public static final int MENU_main = 0;                    //メイン画面	     <item android:id="@+id/mm_main"	android:orderInCategory="101"	android:title="@string/main_screen"/>
	public static final int MENU_conectedt = MENU_main + 1;    //現在の接続先       <item  android:id="@+id/mm_conected" android:orderInCategory="102"  android:title="@string/current_connection"/>
	public static final int MENU_PLC = MENU_conectedt + 1;    //現在地確認       <item android:id="@+id/mm_present_location_confirmation"  android:orderInCategory="103" android:title="@string/present_location_confirmation"　android:icon="@android:drawable/ic_dialog_map"-->
	public static final int MENU_share = MENU_PLC + 1;        //登録したログの確認   <item android:id="@+id/mm_share" android:orderInCategory="104" android:title="@string/Indication_of_the_registered_log"/>
	public static final int MENU_TC = MENU_share + 1;            //廃止；送信先変更    <item  android:id="@+id/mm_transmission_change" android:orderInCategory="107"  android:title="@string/transmission_change"/>
	public static final int MENU_disconect = MENU_TC + 1;        //回線切断              <item android:id="@+id/mm_" android:orderInCategory="108" android:title="@string/info_a_setudann"/>
	public static final int MENU_prefarence = MENU_disconect + 1;        //設定画面   <item android:id="@+id/mm_prefarence" android:title="@string/action_settings"  android:orderInCategory="189"/>
	public static final int MENU_quit = MENU_prefarence + 1;            //    <item android:id="@+id/mm_quit" android:orderInCategory="199" android:title="@string/menu_item_sonota_end"/>
	public static int mMenuType = MENU_main;                    //メニューレイアウト管理用変数

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main , menu);
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu(Menu item) {
//		final String TAG = "onPrepareOptionsMenu[MA}";
//		String dbMsg = "開始" + item;                    //表記が返る
//		try {
//			dbMsg = dbMsg + " , mMenuType= " + mMenuType;
//			switch ( mMenuType ) {
//				case MENU_conectedt:    //現在地確認       <item android:id="@+id/mm_present_location_confirmation"  android:orderInCategory="103" android:title="@string/present_location_confirmation"　android:icon="@android:drawable/ic_dialog_map"-->
//					break;
//				default:
//					break;
//			}
//			//		myLog(TAG, dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + "で" + er.toString());
//		}
//		return true;        //	return super.onOptionsItemSelected ((MenuItem) item);でクラッシュ
//	}                                            //状況に合わせたメニューアイテムの表示/非表示処理	再開時;⑨
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		final String TAG = "onOptionsItemSelected[MA}";
//		String dbMsg = "開始" + item;                    //表記が返る
//		try {
//			myLog(TAG , dbMsg);
//			funcSelected(item);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + "で" + er.toString());
//		}
//		//本当は　return abdToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);			//アイコン回転
//		return super.onOptionsItemSelected(item);
//	}

	/**
	 * MainActivityのメニュー
	 * ドロワーと共通になるので関数化
	 */
	public boolean funcSelected(MenuItem item) {
		final String TAG = "funcSelected[MA}";
		String dbMsg = "MenuItem" + item.toString();/////////////////////////////////////////////////
		try {
			Bundle bundle = new Bundle();
			int id = item.getItemId();
			dbMsg = "id=" + id;
			switch ( id ) {
//				case R.id.mm_qr_read:     //QRコードから接続
//					Intent qra = new Intent(this , QRActivity.class);
//					startActivity(qra);
//					break;
				case R.id.md_call_main:          //web
				case R.id.mm_call_main:          //web
//					Uri uri = Uri.parse("http://ec2-52-197-173-40.ap-northeast-1.compute.amazonaws.com:3080/");
//					Intent webIntent = new Intent(Intent.ACTION_VIEW,uri);
//					startActivity(webIntent);
					Intent webIntent = new Intent(this , CS_Web_Activity.class);
					String dataURI = rootUrlStr;
					dbMsg += "dataURI=" + dataURI;
					webIntent.putExtra("dataURI" , dataURI);                        //最初に表示するページのパス
//					baseUrl = "file://"+extras.getString("baseUrl");				//最初に表示するページを受け取る
//					fType = extras.getString("fType");							//データタイプ
					startActivity(webIntent);
					break;
				case R.id.md_stand_alone:          //スタンドアロン
				case R.id.mm_stand_alone:          //スタンドアロン
					Intent saIntent = new Intent(this , StandAloneActivity.class);
					startActivity(saIntent);
					break;
				case R.id.md_prefarence:      //設定
				case R.id.mm_prefarence:      //設定
					Intent settingsIntent = new Intent(MainActivity.this , MyPreferencesActivty.class);
					startActivityForResult(settingsIntent , REQUEST_PREF);//		StartActivity(intent);
					break;
				case R.id.md_quit:
				case R.id.mm_quit:
					callQuit();
					break;

				default:
					break;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
		return false;
	}                                        //メニューとDrowerからの画面/機能選択

					///////////////////////////////////////////////////////////////////////////////////////////////////////////
 	/**
	 * onCreateに有ったイベントなどの処理パート
	 * onCreateは終了処理後のonDestroyの後でも再度、呼び出されるので実データの割り付けなどを分離する
	 */
	public void laterCreate() {
		final String TAG = "laterCreate[MA]";
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
				public void onClick(View v) { 					// ボタンがクリックされた時に呼び出されます
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
				public void onClick(View v) {	// ボタンがクリックされた時に呼び出されます
					final String TAG = "acsess_bt[MA]";
					String dbMsg = "";
					try {
						Button button = (Button) v;
						String dataURI  = (String) button.getText();
//				String dataURI = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";					//extras.getString("dataURI");                        //最初に表示するページのパス
//					String dataURI = "http://192.168.3.14:3080";
						dbMsg = ",dataURI="+dataURI;
						Intent webIntent = new Intent(MainActivity.this , CS_Web_Activity.class);
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
				public void onClick(View v) { 					// ボタンがクリックされた時に呼び出されます
					final String TAG = "rescan_bt[MA]";
					String dbMsg = "";
					try {
						prevew_ll.setVisibility(View.GONE);
//						release_bt.setVisibility(View.VISIBLE);
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

//camera ///////////////////////////////////////////////////////////////////////////////
	private void startCapture() {
		final String TAG = "startCapture[MA]";
		String dbMsg = "";
		try {
//		startButton?.isEnabled = false
			qrReaderView.decodeSingle(new BarcodeCallback() {
				@Override
				public void barcodeResult(BarcodeResult barcodeResult) {
					final String TAG = "startCapture[MA]";
					String dbMsg = "";
					try {
						String dataURI  = barcodeResult.getText();
						dbMsg = ",dataURI="+dataURI;
						release_bt.setVisibility(View.GONE);
//						acsess_bt.setVisibility(View.VISIBLE);
//						scam_ll.setVisibility(View.GONE);
						prevew_ll.setVisibility(View.VISIBLE);
//					preveiw_iv = (ImageView ) findViewById(R.id.preveiw_iv);
						acsess_bt.setText(dataURI);
						navi_head_sub_tv.setText(dataURI);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}

				}

				@Override
				public void possibleResultPoints(List<ResultPoint > list) {}
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

//	private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
//		@Override
//		public void surfaceCreated(SurfaceHolder holder) {
//			// 生成されたとき
//			mCamera = Camera.open();
//			try {
//				// プレビューをセットする
//				mCamera.setPreviewDisplay(holder);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		@Override
//		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			// 変更されたとき
//			Camera.Parameters parameters = mCamera.getParameters();
//			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//			Camera.Size previewSize = previewSizes.get(0);
//			parameters.setPreviewSize(previewSize.width, previewSize.height);
//			// width, heightを変更する
//			mCamera.setParameters(parameters);
//			mCamera.startPreview();
//		}
//		@Override
//		public void surfaceDestroyed(SurfaceHolder holder) {
//			// 破棄されたとき
//			mCamera.release();
//			mCamera = null;
//		}
//	};
//
//	private OnClickListener onClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			// オートフォーカス
//			if (mCamera != null) {
//				mCamera.autoFocus(autoFocusCallback);
//			}
//		}
//	};
//
//	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
//		@Override
//		public void onAutoFocus(boolean success, Camera camera) {
//			if (success) {
//				// 現在のプレビューをデータに変換
//				camera.setOneShotPreviewCallback(previewCallback);
//			}
//		}
//	};
//
//	private PreviewCallback previewCallback = new PreviewCallback() {
//		@Override
//		public void onPreviewFrame(byte[] data, Camera camera) {
//			// 読み込む範囲
//			int previewWidth = camera.getParameters().getPreviewSize().width;
//			int previewHeight = camera.getParameters().getPreviewSize().height;
//
//			// プレビューデータから BinaryBitmap を生成
//			PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
//					data, previewWidth, previewHeight, 0, 0, previewWidth, previewHeight, false);
//			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//			// バーコードを読み込む
//			Reader reader = new MultiFormatReader();
//			Result result = null;
//			try {
//				result = reader.decode(bitmap);
//				String text = result.getText();
//				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
//			} catch (Exception e) {
//				Toast.makeText(getApplicationContext(), "Not Found", Toast.LENGTH_SHORT).show();
//			}
//		}
//	};
//

	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , MainActivity.this);
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
 *
 *AndroidアプリでQRコードの読取、生成をする     https://qiita.com/11Kirby/items/0f496fe80df84875c132
 *
 * **/


