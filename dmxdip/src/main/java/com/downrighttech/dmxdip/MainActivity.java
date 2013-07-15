//  Copyright 2013 Down Right Technical
//
//        Licensed under the Apache License, Version 2.0 (the "License");
//        you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software
//        distributed under the License is distributed on an "AS IS" BASIS,
//        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//        See the License for the specific language governing permissions and
//        limitations under the License.


package com.downrighttech.dmxdip;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.ArrayList;
import android.content.Context;


public class MainActivity
        extends FragmentActivity
        implements OnClickListener,
        TextWatcher,
        DialogManager.DialogManagerListener {
    private static final String TAG = "MainActivity";
    private EditText editText_Start;
    private EditText editText_Span;
    private ImageButton clearButton;
    private ToggleButton[] toggleButton;
    private ListView listView;
    private ArrayList<Integer> mAddressArray;
    private ArrayList<String> mBitArray;
    private DMXAdapter arrayAdapter;
    private Drawable button_on;
    private Drawable button_off;
    private Vibrator vib;
    private ShareActionProvider mShareActionProvider;
    private Boolean mSkipProcess;
    private SharedPreferences mSharedPreferences;
    private int mCurrentTheme;
    private int mLastAddress;
    private boolean mOffset;
    private Intent mShareIntent;
    private ThemeManager mThemeManager;
    private DialogManager mDialogManager;


    // Constants
    private final int ADDRESS_BUTTONS = 9;
    private final int mFirstAddress = 1;
    private int VIB_TIME = 10;
    private final String FILENAME = "AndroDip.html";
    private final int DIALOG_RECOUNT = 5;


    //TODO: Delete share_text.txt if exists

    // TODO: Make this a fragment?
    private final int BUTTON_TEXT_ADDR[] = {
            R.string.dip_addr_1,
            R.string.dip_addr_2,
            R.string.dip_addr_3,
            R.string.dip_addr_4,
            R.string.dip_addr_5,
            R.string.dip_addr_6,
            R.string.dip_addr_7,
            R.string.dip_addr_8,
            R.string.dip_addr_9};
    private final int BUTTON_TEXT_SW[] = {
            R.string.dip_sw_1,
            R.string.dip_sw_2,
            R.string.dip_sw_3,
            R.string.dip_sw_4,
            R.string.dip_sw_5,
            R.string.dip_sw_6,
            R.string.dip_sw_7,
            R.string.dip_sw_8,
            R.string.dip_sw_9};

    public MainActivity() {
        mSkipProcess = true;
    }

    //@SuppressWarnings("unused")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeManager = new ThemeManager(VERSION.SDK_INT);

        super.onCreate(savedInstanceState);
        // load preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Load Theme from preferences
        mThemeManager.setTheme(Integer.parseInt(mSharedPreferences.getString("pref_theme", "0")));
        setTheme (mThemeManager.getTheme());

        setContentView(R.layout.activity_main);

        //Load Resources
        button_on = getResources().getDrawable(R.drawable.button_on);
        button_off = getResources().getDrawable(R.drawable.button_off);

        // Load Interface Items
        editText_Start = (EditText) findViewById(R.id.editText_Start);
        editText_Span = (EditText) findViewById(R.id.EditText_Span);
        clearButton = (ImageButton) findViewById(R.id.imageButton);
        toggleButton = new ToggleButton[9];
        toggleButton[0] = (ToggleButton) findViewById(R.id.ToggleButton01);
        toggleButton[1] = (ToggleButton) findViewById(R.id.ToggleButton02);
        toggleButton[2] = (ToggleButton) findViewById(R.id.ToggleButton03);
        toggleButton[3] = (ToggleButton) findViewById(R.id.ToggleButton04);
        toggleButton[4] = (ToggleButton) findViewById(R.id.ToggleButton05);
        toggleButton[5] = (ToggleButton) findViewById(R.id.ToggleButton06);
        toggleButton[6] = (ToggleButton) findViewById(R.id.ToggleButton07);
        toggleButton[7] = (ToggleButton) findViewById(R.id.ToggleButton08);
        toggleButton[8] = (ToggleButton) findViewById(R.id.ToggleButton09);
        listView = (ListView) findViewById(R.id.listView1);

        // Load ArrayList
        mAddressArray = new ArrayList<Integer>();
        mBitArray = new ArrayList<String>();
        mAddressArray.ensureCapacity(512);
        mBitArray.ensureCapacity(512);


        // Load Fonts
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RobotoSlab-Regular.ttf");

        // Setup Fonts
        editText_Start.setTypeface(tf);
        editText_Span.setTypeface(tf);
        for (int i = 0; i < ADDRESS_BUTTONS; i++)
            toggleButton[i].setTypeface(tf);

        //Setup setHapticFeedbackEnabled
        vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);

        // Events
        editText_Start.addTextChangedListener(this);
        editText_Span.addTextChangedListener(this);
        clearButton.setOnClickListener(this);

        // What's New Count
        int dialogCount = mSharedPreferences.getInt("pref_startup_dialog", 0);

        // If this is a new version, show the Dialog again
        String lastRunVersion = mSharedPreferences.getString("pref_version","");
        String savedString = getString(R.string.version);
        if (!lastRunVersion.equals(savedString)){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("pref_version", getString(R.string.version));
            editor.commit();
            dialogCount = 0;
        }

        // If the dialog is due to be shown
        Log.v(TAG, "onCreate dialogCount:"+dialogCount);
        if (dialogCount == 0) {
            mDialogManager = new DialogManager(this);
            mDialogManager.setmIcon(R.drawable.ic_launcher);
            mDialogManager.setTitle(getText(R.string.app_name) + " - " + getText(R.string.version));
            mDialogManager.setMessage(R.string.dialog_about_message);
            mDialogManager.setPositiveButton(R.string.dialog_yes);
            mDialogManager.setNeutralButton(R.string.dialog_maybe);
            mDialogManager.show(getSupportFragmentManager(),"What's New");
        }
        else if (dialogCount > 0) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("pref_startup_dialog", --dialogCount);
            editor.commit();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i != SCROLL_STATE_IDLE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();

        // Load the correct Theme.
        if (mThemeManager.getInt() != Integer.parseInt(mSharedPreferences.getString("pref_theme", "0"))) {
            this.finish();
            this.startActivity(new Intent(this, this.getClass()));
            return;
        }

        // Load ArrayAdapter
        arrayAdapter = new DMXAdapter(this, mAddressArray, mBitArray);

        //Assign ArrayAdapter to listView
        listView.setAdapter(arrayAdapter);

        //TODO: Make these the correct type, and make a pref class to take care of all of this.
        String pref_vib = mSharedPreferences.getString("pref_vib", "0");
        String pref_addr = mSharedPreferences.getString("pref_addr", "0");
        String pref_offset2 = mSharedPreferences.getString("pref_offset2", "0");

        if (pref_offset2.equals("0")) {
            mOffset = false;
            mLastAddress = 511;
        } else {
            mOffset = true;
            mLastAddress = 512;
        }

        int pref_start = mSharedPreferences.getInt("pref_start", mFirstAddress);
        int pref_span = mSharedPreferences.getInt("pref_span", 1);

        if (pref_start > mLastAddress)
            pref_start = mLastAddress;

        // Load Vibration from preferences
        if (pref_vib.equals("0"))
            VIB_TIME = 10;
        else if (pref_vib.equals("1"))
            VIB_TIME = 30;
        else if (pref_vib.equals("2"))
            VIB_TIME = 90;

        //Load Button Text
        if (pref_addr.equals("1")) {
            for (int i = 0; i < ADDRESS_BUTTONS; i++) {
                toggleButton[i].setText(getString(BUTTON_TEXT_ADDR[i]));
                toggleButton[i].setTextOn(getString(BUTTON_TEXT_ADDR[i]));
                toggleButton[i].setTextOff(getString(BUTTON_TEXT_ADDR[i]));
                toggleButton[i].setTextSize(16);
            }
        } else {
            for (int i = 0; i < ADDRESS_BUTTONS; i++) {
                toggleButton[i].setText(getString(BUTTON_TEXT_SW[i]));
                toggleButton[i].setTextOn(getString(BUTTON_TEXT_SW[i]));
                toggleButton[i].setTextOff(getString(BUTTON_TEXT_SW[i]));
                toggleButton[i].setTextSize(36);
            }
        }

        // Span
        if (pref_span == 1)
            editText_Span.setText("");
        else
            editText_Span.setText(Integer.toString(pref_span));

        // Start
        if (pref_start <= mFirstAddress) {
            editText_Start.clearComposingText();
        } else {
            editText_Start.setText(Integer.toString(pref_start));
        }
        mSkipProcess = false;
        updateArray(pref_start, pref_span);
        updateButtons();
        buildChart();
        //buildIntent();
    }


    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("span", editText_Span.getText().toString());
        outState.putString("start", editText_Start.getText().toString());
        super.onSaveInstanceState(outState);
        Log.v("lifeCycle", "onSaveInstanceState-" + outState.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v("lifeCycle", "onRestoreInstanceState-" + savedInstanceState.toString());
        //if (savedInstanceState != null){
        editText_Span.setText(savedInstanceState.get("span").toString());
        editText_Start.setText(savedInstanceState.get("start").toString());
        super.onRestoreInstanceState(savedInstanceState);
        //}
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(TAG, "onCreateOptionsMenu-" + menu.toString());
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (VERSION.SDK_INT >= 14) {
            //get xlm menu item (
            MenuItem shareMenuItem = menu.findItem(R.id.menu_share);

            // get Action Provider from XML
            mShareActionProvider = (ShareActionProvider) shareMenuItem.getActionProvider();

            //Setup shareIntent
            mShareIntent = new Intent(Intent.ACTION_SEND);
            mShareIntent.setType("text/plain");

            //buildIntent();
            //mShareActionProvider.setShareIntent(mShareIntent);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected");
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuSettings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuAbout:
                mDialogManager = new DialogManager(this);

                mDialogManager.setmIcon(R.drawable.ic_launcher);
                mDialogManager.setMessage(R.string.dialog_about_message);
                mDialogManager.setTitle(getText(R.string.app_name) + " - " + getText(R.string.version));
                mDialogManager.setPositiveButton(R.string.dialog_yes);
                mDialogManager.setNeutralButton(R.string.dialog_close);
                mDialogManager.show(getSupportFragmentManager(),"About");

                return true;
        }
        return false;
    }

    /**
     * Dialog Callback for a positive click
     * @param dialog a DialogFragment
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pref_startup_dialog", -1);
        editor.commit();

        // Open the Market
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.downrighttech.dmxdip"));
        startActivity(intent);

    }

    /**
     * Dialog Callback for a negative click
     * @param dialog a DialogFragment
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    /**
     * Dialog Callback for a neutral click
     * @param dialog a DialogFragment
     */
    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        int count = mSharedPreferences.getInt("pref_startup_dialog",0);
        Log.v(TAG,"onDialogNeutralClick");

        if (count==0){
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("pref_startup_dialog", 5);
            editor.commit();
        }
    }

    private String swapBin(int input, int length) {
        String bin = Integer.toBinaryString(input);
        int pad = length - bin.length();

        StringBuilder output = new StringBuilder(length);
        output.append(bin).reverse();
        for  (int i = 0 ; i < pad ; i++)
            output.append("0");
        return output.toString();
    }

    @Override
    public void onClick(View v) {
        int start = 0;

        // Clear Button Presses
        switch (v.getId()) {
            case R.id.imageButton:              //Clear Button
                vib.vibrate(VIB_TIME);
                mSkipProcess = true;            // don't build chart twice on clear.
                editText_Start.setText("");
                mSkipProcess = false;
                editText_Span.setText("");
                break;
            case R.id.ToggleButton01:
            case R.id.ToggleButton02:
            case R.id.ToggleButton03:
            case R.id.ToggleButton04:
            case R.id.ToggleButton05:
            case R.id.ToggleButton06:
            case R.id.ToggleButton07:
            case R.id.ToggleButton08:
            case R.id.ToggleButton09:
                vib.vibrate(VIB_TIME);
                if (toggleButton[0].isChecked())
                    start += 1;
                if (toggleButton[1].isChecked())
                    start += 2;
                if (toggleButton[2].isChecked())
                    start += 4;
                if (toggleButton[3].isChecked())
                    start += 8;
                if (toggleButton[4].isChecked())
                    start += 16;
                if (toggleButton[5].isChecked())
                    start += 32;
                if (toggleButton[6].isChecked())
                    start += 64;
                if (toggleButton[7].isChecked())
                    start += 128;
                if (toggleButton[8].isChecked())
                    start += 256;
                if (mOffset)
                    start += 1;
                editText_Start.setText(String.valueOf(start));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "afterTextChanged" + s.toString());
        int start = mFirstAddress;
        int span = 1;

        // Check Start has a length
        if (editText_Start.length() != 0) {
            int currentStart = Integer.parseInt(editText_Start.getText().toString());
            //if greater then last then make it that.
            if (currentStart > mLastAddress) {
                editText_Start.setText(Integer.toString(mLastAddress));
                editText_Start.selectAll();
            }
            if (currentStart < mFirstAddress)
                editText_Start.setText(Integer.toString(mFirstAddress));
            start = Integer.parseInt(editText_Start.getText().toString());
        }

        // Check Span has a length
        if (editText_Span.length() != 0) {
            int currentSpan = Integer.parseInt(editText_Span.getText().toString());
            if (currentSpan > mLastAddress) {
                editText_Span.setText(Integer.toString(mLastAddress));
                editText_Span.selectAll();
            }
            if (Integer.parseInt(editText_Span.getText().toString()) <= 0) {
                editText_Span.setText("1");
                editText_Span.selectAll();
            }
            span = Integer.parseInt(editText_Span.getText().toString());
        }

        if (mSkipProcess)
            return;
        Log.v(TAG, "input: " + start + "." + span);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("pref_start", start);
        editor.putInt("pref_span", span);
        editor.commit();

            updateArray(start, span);
            updateButtons();
            buildChart();
            buildIntent();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void updateArray(int start, int span){
        mAddressArray.clear();
        mBitArray.clear();

        int offset = 0;
        if (mOffset)
            offset = 1;

        for (int i = start; i <= mLastAddress; i += span) {
            mAddressArray.add(i);
            mBitArray.add(swapBin(i - offset, 9));
        }
    }

    public void updateButtons() {
        String bin = mBitArray.get(0);

        for (int i = 0 ; i < bin.length() ; i++) {
            if (bin.charAt(i) == '1') {
                toggleButton[i].setBackgroundDrawable(button_on);
                toggleButton[i].setChecked(true);
            } else {
                toggleButton[i].setBackgroundDrawable(button_off);
                toggleButton[i].setChecked(false);
            }
        }
    }

    public void buildChart() {
        arrayAdapter.notifyDataSetChanged();
        listView.setSelection(0);
    }

    public void buildIntent(){
        if (mShareIntent == null)
            return;
        int pref_start = mSharedPreferences.getInt("pref_start", mFirstAddress);
        int pref_span = mSharedPreferences.getInt("pref_span", 1);

        mShareIntent.putExtra(Intent.EXTRA_SUBJECT, "AndroDip - Start:" + pref_start + " Span:" + pref_span);

        buildHTML bh = new buildHTML(mAddressArray, mBitArray);

        mShareIntent.putExtra(Intent.EXTRA_TEXT, bh.getText());
        if (VERSION.SDK_INT >= 14)
            mShareActionProvider.setShareIntent(mShareIntent);
    }
}