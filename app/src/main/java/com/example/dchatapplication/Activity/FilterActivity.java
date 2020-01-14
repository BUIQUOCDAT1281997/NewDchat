package com.example.dchatapplication.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.dchatapplication.R;

public class FilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);


        Intent intent = getIntent();
        String text = intent.getDataString();

        Uri uri = intent.getData();

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
           Intent intent = getIntent();
           String text = intent.getDataString();

           Uri uri = intent.getData();

           Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }
}
