package com.nytaiji.player.chromecast;

import android.content.Context;
import android.view.Menu;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.nytaiji.player.R;


import org.jetbrains.annotations.Nullable;

public final class ExpandedControlsActivity extends ExpandedControllerActivity {

    public boolean onCreateOptionsMenu(@Nullable Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.expanded_controller, menu);
        if (menu != null) {
            CastButtonFactory.setUpMediaRouteButton((Context)this, menu, R.id.menu_connect_route);
        }

        return true;
    }
}

