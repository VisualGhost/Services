package com.mysevice;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String DATA = "data";

    private int mCount;
    private TextView mCounterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCounterTextView = (TextView) findViewById(R.id.counter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                intent.putExtra(DATA, String.valueOf(mCount++));
                startService(intent);
            }
        });
    }

    private MyBroadcastReceiver mReceiver = new MyBroadcastReceiver();

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver.attachTextView(mCounterTextView);
        IntentFilter filter =
                new IntentFilter(MyService.BROADCAST);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mReceiver);
        mReceiver.detach();
    }
}
