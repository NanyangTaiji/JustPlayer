package com.nytaiji.vlcplayer;

import static com.nytaiji.nybase.utils.RetryUtil.retry;
import static org.videolan.libvlc.RendererDiscoverer.Event.ItemAdded;
import static org.videolan.libvlc.RendererDiscoverer.Event.ItemDeleted;

import android.content.Context;
import android.util.Log;

import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.NetworkMonitor;

import org.videolan.libvlc.RendererDiscoverer;
import org.videolan.libvlc.RendererItem;
import org.videolan.libvlc.interfaces.ILibVLC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class RendererDelegate {

    private static final String TAG = "VLC/RendererDelegate";
    private final ArrayList<RendererDiscoverer> discoverers = new ArrayList<>();
    private ArrayList<RendererItem> renderers = new ArrayList<>();

    private volatile boolean started = false;

    private static RendererDelegate INSTANCE;

    private NetworkMonitor networkMonitor;
    private Context context;

    private RendererDelegate(Context context) {
        this.context = context;
        networkMonitor = NetworkMonitor.getInstance(context, new NetworkMonitor.NetworkChangeListener() {
            @Override
            public void onNetworkChanged(boolean isConnected, boolean isMobile, boolean isVPN) {
                if (isConnected) {
                    Log.e(TAG, "Network complete!");
                    start(new Callback() {
                        @Override
                        public void onComplete() {
                            Log.e(TAG, "renderers complete");
                        }
                    });
                } else {
                    stop();
                }
            }
        });

        // Start monitoring
        networkMonitor.start();

    }

    // Public method to get the singleton instance
    public static synchronized RendererDelegate getInstance(Context context) {
        if (INSTANCE == null || INSTANCE.getRenderers().size() == 0) {
            INSTANCE = new RendererDelegate(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public ArrayList<RendererItem> getRenderers() {
        Set<RendererItem> set = new TreeSet<RendererItem>(new RendererItemComparator());

        // Add all the elements from the list to the set
        set.addAll(renderers);
        return new ArrayList<RendererItem>(set);
    }


    public void start(Callback callback) {
        // Simulating an asynchronous operation
        new Thread(() -> {
            initialize();
            // Notify the callback when the operation is complete
            if (callback != null) callback.onComplete();
        }).start();
    }

    public interface Callback {
        void onComplete();
    }

    private void initialize() {
        //if (!started) {
        try {
            ILibVLC libVlc = VLCInstance.getInstance(AppContextProvider.getAppContext());
            if (RendererDiscoverer.list(libVlc) == null) {
                Log.e(TAG, "RendererDiscoverer==null");
            }

            started = true;

            for (RendererDiscoverer.Description discoverer : RendererDiscoverer.list(libVlc)) {
                RendererDiscoverer rd = new RendererDiscoverer(libVlc, discoverer.name);
                discoverers.add(rd);
                rd.setEventListener(new RendererDiscoverer.EventListener() {
                    @Override
                    public void onEvent(RendererDiscoverer.Event event) {
                        if (event != null) {
                            switch (event.type) {
                                case ItemAdded:
                                    //  Comparator<RendererItem> displayNameComparator = new RendererItemComparator();
                                    renderers.add(event.getItem());
                                    break;
                                case ItemDeleted:
                                    renderers.remove(event.getItem());
                                    break;
                            }
                        }
                    }
                });// Assuming this is the RendererDelegate instance
                retry(5, 1000L, () -> !rd.isReleased() && rd.start());
            }
        } catch (Exception e) {
            // Handle exception appropriately, e.g., log it
            e.printStackTrace();
        }
        // }
    }

    public void stop() {
        if (started) {
            started = false;
            for (RendererDiscoverer discoverer : discoverers) {
                discoverer.stop();
            }
            clear();
        }
    }

    private void clear() {
        discoverers.clear();
        renderers.clear();
    }

    public class RendererItemComparator implements Comparator<RendererItem> {
        @Override
        public int compare(RendererItem item1, RendererItem item2) {
            // Compare based on displayName
            return item1.displayName.compareTo(item2.displayName);
        }
    }

}
