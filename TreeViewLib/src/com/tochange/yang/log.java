package com.tochange.yang;

import android.util.Log;

public class log
{

    private String nTag;

    private boolean mPrintLog;

    public void intLog(String tag, boolean pritLog)
    {
        nTag = tag;
        mPrintLog = !pritLog;
    }

    public void e(String msg)
    {
        if (mPrintLog)
            Log.e(nTag, msg);
    }

    public void w(String msg)
    {
        if (mPrintLog)
            Log.w(nTag, msg);
    }

    public void i(String msg)
    {
        if (mPrintLog)
            Log.i(nTag, msg);
    }

    public void d(String msg)
    {
        if (mPrintLog)
            Log.d(nTag, msg);
    }

    public void v(String msg)
    {
        if (mPrintLog)
            Log.v(nTag, msg);
    }
}