package com.nytaiji.nybase.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.nytaiji.nybase.utils.AppContextProvider;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.List;


public class WifiUtil {
    public WifiUtil(){
    }

    public  String getLocalIp(){
        String ip = findLocalIp1();
        if (ip != null)
            return ip;
        ip = findLocalIp2();
        if (ip != null)
            return ip;
        ip = findLocalIp3();
        return ip;
    }

    private String findLocalIp1(){
        ConnectivityManager connMgr = (ConnectivityManager) AppContextProvider.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connMgr != null) {
            LinkProperties linkProperties;
            String ipAddress = null;
            linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_VPN);
            if (linkProperties == null)
                linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_ETHERNET);
            if (linkProperties == null)
                linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_WIFI);
            if (linkProperties != null)
                ipAddress =  getIp(linkProperties);
            return ipAddress;
        }
        return null;
    }

    private String findLocalIp2(){
        ConnectivityManager connMgr = (ConnectivityManager) AppContextProvider.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;
        if (connMgr != null) {
            wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo.isConnected()) {
                WifiManager myWifiManager = (WifiManager) AppContextProvider.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo myWifiInfo;
                if (myWifiManager != null) {
                    myWifiInfo = myWifiManager.getConnectionInfo();
                    byte[] bytes = BigInteger.valueOf(myWifiInfo.getIpAddress()).toByteArray();
                    InetAddress address;
                    try {
                        address = InetAddress.getByAddress(bytes);
                        if (address != null && address.isSiteLocalAddress())
                            return address.getHostAddress();
                    } catch (UnknownHostException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private String findLocalIp3(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private LinkProperties getLinkProperties(ConnectivityManager connectivityManager, int cap) {
        Network[] nets = connectivityManager.getAllNetworks();
        for (Network n: nets) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(n);
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(n);
            String interfaceName =  linkProperties.getInterfaceName();
            if (interfaceName != null && networkCapabilities != null) {
                if (networkCapabilities.hasTransport(cap))
                    return linkProperties;
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getIp(LinkProperties lp) {
        List<LinkAddress> linkAddresses = lp.getLinkAddresses();
        for(LinkAddress linkAddress: linkAddresses) {
            InetAddress inetAddress = linkAddress.getAddress();
            if (inetAddress instanceof Inet4Address) {
                return inetAddress.getHostAddress();
            }
        }
        return null;
    }

    /**
     * This method requires the caller to hold the permission ACCESS_NETWORK_STATE.
     *
     * @param context context
     * @return if wifi is connected,return true
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }


    public static String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipAddressString = null;
        try {
            InetAddress inetAddress = InetAddress.getByAddress(ipByteArray);
            ipAddressString = inetAddress.getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("","Unable to get host address."+ex.toString());
        }
        return ipAddressString;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities == null) return false;
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true;
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            return true;
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

    }
}
