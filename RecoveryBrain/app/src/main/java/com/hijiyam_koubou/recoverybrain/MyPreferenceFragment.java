package com.hijiyam_koubou.recoverybrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ListAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.Map;


public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	private CS_Util UTIL;
	private OnFragmentInteractionListener mListener;

	public PreferenceScreen sps;
	public static final String DEFAULT = "未設定";
	public Context context;
	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;

	public PreferenceScreen trace_setting_key;        //トレース設定
	public EditTextPreference file_name_key;  		//最初に表示する元画像
	public CheckBoxPreference is_start_last_key;  		//次回は最後に使った元画像からスタート
	public CheckBoxPreference mirror_movement_to_key;  		//上下鏡面動作
	public CheckBoxPreference mirror_movement_lr_key;  		//左右鏡面動作
	public CheckBoxPreference auto_judge_key;  		//トレース後に自動判定
	public ListPreference trace_line_width_key;  		//トレース線の太さ

	public PreferenceScreen conection_setting_key;        //接続設定
	public EditTextPreference rootUrl_key;
	public ListPreference testUrlt_key;

	public PreferenceScreen other_setting_key;        //その他の設定
	public EditTextPreference save_path_key;  		//作成したファイルの保存場所
	public CheckBoxPreference lotet_cansel_key;  		//自動回転阻止

	public String readFileName ="st001.png";  		//最初に表示する元画像
	public String is_start_last ="true";  		//次回は最後に使った元画像からスタート
	public String mirror_movement_to ="false";  		//上下鏡面動作
	public String mirror_movement_lr ="false";  		//左右鏡面動作
	public String auto_judge ="false";  		//トレース後に自動判定
	public String trace_line_width ="50";  		//トレース線の太さ

	public boolean isStartLast =true;  		//次回は最後に使った元画像からスタート
	public boolean is_v_Mirror = true;                //左右鏡面動作  //読み込み時、反転される
	public boolean is_h_Mirror = true;                //上下鏡面動作
	public boolean isAautoJudge =false;  		//トレース後に自動判定
	public int traceLineWidth =50;  		//トレース線の太さ


	public String rootUrlStr = "http://ec2-18-182-237-90.ap-northeast-1.compute.amazonaws.com:3080";					//	String dataURI = "http://192.168.3.14:3080";	//自宅
	public String testUrlStr = "http://192.168.3.14:3080";	//自宅
	public TypedArray testUrlArray;

	public String savePatht =""; 		//作成したファイルの保存場所
	public String lotet_canselt ="true";  		//自動回転阻止
	public boolean isLotetCanselt =true;  		//自動回転阻止

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String TAG = "onCreate[MPF]";
		String dbMsg = "";
		try {
			MyPreferenceFragment.this.addPreferencesFromResource(R.xml.preferences);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
		final String TAG = "onAttach[MPF]";
		String dbMsg = "開始";/////////////////////////////////////////////////
		try {
			this.context = this.getActivity().getApplicationContext();    //( MainActivity ) context;
			readPref(context);
			myLog(TAG , dbMsg);
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
		} catch (Exception er) {
			myLog(TAG , dbMsg + "で" + er.toString());
		}
	}

	/**
	 * 初期表示
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final String TAG = "onActivityCreated[MPF]";
		String dbMsg = "開始";/////////////////////////////////////////////////
		try {
			sps = this.getPreferenceScreen();            //☆PreferenceFragmentなら必要  .
			dbMsg += ",sps=" + sps;
			String summaryStr = "";

			conection_setting_key = ( PreferenceScreen ) sps.findPreference("conection_setting_key");        //


			rootUrl_key = ( EditTextPreference ) sps.findPreference("rootUrl_key");
			dbMsg += ",rootUrlStr=" + rootUrlStr;
			if ( findPreference("rootUrl_key") != null ) {
				rootUrl_key.setDefaultValue(rootUrlStr);
				rootUrl_key.setSummary(rootUrlStr);
			} else {
				(( EditTextPreference ) findPreference("rootUrl_key")).setText(rootUrlStr);
			}

			testUrlt_key = ( ListPreference ) sps.findPreference("testUrlt_key");
			dbMsg += ",testUrlStr=" + testUrlStr;
			if ( findPreference("testUrlt_key") != null ) {
				testUrlt_key.setSummary(testUrlStr);

//				testUrlArray = getResources().obtainTypedArray(R.array.url_array);
////				testUrlArray.
//				String[] urls = getResources().getStringArray(R.array.url_array);

//				video_audio_source_key.setValue(audioSourceName);
//				summaryStr += "," + getResources().getString(R.string.audio_source) + ":" + audioSourceName;
			}
//			isAutoFlash_key = ( CheckBoxPreference ) sps.findPreference("isAutoFlash_key");        //サブカメラに切り替え
//			dbMsg += ",オートフラッシュ=" + isAutoFlash;
//			if ( findPreference("isAutoFlash_key") != null ) {
//				isAutoFlash_key.setChecked(isAutoFlash);
////				if(isAutoFlash){
////					summaryStr +=","+ getResources().getString(R.string.mm_phot_flash) ;
////				}
//			}

			reloadSummary();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 保存時の処理
	 * http://libro.tuyano.com/index3?id=306001&page=4
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final String TAG = "onSaveInstanceState";
		String dbMsg = "開始";/////////////////////////////////////////////////
		try {
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			Log.e(TAG , dbMsg + "で" + e.toString());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final String TAG = "onResume[MPF]";
		String dbMsg = "開始";/////////////////////////////////////////////////
		try {
			sharedPref.registerOnSharedPreferenceChangeListener(this);   //Attempt to invoke virtual method 'android.content.SharedPreferences android.preference.PreferenceScreen.getSharedPreferences()
			reloadSummary();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		final String TAG = "onPause[MPF]";
		String dbMsg = "開始";/////////////////////////////////////////////////
		try {
			sharedPref.unregisterOnSharedPreferenceChangeListener(this);
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			Log.e(TAG , dbMsg + "で" + e.toString());
			//      myLog(TAG, dbMsg + "で" + e.toString(), "e");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final String TAG = "onDestroy[MPF]";
		String dbMsg = "開始";
		try {
			myLog(TAG , dbMsg);
		} catch (Exception e) {
			Log.e(TAG , dbMsg + "で" + e.toString());
			//      myLog(TAG, dbMsg + "で" + e.toString(), "e");
		}
	}                                                            //切替時⑥

	/**
	 * 変更された項目の更新
	 * 2階層目以降の連携更新もここで行う
	 * @param key 変更された項目のkey
	 *            照合の為に文字列定数を使う
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences , String key) {
		final String TAG = "onSharedPrefChd[MPF]";
		String dbMsg = "設定変更;";/////////////////////////////////////////////////
		try {
			dbMsg +=key;
			if ( key.equals("testUrlt_key") ) {
				dbMsg += ",testUrlStr=" + sharedPref.getString(key , testUrlStr);
				String pEntry = ( String )testUrlt_key.getEntry();
				String pVal = ( String ) testUrlt_key.getValue();
				dbMsg += ",Entry=" + pEntry + " ,Value=" + pVal;
				if ( ! pVal.contains(testUrlStr) ) {
					testUrlStr =  pVal;
					myEditor.putString("rootUrl_key" , pVal);
					myEditor.putString("testUrlt_key" , pVal);
					dbMsg += ",更新";
					myEditor.commit();
					dbMsg += "完了";
					rootUrlStr = sharedPref.getString("rootUrl_key", rootUrlStr);
					rootUrl_key.setSummary(rootUrlStr);
					testUrlStr = sharedPref.getString("testUrlt_key" , testUrlStr);
					testUrlt_key.setSummary(testUrlStr);
					String wrString = getResources().getString(R.string.rootUrl) + "=" + rootUrlStr + "\n" + getResources().getString(R.string.testUrl) + "=" + testUrlStr;
					dbMsg += ";書き換え=" + wrString;
					conection_setting_key.setSummary(wrString);
				}
			}

//			reloadSummary();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
		}
	}

	//各項目のプリファレンス上の設定///////////////////////////////////////////////////////////////////////////

	/**
	 * サマリーの更新
	 * https://qiita.com/noppefoxwolf/items/18e785f4d760f7cc4314
	 * 一階層目しか反映されていない
	 */
	private void reloadSummary() {
		String TAG = "reloadSummary[MPF]";
		String dbMsg = "";
		try {
			sps = this.getPreferenceScreen();            //☆PreferenceFragmentなら必要  .
			ListAdapter adapter = sps.getRootAdapter();      //            var adapter = this.preferenceScreen.RootAdapter;
//			for ( int i = 0 ; i < adapter.getCount() ; i++ ) {               //ListとEdit連携用のインデックス取得
//				Object item = adapter.getItem(i);
//				if ( item instanceof EditTextPreference ) {
//					EditTextPreference pref = ( EditTextPreference ) item;
//					String key = pref.getKey();
////					if ( key.equals("waiting_scond_key") ) {
////						waiting_scond_index = i;
////					}
//				} else if ( item instanceof ListPreference ) {
//					ListPreference pref = ( ListPreference ) item;
//					pref.setSummary(pref.getEntry() == null ? "" : pref.getEntry());
//
//					String key = pref.getKey();
////					if ( key.equals("waiting_scond_list") ) {
////						waiting_scond_list_index = i;
////					}
//				}
//			}
//			dbMsg += "waiting_scond_index=" + waiting_scond_index + ",_list_index=" + waiting_scond_list_index;
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
			myEditor = sharedPref.edit();

			for ( int i = 0 ; i < adapter.getCount() ; i++ ) {               //ListとEdit連携用のインデックス取得
				dbMsg += "\n" + i + ")";
				Object item = adapter.getItem(i);
				dbMsg += item;

				if ( item instanceof EditTextPreference ) {
					dbMsg += "EditTextPreference;";
					EditTextPreference pref = ( EditTextPreference ) item;
					String key = pref.getKey();
					String val = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(key , "");
					dbMsg += ";" + key + ";" + val;
					if ( key.equals(rootUrl_key) ) {
						rootUrlStr = val;
						dbMsg += ",rootUrlStr=" + val;
						dbMsg += "," + getResources().getString(R.string.rootUrl) + "=" + rootUrlStr;
					}
					pref.setSummary(val);
				} else if ( item instanceof CheckBoxPreference ) {
					dbMsg += "CheckBoxPreference;";
					CheckBoxPreference pref = ( CheckBoxPreference ) item;
					String key = pref.getKey();
					boolean pVal = pref.isChecked();
					dbMsg += ";" + key + ";" + pVal;
//					if ( key.equals("isTexturView_key") ) {
//						pref.setSummaryOn(getResources().getString(R.string.mm_effect_preview_tv));
//						pref.setSummaryOff(getResources().getString(R.string.mm_effect_preview_sufece));
//					} else {
//						pref.setSummaryOn("現在 On");                        // CheckBox が On の時のサマリーを設定
//						pref.setSummaryOff("現在 Off");                        // CheckBox が Off の時のサマリーを設定
//					}
				} else if ( item instanceof ListPreference ) {
					dbMsg += "ListPreference;";
					ListPreference pref = ( ListPreference ) item;
					String key = pref.getKey();
					String pVal = pref.getValue();
					int pIndex = pref.findIndexOfValue(pVal);
					dbMsg += ";" + key + ";" + pIndex + ")" + pVal;
					pref.setSummary(pVal);
//					if ( key.equals("testUrlt_key") ) {
//						dbMsg += ",rootUrlStr=" + rootUrlStr;
////						String rStr = setTestURL(pIndex);
////						dbMsg += ">>=" + rStr;
//						if ( rootUrlStr != key ) {
//							myEditor.putString("testUrlt_key" , pVal) ;
//							testUrlt_key.setSummary(pVal);
//							myEditor.putString("rootUrl_key" , pVal) ;
//							rootUrl_key.setSummary(pVal);
//							;
//							dbMsg += ",更新";
//							myEditor.commit();
//							dbMsg += "完了";
//							//							Object tItem = adapter.getItem(waiting_scond_index);
////							EditTextPreference tPref = ( EditTextPreference ) tItem;
////							tPref.setDefaultValue(pVal);
////							tPref.setText(pVal);
//						}
//					}
				} else if ( item instanceof PreferenceScreen ) {
					dbMsg += "PreferenceScreen;";
					PreferenceScreen pref = ( PreferenceScreen ) item;
					String key = pref.getKey();
					dbMsg += ";key=" + key;
					//     List<String> vals = new ArrayList<String>();
//					List< String > keyList = new ArrayList< String >();
					String wrString = "";
					if ( key.equals("conection_setting_key") ) {
						rootUrlStr = sharedPref.getString(key , rootUrlStr);
						testUrlStr = sharedPref.getString(key , testUrlStr);
						wrString = getResources().getString(R.string.rootUrl) + "=" + rootUrlStr + "\n" +
											 getResources().getString(R.string.testUrl) + "=" + testUrlStr;
						dbMsg += ",wrString=" + wrString;
					}
					pref.setSummary(wrString);
				} else if ( item instanceof Preference ) {
					dbMsg += "Preference;";
					Preference pref = ( Preference ) item;
					String key = pref.getKey();
					String pVal = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(key , "");
					dbMsg += ";" + key + ";" + pVal;
					dbMsg += ";" + key + ";" + pVal;
					pref.setSummary(pVal);
				}
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
		}
	}

	/**
	 * 全項目読み
	 */
	public void readPref(Context context) {
		String TAG = "readPref[MPF]";
		String dbMsg = "開始";
		try {
			this.context = context;
			sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			myEditor = sharedPref.edit();
			Map< String, ? > keys = sharedPref.getAll();                          //System.collections.Generic.IDictionary<string, object>
			dbMsg += ",読み込み開始;keys=" + keys.size() + "件";        // keys.size()
			if ( UTIL == null ) {
				UTIL = new CS_Util();
			}
			int i = 0;
			for ( String key : keys.keySet() ) {
				i++;
				String rStr = "";
				dbMsg += "\n" + i + "/" + keys.size() + ")" + key;// + "は" + rStr;
				if ( key.equals("rootUrl_key") ) {
					rootUrlStr = sharedPref.getString(key , rootUrlStr);
					dbMsg += ",rootUrlStr=" + rootUrlStr;
				}else if ( key.equals("testUrlt_key") ) {
					testUrlStr = sharedPref.getString(key , rootUrlStr);
					dbMsg += ",testUrlStr=" + testUrlStr;
				}

			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
		}
	}                                                                     //プリファレンスの読込み


//	public String setTestURL(int selectIndex) {
//		final String TAG = "setTestURL[MPF]";
//		String dbMsg = "開始";
//		String retStr =getString(R.string.rootUrlStr);        //0
//		try {
//			dbMsg += ",selectIndex=" + selectIndex;
//			switch ( selectIndex ) {
//				case 1:
//					retStr =getString(R.string.companyUrlStr);
//					break;
//				case 2:
//					retStr =getString(R.string.companyUrlStr);
//					break;
//			}
//			dbMsg += ",retStr=" + retStr;
//			myLog(TAG , dbMsg);
//		} catch (Exception er) {
//			myErrorLog(TAG , dbMsg + "でエラー発生；" + er);
//		}
//		return retStr;
//	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	public  void myLog(String TAG , String dbMsg) {
		if ( UTIL == null ) {
			UTIL = new CS_Util();
		}
		UTIL.myLog(TAG , dbMsg);
	}

	public  void myErrorLog(String TAG , String dbMsg) {
		if ( UTIL == null ) {
			UTIL = new CS_Util();
		}
		UTIL.myErrorLog(TAG , dbMsg);
	}

}