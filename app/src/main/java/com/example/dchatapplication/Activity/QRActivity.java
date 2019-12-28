package com.example.dchatapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dchatapplication.Other.User;
import com.example.dchatapplication.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class QRActivity extends AppCompatActivity {


    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private boolean isUserID = false;
    private String resultScanner;

    private LinearLayout llProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        llProgressBar = findViewById(R.id.llProgressBar);
        cameraView = findViewById(R.id.camera_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {

                    resultScanner = barcodes.valueAt(0).displayValue;

                    llProgressBar.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            cameraView.setVisibility(View.GONE);
                            findViewById(R.id.view_center).setVisibility(View.GONE);
                            llProgressBar.setVisibility(View.VISIBLE);

                        }
                    });

                    accuracy();
                }
            }
        });

    }

    private void accuracy() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getId().equals(resultScanner)) {
                        isUserID = true;
                        break;
                    }
                }

                if (isUserID) {
                    Intent intent = new Intent(QRActivity.this, ChatActivity.class);
                    intent.putExtra("userID", resultScanner);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    llProgressBar.setVisibility(View.GONE);
                    finish();
                } else {
                    Toast.makeText(QRActivity.this, "No matching id found", Toast.LENGTH_LONG).show();
                    llProgressBar.setVisibility(View.GONE);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }

}
