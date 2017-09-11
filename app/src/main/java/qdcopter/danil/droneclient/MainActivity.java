package qdcopter.danil.droneclient;

import android.content.Context;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    ViewInterface mView;
    DataInterface mData;
    boolean Connected  = false;

    private static final String TAG = "Danil";
    DroneIfc mDrone;
    private boolean waitingForConn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = findViewById(android.R.id.content);
        Log.e("Danil", "initializing");
        //Setting Interfaces
        // >> View Ifc
        mView = new ViewInterface(this);
        mView.ToggleConsole();
        // >> Data Ifc
        mData = new DataInterface();
        mData.Accel = new SensorInterface(this);
        mData.ViewIfc = mView;
        mData.UpdateConnectionData_fromView();
        // >> Drone Ifc
        mDrone = new DroneIfc(mView,mData);

        GridLayout but = (GridLayout) findViewById(R.id.ShowConsole);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.ToggleConsole();
            }
        });
        Button grB = (Button) findViewById(R.id.green);
        grB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(v);
            }
        });
        try {
            HandleWifi();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void Click(View v) {
        mView.log("clicked");
        if(mView.ConnOpen){
            mDrone.Disconnect();
            mView.log(TAG,"Closing connection by button","Disconnected",3);
        }
        else {
            mData.UpdateConnectionData_fromView();
            if(Connected&!waitingForConn) {
                try {
                    HandleWifi();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDrone.Connect();
            }
            else{mView.log("Please first connect to internet");}// /
        }

    }

    private void HandleWifi() throws InterruptedException {

        Thread chwtr = new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager cm =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                boolean isconn =  netInfo != null && netInfo.isConnectedOrConnecting();
                if (!isconn) {
                    Connected = false;
                    mView.log("Wifi","You aren't connected.","No Connection",1);
                    mView.log("Wifi","Waiting for Connection.");
                    while (!isconn){
                        waitingForConn = true;
                        netInfo = cm.getActiveNetworkInfo();
                        isconn =  netInfo != null && netInfo.isConnectedOrConnecting();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                waitingForConn = false;
                Connected  = isconn;
                String ip = DataInterface.LocalIP();
                mView.log("Wifi","Your IP address:"+ip);
            }
        });

        chwtr.start();

    }




}
