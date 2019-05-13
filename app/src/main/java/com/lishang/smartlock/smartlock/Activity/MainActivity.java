package com.lishang.smartlock.smartlock.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lishang.smartlock.MapFragment;
import com.lishang.smartlock.R;
import com.lishang.smartlock.smartlock.HomeFragment;
import com.lishang.smartlock.smartlock.MsgFragment;
import com.lishang.smartlock.smartlock.adapter.HomeFragmentPagerAdapter;
import com.lishang.smartlock.smartlock.badgenumberlibrary.BadgeNumberManager;

import java.util.HashMap;
import java.util.Map;

import q.rorbin.badgeview.QBadgeView;

import static com.baidu.mapapi.BMapManager.getContext;
import static com.lishang.smartlock.MapFragment.*;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

    private HomeFragmentPagerAdapter mAdapter;
    private ViewPager vpager;
    private RadioButton tab_btn_home, tab_btn_home1;
    private RadioButton tab_btn_msg, tab_btn_my1;
    private RadioButton tab_btn_my, tab_btn_msg1;
    //几个代表页面的常量
    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;
    int msgNum = 0;
    boolean isread;
    QBadgeView qBadgeView;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    private HomeFragment oneFragment = new HomeFragment();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        try {

            pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            mEditor = pref.edit();
            msgNum = pref.getInt("msgNum", 0);
            setContentView(R.layout.activity_main);
            mAdapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
            bindViews();
            tab_btn_home.setChecked(true);
            //Viewpager设置成3个缓存
            vpager.setOffscreenPageLimit(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindViews() {
        RadioGroup rg_tab_bar = findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        tab_btn_msg = findViewById(R.id.tab_btn_msg);
        tab_btn_msg1 = findViewById(R.id.tab_btn_msg1);
        tab_btn_my = findViewById(R.id.tab_btn_my);
        tab_btn_my1 = findViewById(R.id.tab_btn_my1);
        tab_btn_home1 = findViewById(R.id.tab_btn_home1);
        tab_btn_home = findViewById(R.id.tab_btn_home);
        vpager = findViewById(R.id.ly_content);
        vpager.setAdapter(mAdapter);
        vpager.setCurrentItem(0);
        vpager.addOnPageChangeListener(this);
        tab_btn_home1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                tab_btn_home.setChecked(true);
            }
        });
        tab_btn_msg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                tab_btn_msg.setChecked(true);
            }
        });
        tab_btn_my1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                tab_btn_my.setChecked(true);
            }
        });
    }


    @Override
    public void onCheckedChanged( RadioGroup rg_tab_bar, int checkedId ) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        switch (checkedId) {

            case R.id.tab_btn_home:
                vpager.setCurrentItem(PAGE_ONE);
                break;
            case (R.id.tab_btn_msg):
                vpager.setCurrentItem(PAGE_TWO);

                break;
            case (R.id.tab_btn_my):

                vpager.setCurrentItem(PAGE_THREE);
                break;
        }
        beginTransaction.commit();
    }

    @Override
    public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
    }

    @Override
    public void onPageSelected( int i ) {
    }

    @Override
    public void onPageScrollStateChanged( int state ) {
        //state的状态有三个，0表示什么都没做，1正在滑动，2滑动完毕
        if (state == 2) {
            switch (vpager.getCurrentItem()) {
                case PAGE_ONE:

                    tab_btn_home.setChecked(true);
                    break;
                case PAGE_TWO:
                    tab_btn_msg.setChecked(true);
                    break;
                case PAGE_THREE:
                    tab_btn_my.setChecked(true);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

}



