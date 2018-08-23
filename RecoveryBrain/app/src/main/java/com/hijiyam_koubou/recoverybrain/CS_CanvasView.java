package com.hijiyam_koubou.recoverybrain;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CS_CanvasView extends View {        //org; View	から　io.skyway.Peer.Browser.Canvas	に合わせる
	// extends FrameLayout implements RendererEvents
	//extends FrameLayout implements org.webrtc.RendererCommon.RendererEvents
	private Context context;
	private Toolbar toolbar;

	private boolean isCall = false;                    //newで呼ばれた
	private boolean isRecever = true;                    //受信側
	private boolean isActionUp = false;                    //描画終了
	public boolean  isMirror=false;				//上下鏡面動作
	public boolean  is_h_Mirror=false;				//左右鏡面動作
	public boolean  isAutoJudge=true;				//トレース後に自動判定
	private boolean  isComp=false;				//比較中	;scoreStartRadyでtrueに設定


	private Canvas myCanvas;
	private Paint paint;                        //ペン
	private Path path;                     //ペン
	//	public int penColor = 0xFF008800;        //蛍光グリーン
	public int selectColor = 0xFF008800;        //蛍光グリーン
	public float selectWidth = 5;
	public String selectLineCap = "";
	public String[] lineCapList;

	private Paint eraserPaint;                //消しゴム
	private int eraserColor = Color.WHITE;        //背景色に揃える
	private float eraserWidth = 50.0f;

	//	private Path path;
	public List< PathObject > pathIist;        //InitCanvaとファイル読込みなどの元がzぽう更新直後に初期化

	class PathObject {
		Path path;
		Paint paint;
	}

	float upX;
	float upY;

	static final int REQUEST_CLEAR = 500;                            //全消去
	static final int REQUEST_DROW_PATH = REQUEST_CLEAR + 1;            //フリーハンド
	static final int REQUEST_AGAIN = REQUEST_DROW_PATH + 1;            //もう一度
	static final int REQUEST_ADD_BITMAP = REQUEST_AGAIN + 1;            //ビットマップ挿入
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

	public CS_CanvasView(Context context , boolean isRecever,Toolbar _toolbar) {
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
			this.toolbar= _toolbar;
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
						canvas.drawColor(eraserColor , PorterDuff.Mode.CLEAR);                // 描画クリア
						for ( PathObject pathObject : pathIist ) {
							pathObject.path.reset();
						}
						canvas.drawRect(0 , 0 , caWidth , caHeight , eraserPaint);        //?真っ黒になるので背景色に塗りなおす
						REQUEST_CORD = REQUEST_DROW_PATH;
						break;
					case REQUEST_DROW_PATH:                //501;フリーハンド
						if(output != null) {
							canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
						}
						dbMsg += ".pathIist=" + pathIist.size() + "件";
						Paint lastPaint = null;
						for ( PathObject pathObject : pathIist ) {
							canvas.drawPath(pathObject.path , pathObject.paint);
							lastPaint = pathObject.paint;
						}
						dbMsg += ",REQUEST_CORD=" + REQUEST_CORD;
						if(lastPaint != null && isActionUp){
							myCanvas  = new Canvas();
							myCanvas = canvas;					//現在のCanvasの状態を保存
							RectF bounds = new RectF(0, 0, caWidth, caHeight);
//							int saveFlag = myCanvas.saveLayer(bounds, lastPaint, Canvas.CLIP_TO_LAYER_SAVE_FLAG);
							int saveFlag = myCanvas.save();         //現在の状態を覚える
							dbMsg += ".saveFlag=" + saveFlag;
							/**
							 MATRIX_SAVE_FLAG	Matrix情報（translate, rotate, scale, skew）の情報を保存
							 CLIP_SAVE_FLAG	クリップ領域を保存
							 HAS_ALPHA_LAYER_SAVE_FLAG	アルファ（不透明度）レイヤーを保存
							 FULL_COLOR_LAYER_SAVE_FLAG	カラーレイヤーを保存
							 CLIP_TO_LAYER_SAVE_FLAG	クリップレイヤーとして保存
							 ALL_SAVE_FLAG	全ての状態を保存する*/
							isActionUp = false;                    //描画終了

							dbMsg += ",myCanvas[" + myCanvas.getWidth() + "×" + myCanvas.getHeight() + "]";  //[168×144]?
							if(isAutoJudge){
								CharSequence wStr = "自動判定";
								dbMsg +=  ">>" + wStr;
								Toast.makeText(context, wStr, Toast.LENGTH_SHORT).show();
								scorePixcel();
							}
							myLog(TAG , dbMsg);
						}
						break;
					case REQUEST_AGAIN:                //502；もう一度
						if(output != null) {
							canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
						} else{
							String titolStr = "「もう一度」を指定されました";
							String mggStr = "まだ元画像がセットされていません。";
							messageShow(titolStr , mggStr);
						}
						REQUEST_CORD =  REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す☆止めないとloopする
						break;
					case REQUEST_ADD_BITMAP:                //503;ビットマップ挿入
						dbMsg += "(" + upX + "×" + upY + ")に[" + aBmp.getWidth() + "×" + aBmp.getHeight() + "]" + aBmp.getByteCount() + "バイトのビットマップ挿入";
		                   if(isRecever){
							   getCanvasPixcel(aBmp , canvas);
						   }    else{
//						canvas.drawBitmap(aBmp , upX , upY , ( Paint ) orignLine); // image, x座標, y座標, Paintイタンス
						   }
//						dbMsg += ",orignLine=" +orignLine.toString();
						break;
				}
//				canvas.restore(); // save直前に戻る

			}
//			myLog(TAG , dbMsg);
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
					isActionUp = false;                    //描画終了
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
						addPaint.setStrokeCap(Paint.Cap.SQUARE);
					} else if ( addCap.equals(lineCapList[2]) ) {     //butt
						addPaint.setStrokeCap(Paint.Cap.BUTT);
					}
					dbMsg += ",addWidth=" + addWidth + "px";
					addPaint.setStrokeWidth(addWidth);
					pathObject.paint = addPaint;
					pathIist.add(pathObject);

//					paint = new Paint();
//					paint.setColor(selectColor);                        //
//					paint.setStyle(Paint.Style.STROKE);
//					paint.setStrokeJoin(Paint.Join.ROUND);
//					paint.setStrokeCap(Paint.Cap.ROUND);
//					paint.setStrokeWidth(selectWidth);
//					path = new Path();
//					path.moveTo(xPoint , yPoint);
//					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:   //2
					isActionUp = false;                    //描画終了
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);

//					path.lineTo(xPoint , yPoint);
//					invalidate();
					break;
				case MotionEvent.ACTION_UP:     //1
					isActionUp = true;                    //描画終了
					pathIist.get(pathIist.size() - 1).path.lineTo(xPoint , yPoint);

//					path.lineTo(xPoint , yPoint);
//					invalidate();                        //onDrawを発生させて描画実行
					break;
			}
					invalidate();                        //onDrawを発生させて描画実行
			if(isActionUp){
				myLog(TAG , dbMsg);
			}
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	public Bitmap aBmp;						//書込むビットマップ（16：9）
	public Bitmap output;					//argb配列から作成されたビットマップ
	public Bitmap screenShot;				//トレース後のビットマップ
	//	public double scaleWH;					//書込んだ時のスケール
	int pixels[] = null;					//canvas全体のargb配列
	public String readFileName = "" ;		//読み込んだファイル名☆再読み込みの時は初期化
	public int orgColor;							//トレース元の線色
	public int orgCount;							//トレース元のピクセル数
	public int orgWidth;						//トレース元の線の太さ

	public int scoreVar = 0;
	public int after_Count = 0;
	public int before_Count = 0;
//	var orgCount=0;
//	var compCount=0;
	private ProgressDialog progressDialog;

	/**
	 * トレース前の状態に戻す
	 * */
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

	/**
	 * 元画像のトレース色の残りから評価値を算定してtoolbarに書込む
	 * */
	public void writehScore(Context contex,int beforeCount, int afterCount) {
		final String TAG = "writehScore[CView]";
		String dbMsg = "";
		try {
			dbMsg += afterCount + "/" + beforeCount;
			float sVar = (((beforeCount - afterCount)*100) / beforeCount);
			dbMsg +=  "=" + sVar;
			scoreVar = (int)Math.round(sVar);
			CharSequence wStr = "スコア " + scoreVar +"点 "+ afterCount + "/ " + beforeCount +"ピクセル ";
			dbMsg +=  ">>" + wStr;
			Toast.makeText(contex, wStr, Toast.LENGTH_SHORT).show();
			dbMsg +=  ">>" + toolbar;
			toolbar.setTitle(wStr);   //"" + scoreVar
//			wStr = "点 "+ afterCount + "/ " + beforeCount +"ピクセル ";
// toolbar.setSubtitle(wStr);      //2行になってTitolも小さくなる
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 *指定されたファイルをviewの中心にpixcel配列で書き込む
	 */
	public void addBitMap(String fileName , int canvasWidth , int canvasHeight) {
		final String TAG = "addBitMap[CView]";
		String dbMsg = "";
		try {
			if( ! fileName.equals(readFileName)){
				CharSequence wStr ="トレース元画像に" +  fileName + "を読み込んでいます";
				dbMsg +=  ">>" + wStr;
				Toast.makeText(context, wStr, Toast.LENGTH_SHORT).show();

				REQUEST_CORD = REQUEST_ADD_BITMAP;
				Bitmap rBmp= readFIle( fileName);
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
				invalidate();                        //onDrawを発生させて描画実行
				rsBitmap.recycle();
				readFileName = fileName;

			}
			myLog(TAG , dbMsg);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
	}

  /**
   * assetsフォルダーから画像ファイルを読み込む
   * */
	public Bitmap readFIle(String fileName) {
		final String TAG = "readFIle[CView]";
		String dbMsg = "";
		Bitmap bm = null;
		try {
			AssetManager as = getResources().getAssets();
			dbMsg += "fileName="+fileName;
			InputStream is = as.open(fileName);
			bm = BitmapFactory.decodeStream(is);
			is.close();
			//decodeStream(InputStream,padding.options);
			dbMsg += " = "+bm.getByteCount() + "バイト";
			myLog(TAG , dbMsg);
		} catch (IOException er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		} catch (Exception er) {
			myErrorLog(TAG , dbMsg + ";でエラー発生；" + er);
		}
		return  bm;
	}

 /**
  * ファイル読込み直後のピクセル変換
  * 色と線の太さの読込み
  * */
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
				rBmp.getPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にrBmpのデータを格納する
				int whiteVar = Color.argb(255 , 255 ,255 , 255);   //反転 ;0xFF -各色
				int brackVar = Color.argb(255 , 0 ,0 , 0);   //反転 ;0xFF -各色
				dbMsg += ",whiteVar=" + whiteVar + " ,brackVar= " + brackVar;
				List<String> colorIndex = new ArrayList<>();
				SparseIntArray colorArray = new SparseIntArray();     //キーも値もintのmap
				List<String> widthIndex = new ArrayList<>();
				SparseIntArray widthArray = new SparseIntArray();
				int bColor =1;
				int widthCount = 0;
				for ( int yPoint = 0 ; yPoint < bHight ; yPoint++ ) {
					for ( int xPoint = 0 ; xPoint < bWidth ; xPoint++ ) {
						int bIndex = xPoint + yPoint * bWidth;
						int pixel = pixels[bIndex];
						pixels[bIndex] = Color.argb(Color.alpha(pixel) ,  Color.red(pixel) ,  Color.green(pixel) ,  Color.blue(pixel));   //反転 ;0xFF -各色
//						pixels[bIndex] = Color.argb(Color.alpha(pixel) , 0xFF - Color.red(pixel) , 0xFF - Color.green(pixel) , 0xFF - Color.blue(pixel));
						if ( pixel != whiteVar &&  pixel != brackVar && 255 == Color.alpha(pixel) ) { 							//	if ( pixel <-1 ) {
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

//							if(bColor == pixel){
//								widthCount++;
//							}else{
//								String widthName = widthCount + "";
//								if(0 == widthIndex.size()) {
//									widthIndex.add(widthName);
//									widthArray.put(widthCount , 1);
//								}else if(-1 == widthIndex.indexOf(widthName)){
//									widthIndex.add(widthName);
//									widthArray.put(widthCount , 1);
//								}else{
//									int nowCount = (int)widthArray.get(widthCount)+1;
//									widthArray.put(widthCount , nowCount);
//								}
//								widthCount = 0;
//								bColor = pixel;
//							}

						}
					}
				}
				dbMsg += ",pixels=" + pixels.length;
				output = Bitmap.createBitmap(bWidth , bHight , Bitmap.Config.ARGB_8888);        //出力用画像領域確保
				output.setPixels(pixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                    //出力用の領域にセットする
				dbMsg += ",書込み[" + output.getWidth() + "×" + output.getHeight() + "]" + output.getByteCount() + "バイト";
				canvas.drawBitmap(output , 0 , 0 , null);    //	Bitmapイメージの描画
//				dbMsg += ",colorIndex=" + colorIndex.size() + ",=" + colorArray.toString();
				dbMsg += ",colorArray=" + colorArray.size() + "色";
				orgColor =  colorArray.keyAt(0);      //トレース元の線色
				orgCount = colorArray.valueAt(0);      //トレース元のピクセル数
				for (int i = 0; i < colorArray.size(); i++) {
					int key = colorArray.keyAt(i);
					int val = colorArray.valueAt(i);
					 if(orgCount < val){
						 orgColor = key;
						 orgCount = val;
					 }
				}
				dbMsg += ",orgColor=" + orgColor + " = " + orgCount + "ピクセル";
				dbMsg += ",a:" + Color.alpha(orgColor) +  " r:" + Color.red(orgColor) +" g:" + Color.green(orgColor) + " b:" + Color.blue(orgColor);
//				dbMsg += ",widthArray=" + widthArray.size() + "個所";
				orgWidth = 5;												//トレース元の線の太さ
//				int wCount = widthArray.valueAt(0);					//トレース元のピクセル数
//				for (int i = 0; i < widthArray.size(); i++) {
//					int key = widthArray.keyAt(i);
//					int val = widthArray.valueAt(i);
//					if(wCount < val && (0 < key && key < 51)){        //x軸上50px未満の幅
//						orgWidth = key;
//						wCount = val;
//					}
//				}
//				dbMsg += ",orgWidth=" + orgWidth + "ピクセル= " + wCount + "個所";

				selectColor = Color.argb(255 , 255 ,0 , 0);        //課題；反対色に
				selectWidth = orgWidth* 2 ;
				REQUEST_CORD =  REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す☆止めないとloopする
				writehScore(context,orgCount,orgCount);
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

	/**
	 * トレース後、残った元画像のピクセルをカウントして評価
	 * */
	public void scorePixcel() {
		final String TAG = "scorePixcel[CView]";
		String dbMsg = "";
		try {
			if ( myCanvas != null ) {
				CharSequence wStr = "トレースできた状況を読み取っています。";
				dbMsg +=  ">>" + wStr;
				Toast.makeText(context, wStr, Toast.LENGTH_SHORT).show();

//				myCanvas.restore();               //: Underflow in restore - more restores than saves
				dbMsg += "canvas[" + myCanvas.getWidth() + "×" + myCanvas.getHeight() + "]";  //[168×144]?
				this.setDrawingCacheEnabled(true);
				Bitmap cache = this.getDrawingCache();    				// Viewのキャッシュを取得
				Bitmap screenShot = Bitmap.createBitmap(cache);
				this.setDrawingCacheEnabled(false);
				int bWidth = screenShot.getWidth();
				int bHight = screenShot.getHeight();
				dbMsg += "screenShot[" + bWidth + "×" + bHight + "]" + screenShot.getByteCount() + "バイト";
				int[] trPixels = new int[bWidth * bHight];
				screenShot.getPixels(trPixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にmyBitmapのデータを格納する

//				Bitmap trBmp = Bitmap.createBitmap(bWidth , bHight , Bitmap.Config.ARGB_8888);        //現状を読み込むビットマップ
//				trBmp.getPixels(trPixels , 0 , bWidth , 0 , 0 , bWidth , bHight);                //pixelsの配列にmyBitmapのデータを格納する
				dbMsg +=  ",pixels=" + trPixels.length;
				int whiteVar = Color.argb(255 , 255 ,255 , 255);   //反転 ;0xFF -各色
				int brackVar = Color.argb(255 , 0 ,0 , 0);   //反転 ;0xFF -各色
				dbMsg += ",whiteVar=" + whiteVar + " ,brackVar= " + brackVar + " , orgColor=" +orgColor;
				int remainsCount = 0;
				int chackTotal = 0;

				for ( int yPoint = 0 ; yPoint < bHight ; yPoint++ ) {
					for ( int xPoint = 0 ; xPoint < bWidth ; xPoint++ ) {
						int bIndex = xPoint + yPoint * bWidth;
						int pixel = trPixels[bIndex];
						trPixels[bIndex] = Color.argb(Color.alpha(pixel) ,  Color.red(pixel) ,  Color.green(pixel) ,  Color.blue(pixel));   //反転 ;0xFF -各色
						if ( orgColor == pixel) { 							// pixel != whiteVar &&  pixel != brackVar &&
							remainsCount++;
						}
						chackTotal++;
					}
				}
				dbMsg += ",remainsCount=" +remainsCount + " /orgCount=" + orgCount+ " /chackTotal=" + chackTotal;
				writehScore(context,orgCount,remainsCount);
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



	/**
	 * ファイルからBitMap配列の読込み
	 * 未使用
	 * */
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
	 * */
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
			dbMsg += "isRecever=" + isRecever +  ",REQUEST_CORD=" + REQUEST_CORD;
//			if ( ! isRecever ) {            ////newで呼ばれた場合はこのクラス内でイベント取得
			if(REQUEST_CORD == REQUEST_ADD_BITMAP){       //502;ビットマップ挿入直後
				REQUEST_CORD =  REQUEST_DROW_PATH;    //描画モードをフルーハンドに戻す
			}
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
//					invalidate();                        //onDrawを発生させて描画実行
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