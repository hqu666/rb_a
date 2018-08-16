package com.hijiyam_koubou.recoverybrain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;

public class QRActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qr);
//		DecoratedBarcodeView qrReaderView = findViewById(R.id.decoratedBarcodeView);
//		startCapture()
//		new IntentIntegrator(QRActivity.this).initiateScan();
	}

//	private fun startCapture() {
//		startButton?.isEnabled = false
//
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
//	}
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//		if(result != null) {
//			Log.d("readQR", result.getContents());
//		} else {
//			super.onActivityResult(requestCode, resultCode, data);
//		}
//	}
	
}
