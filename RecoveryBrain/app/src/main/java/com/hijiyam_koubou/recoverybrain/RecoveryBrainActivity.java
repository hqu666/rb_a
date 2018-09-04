package com.hijiyam_koubou.recoverybrain;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AndroidException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecoveryBrainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {
	public com.hijiyam_koubou.recoverybrain.CS_CanvasView sa_disp_v;        //表示側
	public com.hijiyam_koubou.recoverybrain.CS_CanvasView sa_pad_v;        //操作側
	public Toolbar toolbar;
	public LinearLayout score_aria_ll;                //トレース操作パネル
	public ImageButton cp_score_bt;
	public ImageButton cp_mirror_h_bt;
	public ImageButton cp_mirror_v_bt;
	public LinearLayout wh_paret;                //手書きツールパネル
	public Spinner wb_mode_sp;
	public ImageButton wb_color_bt;
	public Spinner wb_width_sp;
	public Spinner wb_linecaps_sp;
	public EditText cp_score_tv;
	public AlertDialog splashDlog;

	public int displyWidth = 0;                     //表示veiwの幅
	public int displyHight = 0;                     //表示veiwの高さ

	public String selectMode;                     //手書き編集のモード
	public String selectLineCap = "round";
	public float selectWidth = 5;
	public int selectColor = Color.GREEN;
	private ColorPickerDialog mColorPickerDialog;
	public boolean isReadPref = false;
	public boolean isRecoveryBrain = false;
	public boolean isStartLandscape = true;        //起動時は横向き

	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;
	public String rootUrlStr = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";                    //	String dataURI = "http://192.168.3.14:3080";	//自宅
	public boolean isNotSet = true;
	public boolean isFarst = false;                    //初回起動
	public boolean isFarstPictRead = false;            //トレース元表示済み
	public String readFileName = "st001.png";
	public String savePatht = "";        //作成したファイルの保存場所
	public boolean isStartLast = true;        //自動送り
	public boolean is_v_Mirror = true;                //左右鏡面動作  //読み込み時、反転される
	public boolean is_h_Mirror = true;                //上下鏡面動作
	public boolean isAautoJudge = false;        //トレース後に自動判定
	public int traceLineWidth = 10;        //トレース線の太さ
	public boolean isPadLeft = false;          //左側にPad
	public boolean isLotetCanselt = false;        //自動回転阻止
	public File[] stereoTypeFiles;    			//定例パターンファイルリスト
	public String[] stereoTypeFileNames;
	public int b_score = 0;


	/**
	 * このアプリケーションの設定ファイル読出し
	 **/
	public void readPref() {
		final String TAG = "readPref[RBS]";
		String dbMsg = "許諾済み";//////////////////
		try {
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                //(初回起動で)全パーミッションの許諾を取る
				dbMsg = "許諾確認";
				String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.INTERNET , Manifest.permission.CAMERA};
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
			}
			dbMsg += ",isReadPref=" + isReadPref;
			MyPreferenceFragment prefs = new MyPreferenceFragment();
			prefs.readPref(this);
			rootUrlStr = prefs.rootUrlStr;
			dbMsg += ",rootUrlStr=" + rootUrlStr;
			readFileName = prefs.readFileName;
			dbMsg += ",readFileName=" + readFileName;
			savePatht = prefs.savePatht;
			dbMsg += ",作成したファイルの保存場所=" + savePatht;
			isStartLast = prefs.isStartLast;
			dbMsg += ",次回は最後に使った元画像からスタート=" + isStartLast;
			is_v_Mirror = prefs.is_v_Mirror;
			dbMsg += ",左右鏡面動作=" + is_v_Mirror;
			is_h_Mirror = prefs.is_h_Mirror;
			dbMsg += ",上下鏡面動作=" + is_h_Mirror;
			isAautoJudge = prefs.isAautoJudge;
			dbMsg += ",トレース後に自動判定=" + isAautoJudge;
			traceLineWidth = prefs.traceLineWidth;
			dbMsg += ",トレース線の太さ=" + traceLineWidth;
			isPadLeft = prefs.isPadLeft;
			dbMsg += ",左側にPad=" + isPadLeft;
			isLotetCanselt = prefs.isLotetCanselt;
			dbMsg += ",自動回転阻止=" + isLotetCanselt;
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);            //	getActivity().getBaseContext()
			myEditor = sharedPref.edit();
			stereoTypeRady( "");

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@SuppressLint ( "ResourceAsColor" )
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[RBS]";
		String dbMsg = "";
		try {
			dbMsg += ";savedInstanceState= " + savedInstanceState;
			readPref();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);        //タスクバーを 非表示
			requestWindowFeature(Window.FEATURE_NO_TITLE);                            //タイトルバーを非表示
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);        //ローディングをタイトルバーのアイコンとして表示☆リソースを読み込む前にセットする
//			switch ( getResources().getConfiguration().orientation ) {
//				case Configuration.ORIENTATION_PORTRAIT:  // 縦長
//					dbMsg += ";縦長";
//					isStartLandscape = false;
//					break;
//				case Configuration.ORIENTATION_LANDSCAPE:  // 横長
//					dbMsg += ";横長";
//					isStartLandscape = true;        //起動時は横向き
//					break;
//				default:
//					break;
//			}
			int surfaceRotation = (( WindowManager ) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
			dbMsg += ",surfaceRotation=" + surfaceRotation;
			switch ( surfaceRotation ) {
				case Surface.ROTATION_90:            //1
				case Surface.ROTATION_270:        //3
					isStartLandscape = true;        //起動時は横向き
					break;
				case Surface.ROTATION_180:            //2
				default:
					isStartLandscape = false;
					break;
			}
			dbMsg += ",起動時は横向き=" + isStartLandscape;
			dbMsg += ",自動回転阻止=" + isLotetCanselt;
			if ( isLotetCanselt ) {
				if ( isStartLandscape ) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);        //横画面で止めておく	横	ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);        //縦画面で止めておく	横	ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
				}
			}
			isFarst = false;       //初回起動
			int stayTime = 1000;
			if ( savedInstanceState == null ) {
				isFarst = true;
				dbMsg += "初回起動=" + isFarst;
				stayTime = 5000;
			}
			try {
				Thread.sleep(stayTime);            // ここで設定秒間スリープし、スプラッシュを表示させたままにする。
			} catch (InterruptedException er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
			setTheme(R.style.AppTheme);                // スプラッシュthemeを通常themeに変更して
			setContentView(R.layout.activity_rb);      //リソース読込み開始
			toolbar = ( Toolbar ) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			 cp_score_tv = ( EditText ) findViewById(R.id.cp_score_tv);
			cp_score_tv.setFocusable(false);
			wh_paret = ( LinearLayout ) findViewById(R.id.wh_paret);
			wh_paret.setVisibility(View.GONE);

			LinearLayout sa_disp_ll = ( LinearLayout ) findViewById(R.id.sa_disp_ll);
			LinearLayout sa_pad_ll = ( LinearLayout ) findViewById(R.id.sa_pad_ll);
			sa_disp_v = new com.hijiyam_koubou.recoverybrain.CS_CanvasView(RecoveryBrainActivity.this , true , toolbar , cp_score_tv);        //表示(受信)側
			sa_pad_v = new com.hijiyam_koubou.recoverybrain.CS_CanvasView(this , false , toolbar , cp_score_tv);        //表示側
			sa_pad_v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
			if ( isStartLandscape ) {                 //起動時は横向き
				dbMsg += ",左側にPad=" + isPadLeft;
				if ( isPadLeft ) {
					sa_disp_ll.addView(sa_pad_v);
					sa_pad_ll.addView(sa_disp_v);
				} else {
					sa_disp_ll.addView(sa_disp_v);
					sa_pad_ll.addView(sa_pad_v);
				}
			} else {                                 //起動時は縦向き
				sa_disp_ll.addView(sa_disp_v);
				sa_pad_ll.addView(sa_pad_v);
			}
			sa_disp_v.readFileName = "";
			score_aria_ll = ( LinearLayout ) findViewById(R.id.score_aria_ll);

			findViewById(R.id.cp_jobselect_bt).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stereoTypeSelect("");
				}
			});

			findViewById(R.id.cp_jobselect_bt).setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					jobserect_click();
					return true;    // 戻り値をtrueにするとOnClickイベントは発生しない
				}
			});

			findViewById(R.id.cp_direction_bt).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					direction_click();
				}
			});

			cp_score_bt = ( ImageButton ) findViewById(R.id.cp_score_bt);
			cp_mirror_h_bt = ( ImageButton ) findViewById(R.id.cp_mirror_h_bt);
			cp_mirror_v_bt = ( ImageButton ) findViewById(R.id.cp_mirror_v_bt);
			wb_mode_sp = ( Spinner ) findViewById(R.id.wb_mode_sp);
			wb_color_bt = ( ImageButton ) findViewById(R.id.wb_color_bt);
			wb_width_sp = ( Spinner ) findViewById(R.id.wb_width_sp);
			wb_linecaps_sp = ( Spinner ) findViewById(R.id.wb_linecaps_sp);
			initDrawer();   //ここで取らないとonPostCreateでNullPointerException
			if ( isFarst ) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(RecoveryBrainActivity.this , R.style.SplashDialogStyle);        //
//				LayoutInflater inflater = ( LayoutInflater ) RecoveryBrainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
//				final View layout = inflater.inflate(R.layout.dlog_splash , ( ViewGroup ) findViewById(R.id.splash_root_ll));
//				builder.setView(layout);
//				splashDlog = builder.create();                // 表示
//				splashDlog.show();                // 表示
			}
//			Toast.makeText(this , getString(R.string.common_yomikomicyuu) , Toast.LENGTH_LONG).show();
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
			if ( hasFocus ) {
				if ( isNotSet ) {
					laterCreate();
				} else if ( !isFarstPictRead ) {            //トレース元表示済み
					trasePictRead(readFileName);
				}

			}
			//			if(isFarst){
//			if ( splashDlog != null ) {
//				if ( splashDlog.isShowing() ) {
//					splashDlog.dismiss();
//				}
//			}
////				isFarst = false;       //初回起動
////			}
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
			if ( !isFarstPictRead ) {            //トレース元表示済み
				trasePictRead(readFileName);
			}
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
			if ( isNotSet ) {
				laterCreate();
			}
			dbMsg += ",トレース後に自動判定=" + isAautoJudge;
			if ( isAautoJudge ) {
				cp_score_bt.setImageResource(android.R.drawable.btn_star_big_on);
			} else {
				cp_score_bt.setImageResource(android.R.drawable.btn_star_big_off);
			}
			dbMsg += ",上下鏡面動作=" + is_h_Mirror;
			if ( is_h_Mirror ) {
				cp_mirror_h_bt.setImageResource(R.drawable.mirror_h_t);
			} else {
				cp_mirror_h_bt.setImageResource(R.drawable.mirror_h);
			}
			dbMsg += ",左右鏡面動作=" + is_v_Mirror;
			if ( is_v_Mirror ) {
				cp_mirror_v_bt.setImageResource(R.drawable.mirror_v_t);
			} else {
				cp_mirror_v_bt.setImageResource(R.drawable.mirror_v);
			}
////			if ( isFarst ) {
//			if ( splashDlog != null ) {
//				if ( splashDlog.isShowing() ) {
//					splashDlog.dismiss();
//				}
//			}
////				isFarst = false;       //初回起動
////			}
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

	//ドロワメニュー//////////////////////////////////////////////////////////////////////////////////////////////////////////
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
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG , dbMsg + "で" + e.toString());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		final String TAG = "onConfigurationChanged[MA}";
		String dbMsg = "";
		try {
			abdToggle.onConfigurationChanged(newConfig);
			dbMsg += ",自動回転阻止=" + isLotetCanselt;
			if ( isLotetCanselt ) {
				switch ( newConfig.orientation ) {
					case Configuration.ORIENTATION_PORTRAIT:  // 縦長
						dbMsg += ";縦長";
//						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);        //縦画面で止めておく	横	ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
						break;
					case Configuration.ORIENTATION_LANDSCAPE:  // 横長
						dbMsg += ";横長";
//						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);        //横画面で止めておく	横	ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
						break;
					default:
						break;
				}
				//方向固定するとonConfigurationChangedも一切発生しなくなる
			}
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG , dbMsg + "で" + e.toString());
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
//					nvh_img = ( ImageView ) findViewById(R.id.nvh_img);                //NaviViewヘッダーのアイコン
			drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
			abdToggle = new ActionBarDrawerToggle(this , drawer , toolbar , R.string.navigation_drawer_open , R.string.navigation_drawer_close);
			abdToggle.setDrawerIndicatorEnabled(true);
			drawer.setDrawerListener(abdToggle);    //Attempt to invoke virtual method 'void android.support.v4.widget.DrawerLayout.setDrawerListener(android.support.v4.widget.DrawerLayout$DrawerListener)' on a null object reference
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);            //左矢印←アイコンになる
			getSupportActionBar().setDisplayShowHomeEnabled(true);

			navi_head_iv = ( ImageView ) findViewById(R.id.navi_head_iv);
			nave_head_main_tv = ( TextView ) findViewById(R.id.nave_head_main_tv);
			navi_head_sub_tv = ( TextView ) findViewById(R.id.navi_head_sub_tv);

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
						myLog(TAG , dbMsg + "で" + e.toString());
						return false;
					}
					return retBool;
				}
			});
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			myErrorLog(TAG , dbMsg + "で" + e.toString());
		}
	}                                                                    //NaviViewの初期設定

	///toolbarへメニュー追加///////////////////////////////////////////////////////////////////////////
	public Menu myMenu;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rb_cont , menu);
		myMenu = menu;
		return true;
	}

	/**
	 * メニュー作成時の非表示や無効化
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final String TAG = "onPrepareOptionsMenu[RBS]";
		String dbMsg = "";
		try {
			dbMsg = ",左側にPad=" + isPadLeft;
			if ( isPadLeft ) {
				menu.findItem(R.id.rbm_lotet_pad).setTitle(getString(R.string.trace_setting_is_pad_right));
			} else {
				menu.findItem(R.id.rbm_lotet_pad).setTitle(getString(R.string.trace_setting_is_pad_left));
			}
			dbMsg = ",起動時は横向き=" + isStartLandscape;
//				menu.findItem(R.id.rbm_lotet_pad).setEnabled(! isStartLandscape);
//			dbMsg = ",上下鏡面=" + is_h_Mirror;
//			mirror_h_click();
////			myMenu.findItem(R.id.rbm_mirror_movement_to).setChecked( is_h_Mirror);
//			dbMsg = ",左右鏡面=" + is_v_Mirror;
//			mirror_v_click();
////			myMenu.findItem(R.id.rbm_mirror_movement_to).setChecked( is_v_Mirror);
//			dbMsg = ",isAutoJudge=" + sa_disp_v.isAutoJudge;
//			scoreAuto();
//			myMenu.findItem(R.id.rbm_auto_judge).setChecked( sa_disp_v.isAutoJudge);
//			menu.findItem(R.id.score_bt).setEnabled(! sa_disp_v.isAutoJudge);					//有効/無効
//			menu.findItem(R.id.score_bt).setVisible(! sa_disp_v.isAutoJudge);					//表示/非表示
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
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
		final String TAG = "funcSelected[RBS]";
		String dbMsg = "MenuItem" + item.toString();/////////////////////////////////////////////////
		try {
			Bundle bundle = new Bundle();
			CharSequence toastStr = "";
			int id = item.getItemId();
			dbMsg = "id=" + id;
			switch ( id ) {
				case R.id.rbm_job_select_setting:            //トレース元設定
				case R.id.rbm_direction_setting:            //変形設定
				case R.id.rbm_trace_setting:                //動作設定
					//メニューグループはまずスキップ //
					break;
				case R.id.rbm_lotet_pad:     //Padの左右
					dbMsg += ",isPadLeft=" + isPadLeft;
					if ( isPadLeft ) {
						isPadLeft = false;
					} else {
						isPadLeft = true;
					}
					myEditor.putBoolean("is_pad_left_key" , isPadLeft);
					dbMsg += ",更新";
					myEditor.commit();
					dbMsg += "完了";
					reStart();
					break;
//				case R.id.rbm_common_back:     //戻す
//					sa_disp_v.canvasBack();
//					break;
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
///トレース元変形////////////////////////////////
				case R.id.rbm_direction_org:     //オリジナルに戻す
					break;
				case R.id.rbm_roat_right:     //右90回転
					sa_disp_v.canvasSubstitution(R.string.rb_roat_right);
					break;
				case R.id.rbm_roat_left:     //左90回転
					sa_disp_v.canvasSubstitution(R.string.rb_roat_left);
					break;
				case R.id.rbm_roat_half:     //180回転
					sa_disp_v.canvasSubstitution(R.string.rb_roat_half);
					break;
				case R.id.rbm_flip_vertical:     //上下反転
					sa_disp_v.canvasSubstitution(R.string.rb_flip_vertical);
					break;
				case R.id.rbm_flip_horizontal:     //左右反転
					sa_disp_v.canvasSubstitution(R.string.rb_flip_horizontal);
					break;
				case R.id.rbm_make_original:     //オリジナルにする
					break;
///動作設定////////////////////////////////
				case R.id.rbm_auto_judge:     //自動判定
					scoreAuto();
					break;
				case R.id.rbm_mirror_movement_to:
					mirror_h_click();
					break;
				case R.id.rbm_mirror_movement_lr:
					mirror_v_click();
					break;
//				case R.id.imgList_bt:     //トレース元画像のリスト表示
//					stereoTypeSelect();
//					break;
				case R.id.again_bt:     //戻す
				case R.id.rbm_hand_again:     //戻す
					sa_disp_v.backAgain();
					sa_disp_v.isPreparation = true;                    //トレーススタート前の準備中
					break;
/////////////////////////////////動作設定//
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
					pendeingMessege();
//					String titolStr = "制作中です";
//					String mggStr = "最終リリースをお待ちください";
//					messageShow(titolStr , mggStr);
//					onContextItemSelected(item);
					break;
			}
//			if (drawer.isDrawerOpen(GravityCompat.START)) {
//				// ドロワーメニューが開いた際に閉じる画像を非表示にする
//				drawer.closeDrawer(GravityCompat.START);
//			} else {
//				drawer.openDrawer(GravityCompat.START);
//				// ドロワーメニューが開いた際に閉じる画像を表示する
//			}
			dbMsg += ">>" + toastStr;
			if ( !toastStr.equals("") ) {
				Toast.makeText(this , toastStr , Toast.LENGTH_SHORT).show();
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
		return false;
	}                                        //メニューとDrowerからの画面/機能選択

	static final int REQUEST_PREF = 100;                          //Prefarensからの戻り

	@Override
	protected void onActivityResult(int requestCode , int resultCode , Intent data) {
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

			sa_pad_v.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v , MotionEvent event) {
					final String TAG = "sa_pad_v[RBS]";
					String dbMsg = "";
					boolean retBool = true; //「TouchEventを消化」したものとしてこのビューに送らない (パッドに線を書かせない)
					try {
						float xPoint = event.getX();       //view上の座標
						float yPoint = event.getY();
						int action = event.getAction();
						dbMsg += "(" + xPoint + "×" + yPoint + ")action=" + action;
						if ( is_v_Mirror ) {   //左右反転
							dbMsg += "、幅=" + sa_disp_v.getWidth();
							xPoint = (sa_disp_v.getWidth() + 1.0f) - xPoint;
							dbMsg += ">xPoint>" + xPoint;

						}
						if ( is_h_Mirror ) {   //上下反転
							dbMsg += "、高さ=" + sa_disp_v.getHeight();
							yPoint = (sa_disp_v.getHeight() + 1.0f) - yPoint;
							dbMsg += ">yPoint>" + yPoint;
						}
						event.setLocation(xPoint , yPoint);
						sa_disp_v.onTouchEvent(event);               //イベントを送信；padのトレースでveiw側に描画
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
					return retBool;
				}
			});

			findViewById(R.id.cp_mirror_h_bt).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mirror_h_click();
				}
			});

			findViewById(R.id.cp_mirror_v_bt).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mirror_v_click();
				}
			});

			cp_score_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if ( !isNotSet ) {
						CharSequence wStr = "トレースした状況を読み取っています。";
						Toast.makeText(RecoveryBrainActivity.this , wStr , Toast.LENGTH_SHORT).show();
					}
					sa_disp_v.scorePixcel(sa_disp_v.PRG_CODE_SET_SCORE);
				}
			});

			cp_score_bt.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					scoreAuto();
					return true;    // 戻り値をtrueにするとOnClickイベントは発生しない
				}
			});

			findViewById(R.id.cp_about_bt).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent webIntent = new Intent(RecoveryBrainActivity.this , CS_Web_Activity.class);
					String dataURI = rootUrlStr + "/about_and/";                                  //http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080/about_and/
					webIntent.putExtra("dataURI" , dataURI);                        //最初に表示するページのパス
					startActivity(webIntent);
				}
			});

			wb_mode_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_mode_sp[WBC]";
					String dbMsg = "";
					try {
						dbMsg = ",position=" + position + ",id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { //
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							if ( -1 == sa_disp_v.selectColor ) {
								sa_disp_v.selectColor = selectColor;
							}
							selectMode = ( String ) spinner.getSelectedItem();
							dbMsg += ",selectMode=" + selectMode;
							if ( selectMode.equals(getString(R.string.rb_edit_tool_comp)) ) {
								transTraceMode();
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_text)) ) {
								showTextInputDlog();
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_free)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_free;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_line)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_line;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_trigone)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_trigone;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_rect)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_rect;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_oval)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_oval;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_erasre)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_erasre;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_select_del)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_select_del;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_stamp)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_stamp;
							} else if ( selectMode.equals(getString(R.string.rb_edit_tool_colorpic)) ) {
								sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_colorpic;
							}
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			wb_color_bt.setBackgroundColor(selectColor);
			wb_color_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "wb_color_bt[WB]";
					String dbMsg = "";
					try {
						mColorPickerDialog = new ColorPickerDialog(RecoveryBrainActivity.this , new ColorPickerDialog.OnColorChangedListener() {
							@Override
							public void colorChanged(int color) {
								final String TAG = "wb_color_bt[WB]";
								String dbMsg = "";
								selectColor = color;
								dbMsg = "selectColor=" + selectColor;
								sa_disp_v.setPenColor(selectColor);
								wb_color_bt.setBackgroundColor(selectColor);
								myLog(TAG , dbMsg);
							}
						} , selectColor);
						mColorPickerDialog.show();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
			wb_width_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_width_sp[WBC]";
					String dbMsg = "";
					try {

						dbMsg = "position=" + position + "id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { // 起動時に一度呼ばれてしまう
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							String item = ( String ) spinner.getSelectedItem();
							dbMsg += "item=" + item;
							selectWidth = Integer.parseInt(item);
							dbMsg += "selectWidth=" + selectWidth;
							if ( selectWidth < 1 ) {
								selectWidth = 1;
							}
							if ( sa_disp_v != null ) {
								sa_disp_v.setPenWidth(Math.round(selectWidth));
							}
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			wb_linecaps_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView< ? > parent , View view , int position , long id) {
					final String TAG = "wb_linecaps_sp[WBC]";
					String dbMsg = "";
					try {
						dbMsg = ",position=" + position + ",id=" + id;
						Spinner spinner = ( Spinner ) parent;
						if ( spinner.isFocusable() == false ) { // 起動時に一度呼ばれてしまう
							dbMsg += "isFocusable=false";
							spinner.setFocusable(true);
						} else {
							String item = ( String ) spinner.getSelectedItem();
							dbMsg += ",item=" + item;
							String[] items = getResources().getStringArray(R.array.lineCapSelecttValList);
							selectLineCap = items[position];
							dbMsg += ",selectLineCap=" + selectLineCap;
							if ( sa_disp_v != null ) {
								sa_disp_v.setPenCap(selectLineCap);                             //先端形状
							}
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onNothingSelected(AdapterView< ? > arg0) {
				}
			});

			ViewTreeObserver observer = sa_disp_v.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					final String TAG = "OnGlobalLayoutListener[RBS]";
					String dbMsg = "";
					try {
						int rInt = sa_disp_v.getWidth();
						if ( 0 < rInt ) {
							displyWidth = rInt;                     //表示veiwの幅
						}
						dbMsg = "{" + displyWidth;
						rInt = sa_disp_v.getHeight();
						if ( 0 < rInt ) {
							displyHight = rInt;                     //表示veiwの高さ
						}
						dbMsg += "×" + displyHight + "]";
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			//**判定後の操作ダイアログ表示*/
			cp_score_tv.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					final String TAG = "beforeTextChanged[RBS.score]";
					String dbMsg = "";
					try {
						dbMsg += "テキスト変更前=" + s + ",start="+start + ",count="+count + ",after="+after;
						String rStr = s.toString();
						rStr = rStr.trim();
						b_score = Integer.parseInt(rStr);
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					final String TAG = "onTextChanged[RBS.score]";
					String dbMsg = "";
					try {
//						dbMsg += "テキスト変更中=" + s + ",start="+start + ",count="+count;
//						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}

				@Override
				public void afterTextChanged(Editable s) {
					final String TAG = "afterTextChanged[RBS.score]";
					String dbMsg = "";
					try {
						dbMsg += "評価終了=" + sa_disp_v.isJudged ;
						  if(sa_disp_v.isJudged &&  0 < sa_disp_v.scoreVar  ){                                                       //  ! sa_disp_v.scoreMssg.equals("")
							  dbMsg += "テキスト変更前=" + b_score ;
							  String rStr = s.toString();
							  rStr = rStr.trim();
							  dbMsg += ",変更後=" + rStr ;
							  AlertDialog.Builder listDlg = new AlertDialog.Builder(RecoveryBrainActivity.this);                // リスト表示用のアラートダイアログ
							  LayoutInflater inflater = ( LayoutInflater ) RecoveryBrainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
							  final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
							  listDlg.setCustomTitle(titolLayout);
							  TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
							  dlog_title_tv.setText(R.string.common_trace_kekka);
							  ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
							  dlog_left_bt.setImageResource(android.R.drawable.btn_star_big_on);
							  ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
							  dlog_close_bt.setOnClickListener(new View.OnClickListener() {
								  @Override
								  public void onClick(View v) {
									  splashDlog.dismiss();
								  }
							  });
							  String msgStr =sa_disp_v.scoreMssg;
							  listDlg.setMessage(msgStr);
							  listDlg.setPositiveButton(R.string.common_next , new DialogInterface.OnClickListener() {
								  @Override
								  public void onClick(DialogInterface dialog , int which) {
									  final String TAG = "Positive[score]";
									  String dbMsg = "";
									  try {
										  dbMsg = "readFileName=" + readFileName;
										  sa_disp_v.scoreVar=0;
										  sa_disp_v.scoreMssg ="";
										  sa_disp_v.isJudged =false;
										  trasePictRead(readFileName);
										  myLog(TAG , dbMsg);
									  } catch (Exception er) {
										  myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
									  }
								  }
							  });
							  listDlg.setNegativeButton(R.string.common_again , new DialogInterface.OnClickListener() {
								  @Override
								  public void onClick(DialogInterface dialog , int which) {
									  final String TAG = "Negativ[score]";
									  String dbMsg = "";
									  try {
										  sa_disp_v.backAgain();
										  sa_disp_v.isPreparation = true;                    //トレーススタート前の準備中
										  sa_disp_v.scoreVar=0;
										  sa_disp_v.scoreMssg ="";
										  sa_disp_v.isJudged =false;
										  splashDlog.dismiss();
										  myLog(TAG , dbMsg);
									  } catch (Exception er) {
										  myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
									  }
								  }
							  });
							  splashDlog = listDlg.create();                // 表示
							  splashDlog.show();                // 表示

						  }

						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
					//
				}
			});

			trasePictRead(readFileName);
//			if(isFarst){
			if ( splashDlog != null ) {
				if ( splashDlog.isShowing() ) {
					splashDlog.dismiss();
				}
			}
			isFarst = false;       //初回起動
//			}
			isNotSet = false;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	protected void trasePictRead(String readFN) {
		final String TAG = "trasePictRead[RBS]";
		String dbMsg = "";
		try {
			isFarstPictRead = false;            //トレース元表示済み
			int canvasWidth = sa_disp_v.getWidth();
			int canvasHeight = sa_disp_v.getHeight();
			dbMsg += "canvas[" + canvasWidth + "×" + canvasHeight + "]";
			if(canvasWidth ==0 || canvasHeight == 0){
				 canvasWidth = displyWidth;    //sa_disp_v.getWidth();
				 canvasHeight = displyHight;    //sa_disp_v.getHeight();
				dbMsg += ">>[" + canvasWidth + "×" + canvasHeight + "]";
			}

			dbMsg +=",readFileName=" + readFN;
			if ( 0 < canvasWidth && 0 < canvasHeight ) {
				if ( sa_disp_v.addBitMap(this , readFN , canvasWidth , canvasHeight) ) {
					isFarstPictRead = true;            //トレース元表示済み
//					readFileName = readFN;
					dbMsg += "読み取り" + isFarstPictRead;
					sendNextStreoType( readFN) ;

//					public File[] stereoTypeFiles;    			//定例パターンファイルリスト
//					public String [];

				}else{
//				String titolStr = RecoveryBrainActivity.this.getString(R.string.common_yomikomicyuu);
//				String mggStr = RecoveryBrainActivity.this.getString(R.string.common_saikidou);
//				messageShow(titolStr , mggStr);
//				new AlertDialog.Builder(RecoveryBrainActivity.this).setTitle(titolStr).setMessage(mggStr).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog , int which) {
//						reStart();
//					}
//				}).create().show();
				}
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void sendNextStreoType(String readFN) {
		final String TAG = "sendNextStreoType[MA]";
		String dbMsg = "";
		try {
			List<String> fnList=Arrays.asList(stereoTypeFileNames);
			int nowIndex = fnList.indexOf(readFN);
			dbMsg += "=" + nowIndex +"/"+ fnList.size();
			if(-1<nowIndex){   //定例パターンにあるファイルで
				if(isStartLast){                             		//自動送りなら
					nowIndex++;
					if(fnList.size()-1 < nowIndex){
						nowIndex =0;
					}
					dbMsg += ">>" + nowIndex +"/"+ fnList.size();
					readFileName =fnList.get(nowIndex) ;
					dbMsg += "=" + readFileName;
					if(! readFileName.endsWith(".png")){
						sendNextStreoType( readFileName);
					}else{
						myEditor.putString("file_name_key" , readFileName);
						dbMsg += ",更新";
						myEditor.commit();
						dbMsg += "完了";
					}
				}
			}

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

	public AlertDialog jobDlog;
	public CharSequence[] jobListitems;
	public AlertDialog directionDlog;
	public CharSequence[] directiontListitems;

	/**
	 * トレース元画像の操作ボタンロングクリック
	 **/
	public void jobserect_click() {
		final String TAG = "jobserect_click[RBS]";
		String dbMsg = "";
		try {
			jobListitems = getResources().getStringArray(R.array.jobSelectList);  //				final CharSequence[] items = {"item1", "item2", "item3"};
			AlertDialog.Builder listDlg = new AlertDialog.Builder(this);                // リスト表示用のアラートダイアログ
			LayoutInflater inflater = ( LayoutInflater ) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
			listDlg.setCustomTitle(titolLayout);
			TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
			dlog_title_tv.setText(R.string.rb_job_select);
			ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
			dlog_left_bt.setImageResource(R.drawable.edit);
			ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
			dlog_close_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					jobDlog.dismiss();
				}
			});
			listDlg.setItems(jobListitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int which) {
					final String TAG = "onClick[jobserect]";
					String directionName = ( String ) jobListitems[which];
					String dbMsg = "(" + which + ")" + directionName;
					if ( directionName.equals(getString(R.string.rb_file_read)) ) {
						stereoTypeSelect(savePatht);
					} else if ( directionName.equals(getString(R.string.rb_hand_made)) ) {
						transEditMode();
					} else if ( directionName.equals(getString(R.string.rb_decision_original)) ) {
						transTraceMode();
					} else if ( directionName.equals(getString(R.string.rb_hand_again)) ) {
						sa_disp_v.backAgain();
					}
					myLog(TAG , dbMsg);
				}
			});
			jobDlog = listDlg.create();                // 表示
			jobDlog.show();                // 表示

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	/**
	 * 手書きで作成　に切り替え
	 */
	protected void transEditMode() {
		final String TAG = "transEditMode[RBS]";
		String dbMsg = "";
		try {
			score_aria_ll.setVisibility(View.GONE);
			wh_paret.setVisibility(View.VISIBLE);
			AlertDialog.Builder mDlg = new AlertDialog.Builder(RecoveryBrainActivity.this);
			LayoutInflater inflater = ( LayoutInflater ) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
			mDlg.setCustomTitle(titolLayout);
			TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
			dlog_title_tv.setText(R.string.rb_edit_tr_titol);
			ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
			dlog_left_bt.setImageResource(R.drawable.edit);
			ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
			dlog_close_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					jobDlog.dismiss();
				}
			});
			mDlg.setTitle(getString(R.string.rb_edit_tr_titol));
			mDlg.setMessage(getString(R.string.rb_edit_tr_msg));
			mDlg.setPositiveButton(R.string.common_yes , new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog , int which) {
					final String TAG = "Positive[transEditMode]";
					String dbMsg = "";
					try {
						sa_disp_v.isDrow = true;
						sa_disp_v.isPreparation = false;                    //トレーススタート前の準備中
						sa_disp_v.clearAll();
						sa_disp_v.InitCanvas();
						transEditEnd();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
			mDlg.setNegativeButton(R.string.common_no , new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog , int which) {
					final String TAG = "Negativ[transEditMode]";
					String dbMsg = "";
					try {
						sa_disp_v.isDrow = true;
						sa_disp_v.isPreparation = false;                    //トレーススタート前の準備中
						selectColor = sa_disp_v.orgColor;
						dbMsg += ",Color=" + selectColor;
						sa_disp_v.selectColor = selectColor;

						selectWidth = sa_disp_v.selectWidth;
						dbMsg += ",Width=" + selectWidth;
						selectLineCap = sa_disp_v.selectLineCap;
						dbMsg += ",LineCap=" + selectLineCap;
						transEditEnd();
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});
			jobDlog = mDlg.create();
			jobDlog.show();


			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	protected void transEditEnd() {
		final String TAG = "transEditEnd[RBS]";
		String dbMsg = "";
		try {
			selectColor = sa_disp_v.selectColor;
			dbMsg += ",Color=" + selectColor;
			selectWidth = sa_disp_v.selectWidth;
			dbMsg += ",Width=" + selectWidth;
			selectLineCap = sa_disp_v.selectLineCap;
			dbMsg += ",LineCap=" + selectLineCap;
			setEditTools(selectColor , selectWidth , selectLineCap);

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	/**
	 * 手書き編集ツールの初期選択
	 **/
	protected void setEditTools(int _color , float _widht , String _caps) {
		final String TAG = "setEditTools[RBS]";
		String dbMsg = "";
		try {
			wb_mode_sp.setSelection(0);
			dbMsg += ",Color=" + _color;
			wb_color_bt.setBackgroundColor(_color);
			dbMsg += ",Width=" + _widht;
			int widhtInt = Math.round(_widht);
			dbMsg += ">>" + widhtInt;
			int selIndex = 0;
			int bInt = 0;
			String[] items = getResources().getStringArray(R.array.lineWidthSelectList);
			for ( selIndex = 0; selIndex < items.length ; selIndex++ ) {
				dbMsg += "(" + selIndex + ")";
				int compInt = Integer.parseInt(items[selIndex]);
				if ( bInt <= widhtInt && widhtInt <= compInt ) {
					break;
				}
				bInt = compInt;
			}
			dbMsg += ">setSelection>" + selIndex;
			wb_width_sp.setSelection(selIndex);

			dbMsg += ",LineCap=" + _caps;
			items = getResources().getStringArray(R.array.lineCapSelecttValList);
			if ( _caps.equals(Paint.Cap.ROUND) || _caps.equals("round") ) {
				selIndex = 0;
			} else if ( _caps.equals(Paint.Cap.SQUARE) || _caps.equals("square") ) {
				selIndex = 1;
			} else if ( _caps.equals(Paint.Cap.BUTT) || _caps.equals("butt") ) {
				selIndex = 2;
			}
			dbMsg += ">setSelection>" + selIndex;
			wb_linecaps_sp.setSelection(selIndex);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onPause();
	}

	/**
	 * トレースモードに切り替え
	 */
	protected void transTraceMode() {
		final String TAG = "transTraceMode[RBS]";
		String dbMsg = "";
		try {
			wh_paret.setVisibility(View.GONE);
			score_aria_ll.setVisibility(View.VISIBLE);
//			sa_disp_v.isRecever = true;                    //受信側
			sa_disp_v.isDrow = false;
			sa_disp_v.isPreparation = true;                    //トレーススタート前の準備中
			readViewBitMap(sa_disp_v);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * このviewのビットマップを読み取ってトレースの準備をする
	 * http://developer.wonderpla.net/entry/blog/engineer/Android_CaptureView/
	 */
	public void readViewBitMap(View view) {
		final String TAG = "readViewBitMap[CView]";
		String dbMsg = "";
		Bitmap screenShot = null;
		try {
			view.setDrawingCacheEnabled(true);
			Bitmap cache = view.getDrawingCache();            // Viewのキャッシュを取得
			screenShot = Bitmap.createBitmap(cache);
			if ( cache == null ) {
				String titolStr = "Bitmapの取得";
				String mggStr = "取得できませ年でした。";
				messageShow(titolStr , mggStr);
				return;
			}
			view.setDrawingCacheEnabled(false);
			dbMsg += " = " + screenShot.getByteCount() + "バイト";
			sa_disp_v.getCanvasPixcel(screenShot);       //
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * トレース元画像の回転ボタンクリック
	 **/
	public void direction_click() {
		final String TAG = "cp_direction_click[RBS]";
		String dbMsg = "";
		try {
			directiontListitems = getResources().getStringArray(R.array.directionSelectList);  //				final CharSequence[] items = {"item1", "item2", "item3"};
			AlertDialog.Builder listDlg = new AlertDialog.Builder(this);                // リスト表示用のアラートダイアログ
			LayoutInflater inflater = ( LayoutInflater ) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
			listDlg.setCustomTitle(titolLayout);
			TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
			dlog_title_tv.setText(R.string.rb_direction_select);
			ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
			dlog_left_bt.setImageResource(android.R.drawable.ic_menu_rotate);
			ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
			dlog_close_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					directionDlog.dismiss();
				}
			});
			listDlg.setItems(directiontListitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int which) {
					final String TAG = "onClick[direction]";
					String directionName = ( String ) directiontListitems[which];
					String dbMsg = "(" + which + ")" + directionName;
					if ( directionName.equals(getString(R.string.rb_direction_org)) ) {
						pendeingMessege();
						sa_disp_v.redrowOrigin();
					} else if ( directionName.equals(getString(R.string.rb_roat_right)) ) {
						sa_disp_v.canvasSubstitution(R.string.rb_roat_right);
					} else if ( directionName.equals(getString(R.string.rb_roat_left)) ) {
						sa_disp_v.canvasSubstitution(R.string.rb_roat_left);
					} else if ( directionName.equals(getString(R.string.rb_roat_half)) ) {
						sa_disp_v.canvasSubstitution(R.string.rb_roat_half);
					} else if ( directionName.equals(getString(R.string.rb_flip_vertical)) ) {
						sa_disp_v.canvasSubstitution(R.string.rb_flip_vertical);
					} else if ( directionName.equals(getString(R.string.rb_flip_horizontal)) ) {
						sa_disp_v.canvasSubstitution(R.string.rb_flip_horizontal);
					} else if ( directionName.equals(getString(R.string.rb_make_original)) ) {
						pendeingMessege();
						sa_disp_v.setOriginPixcel();
					} else if ( directionName.equals(getString(R.string.rb_direction_save)) ) {
						sa_disp_v.bitmapSave(RecoveryBrainActivity.this , RecoveryBrainActivity.this , savePatht);
					}
					myLog(TAG , dbMsg);
				}
			});
			directionDlog = listDlg.create();                // 表示
			directionDlog.show();                // 表示

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	/**
	 * 上下鏡面動作
	 **/
	public void mirror_h_click() {
		final String TAG = "mirror_h_click[RBS]";
		String dbMsg = "";
		try {
			CharSequence toastStr = "";
			if ( is_h_Mirror ) {
				is_h_Mirror = false;
				toastStr = getString(R.string.rb_flip_vertical) + getString(R.string.common_wo) + getString(R.string.common_kijyo);
				cp_mirror_h_bt.setImageResource(R.drawable.mirror_h);
			} else {
				is_h_Mirror = true;
				toastStr = getString(R.string.common_trace_kekka) + getString(R.string.common_ga) + getString(R.string.rb_flip_vertical) + getString(R.string.common_site_hannei_saremasu);
				cp_mirror_h_bt.setImageResource(R.drawable.mirror_h_t);
			}
			myMenu.findItem(R.id.rbm_mirror_movement_to).setChecked(is_h_Mirror);                    //表示/非表示
			if ( !isNotSet ) {
				Toast.makeText(this , toastStr , Toast.LENGTH_SHORT).show();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	/**
	 * 左右鏡面動作
	 **/
	public void mirror_v_click() {
		final String TAG = "mirror_v_click[RBS]";
		String dbMsg = "";
		try {
			CharSequence toastStr = "";
			if ( is_v_Mirror ) {
				is_v_Mirror = false;
				toastStr = getString(R.string.rb_flip_horizontal) + getString(R.string.common_wo) + getString(R.string.common_kijyo);
				cp_mirror_v_bt.setImageResource(R.drawable.mirror_v);
			} else {
				is_v_Mirror = true;
				toastStr = getString(R.string.common_trace_kekka) + getString(R.string.common_ga) + getString(R.string.rb_flip_horizontal) + getString(R.string.common_site_hannei_saremasu);
				cp_mirror_v_bt.setImageResource(R.drawable.mirror_v_t);
			}
			myMenu.findItem(R.id.rbm_mirror_movement_to).setChecked(is_v_Mirror);                    //表示/非表示
			if ( !isNotSet ) {
				Toast.makeText(this , toastStr , Toast.LENGTH_SHORT).show();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	/**
	 * 自動判定
	 **/
	public void scoreAuto() {
		final String TAG = "scoreAuto[RBS]";
		String dbMsg = "";
		try {
			CharSequence toastStr = "";
			toastStr = getString(R.string.aout_judge_msg1);
			if ( sa_disp_v.isAutoJudge ) {
				sa_disp_v.isAutoJudge = false;
				cp_score_bt.setImageResource(android.R.drawable.btn_star_big_off);
			} else {
				sa_disp_v.isAutoJudge = true;
				toastStr = getString(R.string.aout_judge_msg2);
				cp_score_bt.setImageResource(android.R.drawable.btn_star_big_on);
			}
			myMenu.findItem(R.id.rbm_auto_judge).setChecked(is_v_Mirror);
			if ( !isNotSet ) {
				Toast.makeText(this , toastStr , Toast.LENGTH_SHORT).show();
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		super.onStop();
	}

	public AlertDialog stereoTypeDlog;
	public List< String > iconList;

	public void stereoTypeRady(String writeFolder) {
		final String TAG = "stereoTypeRady[RBS]";
		String dbMsg = "";
		try {
			iconList = new ArrayList<>();                // 要素をArrayListで設定
			AssetManager assetMgr = getResources().getAssets();
			 stereoTypeFileNames = assetMgr.list("");
			String dLogTitol = getString(R.string.thumbnail_list_titol);
			if ( !writeFolder.equals("") ) {
				dbMsg += "writeFolder= " + writeFolder;
				CS_Util UTIL = new CS_Util();
				UTIL.maikOrgPass(writeFolder);                    //フォルダを検索して無ければ作る
//				File tFolder = new File(writeFolder);
//				if(tFolder.exists()){
//
//				}
				dLogTitol = getString(R.string.file_list_titol);
				stereoTypeFiles = new File(writeFolder).listFiles();
				int fLen = stereoTypeFiles.length;
				dbMsg += ",tFiles= " + fLen + "件";
				if ( 0 < fLen ) {
					for ( int i = 0 ; i < fLen ; i++ ) {
						String tfName = stereoTypeFiles[i].getName();
						dbMsg += "(" + i + ")" + tfName;
						if ( tfName.endsWith(".png") ) {
							String fpn = writeFolder + File.separator + tfName;
							dbMsg += ">>" + fpn;
							iconList.add(fpn);
						}
					}
				} else {
					String mggStr = writeFolder + "にファイルが有りません。\nPCなどからコピーするか手書きで作成して保存して下さい。";
					messageShow(dLogTitol , mggStr);
					return;
				}
			} else {
				for ( int i = 0 ; i < stereoTypeFileNames.length ; i++ ) {
					dbMsg += "(" + i + ")" + stereoTypeFileNames[i];
					if ( stereoTypeFileNames[i].endsWith(".png") ) {
						iconList.add(stereoTypeFileNames[i]);
					}
				}
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	/**
	 * 定例パターン選択
	 * http://hakoniwadesign.com/?p=10661
	 */
	public void stereoTypeSelect(String writeFolder) {
		final String TAG = "stereoTypeSelect[RBS]";
		String dbMsg = "";
		try {
			if(writeFolder.equals("")){
				stereoTypeRady( writeFolder);
			}
			if(stereoTypeFiles == null || stereoTypeFileNames==null){
				stereoTypeRady( writeFolder);
			}else if(stereoTypeFiles.length == 0 || stereoTypeFileNames.length ==0){
				stereoTypeRady( writeFolder);
			}
			String dLogTitol = getString(R.string.thumbnail_list_titol);
				dLogTitol = getString(R.string.file_list_titol);

			dbMsg += "," + dLogTitol;
			dbMsg += ">>" + iconList.size() + "件";
			// カスタムビューを設定    http://androidguide.nomaki.jp/html/dlg/custom/customMain.html
			LayoutInflater inflater = ( LayoutInflater ) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dlog_thumb , ( ViewGroup ) findViewById(R.id.thum_root));
			GridView gridview = layout.findViewById(R.id.gridview);                // GridViewのインスタンスを生成
			GridAdapter adapter = new GridAdapter(RecoveryBrainActivity.this , R.layout.grid_items , iconList);            // BaseAdapter を継承したGridAdapterのインスタンスを生成
			gridview.setAdapter(adapter);                // gridViewにadapterをセット
			gridview.setOnItemClickListener(this);            // item clickのListnerをセット
			// アラーとダイアログ を生成
			AlertDialog.Builder builder = new AlertDialog.Builder(this , R.style.MyAlertDialogStyle);
			final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
			builder.setCustomTitle(titolLayout);
			TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
			dlog_title_tv.setText(dLogTitol);  //			builder.setTitle( R.string.thumbnail_list_titol);
			ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
			dlog_left_bt.setImageResource(R.drawable.edit);
			ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
			dlog_close_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stereoTypeDlog.dismiss();
				}
			});
			builder.setView(layout);
//			builder.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					// タップしたアイテムの取得
//					ListView listView = (ListView)parent;
//					SampleListItem item = (SampleListItem)listView.getItemAtPosition(position);  // SampleListItemにキャスト
//
//					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//					builder.setTitle("Tap No. " + String.valueOf(position));
//					builder.setMessage(item.getTitle());
//					builder.show();
//				}
//			};
//			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					// Cancel ボタンクリック処理
//				}
//			});
			stereoTypeDlog = builder.create();                // 表示
			stereoTypeDlog.show();                // 表示
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onItemClick(AdapterView< ? > parent , View v , int position , long id) {
		final String TAG = "onItemClick[TA]";
		String dbMsg = "";
		try {
			String readFN = "" + iconList.get(position);
			dbMsg = "" + readFN;   //			textView.setText(il.getName(position));
			trasePictRead(readFN);

//			int canvasWidth = sa_disp_v.getWidth();
//			int canvasHeight = sa_disp_v.getHeight();
//			if(canvasWidth ==0 || canvasHeight == 0){
//				canvasWidth = displyWidth;    //sa_disp_v.getWidth();
//				canvasHeight = displyHight;    //sa_disp_v.getHeight();
//			}
//			dbMsg += "canvas[" + canvasWidth + "×" + canvasHeight + "]";
//			sa_disp_v.addBitMap(this , readFileName , canvasWidth , canvasHeight);
			myLog(TAG , dbMsg);
			stereoTypeDlog.dismiss();
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	class ViewHolder {
		ImageView imageView;
	}

	// BaseAdapter を継承した GridAdapter クラスのインスタンス生成
	class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private int layoutId;
		private List< String > icList = new ArrayList< String >();
//		private String writeFolder;

		GridAdapter(Context context , int layoutId , List< String > iconList) {
			super();
			this.inflater = ( LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutId = layoutId;
			icList = iconList;
//			this.writeFolder = writeFolder;
		}

		@Override
		public View getView(int position , View convertView , ViewGroup parent) {
			final String TAG = "getView[TA]";
			String dbMsg = "";
			try {
				ViewHolder holder;
				if ( convertView == null ) {
					convertView = inflater.inflate(layoutId , parent , false);                    // main.xml の <GridView .../> に grid_items.xml を inflate して convertView とする
					holder = new RecoveryBrainActivity.ViewHolder();                        // ViewHolder を生成
					holder.imageView = ( ImageView ) convertView.findViewById(R.id.image_view);
					convertView.setTag(holder);
				} else {
					holder = ( RecoveryBrainActivity.ViewHolder ) convertView.getTag();
				}
				Bitmap rsBitmap = loadBitmapAsset(icList.get(position));
				int bmpWidth = rsBitmap.getWidth();
				int bmpHeight = rsBitmap.getHeight();
				dbMsg += "[" + bmpWidth + "×" + bmpHeight + "]" + rsBitmap.getByteCount() + "バイト";
				holder.imageView.setImageBitmap(rsBitmap);   //				holder.imageView.setImageResource(icList.get(position));
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
			return convertView;
		}

		/**
		 * assets フォルダから、画像ファイルを読み込む
		 * http://fantom1x.blog130.fc2.com/blog-entry-130.html
		 * separatorから上が有ればフルパスで指定されたファイルを開く
		 */
		public final Bitmap loadBitmapAsset(String fileName) throws IOException {
			final String TAG = "loadBitmapAsset[TA]";
			String dbMsg = "";
			Bitmap retBM = null;
			dbMsg = "fileName=" + fileName;
			if ( fileName.contains(File.separator) ) {
				try {
					File srcFile = new File(fileName);
					FileInputStream fis = new FileInputStream(srcFile);
					retBM = BitmapFactory.decodeStream(fis);
					fis.close();
				} catch (IOException er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				} catch (Exception er) {
					myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
				}
			} else {
				BufferedInputStream bis = null;
				try {
					final AssetManager assetManager = RecoveryBrainActivity.this.getAssets();
					bis = new BufferedInputStream(assetManager.open(fileName));
					retBM = BitmapFactory.decodeStream(bis);
				} finally {
					try {
						bis.close();
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			}
			return retBM;
		}

		@Override
		public int getCount() {
			return iconList.size();            // 全要素数を返す
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	///手書き編集//////////////////////////////////////////////////////////////////
	EditText dtd_input_ti;
	Spinner dtd_size_sp;

	protected void showTextInputDlog() {
		final String TAG = "showTextInputDlog[RBS]";
		String dbMsg = "";
		try {
			LayoutInflater inflater = ( LayoutInflater ) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dlog_text_drow , ( ViewGroup ) findViewById(R.id.dtd_root));
			dtd_input_ti = layout.findViewById(R.id.dtd_input_ti);
			dbMsg += ",変更前；drowStr=" + sa_disp_v.drowStr;
			dtd_input_ti.setText(sa_disp_v.drowStr);
			dtd_size_sp = layout.findViewById(R.id.dtd_size_sp);
//			String[] fontSizeList = getResources().getStringArray(R.array.fontSizeList);
			List< String > fontSizeList = new ArrayList< String >(Arrays.asList(getResources().getStringArray(R.array.fontSizeList)));
			dbMsg += ",drowStrSize=" + sa_disp_v.drowStrSize;
			int sIndex = fontSizeList.indexOf(sa_disp_v.drowStrSize + "");
			if ( -1 == sIndex ) {
				sIndex = fontSizeList.size() - 1;
			}
			dbMsg += ",sIndex=" + sIndex + "/" + (fontSizeList.size() - 1);
			dtd_size_sp.setSelection(sIndex);
			Button dtd_positive_bt = layout.findViewById(R.id.dtd_positive_bt);                // GridViewのインスタンスを生成
			// アラーとダイアログ を生成
			AlertDialog.Builder builder = new AlertDialog.Builder(this , R.style.MyAlertDialogStyle);
			final View titolLayout = inflater.inflate(R.layout.dlog_titol , null);
			builder.setCustomTitle(titolLayout);
			TextView dlog_title_tv = ( TextView ) titolLayout.findViewById(R.id.dlog_title_tv);
			dlog_title_tv.setText(getString(R.string.dtd_titol));  //			builder.setTitle( R.string.thumbnail_list_titol);
			ImageButton dlog_left_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_left_bt);
			dlog_left_bt.setImageResource(R.drawable.edit);
			ImageButton dlog_close_bt = ( ImageButton ) titolLayout.findViewById(R.id.dlog_close_bt);
			dlog_close_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stereoTypeDlog.dismiss();
				}
			});

			dtd_positive_bt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String TAG = "showTextInputDlog[RBS;sTID]";
					String dbMsg = "";
					try {
						stereoTypeDlog.dismiss();
						String setStr = dtd_input_ti.getText() + "";
						dbMsg += "setStr=" + setStr;
						if ( setStr.equals("") ) {
							String titolStr = getString(R.string.dtd_titol);
							String mggStr = getString(R.string.dtd_input_msg2);
							new AlertDialog.Builder(RecoveryBrainActivity.this).setTitle(titolStr).setMessage(mggStr).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog , int which) {
									showTextInputDlog();
								}
							}).create().show();
						} else {
							sa_disp_v.drowStr = setStr;
							setStr = ( String ) dtd_size_sp.getSelectedItem();
							dbMsg += ",size=" + setStr;
							sa_disp_v.drowStrSize = Integer.parseInt(setStr);
							sa_disp_v.REQUEST_CORD = R.string.rb_edit_tool_text;
						}
						myLog(TAG , dbMsg);
					} catch (Exception er) {
						myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
					}
				}
			});

			builder.setView(layout);
//			builder.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					// タップしたアイテムの取得
//					ListView listView = (ListView)parent;
//					SampleListItem item = (SampleListItem)listView.getItemAtPosition(position);  // SampleListItemにキャスト
//
//					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//					builder.setTitle("Tap No. " + String.valueOf(position));
//					builder.setMessage(item.getTitle());
//					builder.show();
//				}
//			};
//			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					// Cancel ボタンクリック処理
//				}
//			});
			stereoTypeDlog = builder.create();                // 表示
			stereoTypeDlog.show();                // 表示
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	public void pendeingMessege() {
		String titolStr = "制作中です";
		String mggStr = "最終リリースをお待ちください";
		messageShow(titolStr , mggStr);
	}


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

/*
 * 課題
 * ・起動Activtyの入れ替え
 * ・単独実行Actvtyから開始
 * ・canvas間通信
 * ・padのトレースでveiw側に描画

 * 8/20 ・定例パターン読込み
 * ・パターン再作成（droｗで端部をきれいに、反転で作った定型の差替え；web更新）
 * 8/21 	・pixcel配列読込み
 * ・評価実装
 * 8/22	 ・消し込みPaintプロパティ送信
 * ・消し込み評価
		 * ・padのtachiendで自動評価
		 * ・toolbarへの書き込み
		 * ・もう一度
 * 8/25・変形
		 * ・右へ90度回転
		 * ・左へ90度回転
		 * ・180度回転
		 * ・上下反転
		 * ・左右反転
		 * ・オリジナル（に戻す）
		 * ・動作設定
 *8/24
 * ・鏡面動作
	 * ・上下
	 * ・左右
	 * ・トレース後に自動判定
	 * ・toolBarカスタマイズ
		 * ・もう一度ボタン
		 * ・変形ボタンリスト
		 * ・手動評価ボタン
 8/23・配布
	 ・GoogleDrive登録
 * ・取説web
	 * ・リンク
	 * ・概要紹介
・ファイルから読込み
・手書き
	・操作パレット切替
	・直線/三角/矩形/楕円
	・消しゴム
・ファイル保存
	9/4	・プログレス処理
	8/23・Splash >> 実際の起動処理中表示

着手後課題
・起動最適化
	・保留バグフィックス
		・トレース/手書きモード保持；説明書きから戻ると初期化される
		・メニュー表示後、鏡面動作や自動評価などのフラグがリセットされる
	・追加開発
		・Stringリソース英訳
	 課題
	   PreferenceScreenの"conection_setting_key"；URLの書き込み方 ；
	   カラーパレットサイズと初期値設定
	   トレース結果；準備中との表示切替

 * ・iOS移植
	 * ・フレームワーク
	 * ・ライフサイクル
	 * ・コールバック
	 * ・GUI
	 * ・デバイスアクセス
             https://drive.google.com/file/d/1SUWH1OHp6NG1RCWw3EtNPKitpOHWntrP/view?usp=sharing
            https://drive.google.com/file/d/1SUWH1OHp6NG1RCWw3EtNPKitpOHWntrP/view?usp=sharing
 **/