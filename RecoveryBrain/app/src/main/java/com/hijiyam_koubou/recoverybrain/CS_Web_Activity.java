package com.hijiyam_koubou.recoverybrain;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//
//public class CS_Web_Activity extends AppCompatActivity {
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_cs_web);
//	}
//}

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
//import android.graphics.Picture;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
//import android.view.View;
//import android.view.Window;				//タイトルバーに文字列を設定
//import android.view.WindowManager;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
//import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//インターネットに出るにはAndroidManifest.xmlを開きandroid.permission.INTERNET
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;

public class CS_Web_Activity extends Activity  implements NavigationView.OnNavigationItemSelectedListener  {
	private Toolbar toolbar;
	public ActionBarDrawerToggle abdToggle;        //アニメーションインジケータ
	public NavigationView navigationView;
	private DrawerLayout drawer;
	public ImageView navi_head_iv;
	public TextView nave_head_main_tv;
	public TextView navi_head_sub_tv;
	
	public WebView webView;
	public WebSettings settings;
	public String dbBlock = "";
	public String fName = null;
	public String MLStr = "";
	public String dataURI = "";
	public String fType = "";
	public String baseUrl = "";
	public boolean En_ZUP = true;            //ズームアップメニュー有効
	public boolean En_ZDW = true;            //ズームアップメニュー無効
	public boolean En_FOR = false;            //1ページ進む";
	public boolean En_BAC = false;            //1ページ戻る";

	//プリファレンス設定
	SharedPreferences myNFV_S_Pref;
	Editor pNFVeditor;

	public static final int MENU_WQKIT = 800;                            //これメニュー
	public static final int MENU_WQKIT_ZUP = MENU_WQKIT + 1;            //ズームアップ
	public static final int MENU_WQKIT_ZDW = MENU_WQKIT_ZUP + 1;        //ズームダウン
	public static final int MENU_WQKIT_FOR = MENU_WQKIT_ZDW + 1;        //1ページ進む
	public static final int MENU_WQKIT_BAC = MENU_WQKIT_FOR + 1;        //1ページ戻る
	public static final int MENU_WQKIT_END = MENU_WQKIT_BAC + 10;        //webkit終了

	public final CharSequence CTM_WQKIT_ZUP = "ズームアップ";
	public final CharSequence CTM_WQKIT_ZDW = "ズームダウン";
	public final CharSequence CTM_WQKIT_FOR = "1ページ進む";
	public final CharSequence CTM_WQKIT_BAC = "1ページ戻る";
	public final CharSequence CTM_WQKIT_END = "表示終了";

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {        //org;publicvoid
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[WabA]";
		String dbMsg = "";
		try {
			Bundle extras = getIntent().getExtras();
			dataURI = extras.getString("dataURI");                        //最初に表示するページのパス
			baseUrl = "file://" + extras.getString("baseUrl");                //最初に表示するページを受け取る
			fType = extras.getString("fType");                            //データタイプ
			String[] testSrA = dataURI.split(File.separator);
			fName = testSrA[testSrA.length - 1];
			dbMsg += "dataURI=" + dataURI + ",fType=" + fType + ",fName=" + fName + ",baseUrl=" + baseUrl;////////////////////////////////////////////////////////////////////////

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);        //タスクバーを 非表示
			requestWindowFeature(Window.FEATURE_NO_TITLE);                            //タイトルバーを非表示

			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);        //ローディングをタイトルバーのアイコンとして表示☆リソースを読み込む前にセットする
			setContentView(R.layout.activity_cs_web);
			
			initDrawer();

			webView = ( WebView ) findViewById(R.id.webview);        // Webビューの作成
//			webView.setVerticalScrollbarOverlay(false);					//縦スクロール有効
			settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);                        //JavaScriptを有効化
			initSize();
			webView.clearCache(true);
			MLStr = dataURI;
			dbMsg += fType + "をMLStr=" + MLStr;////////////////////////////////////////////////////////////////////////
//			webView.loadUrl(MLStr);
			webView.setWebViewClient(new WebViewClient() {        //リンク先もこのWebViewで表示させる；端末のブラウザを起動させない
				@Override
				public void onPageStarted(WebView view , String url , Bitmap favicon) {
					super.onPageStarted(view , url , favicon);
					setProgressBarIndeterminateVisibility(true);
					setTitle(url);    //タイトルバーに文字列を設定
				}

				@Override
				public void onPageFinished(WebView view , String url) {
					super.onPageFinished(view , url);
					if ( fName == null ) {
						String tStr = "";
						tStr = webView.getTitle();
						dbBlock = "tStr=" + tStr;////////////////////////////////////////////////////////////////////////
//						Log.d("onPageFinished","wKit；"+dbBlock);
						//		Toast.makeText(webView.getContext(), webView.getTitle(), Toast.LENGTH_LONG).show();
						setTitle(webView.getTitle());    //タイトルバーに文字列を設定
					}
					setProgressBarIndeterminateVisibility(false);
				}
//				PictureListener picture = new PictureListener(){
//					public void onNewPicture (WebView view, Picture picture){
//						Object loading;
//						if (((Object) loading).isShowing()) {
//							loading.dismiss();
//						}
//					}
//				}

			});
			setNewPage(MLStr);
			registerForContextMenu(webView);     //コンテキストメニューをwebViewに割付け
//			webView.loadUrl(requestToken);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	///ハンバーガーメニュー//////////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Override
//	public void onBackPressed() {
////		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
//		if ( drawer.isDrawerOpen(GravityCompat.START) ) {
//			drawer.closeDrawer(GravityCompat.START);
//		} else {
//			super.onBackPressed();
//		}
//	}
//
//	@Override
//	protected void onPostCreate(Bundle savedInstanceState) {
//		super.onPostCreate(savedInstanceState);
//		final String TAG = "onPostCreate[MA}";
//		String dbMsg = "";
//		try {
//			abdToggle.syncState();    //NaviIconの回転アニメーションなど   Attempt to invoke virtual method 'void android.support.v7.app.ActionBarDrawerToggle.syncState()' on a null object reference
//			myLog(TAG, dbMsg);
//		} catch (Exception e) {
//			myErrorLog(TAG, dbMsg + "で" + e.toString());
//		}
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		final String TAG = "onConfigurationChanged[MA}";
//		String dbMsg = "";
//		try {
//			abdToggle.onConfigurationChanged(newConfig);
//			myLog(TAG, dbMsg);
//		} catch (Exception e) {
//			myErrorLog(TAG, dbMsg + "で" + e.toString());
//		}
//	}

	/**
	 * NaviViewの初期設定
	 * 開閉のイベント設定
	 **/
	public void initDrawer() {            //http://qiita.com/androhi/items/f12b566730d9f951b8ec
		final String TAG = "initDrawer[MA}";
		String dbMsg = "";
		try {
			//		nvh_img = ( ImageView ) findViewById(R.id.nvh_img);                //NaviViewヘッダーのアイコン
			drawer = ( DrawerLayout ) findViewById(R.id.web_dl);
			myLog(TAG, dbMsg);
//			abdToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//			abdToggle.setDrawerIndicatorEnabled(true);
//			drawer.setDrawerListener(abdToggle);    //Attempt to invoke virtual method 'void android.support.v4.widget.DrawerLayout.setDrawerListener(android.support.v4.widget.DrawerLayout$DrawerListener)' on a null object reference
//			getSupportActionBar().setDisplayHomeAsUpEnabled(true);            //左矢印←アイコンになる
//			getSupportActionBar().setDisplayShowHomeEnabled(true);
//			myLog(TAG, dbMsg);
			navigationView = ( NavigationView ) findViewById(R.id.web_nv);
			navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					final String TAG = "onNavigationItemSelected[MyActivity.initDrawer]";
					String dbMsg = "MenuItem" + menuItem.toString();/////////////////////////////////////////////////
					boolean retBool = false;
					try {
						retBool = funcSelected(menuItem);
						CS_Web_Activity.this.drawer.closeDrawers();
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
				case R.id.md_qr_read:     //QRコードから接続
					callQuit();
//					Intent qra = new Intent(this , QRActivity.class);
//					startActivity(qra);
					break;
				case R.id.md_stand_alone:          //スタンドアロン
				case R.id.mm_stand_alone:          //スタンドアロン
					Intent saIntent = new Intent(this , StandAloneActivity.class);
					startActivity(saIntent);
					break;
				case R.id.wd_web_menu:     //webメニュー
//					registerForContextMenu(webView);     //コンテキストメニューをwebViewに割付け
					break;
					case R.id.md_prefarence:      //設定
				case R.id.mm_prefarence:      //設定
					Intent settingsIntent = new Intent(CS_Web_Activity.this , MyPreferencesActivty.class);
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


	public String retML(String dataStr) {        //受け取ったデータによってHTMLを変える
		String retStr = null;
		try {
			dbBlock = "dataStr=" + dataStr;////////////////////////////////////////////////////////////////////////

		} catch (Exception e) {
			Log.e("retML" , dbBlock + "；" + e.toString());
		}
		return retStr;
	}


	public void callQuit() {            //このActivtyの終了
		try {
			this.finish();
		} catch (Exception e) {
			Log.e("callQuit" , "wKitで" + e.toString());
		}
	}


	public boolean wZoomUp() {                //ズームアップして上限に達すればfalse
		try {
			En_ZUP = webView.zoomIn();            //ズームアップメニューのフラグ設定
		} catch (Exception e) {
			Log.e("wZoomUp" , e.toString());
			return false;
		}
		return En_ZUP;
	}

	public boolean wZoomDown() {                //ズームダウンして下限に達すればfalse
		try {
			En_ZDW = webView.zoomOut();            //ズームダウンのフラグ設定
		} catch (Exception e) {
			Log.e("wZoomDown" , e.toString());
			return false;
		}
		return En_ZDW;
	}

	public void wForward() {                    //ページ履歴で1つ後のページに移動する
		try {
			webView.goForward();                //ページ履歴で1つ後のページに移動する
		} catch (Exception e) {
			Log.e("wForward" , e.toString());
		}
	}

	/**
	 * 前に表示していたページに戻る
	 * */
	public void wGoBack() {                    //ページ履歴で1つ前のページに移動する
		try {
			dbBlock = "canGoBack=" + webView.canGoBack();//+",getDisplayLabel="+String.valueOf(event.getDisplayLabel())+",getAction="+event.getAction();////////////////////////////////
			//		Log.d("wGoBack",dbBlock);
			if ( webView.canGoBack() ) {        //戻るページがあれば
				webView.goBack();                    //ページ履歴で1つ前のページに移動する
			} else {                            //無ければ終了
				callQuit();            //このActivtyの終了
//				String titolStr ="バックキーが押されました";
//				String mggStr="webビューを終了しますか？";
//				new AlertDialog.Builder(this)
//						.setTitle(titolStr)
//						.setMessage(mggStr)
//						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								callQuit();            //このActivtyの終了
//							}
//						})
//						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//							}
//						})
//						.create().show();
			}
		} catch (Exception e) {
			Log.e("wGoBack" , e.toString());
		}
	}

	public void zoomSetting() {
		final String TAG = "zoomSetting[WabA]";
		String dbMsg = "";
		try {
//			settings.setBuiltInZoomControls(true);						//ズームコントロールを表示し
			settings.setSupportZoom(true);                                //ピンチ操作を有効化
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 指定したURLを表示
	 */
	public void setNewPage(String urlStr) {
		final String TAG = "setNewPage[WabA]";
		String dbMsg = "urlStr=" + urlStr;
		try {
			webView.loadUrl(urlStr);           //192.168.100.6:3080
			MLStr = urlStr;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	//https://qiita.com/morigamix/items/6083e2e63793021babf9
	public void initSize() {
		final String TAG = "initSize[WabA]";
		String dbMsg = "";
		try {
			// setInitialScaleを使う場合はこの2つをfalseにしないといけない（referenceにも書いてある）
			settings.setLoadWithOverviewMode(true);         //レスポンシブデザインが正常に表示されない場合
			settings.setUseWideViewPort(true);                //100％サイズで表示
			// Densityに合わせてスケーリング
//			var scale = this.context.resources.displayMetrics.density * 100
			webView.setInitialScale(1);
			webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);                            //スクロールバーをWebView内に含める
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * urlを指定して移動
	 */
	public void setURL() {
		final String TAG = "setURL[WabA]";
		String dbMsg = "";
		try {
			String titolStr = getString(R.string.wv_set_url_titol);			//"URL設定";
			String mggStr = getString(R.string.wv_set_url_msg);			//"手入力で移動先のURLを入力して下さい。";
			final EditText editView = new EditText(this);
			editView.setText(webView.getUrl());
			new AlertDialog.Builder(this)
//					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(titolStr).setMessage(mggStr).setView(editView).setPositiveButton("OK" , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int whichButton) {
					MLStr = editView.getText().toString();
					if ( MLStr != null && !MLStr.equals("") ) {
						Toast.makeText(getApplicationContext() , MLStr +  getString(R.string.wv_move_now) , Toast.LENGTH_LONG).show();
						setNewPage(MLStr);         //192.168.100.6:3080
					}
				}
			}).setNegativeButton(getString(R.string.common_cancel) , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog , int whichButton) {
				}
			}).show();

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 履歴リストを表示して移動
	 * http://d.hatena.ne.jp/maachang/20140804/1407132803
	 */
	public void veiwHysteresis() {
		final String TAG = "veiwHysteresis[WabA]";
		String dbMsg = "";
		try {
			WebBackForwardList list = webView.copyBackForwardList();                    // http,httpsのURL以外は無視した、HistoryBackを行う.
			int len = list.getSize();
			if ( 0 < len ) {
				dbMsg = "list=" + len + "件";
				List< String > bfList = new ArrayList<>();
				for ( int i = 0 ; i < len ; i++ ) {
					String url = list.getItemAtIndex(i).getUrl();
					dbMsg += " , " + bfList.size() + ")" + url;
					bfList.add(url);
				}
				dbMsg += ">>" + bfList.size() + "件";
				final CharSequence[] items = ( CharSequence[] ) bfList.toArray(new String[bfList.size()]);
				dbMsg += ">>" + items.length + "件";
				AlertDialog.Builder listDlg = new AlertDialog.Builder(this);
				listDlg.setTitle("タップして選択");
				listDlg.setItems(items , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog , int which) {
						// リスト選択時の処理
						// which は、選択されたアイテムのインデックス
						MLStr = ( String ) items[which];            //	editView.getText().toString();
						if ( MLStr != null ) {
							Toast.makeText(getApplicationContext() , MLStr + "へ移動中…" , Toast.LENGTH_LONG).show();
							setNewPage(MLStr);         //192.168.100.6:3080
						}
					}
				});
				listDlg.create().show();                // 表示


//			webView.goBackOrForward( 0 - cnt ) ;
			} else {
				String titolStr = "履歴";
				String mggStr = "まだ履歴は有りません";
				messageShow(titolStr , mggStr);
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void clearCacheNow() {
		final String TAG = "clearCacheNow[WabA]";
		String dbMsg = "";
		try {
			webView.clearCache(true);    //

			String msg = webView.getOriginalUrl() + " ...." +  getString(R.string.wv_reload_now);
			Toast.makeText(getApplicationContext() , msg , Toast.LENGTH_LONG).show();

			webView.reload();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 指定したスクリプトを実行
	 * **/
	public void executionJavascript(String scripyName) {
		final String TAG = "executionJavascript[WabA]";
		String dbMsg = "scripyName=" + scripyName;                        //     "foo()"  など
		try {
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
				// KitKat以上は専用の関数が用意されたみたい
				// 第二引数はコールバック
				webView.evaluateJavascript(scripyName , null);
			} else {
				// 以前は javascript: *** を呼び出せばOK
				webView.loadUrl("javascript:" + scripyName);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 表示しているページをブックマークに登録する
	 * Android6以降、android.provider.Browser.getAllBookmarks() メソッドと android.provider.Browser.saveBookmark() メソッドが削除されました。同様に、READ_HISTORY_BOOKMARKS と WRITE_HISTORY_BOOKMARKS パーミッションも削除
	 * */
	public void setBookMark() {
		final String TAG = "setBookMark[WabA]";
		String dbMsg = "";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * ブックマークから選択されたページに移動
	 * */
	public void gotBookMarkPage() {
		final String TAG = "gotBookMarkPage[WabA]";
		String dbMsg = "";
		try {

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	@Override
	public boolean onKeyDown(int keyCode , KeyEvent event) {
		final String TAG = "onKeyDown[WabA]";
		String dbMsg = "";
		boolean retBool = true;
		try {
			dbMsg = "keyCode=" + keyCode;//+",getDisplayLabel="+String.valueOf(event.getDisplayLabel())+",getAction="+event.getAction();////////////////////////////////
			//		Log.d("onKeyDown","[wKit]"+dbBlock);
//		dbBlock="ppBtnID="+myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU", false);///////////////////////////////////////////////////////////////////
//			Log.d("onKeyDown","[wKit]"+dbBlock);
			dbMsg += "サイドボリュームとディスプレイ下のキー；canGoBack=" + webView.canGoBack();///////////////////////////////////////////////////////////////////
			switch ( keyCode ) {    //キーにデフォルト以外の動作を与えるもののみを記述★KEYCODE_MENUをここに書くとメニュー表示されない
				case KeyEvent.KEYCODE_DPAD_UP:        //マルチガイド上；19
					//	wZoomUp();						//ズームアップして上限に達すればfalse
					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
					}
				case KeyEvent.KEYCODE_DPAD_DOWN:    //マルチガイド下；20
					//	wZoomDown();					//ズームダウンして下限に達すればfalse
					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
					}
				case KeyEvent.KEYCODE_DPAD_LEFT:    //マルチガイド左；21
					wForward();                        //ページ履歴で1つ後のページに移動する					return true;
					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
					}
				case KeyEvent.KEYCODE_DPAD_RIGHT:    //マルチガイド右；22
					wGoBack();                    //ページ履歴で1つ前のページに移動する
					if ( !myNFV_S_Pref.getBoolean("prefKouseiD_PadUMU" , false) ) {        //キーの利用が無効になっていたら
						pNFVeditor.putBoolean("prefKouseiD_PadUMU" , true);            //キーの利用を有効にして
					}
				case KeyEvent.KEYCODE_VOLUME_UP:    //24
					wZoomUp();                        //ズームアップして上限に達すればfalse
				case KeyEvent.KEYCODE_VOLUME_DOWN:    //25
					wZoomDown();                    //ズームダウンして下限に達すればfalse
				case KeyEvent.KEYCODE_HOME:
				case KeyEvent.KEYCODE_BACK:            //4KEYCODE_BACK :keyCode；09SH: keyCode；4,event=KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
					wGoBack();					//ページ履歴で1つ前のページに移動する;
				default:
					retBool = false;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}

	//メニューボタンで表示するメニュー///////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu wkMenu) {
		//	Log.d("onCreateOptionsMenu","NakedFileVeiwActivity;mlMenu="+wkMenu);
		makeOptionsMenu(wkMenu);    //ボタンで表示するメニューの内容の実記述
		return super.onCreateOptionsMenu(wkMenu);
	}

	public boolean makeOptionsMenu(Menu wkMenu) {    //ボタンで表示するメニューの内容
		dbBlock = "MenuItem" + wkMenu.toString();////////////////////////////////////////////////////////////////////////////
//		Log.d("makeOptionsMenu",dbBlock);
//			wkMenu.add(0, MENU_kore, 0, "これ");	//メニューそのもので起動するパターン
		SubMenu koreMenu = wkMenu.addSubMenu("操作");
		koreMenu.add(MENU_WQKIT , MENU_WQKIT_ZUP , 0 , CTM_WQKIT_ZUP);                //ズームアップ";
		koreMenu.add(MENU_WQKIT , MENU_WQKIT_ZDW , 0 , CTM_WQKIT_ZDW);                //ズームダウン";
		koreMenu.add(MENU_WQKIT , MENU_WQKIT_FOR , 0 , CTM_WQKIT_FOR);                //1ページ進む";
		koreMenu.add(MENU_WQKIT , MENU_WQKIT_BAC , 0 , CTM_WQKIT_BAC);                //1ページ戻る";
		koreMenu.add(MENU_WQKIT , MENU_WQKIT_END , 0 , CTM_WQKIT_END);        // = "終了";
		return true;
		//	return super.onCreateOptionsMenu(wkMenu);			//102SHでメニューが消えなかった
	}

	//
	@Override
	public boolean onPrepareOptionsMenu(Menu wkMenu) {            //表示直前に行う非表示や非選択設定
		dbBlock = "MenuItem" + wkMenu.toString() + ",進み" + webView.canGoForward() + ",戻り" + webView.canGoBack();////////////////////////////////////////////////////////////////////////////
		Log.d("onPrepareOptionsMenu" , dbBlock);
		if ( webView.canGoForward() ) {        //戻るページがあれば
			En_FOR = true;                //1ページ進むを表示
		} else {
			En_FOR = false;
		}
		if ( webView.canGoBack() ) {        //戻るページがあれば
			En_BAC = true;                //1ページ戻るを表示
		} else {
			En_BAC = false;
		}
		wkMenu.findItem(MENU_WQKIT_ZUP).setEnabled(En_ZUP);        //ズームアップ";
		wkMenu.findItem(MENU_WQKIT_ZDW).setEnabled(En_ZDW);        //ズームダウン";
		wkMenu.findItem(MENU_WQKIT_FOR).setEnabled(En_FOR);        //1ページ進む";
		wkMenu.findItem(MENU_WQKIT_BAC).setEnabled(En_BAC);        //1ページ戻る";
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String TAG = "onOptionsItemSelected[WabA]";
		String dbMsg = "";
		try {
			dbBlock = "MenuItem" + item.getItemId() + "を操作";////////////////////////////////////////////////////////////////////////////
			//			Log.d("onOptionsItemSelected",dbBlock);
			switch ( item.getItemId() ) {
				case MENU_WQKIT_ZUP:                        //ズームアップ";
					wZoomUp();            //ズームアップして上限に達すればfalse
					return true;
				case MENU_WQKIT_ZDW:                //ズームダウン";
					wZoomDown();                    //ズームダウンして下限に達すればfalse
					return true;
				case MENU_WQKIT_FOR:                //1ページ進む";
					wForward();                        //ページ履歴で1つ後のページに移動する
					return true;
				case MENU_WQKIT_BAC:                //1ページ戻る";
					wGoBack();                        //ページ履歴で1つ前のページに移動する
					return true;

				case MENU_WQKIT_END:                        //終了";
					callQuit();            //このActivtyの終了
					return true;
			}
			myLog(TAG , dbMsg);
			return false;
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			return false;
		}
	}

	@Override
	public void onOptionsMenuClosed(Menu wkMenu) {
		final String TAG = "onOptionsMenuClosed[WabA]";
		String dbMsg = "";
		try {
			dbBlock = "NakedFileVeiwActivity;mlMenu=" + wkMenu;//////////////拡張子=.m4a,ファイルタイプ=audio/*,フルパス=/mnt/sdcard/Music/AC DC/Blow Up Your Video/03 Meanstreak.m4a
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo) {
		// registerForContextMenu()で登録したViewが長押しされると、 onCreateContextMenu()が呼ばれる。ここでメニューを作成する。
		super.onCreateContextMenu(menu , v , menuInfo);
		getMenuInflater().inflate(R.menu.wev_cont , menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final String TAG = "onContextItemSelected[WabA}";
		String dbMsg = "開始" + item;                    //表記が返る
		try {
			myLog(TAG , dbMsg);
			dbBlock = "MenuItem" + item.getItemId() + "を操作";////////////////////////////////////////////////////////////////////////////
			switch ( item.getItemId() ) {
				case R.id.web_menu_rewrite:                        //再読込み
					clearCacheNow();
					return true;
				case R.id.web_menu_url_change:                        //URL変更
					setURL();
					return true;
				case R.id.web_menu_hysteresis:                        //履歴
					veiwHysteresis();
					return true;
				case R.id.web_menu_backpage:					//前のページ
					wGoBack();
					return true;
				case R.id.web_menu_prepage:					//次のページ
					wForward();
					return true;
//				case R.id.web_menu_bookmark_set:					//登録
//					setBookMark();
//					return true;
//				case R.id.web_menu_bookmark_get:					//選択
//					gotBookMarkPage();
//					return true;
				case R.id.web_menu_rescaling:                //サイズ合わせ;
					initSize();
					return true;
				case R.id.web_menu_zoom_enable:                //ズーム有効化;
					zoomSetting();
					return true;
				case R.id.web_menu_quit:                        //終了";
					callQuit();            //このActivtyの終了
					return true;
			}
			return true; // 処理に成功したらtrueを返す
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "で" + er.toString());
		}
		return super.onContextItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		final String TAG = "onDestroy[WabA]";
		String dbMsg = "";
		try {
			dbBlock = "onDestroy発生";//////////////拡張子=.m4a,ファイルタイプ=audio/*,フルパス=/mnt/sdcard/Music/AC DC/Blow Up Your Video/03 Meanstreak.m4a
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////
	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , CS_Web_Activity.this);
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
