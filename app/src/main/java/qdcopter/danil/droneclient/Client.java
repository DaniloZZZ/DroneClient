package qdcopter.danil.droneclient;

/**
 * Created by Danil on 4/30/2017.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.RuleBasedCollator;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends Thread implements Runnable {

    DataInterface mData;
    ViewInterface mView;
    private String LogTag="Client";

    public String response = "";
    public String send;
    boolean upd  = true;
    public  String receivedData;
    public boolean newDataAval;
    private Socket socket;
    private TimerTask tt;

    public Client(ViewInterface v,DataInterface d) {
        mView = v;
        mData = d;
    }
    @Override
    public void run(){
        Integer i =0;
        String result = "shit";

        try {
            socket = new Socket();
            socket.setSoTimeout(1500);
            socket.connect(new InetSocketAddress(mData.Addr,mData.Port),2000);
            OutputStream out = socket.getOutputStream();
            final InputStream inp = socket.getInputStream();
            mView.ConnOpen = true;
            mView.log(LogTag,"Connection Established","Connected",0);
            mView.ChangeButtonColor();

            while (upd){
                i++;

                SendData(out,i);
               // Thread.sleep(10);
                newDataAval = ReadData(inp);
               // Thread.sleep(10);
            }
            result = "read "+i.toString()+" values";
        }
        catch (Exception e){
            result = e.toString();
            throw new RuntimeException(e.toString());
        }
        finally {
            throw  new RuntimeException("ecxec ended "+result);
        }
    }

    public void Close(){
        upd = false;
        if(mView.ConnOpen==false)
            mView.log(LogTag,"Failed to Connect","No response",1);
        else
            mView.log(LogTag,"Connection Lost","Connection Lost",1);
        mView.ConnOpen = false;
        mView.ChangeButtonColor();

        if ((socket != null)) {
            try {
                mView.log(LogTag,"Closing socket");
                socket.close();
            }
            catch (Exception e) {
              mView.log(LogTag,e.toString());
            }
        }
    };

    private boolean ReadData(InputStream inp ) throws IOException, InterruptedException {
        String s;
        ByteArrayOutputStream inpArray = new ByteArrayOutputStream(512);
        byte[] buffer = new byte[512];

        int bytesRead;
       // mView.log(LogTag,"Reading");
//                 * notice: inputStream.read() will block if no data return
        while ((bytesRead = inp.read(buffer)) > 0) {
            inpArray.write(buffer, 0, bytesRead);
            s = inpArray.toString("UTF-8");
            response = s;
            mView.log(LogTag,s+String.valueOf(bytesRead));
            if ((buffer[bytesRead - 1]) == 36) {
                receivedData = response;
                mView.log(LogTag, "Read data >" + response + "<");
                response = "";
                return true;
            }
        }
        return false;
    }

    private void SendData(OutputStream out, Integer i){
        PrintWriter output = new PrintWriter(out);
        output.write(send);
        mView.log("Client","Send "+i.toString()+" data >"+send+"<");
        output.flush();
        }

}

