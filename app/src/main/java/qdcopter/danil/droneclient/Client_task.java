package qdcopter.danil.droneclient;

/**
 * Created by Danil on 4/30/2017.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client_task extends AsyncTask<Void, Object, String> {

    String dstAddress;
    private String LogTag="ww";
    int dstPort;
    String response = "";
    TextView textResponse;
    String textAnswer;

    Client_task(String addr, int port, TextView textResponse, TextView textAns) {
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        this.textAnswer = textAns.getText().toString();

    }

    @Override
    protected String doInBackground(Void... arg0) {

        Socket socket = null;

        try {
            InetAddress serverAddr = InetAddress.getByName(dstAddress);
            socket = new Socket(serverAddr,dstPort);

            OutputStream out = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            PrintWriter output = new PrintWriter(out);

            output.println(textAnswer);
            output.flush();




            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[64];

            int bytesRead=1;
            Log.e(LogTag, "Creating input stream\f");

			/*;
             * notice: inputStream.read() will block it no data return
			 */

            while ((bytesRead = inputStream.read(buffer))>=1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                String s  = byteArrayOutputStream.toString("UTF-8");
                response += s;
                int a = (buffer[bytesRead-1]);
                if((buffer[bytesRead-1])==36) {
                    break;
                }
            }
            publishProgress(response);

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        }
        catch (Exception e){
            response = "some Exeption:"+e.toString();
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (Exception e) {
                    response = e.toString();
                }
            }
        }
        return response;
    }

    @Override
    protected void onProgressUpdate(Object... progress) {
        textResponse.setText("\np"+progress[0].toString()+textResponse.getText());

    }


    @Override
    protected void onPostExecute(String result) {
        textResponse.setText("\nPost exec:"+response+textResponse.getText());
        super.onPostExecute(result);
    }


}
