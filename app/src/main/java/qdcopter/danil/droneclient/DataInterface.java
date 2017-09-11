package qdcopter.danil.droneclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.SeekBar;

import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.util.Collections;
import java.util.List;

import static android.provider.Settings.System.getString;


/**
 * Created by Danil on 5/2/2017.
 */

public class DataInterface {

    public String Addr;
    public int Port;

    protected SensorInterface Accel;
    protected ViewInterface ViewIfc;

    public DataInterface(){
    }

    public void UpdateConnectionData_fromView(){
        // TODO: subsctibe to update events of fields for auto update
        Port = Integer.parseInt(ViewIfc.PortText());
        Addr = ViewIfc.AddrText();
        // Saving ip address
        saveIP(ViewIfc.Contx,Addr);
    }
    public static String getSavedIP(Context context){
        SharedPreferences sharedPref = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "192.168.0.1";
        return sharedPref.getString( context.getString(R.string.addr_storage_key), defaultValue);
    }
    public static void saveIP(Context context, String ip){
        SharedPreferences sharedPref =  ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.addr_storage_key), ip);
        editor.commit();
    }

    public String ToSend(){
        double f  =((SeekBar) ViewIfc.Contx.findViewById(R.id.ThrottleSetter)).getProgress();
        return String.format("%.3f",ViewIfc.FilterK)+";"+String.format("%.3f",f/10.0)+";"+String.format("%.3f",Accel.az);
    }
    public static String LocalIP(){
        return getIPAddress(true);
    }
    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {} // for now eat exceptions
        return "";
    }
}
