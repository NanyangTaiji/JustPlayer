package com.amaze.filemanager.fileoperations.scanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkConnection {

    public static boolean isWifiConnected(ConnectivityManager connectivityManager) {
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo.isConnected();
    }

    public static ConnectivityManager connectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isWifiConnected(Context context) {
        return isWifiConnected(connectivityManager(context));
    }

    public static boolean isInternetAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isInternetAvailable(Context context) {
        return isInternetAvailable(connectivityManager(context));
    }

    public static String parseIpAddress(long ip) {
        try {
            byte[] byteAddress = BigInteger.valueOf(ip).toByteArray();
            reverseByteArray(byteAddress);
            return InetAddress.getByAddress(byteAddress).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void reverseByteArray(byte[] array) {
        if (array == null) {
            return;
        }

        int i = 0;
        int j = array.length - 1;

        while (i < j) {
            byte temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            i++;
            j--;
        }
    }
}
