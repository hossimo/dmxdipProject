package com.downrighttech.dmxdip;

/**
 * Created by aheadley on 13-06-22.
 */
public class ThemeManager {
    private int mCurrentTheme = 0;
    private int mCurrentAPI = 0;
    private int mCurrentInt = 0;
    public ThemeManager(int currentAPI){
        mCurrentAPI = currentAPI;
    }

    public void setTheme(int theme) {
        mCurrentInt = theme;
        if (theme == 0) {
            if (mCurrentAPI >= 11)
                mCurrentTheme = android.R.style.Theme_Holo_Light;
            else
                mCurrentTheme = android.R.style.Theme_Light;
        }
        else
                mCurrentTheme = android.R.style.Theme_Holo;
    }

    public int getTheme() {
        return mCurrentTheme;
    }

    public int getInt () {
        return mCurrentInt;
    }
}
