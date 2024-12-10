package com.nytaiji.nybase.libmediaStreamer;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.CastDevice;

import java.io.IOException;
import java.net.InetAddress;


public class WifiSingleHttpServer extends AbsHttpServer {
    private InetAddress inetAddress;

    public WifiSingleHttpServer(Context context) throws IOException {
        super(context, ServerType.WIFIHTTPSERVER);
    }

    @NonNull
    public WifiSingleHttpServer setAllowedClient(CastDevice castDevice) {
        InetAddress address;
        if (castDevice != null) {
            address = castDevice.getInetAddress();
        } else {
            address = null;
        }

        this.inetAddress = address;
        return this;
    }

    boolean isValidAddress(InetAddress address) {
        InetAddress inetAddress;
        return (inetAddress = this.inetAddress) != null && inetAddress.equals(address);
    }
}

