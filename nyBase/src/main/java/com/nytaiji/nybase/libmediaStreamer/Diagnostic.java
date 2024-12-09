package com.nytaiji.nybase.libmediaStreamer;

import androidx.annotation.NonNull;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Diagnostic
{
    public final Code code;
    public final String extra;

    private Diagnostic(final Code code, final String extra) {
        this.code = code;
        this.extra = extra;
    }

    @NonNull
    public static List<Diagnostic> diagnose() {
        final ArrayList<Diagnostic> list = new ArrayList<Diagnostic>();
        final ProxySelector default1= ProxySelector.getDefault();
        if (default1!= null) {
            try {
                final List<Proxy> select;
                final Proxy proxy;
                if (!(select = default1.select(new URI("http://127.0.0.1/"))).isEmpty() && (proxy = select.get(0)) != Proxy.NO_PROXY) {
                    final Code proxy1 = Code.PROXY;
                    list.add(new Diagnostic(proxy1, proxy.toString()));
                }
            }
            catch (URISyntaxException ignored) {}
        }
        return list;
    }

    /* renamed from: dev.dworks.apps.anexplorer.http.Diagnostic$Code */
    public enum Code
    {
        PROXY;
    }
}


