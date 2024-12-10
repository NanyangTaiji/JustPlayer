package com.nytaiji.vlcplayer;

public class VlcConstants {
    protected static final String ID_VIDEO = "video";
    protected static final String ID_AUDIO = "audio";
    protected static final String ID_NETWORK = "network";
    protected static final String ID_DIRECTORIES = "directories";
    protected static final String ID_HISTORY = "history";
    protected static final String ID_MRL = "mrl";
    protected static final String ID_PREFERENCES = "preferences";
    protected static final String ID_ABOUT = "about";
    private static final String PREF_FIRST_RUN = "first_run";
    public static final String EXTRA_FIRST_RUN = "extra_first_run";
    public static final String EXTRA_UPGRADE = "extra_upgrade";
    public final static String ACTION_MEDIALIBRARY_READY = "VLC/VLCApplication";
    public final static String ACTION_INIT = "medialibrary_init";
    public final static String ACTION_RELOAD = "medialibrary_reload";
    public final static String ACTION_DISCOVER = "medialibrary_discover";
    public final static String ACTION_DISCOVER_DEVICE = "medialibrary_discover_device";

    public final static String EXTRA_PATH = "extra_path";
    public final static String EXTRA_UUID = "extra_uuid";

    public final static String ACTION_RESUME_SCAN = "action_resume_scan";
    public final static String ACTION_PAUSE_SCAN = "action_pause_scan";
    public final static String ACTION_SERVICE_STARTED = "action_service_started";
    public final static String ACTION_SERVICE_ENDED = "action_service_ended";
    public final static String ACTION_PROGRESS = "action_progress";
    public final static String ACTION_PROGRESS_TEXT = "action_progress_text";
    public final static String ACTION_PROGRESS_VALUE = "action_progress_value";
    public static final long NOTIFICATION_DELAY = 1000L;
    private static final int ACTION_MEDIA_MOUNTED = 1337;
    private static final int ACTION_MEDIA_UNMOUNTED = 1338;
    private static final int ACTION_DISPLAY_PROGRESSBAR = 1339;
}
