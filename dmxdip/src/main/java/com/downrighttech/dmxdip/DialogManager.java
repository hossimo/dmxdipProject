package com.downrighttech.dmxdip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

public class DialogManager extends DialogFragment implements DialogInterface.OnClickListener {

    public interface DialogManagerListener {
        public void onDialogPositiveClick (DialogFragment dialog);
        public void onDialogNegativeClick (DialogFragment dialog);
        public void onDialogNeutralClick (DialogFragment dialog);
    }

    private CharSequence mMessage;
    private CharSequence mPositiveButton;
    private CharSequence mNegativeButton;
    private CharSequence mNeutralButton;
    private CharSequence mTitle;
    private Context mContext;
    private int mIcon;
    private static final String TAG = "DialogManager";
    private AlertDialog.Builder mBuilder;
    private int mResult;
    private DialogManagerListener mListener;

    public DialogManager() {
    }

    public DialogManager(Context context) {
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //verify that host implements the callback interface
        try {
            //Instantiate the Listener to send events to the host
            mListener = (DialogManagerListener) activity;
        }
        catch (ClassCastException e){
            // oops
            throw new ClassCastException(activity.toString() + " must implement DialogManagerListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle(mTitle);
        mBuilder.setMessage(mMessage);
        mBuilder.setPositiveButton(mPositiveButton, this);
        mBuilder.setNegativeButton(mNegativeButton, this);
        mBuilder.setNeutralButton(mNeutralButton, this);
        mBuilder.setIcon(mIcon);

        return  mBuilder.create();
    }


    // Title
    public void setTitle(CharSequence string) {mTitle = string;}
    public void setTitle(int stringResource) {
        if (mContext==null)
            return;
        mTitle = mContext.getString(stringResource);
    }

    // Message
    public void setMessage(CharSequence message) {mMessage = message;}
    public void setMessage(int stringResource) {
        if (mContext==null)
            return;
        mMessage = mContext.getString(stringResource);
    }

    // Positive
    public void setPositiveButton(CharSequence text){mPositiveButton = text;}
    public void setPositiveButton(int stringResource){
        if (mContext==null)
            return;
        mPositiveButton = mContext.getString(stringResource);
    }

    // Negative
    public void setNegativeButton(CharSequence text){mNegativeButton = text;}
    public void setNegativeButton(int stringResource){
        if (mContext==null)
            return;
        mNegativeButton = mContext.getString(stringResource);
    }

    // Neutral
    public void setNeutralButton(CharSequence text){mNeutralButton = text;}
    public void setNeutralButton(int stringResource){
        if (mContext==null)
            return;
        mNeutralButton = mContext.getString(stringResource);
    }

    public void setmIcon(int iconId) {mIcon = iconId;}

    public int getResult() {
        return mResult;
    }

    public void reset(){
        mBuilder.setMessage(0);
        mBuilder.setPositiveButton(null, null);
        mBuilder.setNegativeButton(null, null);
        mBuilder.setNeutralButton(null, null);
        mBuilder.setTitle(null);
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        Log.v (TAG,"OnClick: "+dialog.toString() + " : " + id);
        mResult = id;
      switch (id) {
          case DialogInterface.BUTTON_POSITIVE:
              mListener.onDialogPositiveClick(this);
              break;
          case DialogInterface.BUTTON_NEUTRAL:
              mListener.onDialogNeutralClick(this);
              break;
          case DialogInterface.BUTTON_NEGATIVE:
              mListener.onDialogNegativeClick(this);
              break;
      }
    }
}