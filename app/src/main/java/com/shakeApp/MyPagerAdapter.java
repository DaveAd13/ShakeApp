package com.shakeApp;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;


class MyPagerAdapter extends PagerAdapter
{

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<String> step_list = Arrays.asList("1.Press \"Select action\" button",
            "2.Select action from the list.",
            "3.If You picked \"Run Installed App\" click on appeared button.",
            "4.Select app from the list.",
            "5.Press \"Record Move\" button.",
            "6.Press \"Start\" button and start moving the phone.",
            "7.Press \"Stop\" exact in the end of movement.",
            "8.Repeat recorded movement to use action.");
    private int[] background = {
            R.drawable.step1,
            R.drawable.step2,
            R.drawable.step3,
            R.drawable.step4,
            R.drawable.step5,
            R.drawable.step6,
            R.drawable.step7,
            R.drawable.splash};


    MyPagerAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {

        final View itemView = mLayoutInflater.inflate(R.layout.how_to_use_layout, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        TextView textView = (TextView) itemView.findViewById(R.id.textView2);
        textView.setText(step_list.get(position));
        imageView.setImageResource(background[position]);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view)
    {
        collection.removeView((View) view);
    }

    @Override
    public int getCount()
    {
        return step_list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

}