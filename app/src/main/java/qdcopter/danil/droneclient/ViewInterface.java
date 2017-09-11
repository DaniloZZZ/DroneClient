package qdcopter.danil.droneclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danil on 5/2/2017.
 */

public class ViewInterface {

    private final TextView StatusField;
    public ScrollView ScrollLog;
    public float FilterK;
    private RelativeLayout Console;
    private TextView LogField;
    private GridLayout ConsoleButton;

    private TextView AddrField;
    private TextView PortField;
    private Button ConnButt;
    private List<Plot> Plots = new ArrayList<>();

    public MainActivity Contx;

    public boolean ConnOpen = false;

    private static String LOGTAG = "Danil";
    private boolean ConsOpen = true;

    public ViewInterface(Context main){
        Contx = (MainActivity)main;
        MainActivity m = Contx;
        Console = (RelativeLayout)m.findViewById(R.id.Console);
        ScrollLog = (ScrollView) m.findViewById(R.id.ScrollLog);
        ConsoleButton =(GridLayout)  m.findViewById(R.id.ShowConsole);
        LogField  = (TextView) m.findViewById(R.id.log);
        StatusField = (TextView) m.findViewById(R.id.StatusText);

        AddrField = (TextView) m.findViewById(R.id.IpText);
        PortField = (TextView) m.findViewById(R.id.PortText);
        ConnButt  = (Button) m.findViewById(R.id.green) ;
        m.findViewById(R.id.SetFilterK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetFilterK();
            }
        });

        SetFilterK();

        AddrField.setText(DataInterface.getSavedIP(Contx));

        Plots.add((Plot) m.findViewById(R.id.AccelGraph));
        Plots.add((Plot) m.findViewById(R.id.GyroGraph));
        Plots.add((Plot) m.findViewById(R.id.AccelTraj));
        Plots.add((Plot) m.findViewById(R.id.GyroTraj));
    }

    private void SetFilterK() {
        try {
            FilterK = Float.parseFloat(((EditText)Contx.findViewById(R.id.FilterK)).getText().toString());
        } catch (Exception e){
            Log.e("Danil",e.getMessage());
        }
    }

    public String PortText(){
        return PortField.getText().toString();
    }
    public String AddrText(){
        return AddrField.getText().toString();
    }

    public void log(final String TAG, final String text, final  String status){
        Log.e(LOGTAG,TAG+">>"+text);
        LogField.post(new Runnable() {
            @Override
            public void run() {
                String tx = LogField.getText().toString();
                String ms = "\n"+TAG+">> "+text;
                if(tx.length()>2000){tx = tx.substring(ms.length(),tx.length());}
                LogField.setText(tx+ms);
                StatusField.setText(status);
            }
        });
    }
    public void log(final String TAG, final String text, final  String status, final int type){ // 0-OK; 1-error; 2-info
        Log.e(LOGTAG,TAG+">>"+text);
        LogField.post(new Runnable() {
            @Override
            public void run() {
                String tx = LogField.getText().toString();
                String ms = "\n"+TAG+">> "+text;
                if(tx.length()>2000){tx = tx.substring(ms.length(),tx.length());}
                LogField.setText(tx+ms);
                if(type ==1) {
                    StatusField.setTextColor(ContextCompat.getColor(Contx,R.color.StatusError));
                }else if(type == 0){
                    StatusField.setTextColor(ContextCompat.getColor(Contx,R.color.StatusOk));
                }else{
                    StatusField.setTextColor(ContextCompat.getColor(Contx,R.color.StatusInfo));
                }
                StatusField.setText(status);
            }
        });
    }
    public void log(final String TAG, final String text){
        Log.e(LOGTAG,TAG+">>"+text);
        LogField.post(new Runnable() {
            @Override
            public void run() {
                String tx = LogField.getText().toString();
                String ms = "\n"+TAG+">> "+text;
                if(tx.length()>2000){tx = tx.substring(ms.length(),tx.length());}
                LogField.setText(tx+ms);
            }
        });

    }


    public void UpdateGraph(final ArrayList<Float> val){
        PostDot(Plots.get(0),-val.get(1),-val.get(0),0);
        PostDot(Plots.get(0), val.get(3), val.get(4),1);
        PostDot(Plots.get(1),-val.get(6),-val.get(7),0);
        PostDot(Plots.get(1),-val.get(8),-val.get(9),1);
        PostDot(Plots.get(1),-val.get(10),-val.get(11),2);

        PostValue(Plots.get(2),val.get(0),0);
        PostValue(Plots.get(2),val.get(3),1);
        PostValue(Plots.get(3),val.get(6),0);
        PostValue(Plots.get(3),val.get(8),1);
        PostValue(Plots.get(3),val.get(10),2);
        //PostValue(Plots.get(2),val.get(1),1);

    }
    private void PostValue(final Plot g , final Float val,final int i){
        g.post(new Runnable() {
            @Override
            public void run() {
                g.Graphs.get(i).addVal(val);
                g.invalidate();
            }
        });

    }
    private void PostDot(final Plot g , final Float  v , final Float a,final int i){
        g.post(new Runnable() {
            @Override
            public void run() {
                g.Graphs.get(i).addVal(v);
                g.Graphs.get(i).addArg(a);
                g.invalidate();
            }
        });

    }
    public void ChangeButtonColor(){
        ConnButt.post(new Runnable() {
            @Override
            public void run() {
                if (ConnOpen) {
                    ConnButt.setBackgroundColor(Color.RED);
                }else {
                    ConnButt.setBackgroundColor(Color.GREEN);
                }
            }
        });
    }
    public void log(final String text){
        Log.e(LOGTAG,"General"+">>"+text);
        LogField.post(new Runnable() {
            @Override
            public void run() {
                String tx = LogField.getText().toString();
                String ms =  "\n"+"General"+">> "+text;
                if(tx.length()>2000){tx = tx.substring(ms.length(),tx.length());}
                LogField.setText(tx+ms);
            }
        });
        ScrollLog.post(new Runnable() {
            @Override
            public void run() {
                ScrollLog.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    public void ToggleConsole(){
        if(ConsOpen){
            AnimateConsoleClose();
        }
        else {
            AnimateConsoleOpen();
        }
        ConsOpen = !ConsOpen;
    }

    private void AnimateConsoleOpen() {
        Animation anim  = AnimationUtils.loadAnimation(Contx,R.anim.openconsole);
        anim.setFillAfter(true);
        Console.startAnimation(anim);
        Animation anim1 = AnimationUtils.loadAnimation(Contx,R.anim.consolebuttonopen);
        anim1.setFillAfter(true);
        ConsoleButton.startAnimation(anim1);
    }

    private void AnimateConsoleClose() {
        Animation anim  = AnimationUtils.loadAnimation(Contx,R.anim.closeconsole);
        anim.setFillAfter(true);
        Console.startAnimation(anim);
        Animation anim1 = AnimationUtils.loadAnimation(Contx,R.anim.consolebuttonclose);
        anim1.setFillAfter(true);
        ConsoleButton.startAnimation(anim1);
    }
}
