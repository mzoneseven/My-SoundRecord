package com.android.recorder;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

public class RecoderViewpagerAdapter extends PagerAdapter {

    private List<ImageView> list;
    boolean isRefesh = true;

    public RecoderViewpagerAdapter(List list) {
        // TODO Auto-generated constructor stub
        this.list = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        // TODO Auto-generated method stub
        // super.destroyItem(container, position, object);
        ((ViewPager) container).removeView(list.get(position));

    }

    @Override
    public Object instantiateItem(View container, int position) {
        // TODO Auto-generated method stub
        // return super.instantiateItem(container, position);
        ((ViewPager) container).addView(list.get(position), 0);
        return list.get(position);
    }
}
