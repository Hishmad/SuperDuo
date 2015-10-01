package it.jaschke.alexandria.CameraPreview;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.fragment.AddBook;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 *
 */
public class BarcodeScanner extends ActionBarActivity implements ZBarScannerView.ResultHandler {


    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);
        setupFormats();
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        if (rawResult.getBarcodeFormat().getId() == BarcodeFormat.ISBN13.getId()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AddBook.SCAN_CONTENTS, rawResult.getContents());
            resultIntent.putExtra(AddBook.SCAN_FORMAT, rawResult.getBarcodeFormat().getName());
            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        } else {
            mScannerView.startCamera();
        }
    }

    /**
     * This makes sure only ISBN & EAN format will get scan
     */
    private void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.ISBN13);
        formats.add(BarcodeFormat.EAN13);
        if(mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }

}
