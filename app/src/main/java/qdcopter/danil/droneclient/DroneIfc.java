package qdcopter.danil.droneclient;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Danil on 5/27/2017.
 */

public class DroneIfc {

    public  boolean connected;
    public int timeout = 2000;

    DataInterface data;
    ViewInterface mView;


private  TimerTask tt;
    private  TimerTask ht;
    Client mClient;
    char endch = '$';
    char commch = '>';
    char msgch = '!';
    char datach = '#';
    private String TAG = "DroneIfc";
    private Timer sendDatatim ;
    private Timer handleDatatim ;

    DroneIfc(ViewInterface view,DataInterface mdata){
        mView = view;
        data = mdata;
        setTasks();
    }

    private void setTasks() {
        tt = new TimerTask() {
            @Override
            public void run() {
                SendData();
            }
        };
        ht  = new TimerTask() {
            @Override
            public void run() {

                if(mClient.newDataAval) {
                    String r = mClient.receivedData;
                    HandleData(r);
                    mClient.newDataAval = false;
                }
            }
        };
    }

    void Disconnect(){
        handleDatatim.cancel();
        sendDatatim.cancel();
        SendCommand("BREAK");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            e1.printStackTrace();
        }
        connected = false;


        mClient.upd = false;
        mClient.interrupt();
        mClient.Close();
    }

    void Connect(){
        try {
            mView.log(TAG,"Creating Client and connecting","Connecting...",3);
            mClient = new Client(mView,data);
            SendMessage("OK");
            mClient.start();
            mClient.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    //mView.log("Client",e.toString(),"Connection lost",1);
                    Disconnect();
                }
            });

        }catch (Exception e){
            mView.log(TAG,e.getMessage());
        }

        sendDatatim = new Timer();
        handleDatatim = new Timer();
        setTasks();
        sendDatatim.schedule(tt,100,20);
        handleDatatim.schedule(ht,120,20);
    }

    void HandleData(String r){
        char s ='o';
        ArrayList<Float> data;
        if (r!=null) s= r.charAt(0);
        if(s==commch){
            String comm = r.substring(1,r.indexOf(endch));
            mView.log("DRONE","GotCommand:"+comm,"Command:"+comm,0);
            switch (comm){
                case ("OK"):
                    mView.log(TAG,"Got response from Drone");
                case ("WAIT"):
                case ("disconn"):
                    Disconnect();
            }
        }else if(s==msgch){
            mView.log(r.substring(1,r.indexOf(endch)));

        }else if(s==datach){
           data = parseData(r);
            if(data.size()==12){
                mView.UpdateGraph(data);
            }else {
                mView.log("Drone","Data parsing error");
            }
        }else{
            mView.log(TAG,"Not understood Drone");
        }
    }
    ArrayList<Float> parseData(String s){
        ArrayList<Float> ret =new ArrayList<>();
        int i=0,j=1;
        String tmp ;
        String sub;
        while (true){
            j=s.indexOf(';',i+1);
            if(j==-1) return ret;
            sub = s.substring(i+1,j);
            try {
                ret.add(Float.parseFloat(sub));
            }
            catch (Exception e){
                return ret;
            }
            i=j;
        }
    }
    void SendData(){
        mClient.send = datach+data.ToSend() +endch;
    }
    void SendCommand(String comm){
        mClient.send = commch + comm +endch;
    }
    void SendMessage(String msg){
        mClient.send = msgch + msg +endch;
    }
}
