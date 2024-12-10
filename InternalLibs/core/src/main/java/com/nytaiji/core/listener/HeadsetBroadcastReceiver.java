package com.nytaiji.core.listener;

import static com.nytaiji.core.base.BaseConstants.MEDIA_KEY;
import static com.nytaiji.core.base.BaseConstants.MEDIA_PAUSE;
import static com.nytaiji.core.base.BaseConstants.MEDIA_PLAY;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class HeadsetBroadcastReceiver extends BroadcastReceiver {

    public static final String MEDIA_ACTION = HeadsetBroadcastReceiver.class.getName();


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent mIntent = new Intent(MEDIA_ACTION);
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null) {
                return;
            }
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        mIntent.putExtra(MEDIA_KEY, MEDIA_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        mIntent.putExtra(MEDIA_KEY, MEDIA_PLAY);
                        break;
                }
            }
        } else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            mIntent.putExtra(MEDIA_KEY, MEDIA_PAUSE);
        }
        context.sendBroadcast(mIntent);
    }
}
