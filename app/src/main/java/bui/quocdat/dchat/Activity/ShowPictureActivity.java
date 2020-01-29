package bui.quocdat.dchat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import bui.quocdat.dchat.R;

public class ShowPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        String pictureURL = getIntent().getStringExtra("pictureURL");
        ImageView imageView = findViewById(R.id.img_picture_show);
        Glide.with(this).load(pictureURL).into(imageView);
    }
}
