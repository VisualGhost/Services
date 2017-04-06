package com.mysevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;


public class MyBroadcastReceiver extends BroadcastReceiver {

    private StringBuilder mStringBuilder = new StringBuilder();

    private TextView mCounterTextView;

    void attachTextView(TextView textView) {
        mCounterTextView = textView;
    }

    void detach() {
        mCounterTextView = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String s = intent.getStringExtra(MyService.RESULT);
        setTextView(s);
        checkHandlingResponse(intent);
    }

    private void setTextView(String s) {
        mStringBuilder.append(s);
        mStringBuilder.append("\n");
        if (mCounterTextView != null) {
            mCounterTextView.setText(mStringBuilder.toString());
        }
    }

    private void checkHandlingResponse(Intent intent) {
        boolean isHandle = true;
        intent.putExtra(MyService.HANDLED, isHandle);
    }
}
