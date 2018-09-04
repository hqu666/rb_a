package com.hijiyam_koubou.recoverybrain;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CS_CanvasView extends View {        //org; View	から　io.skyway.Peer.Browser.Canvas	に合わせる
	// extends FrameLayout implements RendererEvents
	//extends FrameLayout implements org.webrtc.RendererCommon.RendererEvents
	private Context context;
	private Toolbar toolbar;
	private EditText cp_score_tv;

	private boolean isCall = false;                    //newで呼ばれた
	public boolean isRecever = true;                    //受信側
	private boolean isActionUp = false;                    //描画終了
	public boolean isAutoJudge = true;                //トレース後に自動判定 //読み込み時、反転される
	private boolean isComp = false;                //比較中	;scoreStartRadyでtrueに設定
	public boolean isDrow = false;                //手書き編集中
	public boolean isSelect = false;                //選択中
	public int stdColor = Color.BLUE;
	public String stdCaps = "round";
	public int stdWidth = 5;


	private Canvas myCanvas;
	private Paint paint;                        //ペン
	private Path path;                     //ペン
	public int selectColor = 0xFF008800;        //蛍光グリーン
	public float selectWidth = stdWidth;
	public String selectLineCap = stdCaps;
	public String[] lineCapList;
	public String drowStr = "";
	public int drowStrSize = 192;


	public String readFileName = "";        //読み込んだファイル名☆再読み込みの時は初期化
	public int orgColor;                            //トレース元の線色
	public int orgCount;                            //トレース元のピクセル数
	public int orgWidth;                        //トレース元の線の太さ

	private Paint eraserPaint;                //消しゴム
	private int eraserColor = Color.WHITE;        //背景色に揃える
	private float eraserWidth = 50.0f;
	public int whiteVar = Color.argb(255 , 255 , 255 , 255);   //反転 ;0xFF -各色
	public int brackVar = Color.argb(255 , 0 , 0 , 0);   //反転 ;0xFF -各色


	//	private Path path;
	public List< PathObject > pathIist;        //InitCanvaとファイル読込みなどの元画像更新直後に初期化

	class PathObject {
		Path path;
		Paint paint;
		String text;
	}

	public float startX;       //範囲選択開始点
	public float startY;
	public float endX;       //範囲選択終了点
	public float endY;
	public float upX;         //範囲選択終了点
	public float upY;

	private Matrix matrix = new Matrix();

	/////////////////////////////////////
	static final int REQUEST_CLEAR = 500;                            //全消去
	static final int REQUEST_DROW_PATH = REQUEST_CLEAR + 1;            //フリーハンド
	static final int REQUEST_AGAIN = REQUEST_DROW_PATH + 1;            //もう一度
// static final int REQUEST_DROW_LINE = REQUEST_AGAIN + 1;            //直線
//	static final int REQUEST_DROW_TRIGONE = REQUEST_DROW_LINE + 1;            //三角
//	static final int REQUEST_DROW_RECT = REQUEST_DROW_TRIGONE + 1;            //矩形
//	static final int REQUEST_DROW_OVAL = REQUEST_DROW_RECT + 1;					//楕円
//	static final int REQUEST_ARIA_DEL = REQUEST_DROW_OVAL + 1;												//選択範囲を消去
//	static final int REQUEST_DROW_DEL = REQUEST_ARIA_DEL + 1;												//消しゴム
//	static final int REQUEST_DROW_TEXT = REQUEST_DROW_DEL + 1;												//テキスト入力

	static final int REQUEST_ADD_BITMAP = REQUEST_AGAIN + 1;            //ビットマップ挿入
	static final int REQUEST_PICEL_BITMAP = REQUEST_ADD_BITMAP + 1;      //トレース前のビットマップ挿入
	public int REQUEST_CORD = REQUEST_DROW_PATH;

	//RecvrryBrain
	private Paint orignLine = null;                        //トレース元画像
	public boolean isPreparation = true;                    //トレーススタート前の準備中
	public boolean isJudged= false;                    //評価終了
	public String scoreMssg="";

	/**
	 * xmlに書き込む場合
	 */
	public CS_CanvasView(Context context , AttributeSet attrs) {
		super(context , attrs);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "xmlから";
		try {
			isCall = false;                    //newで呼ばれた
			commonCon(context);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public CS_CanvasView(Context context) {
		super(context);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "メソッド内から";
		try {
			isCall = true;                    //newで呼ばれた
			commonCon(context);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public CS_CanvasView(Context context , boolean isRecever , Toolbar _toolbar , EditText _cp_score_tv) {
		super(context);
		final String TAG = "CS_CanvasView[CView]";
		String dbMsg = "メソッド内から";
		try {
			isCall = true;                    //newで呼ばれた
			if ( isRecever ) {
				this.isRecever = isRecever;
				dbMsg += "受信側として生成";
			} else {
				dbMsg += "送信側として生成";
			}
			this.toolbar = _toolbar;
			this.cp_score_tv = _cp_score_tv;
			commonCon(context);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	public void commonCon(final Context context) {
		final String TAG = "commonCon[CView]";
		String dbMsg = "";
		try {
			this.context = context;
			lineCapList = context.getResources().getStringArray(R.array.lineCapSelecttValList);
			selectLineCap = lineCapList[0];
			InitCanvas();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void InitCanvas() {
		final String TAG = "InitCanva[CView]";
		String dbMsg = "";
		try {
			dbMsg += "isDrow=" + isDrow;
			if ( isDrow ) {
				selectColor = stdColor;
				selectWidth = stdWidth;
				selectLineCap = stdCaps;
			}
			pathIist = new ArrayList< PathObject >();
			paint = new Paint();
			dbMsg += ",ペン；" + selectColor;
			paint.setColor(selectColor);                        //

			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			dbMsg += "," + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);

			eraserPaint = new Paint();                //消しゴム
			dbMsg += ",消しゴム；" + eraserColor;
			eraserPaint.setColor(eraserColor);
			dbMsg += "," + eraserWidth + "px";
			eraserPaint.setStrokeWidth(eraserWidth);

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * ペンの色変更
	 */
	public void setPenColor(int selectColor) {
		final String TAG = "setPenColor[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectColor=" + selectColor;
			this.selectColor = selectColor;
			paint = new Paint();
			dbMsg += ",ペン；" + selectColor;
			paint.setColor(selectColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			if ( selectLineCap.equals(lineCapList[0]) ) {     //round
				paint.setStrokeCap(Paint.Cap.ROUND);
			} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
				paint.setStrokeCap(Paint.Cap.SQUARE);
			} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
				paint.setStrokeCap(Paint.Cap.BUTT);
			}
			dbMsg += "," + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public int getPenColor() {
		final String TAG = "setPenColor[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectColor=" + selectColor;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectColor;
	}


	/**
	 * ペンの太さ変更
	 */
	public void setPenWidth(int _selectWidth) {
		final String TAG = "setPenWidth[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectWidth=" + selectWidth;
			selectWidth = _selectWidth;
			paint = new Paint();
			if ( REQUEST_CORD == R.string.rb_edit_tool_erasre ) {
				selectColor = eraserColor;
			}
			dbMsg += ",ペン；selectColor=" + selectColor;
			paint.setColor(selectColor);                        //
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			if ( selectLineCap.equals(lineCapList[0]) ) {     //round
				paint.setStrokeCap(Paint.Cap.ROUND);
			} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
				paint.setStrokeCap(Paint.Cap.SQUARE);
			} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
				paint.setStrokeCap(Paint.Cap.BUTT);
			}
			dbMsg += ",selectWidth=" + selectWidth + "px";
			paint.setStrokeWidth(selectWidth);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public float getPenWidth() {
		final String TAG = "getPenWidth[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectWidth=" + selectWidth;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectWidth;
	}

	/**
	 * ペンの先端形状
	 */
	public void setPenCap(String lineCap) {
		final String TAG = "setPenCap[CView]";
		String dbMsg = "";
		try {
			dbMsg = "this.selectLineCap=" + this.selectLineCap;
			dbMsg += "lineCap=" + lineCap;
			if ( !lineCap.equals(this.selectLineCap) ) {
				this.selectLineCap = lineCap;
				paint = new Paint();
				paint.setColor(selectColor);                        //
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeJoin(Paint.Join.ROUND);
				if ( selectLineCap.equals(lineCapList[0]) ) {     //round
					paint.setStrokeCap(Paint.Cap.ROUND);
				} else if ( selectLineCap.equals(lineCapList[1]) ) {     //square
					paint.setStrokeCap(Paint.Cap.SQUARE);
				} else if ( selectLineCap.equals(lineCapList[2]) ) {     //butt
					paint.setStrokeCap(Paint.Cap.BUTT);
				}
				dbMsg += "," + selectWidth + "px";
				paint.setStrokeWidth(selectWidth);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public String getPenCap() {
		final String TAG = "getPenCap[CView]";
		String dbMsg = "";
		try {
			dbMsg = "selectLineCap=" + selectLineCap;
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return selectLineCap;
	}


	/**
	 * ViewGroup の canvas操作
	 */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		final String TAG = "dispatchDraw[CView]";
		String dbMsg = "";
		try {
			canvasDraw(canvas);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final String TAG = "onTouchEvent[CView]";
		String dbMsg = "";
		boolean retBool = false; //trueに設定すると「TouchEventを消化」したものとして他に送らない
		try {
			dbMsg += "isRecever=" + isRecever + ",REQUEST_CORD=" + REQUEST_CORD;
//			if ( ! isRecever ) {            ////newで呼ばれた場合はこのクラス内でイベント取得
			if ( REQUEST_CORD == REQUEST_ADD_BITMAP ) {       //502;ビットマップ挿入直後
				REQUEST_CORD = REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す
			}
			float xPoint = event.getX();
			float yPoint = event.getY();
			int action = event.getAction();
			dbMsg += "(" + xPoint + "×" + yPoint + ")action=" + action;
			endX = xPoint;
			endY = yPoint;       //範囲選択終了点

			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                        //全消去
					clearAll();
					break;
				case REQUEST_DROW_PATH:                        //フリーハンド
				case R.string.rb_edit_tool_free:
					dbMsg += ",selectColor=" + selectColor + "mselectWidth=" + selectWidth + ",selectLineCap=" + selectLineCap;
					drawPathLine(xPoint , yPoint , selectColor , selectWidth , selectLineCap , action , R.string.rb_edit_tool_free);
					dbMsg += ",isPreparation=" + isPreparation;
					if ( isPreparation && output != null ) {            //開始前判定
						int bIndex = Math.round(xPoint) + Math.round(yPoint) * output.getWidth();
						dbMsg += ",bIndex=" + bIndex;
						int pixel = pixels[bIndex];
						dbMsg += ",pixel=" + pixel;
						if ( pixel == orgColor ) {
							dbMsg += ",hitStart";
							for ( PathObject pathObject : pathIist ) {
								pathObject.path.reset();
							}
							isPreparation = false;                    //トレーススタート前の準備中
							downAction(xPoint , yPoint , selectColor , selectWidth , selectLineCap , R.string.rb_edit_tool_free);
						}
					}
					break;
				case R.string.rb_edit_tool_erasre:
					drawPathLine(xPoint , yPoint , eraserColor , selectWidth , selectLineCap , action , REQUEST_CORD);
					break;
				case R.string.rb_edit_tool_line:
				case R.string.rb_edit_tool_trigone:
				case R.string.rb_edit_tool_rect:
				case R.string.rb_edit_tool_oval:
				case R.string.rb_edit_tool_select_del:
				case R.string.rb_edit_tool_text:
					if ( action == 0 ) {
						startX = xPoint;
						startY = yPoint;            //範囲選択開始点
						isSelect = true;                //選択中
						downAction(xPoint , yPoint , selectColor , selectWidth , selectLineCap , REQUEST_CORD);
					} else if ( action == 1 ) {              //moveEnd参照
						isSelect = false;                //選択中
//						endX = xPoint;
//						endY = yPoint;       //範囲選択終了点
						upAction(xPoint , yPoint , REQUEST_CORD);
					} else {
						invalidate();                        //onDrawを発生させて描画実行
					}
					break;
				case REQUEST_ADD_BITMAP:                        //502;ビットマップ挿入直後
					upX = xPoint;
					upY = yPoint;
					break;
				default:
					upX = xPoint;
					upY = yPoint;
					break;
			}
			retBool = true;
//			}
//			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}

	/**
	 * 一回の書き込み
	 * 全消去される
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		final String TAG = "onDraw[CView]";
		String dbMsg = "";
		try {
			dbMsg += "REQUEST_CORD=" + REQUEST_CORD;
			canvasDraw(canvas);
//			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	@SuppressLint ( "WrongConstant" )
	public void canvasDraw(Canvas canvas) {
		final String TAG = "canvasDraw[CView]";
		String dbMsg = "";
		try {
			dbMsg += "isRecever=" + isRecever;
			if ( isRecever ) {
				dbMsg += ",REQUEST_CORD=" + REQUEST_CORD;
				int caWidth = canvas.getWidth();
				int caHeight = canvas.getHeight();
				dbMsg += ".canvas[" + caWidth + "×" + caHeight + "]";

				switch ( REQUEST_CORD ) {
					case REQUEST_CLEAR:                //500;全消去
						clearAllBody(canvas);
//						canvas.drawColor(eraserColor , PorterDuff.Mode.CLEAR);                // 描画クリア
//						for ( PathObject pathObject : pathIist ) {
//							pathObject.path.reset();
//						}
//						canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
						REQUEST_CORD = REQUEST_DROW_PATH;
						break;
					case REQUEST_DROW_PATH:                //501;フリーハンド
					case R.string.rb_edit_tool_free:
					case R.string.rb_edit_tool_erasre:
						PathObject pathObject = canvasRedrow(canvas);
						if ( pathObject != null ) {
							Paint lastPaint = pathObject.paint;
//						if ( output != null ) {
//							canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
//						}
//						dbMsg += ".pathIist=" + pathIist.size() + "件";
//						Paint lastPaint = null;
//						for ( PathObject pathObject : pathIist ) {
//							canvas.drawPath(pathObject.path , pathObject.paint);
//							lastPaint = pathObject.paint;
//							if ( !pathObject.text.equals("") ) {
//								pathObject.path.lineTo(upX,upY);
//								canvas.drawTextOnPath(pathObject.text , pathObject.path , 0 , 0 , pathObject.paint);   //upX, upY,
//							}
//						}
							dbMsg += ",isActionUp=" + isActionUp;
							if ( lastPaint != null && isActionUp ) {
								myCanvas = new Canvas();
								myCanvas = canvas;                    //現在のCanvasの状態を保存
//							RectF bounds = new RectF(0, 0, caWidth, caHeight);
////							int saveFlag = myCanvas.saveLayer(bounds, lastPaint, Canvas.CLIP_TO_LAYER_SAVE_FLAG);
//							int saveFlag = myCanvas.save();         //現在の状態を覚える
//							dbMsg += ".saveFlag=" + saveFlag;
//							/**
//							 MATRIX_SAVE_FLAG	Matrix情報（translate, rotate, scale, skew）の情報を保存
//							 CLIP_SAVE_FLAG	クリップ領域を保存
//							 HAS_ALPHA_LAYER_SAVE_FLAG	アルファ（不透明度）レイヤーを保存
//							 FULL_COLOR_LAYER_SAVE_FLAG	カラーレイヤーを保存
//							 CLIP_TO_LAYER_SAVE_FLAG	クリップレイヤーとして保存
//							 ALL_SAVE_FLAG	全ての状態を保存する*/
								isActionUp = false;                    //描画終了
								if ( !isDrow ) {
									dbMsg += ",myCanvas[" + myCanvas.getWidth() + "×" + myCanvas.getHeight() + "]";  //[168×144]?
									if ( isAutoJudge ) {
//										CharSequence wStr = context.getString(R.string.rb_auto_judge);
//										dbMsg += ">>" + wStr;
//										Toast.makeText(context , wStr , Toast.LENGTH_SHORT).show();
										scorePixcel(PRG_CODE_SET_SCORE);
									}
								}
							}
						}
						break;
					case R.string.rb_edit_tool_line:
					case R.string.rb_edit_tool_trigone:
					case R.string.rb_edit_tool_rect:
					case R.string.rb_edit_tool_oval:
					case R.string.rb_edit_tool_select_del:
					case R.string.rb_edit_tool_text:
						pathObject = canvasRedrow(canvas);

//						for ( PathObject pathObject : pathIist ) {
//							canvas.drawPath(pathObject.path , pathObject.paint);
//							if ( !pathObject.text.equals("") ) {
//								pathObject.path.lineTo(upX,upY);
//								canvas.drawTextOnPath(pathObject.text , pathObject.path , 0 , 0 , pathObject.paint);   //upX, upY,
//							}
//						}
						if ( isSelect && REQUEST_CORD != R.string.rb_edit_tool_text ) {               //選択中
							paint = new Paint();
							paint.setColor(Color.argb(255 , 0 , 0 , 0));
							paint.setStyle(Paint.Style.STROKE);
							paint.setStrokeWidth(1); // 線の太さ
							paint.setAntiAlias(true);                       //点線
							paint.setPathEffect(new DashPathEffect(new float[]{8.0f , 8.0f} , 0));
							canvas.drawLine(startX , startY , endX , startY , paint);
							canvas.drawLine(endX , startY , endX , endY , paint);
							canvas.drawRect(startX , endY , endX , endY , paint);
							canvas.drawLine(startX , startY , startX , endY , paint);
						}
						break;
					case REQUEST_AGAIN:                //502；もう一度
						if ( output != null ) {
							clearAllBody(canvas);
							dbMsg += "トレース前[" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
							canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
						} else {
							String titolStr = "「もう一度」を指定されました";
							String mggStr = "まだ元画像がセットされていません。";
							messageShow(titolStr , mggStr);
						}
						REQUEST_CORD = REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す☆止めないとloopする
						break;
					case R.string.rb_roat_right:
					case R.string.rb_roat_left:
					case R.string.rb_roat_half:
					case R.string.rb_flip_vertical:
					case R.string.rb_flip_horizontal:
						dbMsg += ",output=" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
						dbMsg += ",matrix=" + matrix.toString();
						Bitmap screenShot = Bitmap.createBitmap(output , 0 , 0 , output.getWidth() , output.getHeight() , matrix , false);
						dbMsg += ",screenShot[" + screenShot.getWidth() + "×" + screenShot.getHeight() + "]" + screenShot.getByteCount() + "バイト";
						canvas.drawColor(Color.WHITE);
						canvas.drawBitmap(screenShot , 0 , 0 , paint);
						getCanvasPixcel(screenShot);        //
						screenShot.recycle();
						backAgain();   //トレース後の線を消す☆先にできない？
						break;
					case REQUEST_ADD_BITMAP:                //503;ビットマップ挿入
						dbMsg += "(" + upX + "×" + upY + ")に[" + aBmp.getWidth() + "×" + aBmp.getHeight() + "]" + aBmp.getByteCount() + "バイトのビットマップ挿入";
						if ( isRecever ) {
							getCanvasPixcel(aBmp);        //
						} else {
//						canvas.drawBitmap(aBmp , upX , upY , ( Paint ) orignLine); // image, x座標, y座標, Paintイタンス
						}
						break;
					case REQUEST_PICEL_BITMAP:                //トレース前のビットマップ挿入
						canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
						setTreace();
						break;
				}
//				canvas.restore(); // save直前に戻る
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void downAction(float xPoint , float yPoint , int addColor , float addWidth , String addCap , int toolID) {
		final String TAG = "downAction[CView]";
		String dbMsg = "";
		try {
			dbMsg += ",toolID=" + toolID + "[" + xPoint + "×" + yPoint + "]";
			isActionUp = false;                    //描画終了
			PathObject pathObject = new PathObject();
			Path addPath = new Path();
//			switch ( toolID ) {
//				case R.string.rb_edit_tool_trigone:
//					break;
//				default:
			addPath.moveTo(xPoint , yPoint);
//					break;
//			}
			pathObject.path = addPath;

			switch ( toolID ) {
				case R.string.rb_edit_tool_text:
					dbMsg += ",書き込む文字=" + drowStr + "を" + drowStrSize + "pxで";            //[" +upX + "×" +upY +"]";
					Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					textPaint.setTextSize(drowStrSize);
					textPaint.setColor(addColor);
					pathObject.paint = textPaint;
					pathObject.text = drowStr;
					break;
				default:
					Paint addPaint = new Paint();
					dbMsg += ",addColor=" + addColor;
					switch ( toolID ) {
						case R.string.rb_edit_tool_erasre:
						case R.string.rb_edit_tool_select_del:
							addColor = eraserColor;
							break;
						default:
							addColor = selectColor;
							break;
					}
					dbMsg += ",>>" + addColor;
					addPaint.setColor(addColor);
					switch ( toolID ) {
						case R.string.rb_edit_tool_select_del:
							addPaint.setStyle(Paint.Style.FILL_AND_STROKE);      //塗り潰し
							break;
						default:
							addPaint.setStyle(Paint.Style.STROKE);
							break;
					}
					addPaint.setStrokeJoin(Paint.Join.ROUND);
					dbMsg += ",addCap=" + addCap;
					if ( addCap.equals(lineCapList[0]) ) {     //round
						addPaint.setStrokeCap(Paint.Cap.ROUND);
					} else if ( addCap.equals(lineCapList[1]) ) {     //square
						addPaint.setStrokeCap(Paint.Cap.SQUARE);
					} else if ( addCap.equals(lineCapList[2]) ) {     //butt
						addPaint.setStrokeCap(Paint.Cap.BUTT);
					}
					dbMsg += ",addWidth=" + addWidth + "px";
					addPaint.setStrokeWidth(addWidth);
					pathObject.paint = addPaint;
					pathObject.text = "";
					break;
			}
			pathIist.add(pathObject);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public PathObject canvasRedrow(Canvas canvas) {
		final String TAG = "canvasRedrow[CView]";
		String dbMsg = "";
		PathObject lastPathObject = null;
		try {
			if ( output != null ) {
				canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
			}
			if ( pathIist != null ) {
				int lastCount = pathIist.size();
				dbMsg += ".pathIist=" + lastCount + "件";
				int pathCount = 0;
				for ( PathObject pathObject : pathIist ) {
					pathCount++;
					lastPathObject = pathObject;
					canvas.drawPath(pathObject.path , pathObject.paint);
					if ( !pathObject.text.equals("") ) {                    //書込む伊対象はテキスト
						dbMsg += "(" + pathCount + "/" + lastCount + ")[" + endX + "," + endY + ")isSelect=" + isSelect;
						if ( lastCount == pathCount ) {                                        //そのテキストの操作中で
							if ( isSelect ) {                                                        //位置を確定するまで
								canvas.drawText(pathObject.text , endX , endY , pathObject.paint);        //一過的に表示
							} else {                                                                //位置調整中は
								float lineEnd = canvas.getWidth() * 1.0f;                                //パスを更新
								dbMsg += ",lineEnd=" + lineEnd;
								pathIist.get(pathIist.size() - 1).path.lineTo(lineEnd , endY);
								canvas.drawTextOnPath(pathObject.text , pathObject.path , 0 , 0 , pathObject.paint);   //upX, upY,
							}
						} else {
							canvas.drawTextOnPath(pathObject.text , pathObject.path , 0 , 0 , pathObject.paint);   //upX, upY,
						}
						myLog(TAG , dbMsg);
					}
				}
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return lastPathObject;
	}

	public void upAction(float xPoint , float yPoint , int toolID) {
		final String TAG = "upAction[CView]";
		String dbMsg = "";
		try {
			isActionUp = true;                    //描画終了
			dbMsg += "(" + pathIist.size() + ")";
			dbMsg += ",toolID=" + toolID + "[" + xPoint + "×" + yPoint + "]";
			switch ( toolID ) {
				case R.string.rb_edit_tool_trigone:
					pathIist.get(pathIist.size() - 1).path.moveTo(startX + (xPoint - startX) / 2 , startY);
//					pathIist.get(1).path.lineTo(startX , yPoint);    //左辺
					pathIist.get(pathIist.size() - 1).path.lineTo(startX , yPoint);    //左辺
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);        //底辺
					pathIist.get(pathIist.size() - 1).path.lineTo(startX + (xPoint - startX) / 2 , startY);    //右辺
					break;
				case R.string.rb_edit_tool_rect:
					RectF rectf = new RectF(startX , startY , xPoint , yPoint);
					pathIist.get(pathIist.size() - 1).path.addRect(rectf , Path.Direction.CW);
					break;
				case R.string.rb_edit_tool_oval:
					paint.setStyle(Paint.Style.STROKE); // 塗りつぶし無し
					rectf = new RectF(startX , startY , xPoint , yPoint);
					pathIist.get(pathIist.size() - 1).path.addOval(rectf , Path.Direction.CW);
					break;
				case R.string.rb_edit_tool_select_del:
					rectf = new RectF(startX , startY , xPoint , yPoint);
					pathIist.get(pathIist.size() - 1).path.addRect(rectf , Path.Direction.CW);
					break;
				case R.string.rb_edit_tool_text:
					pathIist.get(pathIist.size() - 1).path.moveTo(xPoint , yPoint);
					break;
				case R.string.rb_edit_tool_erasre:
				case R.string.rb_edit_tool_line:
				default:
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					break;
			}
			invalidate();                        //onDrawを発生させて描画実行
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * パス配列に追加
	 */
	public void drawPathLine(float xPoint , float yPoint , int addColor , float addWidth , String addCap , int action , int toolID) {
		final String TAG = "drawPathLine[CView]";
		String dbMsg = "";
		try {

			dbMsg = "action=" + action + "(" + xPoint + " , " + yPoint + ")";
			switch ( action ) {
				case MotionEvent.ACTION_DOWN:   //0
					downAction(xPoint , yPoint , addColor , addWidth , addCap , toolID);
					break;
				case MotionEvent.ACTION_MOVE:   //2
					isActionUp = false;                    //描画終了
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					break;
				case MotionEvent.ACTION_UP:     //1
					isActionUp = true;                    //描画終了
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					break;
			}
			invalidate();                        //onDrawを発生させて描画実行
			if ( isActionUp ) {
				myLog(TAG , dbMsg);
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * トレース前の状態に戻す
	 */
	public void backAgain() {
		final String TAG = "backAgain[CView]";
		String dbMsg = "";
		try {
			dbMsg += "トレース前[" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
			REQUEST_CORD = REQUEST_AGAIN;
			invalidate();                        //onDrawを発生させて描画実行
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}
	/////////////////////////////////////////////////////////////////////////

	/**
	 * 元画像のトレース色の残りから評価値を算定してtoolbarに書込む
	 */
	public void writehScore(Context contex , int beforeCount , int afterCount , int tracceCount) {
		final String TAG = "writehScore[CView]";
		String dbMsg = "";
		try {
			dbMsg += afterCount + "/" + beforeCount + ":tracce=" + tracceCount;
			int eraceCount = beforeCount - afterCount;
			dbMsg += ">erace>" + eraceCount;
			float sVar = 0;
			if ( afterCount < beforeCount ) {
				sVar = (eraceCount * 10000.0f) / (beforeCount * 100.0f);
			}
			dbMsg += "=" + sVar + "点";
			float hit = 0.0f;
			if ( 0 < tracceCount ) {
				hit = (100.0f * eraceCount) / (100.0f * tracceCount);        // * 1.0f
				dbMsg += ",Hit=" + hit;
//				if ( beforeCount < tracceCount ) {
//					hit =  (100.0f * eraceCount) /  (100.0f *(tracceCount - beforeCount));      //(tracceCount - beforeCount)
//					dbMsg += ">>" + hit;
//				}
				sVar = Math.round(sVar * hit);   //Math.round((eraceCount * 100) / beforeCount * hit);
				dbMsg += ">Hit>" + sVar;
			}
			scoreVar = ( int ) Math.round(sVar);
			scoreMssg = context.getString(R.string.common_score) + scoreVar + context.getString(R.string.common_ten) + "\n"+
								context.getString(R.string.common_nokori) + afterCount + "/ " + beforeCount+context.getString(R.string.common_pixcel) + "\n" +
								context.getString(R.string.common_trace_line) + tracceCount + context.getString(R.string.common_pixcel) + "\n" + context.getString(R.string.common_hitparcent) + "= " + (hit * 100) + "%";
			dbMsg += ">>" + scoreMssg;
//			Toast.makeText(contex , wStr , Toast.LENGTH_LONG).show();               common_nokori
			if(0<scoreVar){
				isJudged= true;                    //評価終了
			}
			cp_score_tv.setText(" " + scoreVar);  //書き換えイベントから自動送りメソッドへ

			String wStr = "" + afterCount + "/ " + beforeCount + ": " + tracceCount + "px";
			if ( 0 < tracceCount ) {
				wStr += ": " + Math.round(hit * 100) + "%";
			}
			toolbar.setTitle(wStr);   //"" + scoreVar
// toolbar.setSubtitle(wStr);      //2行になってTitolも小さくなる

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 指定されたファイルをviewの中心に配置する
	 * ☆Activityから呼ばれる
	 */
	public boolean addBitMap(Context context , String fileName , int canvasWidth , int canvasHeight) {
		final String TAG = "addBitMap[CView]";
		String dbMsg = "";
		boolean retBool = false;
		try {
			this.context = context;
			dbMsg += ",fileName=" + fileName  + "(既読;" + readFileName +")" ;
			dbMsg += ",画面[" + canvasWidth + "×" + canvasHeight + "]";
			if ( !fileName.equals(readFileName) ) {
//				CharSequence wStr = "トレース元画像に" + fileName + "を読み込んでいます";
//				dbMsg += ">>" + wStr;
//				Toast.makeText(context , wStr , Toast.LENGTH_SHORT).show();
				if ( pathIist != null ) {
					for ( PathObject pathObject : pathIist ) {
						pathObject.path.reset();
					}
				}

				REQUEST_CORD = REQUEST_ADD_BITMAP;
				dbMsg += ",fileName=" + fileName ;
				Bitmap rBmp = readFIle(fileName);
				if ( rBmp != null ) {
					int bmpWidth = rBmp.getWidth();
					int bmpHeight = rBmp.getHeight();
					dbMsg += "読込み[" + bmpWidth + "×" + bmpHeight + "]" + rBmp.getByteCount() + "バイト";
					dbMsg += "、 canvas[" + canvasWidth + "×" + canvasHeight + "]";
					double scaleWidth = 1.0;
					if ( canvasWidth < bmpWidth ) {
						scaleWidth = ( double ) ((canvasWidth * 1000) / bmpWidth) / 1000;
					}
					if ( scaleWidth == 0 ) {
						scaleWidth = 1.0;
					}
					double scaleHeight = 1.0;
					if ( canvasHeight < bmpHeight ) {
						scaleHeight = ( double ) ((canvasHeight * 1000) / bmpHeight) / 1000;
					}
					if ( scaleHeight == 0 ) {
						scaleHeight = 1.0;
					}
					dbMsg += "scale[" + scaleWidth + "×" + scaleHeight + "]";
					double scaleWH = scaleWidth;
					if ( scaleHeight < scaleWidth ) {
						scaleWH = scaleHeight;
					}
					int rWidth = ( int ) Math.ceil(( double ) bmpWidth * scaleWH);
					int rHight = ( int ) Math.ceil(( double ) bmpHeight * scaleWH);
					dbMsg += "、縮小[" + rWidth + "×" + rHight + "]" + scaleWH + "倍";
					Bitmap rsBitmap = Bitmap.createScaledBitmap(rBmp , rWidth , rHight , false);            // 100x100にリサイズ
					bmpWidth = rsBitmap.getWidth();
					bmpHeight = rsBitmap.getHeight();
					dbMsg += ",リサイズ[" + bmpWidth + "×" + bmpHeight + "]" + rsBitmap.getByteCount() + "バイト";
					int canvasShiftX = 0;
					int canvasShiftY = 0;
					if ( 1 < (canvasWidth - bmpWidth) ) {
						canvasShiftX = (canvasWidth - bmpWidth) / 2;
					}
					if ( 1 < (canvasHeight - bmpHeight) ) {
						canvasShiftY = (canvasHeight - bmpHeight) / 2;
					}
					dbMsg += ",シフト(" + canvasShiftX + " , " + canvasShiftY + ")";
					aBmp = Bitmap.createBitmap(canvasWidth , canvasHeight , Bitmap.Config.ARGB_8888);    //キャンバスサイズのビットマップを作成して
					Canvas cv = new Canvas(aBmp);                                                        //ビットマップをcanvasにして
					cv.drawBitmap(rsBitmap , canvasShiftX , canvasShiftY , ( Paint ) null); // リサイズしたビットマップを, x座標, y座標, Paintイタンスで書き込む
					invalidate();                        //onDrawを発生させて描画実行
					rsBitmap.recycle();
					readFileName = fileName;
					retBool = true;
				} else {
					CharSequence wStr = context.getString(R.string.bmr_msg1) + context.getString(R.string.can_not_conect_msg3);
					dbMsg += ">>" + wStr;
					Toast.makeText(context , wStr , Toast.LENGTH_SHORT).show();
				}
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}

	/**
	 * assetsフォルダーから画像ファイルを読み込む
	 * addBitMapから呼ばれる
	 */
	public Bitmap readFIle(String fileName) {
		final String TAG = "readFIle[CView]";
		String dbMsg = "";
		Bitmap bm = null;
		try {
			if ( fileName.contains(File.separator) ) {
				File srcFile = new File(fileName);
				FileInputStream fis = new FileInputStream(srcFile);
				bm = BitmapFactory.decodeStream(fis);
				fis.close();
			} else {
				AssetManager as = getResources().getAssets();
				dbMsg += "fileName=" + fileName;
				InputStream is = as.open(fileName);
				bm = BitmapFactory.decodeStream(is);
				is.close();
			}
			//decodeStream(InputStream,padding.options);
			dbMsg += " = " + bm.getByteCount() + "バイト";
			myLog(TAG , dbMsg);
		} catch (IOException er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return bm;
	}

	///Pixcel読み取り///////////////////////////////////
	public Bitmap aBmp;                        //書込むビットマップ（16：9）
	public Bitmap output;                    //argb配列から作成されたビットマップ
	public Bitmap screenShot;                //トレース後のビットマップ
	//	public double scaleWH;					//書込んだ時のスケール
	static final int PRG_CODE_GET_PIXEL = REQUEST_DROW_PATH + 100;            //ピクセル変換
	static final int PRG_CODE_RE_READ = PRG_CODE_GET_PIXEL + 1;            //ピクセル変換後の読み直し
	static final int PRG_CODE_SET_SCORE = PRG_CODE_RE_READ + 1;            //スコア判定
	public int bWidth;
	public int bHight;
	public int[] pixels = null;                    //canvas全体のargb配列
	public int[] trPixels = null;                    //判定用
	public int scoreVar = 0;
	public int remainsCount = 0;
	public int tracceCount = 0;
	public int chackTotal = 0;
	public SparseIntArray colorArray;         //使われている色
	public SparseIntArray widthArray;            //使われている線の太さ
	public List< String > colorIndex;
	public List< String > widthIndex;
	int bColor = 1;
	int widthCount = 0;
	private ProgressDialog progressDialog;

	/**
	 * canvasDrawでファイル読込み直後のピクセル変換してBitMap再作成
	 * 色と線の太さの読込み
	 */
	public void getCanvasPixcel(Bitmap rBmp) {        //, Canvas canva
		final String TAG = "getCanvasPixcel[CView]";
		String dbMsg = "";
		try {
//			Bitmap rBmp = getViewBitmap(this);
			bWidth = rBmp.getWidth();
			bHight = rBmp.getHeight();
			dbMsg += "読込み[" + bWidth + "×" + bHight + "]" + rBmp.getByteCount() + "バイト";
			pixels = new int[bWidth * bHight];
			rBmp.getPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にrBmpのデータを格納する
//			dbMsg += ",whiteVar=" + whiteVar + " ,brackVar= " + brackVar;
			colorArray = new SparseIntArray();     //キーも値もintのmap
			widthArray = new SparseIntArray();
			colorIndex = new ArrayList<>();
			widthIndex = new ArrayList<>();
			bColor = 1;
			widthCount = 0;
			chackTotal = 0;
			AsyncTask< Integer, Integer, Integer > progD = new loadAsyncTask(context , context.getString(R.string.common_yomikonde_imasu) , readFileName + "[" + bWidth + context.getString(R.string.common_sekisan_hugou) + bHight + "]" + rBmp.getByteCount() + context.getString(R.string.common_bite) , 0);
			progD.execute(PRG_CODE_GET_PIXEL , bWidth , bHight);

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void getCanvasPixcelBody(int xPoint , int yPoint , int bWidth) {        // , Canvas canva
		final String TAG = "getCanvasPixcelBody[CView]";
		String dbMsg = "";
		int bIndex = 0;
		try {
			bIndex = xPoint + yPoint * bWidth;
			int pixel = pixels[bIndex];
			pixels[bIndex] = Color.argb(Color.alpha(pixel) , Color.red(pixel) , Color.green(pixel) , Color.blue(pixel));   //反転 ;0xFF -各色
			if ( pixel != whiteVar && pixel != brackVar && 255 == Color.alpha(pixel) ) {                            //	if ( pixel <-1 ) {
				chackTotal++;
				String colorName = pixel + "";
				if ( 0 == colorIndex.size() ) {
					colorIndex.add(colorName);
					colorArray.put(pixel , 1);
				} else if ( -1 == colorIndex.indexOf(colorName) ) {
					colorIndex.add(colorName);
					colorArray.put(pixel , 1);
				} else {
					int nowCount = ( int ) colorArray.get(pixel) + 1;
					colorArray.put(pixel , nowCount);
				}

				if ( bColor == pixel ) {
					widthCount++;
				} else {
					String widthName = widthCount + "";
					if ( 0 == widthIndex.size() ) {
						widthIndex.add(widthName);
						widthArray.put(widthCount , 1);
					} else if ( -1 == widthIndex.indexOf(widthName) ) {
						widthIndex.add(widthName);
						widthArray.put(widthCount , 1);
					} else {
						dbMsg += "(" + xPoint + "×" + yPoint + ")W=" + bWidth + ">>" + bIndex + ",chack=" + chackTotal + ",pixel=" + pixel;
						int nowCount = ( int ) widthArray.get(widthCount) + 1;
						dbMsg += ",nowCount=" + nowCount;
						widthArray.put(widthCount , nowCount);
//			myLog(TAG , dbMsg);
					}
					widthCount = 0;
					bColor = pixel;
				}

			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
//		return bIndex;
	}

	public void afterCanvasPixcel() {        // , Canvas canva
		final String TAG = "afterCanvasPixcel[CView]";
		String dbMsg = "";
		try {
			dbMsg += ",pixels=" + pixels.length;
			output = Bitmap.createBitmap(bWidth , bHight , Bitmap.Config.ARGB_8888);        //出力用画像領域確保
			output.setPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                    //出力用の領域にセットする
			dbMsg += ",書込み[" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
			REQUEST_CORD = REQUEST_PICEL_BITMAP;      //トレース前のビットマップ挿入
			invalidate();    //onDorw後   setTreace
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * トレース元画像を読んで使用されている色と線の太さを取得
	 * トレース色も設定する
	 */
	public void setTreace() {
		final String TAG = "setTreace[CView]";
		String dbMsg = "";
		try {
			dbMsg += ",colorArray=" + colorArray.size() + "色";
			orgColor = colorArray.keyAt(0);      //トレース元の線色
			orgCount = colorArray.valueAt(0);      //トレース元のピクセル数
			for ( int i = 0 ; i < colorArray.size() ; i++ ) {
				int key = colorArray.keyAt(i);
				int val = colorArray.valueAt(i);
				if ( orgCount < val ) {
					orgColor = key;
					orgCount = val;
				}
			}
			dbMsg += ",orgColor=" + orgColor + " = " + orgCount + "ピクセル";
			dbMsg += ",a:" + Color.alpha(orgColor) + " r:" + Color.red(orgColor) + " g:" + Color.green(orgColor) + " b:" + Color.blue(orgColor);
			dbMsg += ",widthArray=" + widthArray.size() + "個所";
			orgWidth = 5;                                                //トレース元の線の太さ
			int wCount = widthArray.valueAt(0);                    //トレース元のピクセル数
			for ( int i = 0 ; i < widthArray.size() ; i++ ) {
				int key = widthArray.keyAt(i);
				int val = widthArray.valueAt(i);
				if ( wCount < val && (0 < key && key < 51) ) {        //x軸上50px未満の幅
					orgWidth = key;
					wCount = val;
				}
			}
			dbMsg += ",orgWidth=" + orgWidth + "ピクセル= " + wCount + "個所";
			dbMsg += ",Color=" + orgColor;
			CS_Util UTIL = new CS_Util();
			selectColor = UTIL.complementaryColor(orgColor);
			dbMsg += ">selectColor>" + selectColor;
			dbMsg += ",a:" + Color.alpha(selectColor) + " r:" + Color.red(selectColor) + " g:" + Color.green(selectColor) + " b:" + Color.blue(selectColor);
			selectWidth = orgWidth * 2;
			REQUEST_CORD = REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す☆止めないとloopする
//			writehScore(context , orgCount , orgCount , 0);
			isPreparation = true;                    //トレーススタート前の準備中
			scorePixcel(PRG_CODE_RE_READ);    		//評価時と同条件で再カウント
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public class loadAsyncTask extends AsyncTask< Integer, Integer, Integer > implements DialogInterface.OnCancelListener {

		final String TAG = "loadAsyncTask";
		public Context context;
		public String titolStr = "元画像を読み込んでいます";
		public String dlogMsg = "読込み中";
		int endVal = 100;
		public ProgressDialog pDialog;

		public loadAsyncTask(Context context , String titolStr , String dlogMsg , int endVal) {
			final String TAG = "loadAsyncTask[CSC.L]";
			String dbMsg = "";
			try {
				dbMsg += "titolStr=" + titolStr + ",dlogMsg=" + dlogMsg + ",endVal=" + endVal;
				this.context = context;
				this.titolStr = titolStr;
				this.dlogMsg = dlogMsg;
				this.endVal = endVal;
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}

		@Override
		protected void onPreExecute() {
			final String TAG = "onPreExecute[CSC.L]";
			String dbMsg = "";
			try {
				pDialog = new ProgressDialog(this.context);
				dbMsg += "titolStr=" + titolStr + ",dlogMsg=" + dlogMsg + ",endVal=" + endVal;
				pDialog.setTitle(titolStr);
				pDialog.setMessage(dlogMsg);
				if ( 0 < endVal ) {
					pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					pDialog.setMax(endVal);
					pDialog.setProgress(0);
				} else {
					pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				}
				pDialog.setCancelable(true);
				pDialog.setOnCancelListener(this);
				pDialog.show();
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			final String TAG = "doInBackground[CSC.L]";
			String dbMsg = "";
			Integer PRG_CODE = params[0];
			try {
				dbMsg += "params=";
				for ( int i = 0 ; i < params.length ; i++ ) {
					dbMsg += " , " + params[i];
				}
				int bWidth = params[1];
				int bHight = params[2];
				dbMsg += "=[" + bWidth + "×" + bHight + "]PRG_CODE=" + PRG_CODE;
				for ( int yPoint = 0 ; yPoint < bHight ; yPoint++ ) {
					for ( int xPoint = 0 ; xPoint < bWidth ; xPoint++ ) {
						switch ( PRG_CODE ) {
							case PRG_CODE_GET_PIXEL:
								getCanvasPixcelBody(xPoint , yPoint , bWidth);
								break;
							case PRG_CODE_RE_READ:
							case PRG_CODE_SET_SCORE:
								scorePixcelBody(xPoint , yPoint , bWidth);
								break;
						}
					}
					publishProgress(yPoint);
				}
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
			return PRG_CODE;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			final String TAG = "onProgressUpdate[CSC.L]";
			String dbMsg = "";
			try {
				dbMsg = "values=" + values[0];
				pDialog.setProgress(values[0]);
//				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}

		@Override
		protected void onCancelled() {
			final String TAG = "onCancelled[CSC.L]";
			String dbMsg = "";
			try {
				dbMsg += "onCancelled";
				pDialog.dismiss();
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			final String TAG = "onPostExecute[CSC.L]";
			String dbMsg = "";
			try {
				dbMsg += "result=" + result;
				pDialog.dismiss();
				switch ( result ) {
					case PRG_CODE_GET_PIXEL:
						afterCanvasPixcel();
						break;
					case PRG_CODE_RE_READ:
						orgCount =   remainsCount;
						dbMsg += ",remain=" + remainsCount + " /org=" + orgCount + " /tracce=" + tracceCount;
						writehScore(context , orgCount , remainsCount , tracceCount);
						break;
					case PRG_CODE_SET_SCORE:
						dbMsg += ",remain=" + remainsCount + " /org=" + orgCount + " /tracce=" + tracceCount;
						writehScore(context , orgCount , remainsCount , tracceCount);
						break;
				}
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}

		}

		@Override
		public void onCancel(DialogInterface dialog) {
			final String TAG = "onCancel[CSC.L]";
			String dbMsg = "";
			try {
				dbMsg += "Dialog onCancell... calling cancel(true)";
				this.cancel(true);
				myLog(TAG , dbMsg);
			} catch (Exception er) {
				myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
			}
		}
	}

	/**
	 * トレース後、残った元画像のピクセルをカウントして評価
	 */
	public void scorePixcel(int PRG_CODE) {
		final String TAG = "scorePixcel[CView]";
		String dbMsg = "";
		try {
//			if ( myCanvas != null ) {
//				dbMsg += "canvas[" + myCanvas.getWidth() + "×" + myCanvas.getHeight() + "]";  //[168×144]?
			isJudged= false;                    //評価終了

			Bitmap screenShot = getViewBitmap(this);
			int bWidth = screenShot.getWidth();
			int bHight = screenShot.getHeight();
			dbMsg += "screenShot[" + bWidth + "×" + bHight + "]" + screenShot.getByteCount() + "バイト";
			trPixels = new int[bWidth * bHight];
			screenShot.getPixels(trPixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にmyBitmapのデータを格納する
			dbMsg += ",pixels=" + trPixels.length;
			remainsCount = 0;
			chackTotal = 0;
			tracceCount = 0;
			dbMsg += ",orgColor=" + orgColor + ",selectColor=" + selectColor;
			AsyncTask< Integer, Integer, Integer > progD = new loadAsyncTask(context , context.getString(R.string.common_hanteisite_imasu) , readFileName + "[" + bWidth + context.getString(R.string.common_sekisan_hugou) + bHight + "]" + screenShot.getByteCount() + context.getString(R.string.common_bite) , bHight);
			progD.execute(PRG_CODE , bWidth , bHight);
//			} else {
////				String titolStr = "取得できません";
////				String mggStr = "まだ書き込みが行われていません。";
////				messageShow(titolStr , mggStr);
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void scorePixcelBody(int xPoint , int yPoint , int bWidth) {
		final String TAG = "scorePixcelBody[CView]";
		String dbMsg = "";
		try {
			int bIndex = xPoint + yPoint * bWidth;

			int pixel = trPixels[bIndex];
//			trPixels[bIndex] = Color.argb(Color.alpha(pixel) , Color.red(pixel) , Color.green(pixel) , Color.blue(pixel));   //反転 ;0xFF -各色
			if ( pixel != whiteVar && pixel != brackVar && 255 == Color.alpha(pixel) ) {                            //	if ( pixel <-1 ) {
				chackTotal++;
				if ( orgColor == pixel ) {                            // pixel != whiteVar &&  pixel != brackVar &&
					remainsCount++;
				} else if ( selectColor == pixel ) {                            //トレース色    ||
					tracceCount++;
				} else {
					dbMsg += "(" + xPoint + "×" + yPoint + ")W=" + bWidth + ">>" + bIndex + ",chack=" + chackTotal + ",pixel=" + pixel;
//					 myLog(TAG , dbMsg);
				}
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * このViewのBitMapを読込む
	 * 8/25;scorePixcelからしか使えない
	 * 9/2;701SH; Skipped 32 frames!  The application may be doing too much work on its main thread.
	 */
	public Bitmap getViewBitmap(View targetView) {
		final String TAG = "getViewBitmap[CView]";
		String dbMsg = "";
		Bitmap retBitmap = null;
		try {
			dbMsg += "対象[" + targetView.getWidth() + "×" + targetView.getHeight() + "]";
			targetView.setDrawingCacheEnabled(true);
			Bitmap cache = targetView.getDrawingCache();                    // Viewのキャッシュを取得
			retBitmap = Bitmap.createBitmap(cache);
			if ( cache == null ) {
				String titolStr = "Bitmapの取得";
				String mggStr = "取得できませんでした。";
				messageShow(titolStr , mggStr);
				return null;
			}
			targetView.setDrawingCacheEnabled(false);
			int bWidth = retBitmap.getWidth();
			int bHight = retBitmap.getHeight();
			dbMsg += "screenShot[" + bWidth + "×" + bHight + "]" + retBitmap.getByteCount() + "バイト";
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBitmap;
	}

	/**
	 * ファイルからBitMap配列の読込み
	 * 未使用
	 */
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		final String TAG = "getBitmapAsByteArray[CView]";
		String dbMsg = "";
		byte[] bitmapArray = null;
		try {
			dbMsg += ",書込み[" + bitmap.getWidth() + "×" + bitmap.getHeight() + "]";
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG , 100 , byteArrayOutputStream);            //PNG, クオリティー100としてbyte配列にデータを格納
			bitmapArray = byteArrayOutputStream.toByteArray();
			dbMsg += "," + bitmapArray.length + "バイト";
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return bitmapArray;
	}

	/**
	 * 設定したPaintの取得
	 * 未使用
	 */
	public void readPaint(Paint rPaint) {
		final String TAG = "readPaint[CView]";
		String dbMsg = "";
		try {
			if ( rPaint != null ) {
				int cInt = rPaint.getColor();
				dbMsg = "Color= " + cInt + "=" + Integer.toHexString(cInt);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 現在canvasに在るピクセル配列を保持する
	 */
	public void setOriginPixcel() {
		final String TAG = "setOriginPixcel[CSC]";
		String dbMsg = "";
		try {
			dbMsg += "originPixcel=" + pixels.length;
//		var cWidth = canvas.width;
//		var cHeight = canvas.height;
//		dbMsg += "["+ cWidth + "×"+ cHeight +  "]";
//		// var context = canvas.getContext('2d');
//		originalCanvas = context.getImageData(0, 0, cWidth, cHeight);
//		originPixcel = new Array();											//原版の保持領域初期化
//		originPixcel = originalCanvas.data;					//ファイルから読み込まれたピクセル配列を保持
//		dbMsg += ">>"+originPixcel.length;
//		myLog(dbMsg);
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 保持したピクセル配列をcanvasに戻す
	 */
	public void redrowOrigin() {
		final String TAG = "redrowOrigin[CSC]";
		String dbMsg = "";
		try {
			dbMsg += "originPixcel=" + pixels.length;
//		if(0 < originPixcel.length){						//再選択時は
//			context.putImageData(originalCanvas, 0, 0);			// コピーしたピクセル情報をCanvasに転送
//		}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * ①読み込んだ画像をピクセル配列に変換する。
	 * ②指定されたCanvacs内のビットマップを指定方向に置き換える。
	 * @param {*} canvas 捜査対象
	 * @param {*} direction 置換え方向　0：そのまま　、　1;鏡面（上下）
	 *            http://andante.in/i/%E6%8F%8F%E7%94%BB/android%E3%81%AEbitmap%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6%E3%81%BE%E3%81%A8%E3%82%81/
	 */
	public void canvasSubstitution(int direction) {
		final String TAG = "canvasSubstitution[CSC]";
		String dbMsg = "";
		try {
			dbMsg = "direction=" + direction + "=" + context.getString(direction);
			REQUEST_CORD = direction;
//			backAgain();
			paint = new Paint();
			matrix = new Matrix();
			switch ( direction ) {
				case R.string.rb_roat_right:
					matrix.postRotate(90);
					break;
				case R.string.rb_roat_left:
					matrix.postRotate(270);
					break;
				case R.string.rb_roat_half:
					matrix.preScale(-1 , -1);
					break;
				case R.string.rb_flip_vertical:
					matrix.preScale(1 , -1);
					break;
				case R.string.rb_flip_horizontal:
					matrix.preScale(-1 , 1);
					break;
			}
			invalidate();                        //onDrawを発生させて描画実行
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * viewの内容をファイルに保存する
	 */
	public void bitmapSave(Context _context , Activity _activity , String writeFolder) {
		final String TAG = "bitmapSave[CSC]";
		String dbMsg = "";
		try {
			if ( output != null ) {
				int byteCount = output.getByteCount();
				dbMsg += ",output[" + output.getWidth() + "×" + output.getHeight() + "]";
				dbMsg += "" + byteCount + "バイト";
				if ( 0 < byteCount ) {
					CS_Util UTIL = new CS_Util();
					UTIL.maikOrgPass(writeFolder);
					Date mDate = new Date();
					SimpleDateFormat fileNameDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
					String fileName = fileNameDate.format(mDate) + ".png";
					String saveFileName = writeFolder + File.separator + fileName;
					dbMsg += ",saveFileName=" + saveFileName;
					BmpSaver BS = new BmpSaver(_context , _activity , output , saveFileName);

					HandlerThread mBackgroundThread;
					mBackgroundThread = new HandlerThread("CameraBackground");
					mBackgroundThread.start();
					Handler mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

					mBackgroundHandler.post(BS);

					mBackgroundHandler = null;
					mBackgroundThread.quitSafely();
					mBackgroundThread.join();
					mBackgroundThread = null;

					Toast.makeText(context , saveFileName + context.getString(R.string.bms_msg1) , Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context , context.getString(R.string.bms_msg2) , Toast.LENGTH_SHORT).show();
			}

			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 *
	 * */
	public void canvasBack() {
		final String TAG = "canvasBack[CView]";
		String dbMsg = "";
		try {
			if ( myCanvas != null ) {
				myCanvas.restore(); // save直前に戻る   ;java.lang.IllegalStateException: Underflow in restore - more restores than saves
			} else {
				String titolStr = "戻せません";
				String mggStr = "まだ書き込みが行われていません。";
				messageShow(titolStr , mggStr);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	public void clearAll() {
		final String TAG = "clearAll[CView]";
		String dbMsg = "";
		try {
			REQUEST_CORD = REQUEST_CLEAR;
			invalidate();                        //onDrawを発生させて描画実行
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 全消去
	 */
	public void clearAllBody(Canvas canvas) {
		final String TAG = "clearAllBody[CView]";
		String dbMsg = "";
		try {
			if ( isDrow ) {
				if ( output != null ) {
					output.recycle();
					output = null;
				}
			}
			canvas.drawColor(eraserColor , PorterDuff.Mode.CLEAR);                // 描画クリア
			for ( PathObject pathObject : pathIist ) {
				pathObject.path.reset();
			}
			canvas.drawColor(0 , PorterDuff.Mode.CLEAR);
			float caWidth = canvas.getWidth();
			float caHeight = canvas.getHeight();
			dbMsg += ",canvas[" + caWidth + "×" + caHeight + "]";
			canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
			if ( !isDrow ) {
				writehScore(context , orgCount , orgCount , 0);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}


	///////////////////////////////////////////////////////////////////////////////////
	public static void myLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myLog(TAG , dbMsg);
	}

	public static void myErrorLog(String TAG , String dbMsg) {
		CS_Util UTIL = new CS_Util();
		UTIL.myErrorLog(TAG , dbMsg);
	}

	public void messageShow(String titolStr , String mggStr) {
		CS_Util UTIL = new CS_Util();
		UTIL.messageShow(titolStr , mggStr , CS_CanvasView.this.context);
	}

}

//		Canvas と Path による手書き View の簡単な実装		http://android.keicode.com/basics/ui-canvas-path.php
//FrameLayout implements RendererCommon.RendererEvents では   onTouchEventは発生しても  onDrawが発生しない
//webRTC for android	https://qiita.com/nakadoribooks/items/7950e29ad3b751ddab12
/**
 * 課題
 * ・canvas間通信
 * ・padのトレースでveiw側に描画
 * ・tachiendで自動評価
 * ・評価実装
 * ・消し込み評価
 * ・toolbarへの書き込み
 * ・もう一度
 * ・定例パターン読込み
 * ・pixcel配列読込み
 * ・変形
 * ・右へ90度回転
 * ・左へ90度回転
 * ・180度回転
 * ・上下反転
 * ・左右反転
 * ・オリジナル（に戻す）
 * ・動作設定
 * ・鏡面動作
 * ・上下
 * ・左右
 * ・トレース後に自動判定
 * ・手書き
 * ・フリーハンド
 * ・start～end範囲取得
 * ・消去
 * ・直線
 * ・三角
 * ・矩形
 * ・text入力
 * ・pixcel配列記録  （オリジナルにする）
 * ・もう一度
 **/