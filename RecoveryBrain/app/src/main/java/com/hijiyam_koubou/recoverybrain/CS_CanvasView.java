package com.hijiyam_koubou.recoverybrain;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;


import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CS_CanvasView extends View {        //org; View	から　io.skyway.Peer.Browser.Canvas	に合わせる
	// extends FrameLayout implements RendererEvents
	//extends FrameLayout implements org.webrtc.RendererCommon.RendererEvents
	private Context context;
	private boolean isCall = false;                    //newで呼ばれた
	private boolean isRecever = true;                    //受信側
	private Canvas myCanvas;
	private Paint paint;                        //ペン
	//	public int penColor = 0xFF008800;        //蛍光グリーン
	public int selectColor = 0xFF008800;        //蛍光グリーン

	//	private float penWidth = 5;
	public float selectWidth = 5;
	public String selectLineCap = "";
	public String[] lineCapList;

	private Paint eraserPaint;                //消しゴム
	private int eraserColor = Color.WHITE;        //背景色に揃える
	private float eraserWidth = 50.0f;

	//	private Path path;
	public List< PathObject > pathIist;

	class PathObject {
		Path path;
		Paint paint;
	}

	float upX;
	float upY;

	static final int REQUEST_CLEAR = 500;                            //全消去
	static final int REQUEST_DROW_PATH = REQUEST_CLEAR + 1;            //フリーハンド
	static final int REQUEST_ADD_BITMAP = REQUEST_DROW_PATH + 1;            //ビットマップ挿入
	public int REQUEST_CORD = REQUEST_DROW_PATH;

	//RecvrryBrain
	private Paint orignLine = null;                        //トレース元画像


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

	public CS_CanvasView(Context context , boolean isRecever) {
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
			InitCanva();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void InitCanva() {
		final String TAG = "InitCanva[CView]";
		String dbMsg = "";
		try {
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
				int saveFlag = canvas.save();         //現在の状態を覚える
				dbMsg += ".saveFlag=" + saveFlag;
				/**
				 MATRIX_SAVE_FLAG	Matrix情報（translate, rotate, scale, skew）の情報を保存
				 CLIP_SAVE_FLAG	クリップ領域を保存
				 HAS_ALPHA_LAYER_SAVE_FLAG	アルファ（不透明度）レイヤーを保存
				 FULL_COLOR_LAYER_SAVE_FLAG	カラーレイヤーを保存
				 CLIP_TO_LAYER_SAVE_FLAG	クリップレイヤーとして保存
				 ALL_SAVE_FLAG	全ての状態を保存する*/

				switch ( REQUEST_CORD ) {
					case REQUEST_CLEAR:                //500;全消去
						canvas.drawColor(eraserColor , PorterDuff.Mode.CLEAR);                // 描画クリア
						for ( PathObject pathObject : pathIist ) {
							pathObject.path.reset();
						}
						canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
						REQUEST_CORD = REQUEST_DROW_PATH;
						break;
					case REQUEST_DROW_PATH:                //501;フリーハンド
						for ( PathObject pathObject : pathIist ) {
							canvas.drawPath(pathObject.path , pathObject.paint);
						}
						break;
					case REQUEST_ADD_BITMAP:                //502;ビットマップ挿入
						dbMsg += "(" + upX + "×" + upY + ")に[" + aBmp.getWidth() + "×" + aBmp.getHeight() + "]" + aBmp.getByteCount() + "バイトのビットマップ挿入";
//						canvas.drawBitmap(aBmp , upX , upY , ( Paint ) orignLine); // image, x座標, y座標, Paintイタンス
						getCanvasPixcel(aBmp , canvas);
//						dbMsg += ",orignLine=" +orignLine.toString();
						break;
				}
				canvas.restore(); // save直前に戻る

				myCanvas = canvas;            //現在のCanvasの状態を保存
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * 受信/送信前のイベント処理
	 */
	public void drawPathLine(float xPoint , float yPoint , int addColor , float addWidth , String addCap , int action) {
		final String TAG = "drawPathLine[CView]";
		String dbMsg = "";
		try {

			dbMsg = "action=" + action + "(" + xPoint + " , " + yPoint + ")";
			switch ( action ) {
				case MotionEvent.ACTION_DOWN:   //0
					PathObject pathObject = new PathObject();
					Path addPath = new Path();
					addPath.moveTo(xPoint , yPoint);
					pathObject.path = addPath;

					Paint addPaint = new Paint();
					dbMsg += ",addColor=" + addColor;
					addPaint.setColor(addColor);                        //
					addPaint.setStyle(Paint.Style.STROKE);
					addPaint.setStrokeJoin(Paint.Join.ROUND);
					dbMsg += ",addCap=" + addCap;
					if ( addCap.equals(lineCapList[0]) ) {     //round
						addPaint.setStrokeCap(Paint.Cap.ROUND);
					} else if ( addCap.equals(lineCapList[1]) ) {     //square
						paint.setStrokeCap(Paint.Cap.SQUARE);
					} else if ( addCap.equals(lineCapList[2]) ) {     //butt
						addPaint.setStrokeCap(Paint.Cap.BUTT);
					}
					dbMsg += ",addWidth=" + addWidth + "px";
					addPaint.setStrokeWidth(addWidth);
					pathObject.paint = addPaint;

					pathIist.add(pathObject);

//					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:   //2
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					invalidate();
					break;
				case MotionEvent.ACTION_UP:     //1
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);
					invalidate();                        //onDrawを発生させて描画実行
					break;
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public Bitmap aBmp;             //書込むビットマップ（16：9）
//	public double scaleWH;			//書込んだ時のスケール
	int pixels[] = null;			//canvas全体のargb配列

	public void addBitMap(Bitmap rBmp , int canvasWidth , int canvasHeight) {
		final String TAG = "addBitMap[CView]";
		String dbMsg = "";
		try {
			REQUEST_CORD = REQUEST_ADD_BITMAP;
			int bmpWidth = rBmp.getWidth();
			int bmpHeight = rBmp.getHeight();
			dbMsg += "読込み[" + bmpWidth + "×" + bmpHeight + "]" + rBmp.getByteCount() + "バイト";
			dbMsg += "、 canvas[" + canvasWidth + "×" + canvasHeight + "]";
			double scaleWidth = 1.0;
			if ( canvasWidth < bmpWidth ) {
				scaleWidth = ( double ) ((canvasWidth * 1000) / bmpWidth) / 1000;
			}
			double scaleHeight = 1.0;
			if ( canvasHeight < bmpHeight ) {
				scaleHeight = ( double ) ((canvasHeight * 1000) / bmpHeight) / 1000;
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
			orignLine = new Paint();
			invalidate();                        //onDrawを発生させて描画実行
			readPaint(orignLine);
			rsBitmap.recycle();
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public void getCanvasPixcel(Bitmap rBmp , Canvas canvas) {
		final String TAG = "getCanvasPixcel[CView]";
		String dbMsg = "";
		try {
			if ( canvas != null ) {
				dbMsg += "canvas[" + canvas.getWidth() + "×" + canvas.getHeight() + "]";
				int bWidth = rBmp.getWidth();
				int bHight = rBmp.getHeight();
				dbMsg += "読込み[" + bWidth + "×" + bHight + "]" + rBmp.getByteCount() + "バイト";
//				byte[] pixels = getBitmapAsByteArray( rBmp);
				 pixels = new int[bWidth * bHight];
				rBmp.getPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にmyBitmapのデータを格納する
				List<String> colorIndex = new ArrayList<>();
				SparseIntArray colorArray = new SparseIntArray();     //キーも値もintのmap
				for ( int yPoint = 0 ; yPoint < bHight ; yPoint++ ) {
					for ( int xPoint = 0 ; xPoint < bWidth ; xPoint++ ) {
						int bIndex = xPoint + yPoint * bWidth;
						int pixel = pixels[bIndex];
						pixels[bIndex] = Color.argb(Color.alpha(pixel) ,  Color.red(pixel) ,  Color.green(pixel) ,  Color.blue(pixel));   //反転 ;0xFF -各色
//						pixels[bIndex] = Color.argb(Color.alpha(pixel) , 0xFF - Color.red(pixel) , 0xFF - Color.green(pixel) , 0xFF - Color.blue(pixel));
						if ( pixel <-1 ) {
							String colorName =pixel+"";
							if(0 == colorIndex.size()) {
								colorIndex.add(colorName);
								colorArray.put(pixel , 1);
							}else if(-1 == colorIndex.indexOf(colorName)){
								colorIndex.add(colorName);
								colorArray.put(pixel , 1);
							}else{
								int nowCount = (int)colorArray.get(pixel)+1;
								colorArray.put(pixel , nowCount);
							}
////							String rHex = Integer.toHexString(pixel);
////							dbMsg += "(" +xPoint + "," + yPoint+ ")"  + pixel;
////							dbMsg += ",R= " + Color.red(pixel)+ ",G= " + Color.green(pixel)+ ",B= " + Color.blue(pixel);
						}
					}
				}
				dbMsg += ",pixels=" + pixels.length;
				Bitmap output = Bitmap.createBitmap(bWidth , bHight , Bitmap.Config.ARGB_8888);        //出力用画像領域確保
				output.setPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                    //出力用の領域にセットする
				dbMsg += ",書込み[" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
				canvas.drawBitmap(output , 0 , 0 , orignLine);    //	Bitmapイメージの描画
//				dbMsg += ",colorIndex=" + colorIndex.size() + ",=" + colorArray.toString();
				dbMsg += ",colorArray=" + colorArray.size();
				   // 8/22 ;ソートして一丸多い色を取得　太さも同様
			} else {
				String titolStr = "取得できません";
				String mggStr = "まだ書き込みが行われていません。";
				messageShow(titolStr , mggStr);
			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

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

	/////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final String TAG = "onTouchEvent[CView]";
		String dbMsg = "";
		boolean retBool = false; //trueに設定すると「TouchEventを消化」したものとして他に送らない
		try {
			dbMsg += "isRecever=" + isRecever;
//			if ( isCall ) {            ////newで呼ばれた場合はこのクラス内でイベント取得
			float xPoint = event.getX();
			float yPoint = event.getY();
			int action = event.getAction();
			dbMsg += "(" + xPoint + "×" + yPoint + ")action=" + action;
			switch ( REQUEST_CORD ) {
				case REQUEST_CLEAR:                        //全消去
					break;
				case REQUEST_DROW_PATH:                        //フリーハンド
					dbMsg += ",selectColor=" + selectColor + "mselectWidth=" + selectWidth + ",selectLineCap=" + selectLineCap;
					drawPathLine(xPoint , yPoint , selectColor , selectWidth , selectLineCap , action);
//						invalidate();                        //onDrawを発生させて描画実行
					break;
				case REQUEST_ADD_BITMAP:                        //502;ビットマップ挿入
					upX = xPoint;
					upY = yPoint;
					REQUEST_CORD = 0;
					break;
				default:
					upX = xPoint;
					upY = yPoint;
					break;
			}
			retBool = true;
//			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}
/////////////////////////////////////////////////////////////////////////

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