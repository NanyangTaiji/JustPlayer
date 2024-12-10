package com.nytaiji.vlcplayer;

import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.utils.NyFileUtil.encodeVLCMrl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.AppContextProvider;
import com.nytaiji.nybase.utils.SystemUtils;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.RendererItem;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class VlcSimplePlayActivity extends AppCompatActivity {

    private VLCVideoLayout videoLayout;
    private ImageButton castButton;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private boolean isCasting = false;
    private ArrayList<RendererItem> renderers;
    private Uri uri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);
        SystemUtils.keepScreenOn(AppContextProvider.getAppContext());
        RendererDelegate rendererDelegate = RendererDelegate.getInstance(AppContextProvider.getAppContext());
        renderers = rendererDelegate.getRenderers();

        videoLayout = findViewById(R.id.video_layout);
        castButton = findViewById(R.id.cast_button);
        Intent intent = getIntent();
        uri = intent.getData();
        String path = encodeVLCMrl(uri.toString());

        if (!path.contains("http") && path.contains("_NY")) {
            //method 1
            // sampleVideoStream = webAddress.append(path).toString();
            //method 2
            //   sampleVideoStream = getStremerPath(this, new NyHybrid(path), false);
            //method 3
            String reroute = getPreferredServerUrl(false, false);
            WifiShareUtil.stopHttpServer();
            WifiShareUtil.httpShare(this, new NyHybrid(path), reroute, getMessageHandler(this.findViewById(R.id.content)));
            WifiShareUtil.setUnique(true);
            uri = Uri.parse(reroute);
        }

        // Check if Renderers are available and update the visibility of the casting icon
        if (renderers != null && renderers.size() > 0) {
            castButton.setVisibility(View.VISIBLE);
            castButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleCasting();
                }
            });
        } else {
            castButton.setVisibility(View.GONE);
        }

        libVLC = (LibVLC) VLCInstance.getInstance(AppContextProvider.getAppContext());

        initializePlayer(0L);
    }

    private void initializePlayer(long playTime) {
        // Initialize LibVLC
        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer(libVLC);

        // Set media options based on your requirements
        Media media = new Media(libVLC, uri);
        mediaPlayer.setMedia(media);
        media.release();
        VLCOptions.setMediaOptions(media, this, 1, false);
        // Attach the VLCVideoLayout to the MediaPlayer
        mediaPlayer.attachViews(videoLayout, null, false, true);

        // Start playback
        mediaPlayer.play();
        mediaPlayer.setTime(playTime);
    }

    private void toggleCasting() {
        if (isCasting) {
            // Stop casting and switch to local playback
            stopCasting();
        } else {
            // Start casting and switch to Chromecast playback
            startCasting();
        }
    }

    private void startCasting() {
        if (renderers.size() < 1) return;
        RenderersDialog renderersDialog = new RenderersDialog();
        renderersDialog.show(getSupportFragmentManager(), "RenderersDialog");
        // Set the listener to get the selected renderer
        renderersDialog.setRenderersDialogListener(new RenderersDialog.RenderersDialogListener() {
            @Override
            public void onRendererSelected(RendererItem selectedRenderer) {
                Toast.makeText(VlcSimplePlayActivity.this, selectedRenderer.displayName, Toast.LENGTH_SHORT).show();
                mediaPlayer.pause();
                mediaPlayer.setRenderer(selectedRenderer);
                mediaPlayer.play();
                isCasting = true;
                castButton.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast_connected);
            }
        });
    }

    private void stopCasting() {
        long playeTime = mediaPlayer.getTime();
        mediaPlayer.pause();
        mediaPlayer.setRenderer(null);
        mediaPlayer.release();
        isCasting = false;
        castButton.setImageResource(com.nytaiji.nybase.R.drawable.ic_cast);
        initializePlayer(playeTime);
        //  mediaPlayer.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources when the activity is destroyed
        mediaPlayer.release();
        libVLC.release();
    }

}

