package com.example.vasic171.gcmtestv1.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.vasic171.gcmtestv1.R;
import com.example.vasic171.gcmtestv1.services.GcmRegisterService;
import com.example.vasic171.gcmtestv1.views.MyNotification;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Tutorial: Google Cloud Messaging
 * http://www.akexorcist.com/2016/03/how-to-use-google-cloud-messaging-on-android.html
 *
 * POST Request
 * https://gcm-http.googleapis.com/gcm/send
 * Authorization : key=YOUR_API_KEY
 * Content-Type : application/json
 * Body - 1) Data   or
 *      - 2) Notification
 *
 * 1) Data
 *
 * {
 "to" : "TARGET_DEVICE_TOKEN",
 "data" : {
 "name" : "Akexorcist",
 "id" : "0001",
 "score" : 635709,
 "photo" : "/user_photo/0001"
 }
 }
 *
 * OR
 *
 * 2) Notification (for Push Notification)
 *
 * {
 "to" : "TARGET_DEVICE_TOKEN",
 "notification" : {
 "body" : "Hello Android User",
 "title" : "Akexorcist",
 "icon" : "ic_launcher"
 }
 }
 *
 *
 * (Many Devices)
 *
 * {
 "registration_ids" : ["DEVICE_1", "DEVICE_2", "DEVICE_3"],
 "notification" : {
 "body" : "Hello Android User",
 "title" : "Akexorcist",
 "icon" : "ic_launcher"
 }
 }
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean isReceiverRegistered;

    Button btnShowNoti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver();

        if (checkPlayServices()) {
            registerGcm();
        } else {
            Toast.makeText(getApplicationContext(),
                    "This device hasn't Google Play Services",
                    Toast.LENGTH_SHORT).show();
        }

        setupButton();
    }

    private void setupButton() {
        btnShowNoti = (Button) findViewById(R.id.btn_show_noti);
        btnShowNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyNotification createNotification = new MyNotification(MainActivity.this);
                createNotification.callLocalNotification();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    private void registerGcm() {
        Intent intent = new Intent(this, GcmRegisterService.class);
        startService(intent);
    }

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean sentToken = sharedPreferences.getBoolean(GcmRegisterService.SENT_TOKEN_TO_SERVER, false);
            Log.d("onReceive", "Sent Token: " + sentToken);
        }
    };

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(GcmRegisterService.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
    }
}
