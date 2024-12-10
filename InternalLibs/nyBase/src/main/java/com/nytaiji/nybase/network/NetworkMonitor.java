package com.nytaiji.nybase.network;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;

public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";

    private final Context context;
    private final ConnectivityManager cm;
    private NetworkChangeListener listener;

    private boolean registered = false;


    private static NetworkMonitor instance;

    public static synchronized NetworkMonitor getInstance(Context context, NetworkChangeListener listener) {
        if (instance == null) {
            instance = new NetworkMonitor(context.getApplicationContext(), listener);
        }
        return instance;
    }

    public NetworkMonitor(Context context, NetworkChangeListener listener) {
        this.context = context;
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    public void start() {
        if (!registered) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(receiver, filter);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(networkCallback);
            }
            registered = true;
        }
    }

    public void stop() {
        if (registered) {
            context.unregisterReceiver(receiver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.unregisterNetworkCallback(networkCallback);
            }
            registered = false;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Network change broadcast received");
            if (listener != null) {
                listener.onNetworkChanged(isConnected(), isMobile(), updateVPNStatus());
            }
        }
    };

    private final ConnectivityManager.NetworkCallback networkCallback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.d(TAG, "Network available callback");
                    if (listener != null) {
                        listener.onNetworkChanged(isConnected(), isMobile(), updateVPNStatus());
                    }
                }

                @Override
                public void onLost(Network network) {
                    Log.d(TAG, "Network lost callback");
                    if (listener != null) {
                        listener.onNetworkChanged(isConnected(), isMobile(), updateVPNStatus());
                    }
                }
            };

    private boolean isConnected() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isMobile() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean updateVPNStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return true;
                }
            }
            return false;
        } else {
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName("ppp0");
                if (networkInterface != null) {
                    return true;
                }
            } catch (SocketException ignored) {
            }
            return false;
        }
    }

    private void updateConnectionStatus() {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        boolean isMobile = isConnected && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        boolean isVPN = isConnected && updateVPNStatus();

        // Notify the listener about the connection status change
        if (listener != null) {
            listener.onNetworkChanged(isConnected, isMobile, isVPN);
        }
    }

    public void setNetworkChangeListener(NetworkChangeListener listener) {
        this.listener = listener;
    }

    public interface NetworkChangeListener {
        void onNetworkChanged(boolean isConnected, boolean isMobile, boolean isVPN);
    }
}


