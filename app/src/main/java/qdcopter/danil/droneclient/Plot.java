package qdcopter.danil.droneclient;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.System.arraycopy;

public class Plot extends LinearLayout{

    public String Title;
    protected ArrayList<Graph> Graphs = new ArrayList<>();
    private float max=0;
    private float min=0;
    private View root;
    private int AccentColor;
    public int graphCount = 1;
    public int pointCount = 100;

    public Plot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public Plot(Context context, AttributeSet attrs) {
        super(context, attrs);

        ConfigureView(context, attrs);

        for (int i = 0; i < graphCount; i++) {
            Graphs.add(new Graph(100));
        }
        float hsv [] = new  float[3];
        Color.colorToHSV(AccentColor,hsv);
        for(int i = 0;i<graphCount;i++){
            setGraphColor(hsv,i);
        }

        Timer updMinMax = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                UpdateMinMax();
            }
        };
        updMinMax.schedule(tt, 300, 500);
    }

    private void ConfigureView(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.Plot,0, 0);
        try {
            pointCount = a.getInt(R.styleable.Plot_PointsCount,100);
            graphCount = a.getInt(R.styleable.Plot_GraphsCount,1);
            setBackgroundColor(a.getColor(R.styleable.Plot_Background,0xF0D0F0));
            AccentColor = (a.getColor(R.styleable.Plot_Color,0xF01010));
            Title = a.getString(R.styleable.Plot_Title);
        }
        catch (Exception e){
            Log.e("Danil",e.getMessage());
            a.recycle();
        }

        createRoot(context);

        ((TextView)root.findViewById(R.id.graphTitle)).setText(Title);
        root.findViewById(R.id.Accent).setBackgroundColor(AccentColor);
        root.findViewById(R.id.Accent).getBackground().setAlpha(0x40);
    }


    private void createRoot(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(R.layout.plot,null);
        root.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(root);
    }
    private void setGraphColor(float[] hsv, int i) {
        float h[] = new float[3];
        h[1] = hsv[1];
        h[2] = hsv[2];
        h[0] = (hsv[0] + 50*(2*i-(graphCount-1))/2f); //
        h[0] = h[0]<0? (360+h[0]) : h[0];
        h[0] = h[0]>360 ? (h[0]-360) : h[0];
        Graphs.get(i).paint.setColor(Color.HSVToColor(h)); // setting color for each graph
    }

    private void UpdateMinMax() {
        max=0;
        min=0;
        for (Graph g:Graphs) {
            g.getMaxMin();
            max = max<g.max ? g.max : max;
            min = min>g.min ? g.min : min;
        }
        for (Graph g:Graphs) {
            g.setMaxMin(max,min);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
       for(Graph g:Graphs){
           g.Draw(canvas);
       }
    }

}
class Graph{
    public float [] vals;
    public float [] args;
    public float max = 0; // 0 will always be "visible"
    public float min = 0;
    public Paint paint = new Paint();
    protected String Title;

    private int length;
    private boolean bidir;

    public Graph(int len){
        length = len;
        vals = new float[len];
        args = new float[len];
        for(int i = 0;i<len;i++) {
            args[i] = i;
        }
    }

    public void Draw(Canvas canvas){
        float dx;
        float dy;
        float height = canvas.getHeight();
        float width = canvas.getWidth();
        if (vals.length > 0) {
            if(bidir){
                float siz  = abs(max)> abs(min) ? 2*abs(max):2*abs(min);
                dy = height/ siz;
                dx = width / siz;
                paint.setAlpha(0x12);
                //draw axis
                canvas.drawLine(width/5 , height/2, 4*width/5, height/2 ,paint);
                canvas.drawLine(width/2, height/5, width/2, 4*height/5 ,paint);
            }else {
                dy = height / (max-min);
                dx = width / vals.length;
            }
            //Log.e("Danil",String.valueOf(max)+String.valueOf(min));
            for (int i = 1; i < vals.length; i++) {
                if(bidir){
                    paint.setAlpha((i+1)*256/(vals.length));
                    canvas.drawLine(width/2 + (args[i - 1])* dx, height/2 - (vals[i - 1]) * dy,
                            width/2 +( args[i])*dx, height/2 - (vals[i]) * dy, paint);
                }
                else{
                    canvas.drawLine(args[i - 1] * dx, height - (vals[i - 1] - min) * dy, dx * args[i], height - (vals[i] - min) * dy, paint);
                }
            }
        }
    }
    public void addVal(float v){
        arraycopy(vals,1,vals,0,length-1);
        vals[length-1]=v;


    }
    public void addArg(float v ){
        bidir = true;
        arraycopy(args,1,args,0,length-1);
        args[length-1]=v;

    }
    public void getMaxMin(){
        max=0;
        min=0;
        if(bidir){
        for(int i=0;i<length;i++) {
            max = args[i] > max ? args[i] : max;
            min = args[i] < min ? args[i] : min;
        }}
        for(int i=0;i<length;i++) {
            max = vals[i] > max ? vals[i] : max;
            min = vals[i] < min ? vals[i] : min;
        }
       // Log.e("Danil",String.valueOf(max)+String.valueOf(min));
    }
    public void setMaxMin(float max,float min) {
        this.max = max;
        this.min = min;
    }
}