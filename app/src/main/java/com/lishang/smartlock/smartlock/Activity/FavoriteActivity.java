package com.lishang.smartlock.smartlock.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lishang.smartlock.R;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

    }
    public void btn_list(View v){
        Intent intent = new Intent(FavoriteActivity.this
                , MainActivity.class);
        startActivity(intent);
    }

}
