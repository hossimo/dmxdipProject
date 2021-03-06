package com.downrighttech.dmxdip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;


//TODO: Suppressing NewApi for a call to setBackground >= 16 need to make a default
// perhaps set color?
@SuppressLint("NewApi")
public class DMXAdapter extends BaseAdapter {
    private ArrayList<Integer> mStart;
    private ArrayList<String> mBits;
    private final Context mContext;
    private Typeface tf;
    private SharedPreferences mSharedPreferences;
    private ViewHolder mHolder;
    private String pref_addr;
    private int mSpan;
    private boolean mOffset;

    private final int DMX_LENGTH = 512;
    //private String pref_offset2;
    //private boolean mOffset;

    public DMXAdapter(Context context, ArrayList<Integer> addressArray, ArrayList<String> bitArray) {
        mContext = context;
        mStart = addressArray;
        mBits = bitArray;
        tf = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab-Regular.ttf");
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        pref_addr = mSharedPreferences.getString("pref_addr", "0");
        //pref_offset2 = mSharedPreferences.getString("pref_offset2", "0");
        //mOffset = false;

    }

    @Override
    public int getCount() {
        return mStart.size();
    }

//    public void setOffset(boolean offset) {
//        mOffset = offset;
//        Log.v("setOffset", Boolean.toString(offset));
//    }

    @Override
    public Object getItem(int position) {
        return mStart.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {

        mHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dmx_adapter_layout, null);

            mHolder.tva = new TextView[9];
            mHolder.detail = (View) convertView.findViewById(R.id.detail);
            mHolder.address = (TextView) convertView.findViewById(R.id.textView1);
            mHolder.tva[0] = (TextView) convertView.findViewById(R.id.ToggleButton01);
            mHolder.tva[1] = (TextView) convertView.findViewById(R.id.ToggleButton02);
            mHolder.tva[2] = (TextView) convertView.findViewById(R.id.ToggleButton03);
            mHolder.tva[3] = (TextView) convertView.findViewById(R.id.ToggleButton04);
            mHolder.tva[4] = (TextView) convertView.findViewById(R.id.ToggleButton05);
            mHolder.tva[5] = (TextView) convertView.findViewById(R.id.ToggleButton06);
            mHolder.tva[6] = (TextView) convertView.findViewById(R.id.ToggleButton07);
            mHolder.tva[7] = (TextView) convertView.findViewById(R.id.ToggleButton08);
            mHolder.tva[8] = (TextView) convertView.findViewById(R.id.ToggleButton09);
            mHolder.error = (TextView) convertView.findViewById(R.id.error);

            mHolder.button_on = mContext.getResources().getDrawable(R.drawable.button_on);
            mHolder.button_off = mContext.getResources().getDrawable(R.drawable.button_off);
            mHolder.button_on.setAlpha(130);
            mHolder.button_off.setAlpha(130);

            convertView.setTag(mHolder);
        } else
            mHolder = (ViewHolder) convertView.getTag();

        String bin = mBits.get(index);

        // Set Fonts for Address
        mHolder.address.setTypeface(tf);
        for (int i=0 ; i <= 8 ; i++)
            mHolder.tva[i].setTypeface(tf);

        // If displaying addresses resize fonts and update text
        if (! pref_addr.equals("0")) {
            int display = 1;
            mHolder.address.setTextSize(20);
            for (int i=0 ; i <= 8 ; i++){
                mHolder.tva[i].setTypeface(tf);
                mHolder.tva[i].setTextSize(11);
                mHolder.tva[i].setText(Integer.toString(display));
                display = display << 1;
            }
        }

        mHolder.address.setGravity(Gravity.RIGHT);
        mHolder.address.setText(String.format("%3s", mStart.get(index)) + ":");

        char test;
        int i = 0;

        if (VERSION.SDK_INT >= 16){
            for ( ; i < bin.length() ; i++) {
                test = bin.charAt(i);
                if (test == '1')
                    mHolder.tva[i].setBackground(mHolder.button_on);
                else
                    mHolder.tva[i].setBackground(mHolder.button_off);
            }
            for ( ; i < 9 ; i++)
                    mHolder.tva[i].setBackground(mHolder.button_off);
        }
        else {
            for ( ; i < bin.length() ; i++) {
                test = bin.charAt(i);
                if (test == '1')
                    mHolder.tva[i].setBackgroundDrawable(mHolder.button_on);
                else
                    mHolder.tva[i].setBackgroundDrawable(mHolder.button_off);
            }
            for ( ; i < 9 ; i++)
                mHolder.tva[i].setBackgroundDrawable(mHolder.button_off);
        }

        if (mStart.get(index) + mSpan - 1 > DMX_LENGTH) {
            int over = (mStart.get(index) + mSpan - 1) - DMX_LENGTH;
            mHolder.error.setVisibility(View.VISIBLE);
            mHolder.error.setText(mStart.get(index) + " Missing " + over + " channel" + (over>1?"s":""));
            mHolder.detail.setBackgroundColor(mContext.getResources().getColor(R.color.background_error));
        }
        else {
            mHolder.error.setVisibility(View.GONE);
            mHolder.detail.setBackgroundColor(android.R.color.transparent);
        }
        return convertView;
    }

    public void setSpan(int span){
        mSpan = span;
    }

    public void setOffset (boolean offset){
        mOffset = offset;
    }

    static class ViewHolder {
        TextView address;
        TextView tva[];
        Drawable button_on;
        Drawable button_off;
        TextView error;
        View detail;
    }
}
