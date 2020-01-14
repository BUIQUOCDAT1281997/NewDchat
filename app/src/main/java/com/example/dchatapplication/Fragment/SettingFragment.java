package com.example.dchatapplication.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dchatapplication.Activity.ProfileActivity;
import com.example.dchatapplication.Activity.QRActivity;
import com.example.dchatapplication.R;
import com.example.dchatapplication.Activity.StartActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {


    private static final int CAMERA_CODE = 150;

    //QR-code
    private ImageView imgQR;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.setting_tv_account).setOnClickListener(this);
        view.findViewById(R.id.setting_tv_log_out).setOnClickListener(this);
        view.findViewById(R.id.button_scan_qr_code).setOnClickListener(this);

        //QR-code
        imgQR = view.findViewById(R.id.img_qr_code);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setQRImage();

    }

    private void setQRImage() {
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    userID,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    300, 300, null);
        } catch (WriterException e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }

        assert bitMatrix != null;
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black) : getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 300, 0, 0, bitMatrixWidth, bitMatrixHeight);

        imgQR.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_tv_account:
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.setting_tv_log_out:
                showDialog();
                break;
            case R.id.button_scan_qr_code:
                requestPermission();
                break;

            default:
                break;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            } else {
                gotoQRActivity();
            }
        } else {
            gotoQRActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "permission denied, boo!", Toast.LENGTH_SHORT).show();
            } else {
                gotoQRActivity();
            }
        }
    }

    private void gotoQRActivity() {
        Intent intent = new Intent(getContext(), QRActivity.class);
        getActivity().startActivity(intent);
    }

    private void showDialog(){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setMessage(getResources().getString(R.string.you_want_to_log_out));
        builder.setCancelable(false);
        builder.setBackground(getResources().getDrawable(R.drawable.alert_dialog_bg));
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), StartActivity.class));
                getActivity().finish();
            }
        });
        builder.show();
    }
}
