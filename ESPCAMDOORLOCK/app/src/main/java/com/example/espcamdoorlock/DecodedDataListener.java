package com.example.espcamdoorlock;

import android.graphics.Bitmap;

public interface DecodedDataListener
{
    public void onConnect(boolean flg);
    public void frameData(Bitmap frame,boolean flg);
    public void relayData(boolean flg);
    public void flashData(boolean flg);
}
