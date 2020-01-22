package com.example.dchatapplication.Fragment;



import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dchatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyQRFragment extends Fragment {

    //QR-code
    private ImageView imgQR;
    private String userID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //QR-code
        imgQR = view.findViewById(R.id.qr_code_img);
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


}
