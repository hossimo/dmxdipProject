package com.downrighttech.dmxdip;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by aheadley on 13-06-10.
 */
public class buildHTML {
    private ArrayList<Integer> mAddress;
    private ArrayList<String> mBits;


    buildHTML(ArrayList<Integer> address, ArrayList<String> bits){
        mAddress = address;
        mBits = bits;
    }

    public String getHTML(){
        String str;
        String zebra = "odd";
        str = "<html>";
        str += "<body>";
        for (int i = 0 ; i < mAddress.size() ; i ++) {
            str += "<tr class=" + zebra + "><td>" + mAddress.get(i) + "</td><td>" + mBits.get(i) + "</td></tr>";

            if (zebra.equals("odd"))
                zebra = "even";
            else
                zebra = "odd";
        }

        str += "</body>";
        str += "</html>";
        return str;
    }

    public String getText(){
        int count = mAddress.size();

        StringBuilder sb = new StringBuilder((count+2)*16);

        sb.append("DMX : ADDRESS  \n"); //16
        sb.append("--- : ---------\n"); //16

        for (int i = 0; i < count ; i++){
            int pad = 3 - mAddress.get(i).toString().length();
            for (int j = 0; j < pad; j++)
                sb.append("0");
            sb.append(mAddress.get(i).toString());
            sb.append(" : ");
            sb.append(mBits.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
