package com.lishang.smartlock.smartlock.adapter;



import android.database.DataSetObservable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.lishang.smartlock.MapFragment;
import com.lishang.smartlock.smartlock.Activity.MainActivity;
import com.lishang.smartlock.smartlock.HomeFragment;
import com.lishang.smartlock.smartlock.MsgFragment;
import com.lishang.smartlock.smartlock.MyFragment;

import java.util.List;

public class HomeFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private final int PAGER_COUNT = 3;
    private HomeFragment mHomeFragment = null;
    private MsgFragment mMsgFragment = null;
    private MyFragment mMyFragment = null;
    private MapFragment mapFragment = null;
    private FragmentManager mfragmentManager;
    private List<Fragment> fragments;

    private DataSetObservable mObservable = new DataSetObservable();
    public HomeFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mapFragment = new MapFragment();
        mHomeFragment = new HomeFragment();
        mMsgFragment = new MsgFragment();
        mMyFragment = new MyFragment();

    }
    @Override
    public int getCount() {
        return PAGER_COUNT;
    }
    @Override
    public Object instantiateItem( ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        System.out.println("position Destory" + position);
        super.destroyItem(container, position, object);
        Fragment fragment = (Fragment) object;

    }
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case MainActivity.PAGE_ONE:
                fragment = mHomeFragment;
                break;
            case MainActivity.PAGE_TWO:
                fragment = mMsgFragment;
                break;
            case MainActivity.PAGE_THREE:
                fragment = mMyFragment;
                break;
        }
        return fragment;
    }
   @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
    }

