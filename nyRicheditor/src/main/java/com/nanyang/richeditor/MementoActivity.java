package com.nanyang.richeditor;

import static com.nanyang.richeditor.database.DatabaseModel.TYPE_YOUTUBE;
import static com.nanyang.richeditor.database.DatabaseUtils.autoRestore;
import static com.nanyang.richeditor.database.DatabaseUtils.autoSave;
import static com.nanyang.richeditor.database.DatabaseUtils.backupData;
import static com.nanyang.richeditor.database.DatabaseUtils.dbToJson;
import static com.nanyang.richeditor.database.DatabaseUtils.readBackupFile;
import static com.nanyang.richeditor.database.DatabaseUtils.restoreData;
import static com.nanyang.richeditor.database.DatabaseUtils.synchronizeTask;
import static com.nanyang.richeditor.database.DatabaseUtils.viewJson;
import static com.nanyang.richeditor.editor.EditorUtils.RestoreOrinalKeywords;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_BACKUP;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_JSON_TO_DB;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_DRAW;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_GESTURE;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_JSON_VIEW;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_RESTORE;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_SETTINGS;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_SYNCHRONIZE;
import static com.nanyang.richeditor.view.DrawerAdapter.TYPE_TEXT;
import static com.nytaiji.cloud.database.DbHelper.initCloudDbHelper;
import static com.nytaiji.nybase.utils.SystemUtils.hideStatusBar;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.OpenHelper;
import com.nanyang.richeditor.editor.ShowHtmlFragment;
import com.nanyang.richeditor.memento.BaseFragment;
import com.nanyang.richeditor.memento.CategoryFragment;
//import com.nanyang.richeditor.text.TextEditorActivity;
import com.nanyang.richeditor.util.TTSUtils;
import com.nanyang.richeditor.view.DrawerAdapter;
import com.nytaiji.nybase.NyPermissionsActivity;
import com.nytaiji.nybase.utils.NyFormatter;
//import com.nanyang.richeditor.youtube.YouTubeActivity;
import com.nytaiji.nybase.filePicker.FilePickDialog;
//import com.nytaiji.drawview.DrawingActivity;

import java.util.ArrayList;
import java.util.List;

//https://github.com/yaa110/Memento
public class MementoActivity extends NyPermissionsActivity {
    private static String TAG = "MementoActivity";
    public static final int PERMISSION_REQUEST = 3;

    private DrawerLayout drawerLayout;
    public View drawerHolder;
    private boolean exitStatus = false;

    public BaseFragment fragment;
    public Toolbar toolbar;
    private final boolean checkForPermission = true;
    private int libIndex = 0;
    private GestureOverlayView mGestureview;
    private TabLayout tabLayout;

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            exitStatus = false;
        }
    };


    public static final String CLOUD_AUTHENTICATOR_GDRIVE = "android.intent.category.BROWSABLE";
    public static final String CLOUD_AUTHENTICATOR_REDIRECT_URI = "com.amaze.filemanager:/auth";
    private List<CategoryFragment> mFragmentList = new ArrayList<>();
    private static AppCompatActivity mementoActivity;

    public static AppCompatActivity geInstance() {
        return mementoActivity;
    }

    SharedPreferences prefs;

    public boolean fileProcessPermitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memento);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initCloudDbHelper(this);

        fileProcessPermitted = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        mementoActivity = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }

        setupDrawer();

        tabLayout = findViewById(R.id.tablayout);

        tabLayout.setTabTextColors(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.red));

        for (int i = 0; i < App.getAllLibs().size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(App.getAllLibs().get(i).getTitle()));
            CategoryFragment fragment = createFragment(i);
            mFragmentList.add(fragment);
        }

        changeFragment(0); //默认显示第一页
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (ShowHtmlFragment.getTabIndex() != -1)
                    ShowHtmlFragment.collapse(getSupportFragmentManager());
                changeFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (ShowHtmlFragment.getTabIndex() != -1)
                    ShowHtmlFragment.collapse(getSupportFragmentManager());
            }
        });

        //-----------------------
        //
        mGestureview = (GestureOverlayView) findViewById(R.id.gestureview);
        TTSUtils.getInstance(this);
        initGestureOverlay();
        //---------------------

      /*  findViewById(R.id.iv_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //   Intent intent = new Intent(MementoActivity.this, EditorActivity.class);
                                Intent intent = new Intent(MementoActivity.this, YouTubeActivity.class);
                                intent.putExtra(OpenHelper.COLUMN_TYPE, TYPE_YOUTUBE);
                                startActivity(intent);
                                // }
                            }
                        });
                        interrupt();
                    }
                }.start();
            }
        });*/
    }

    @Override
    protected void postPermissionGranted() {
        fileProcessPermitted = true;
    }


    private void startDatabaseFragment(int labI) {
        this.libIndex = labI;
        App.setCurrentLib(labI);
        fragment = new CategoryFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        fragment.showToolbarTitle();
    }

    @NonNull
    private CategoryFragment createFragment(int labI) {
        App.setCurrentLib(labI);
        CategoryFragment categoryFragment = new CategoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("LIBTITLE", App.getAllLibs().get(labI).getName());
        categoryFragment.setArguments(bundle);
        return categoryFragment;
    }

    private void changeFragment(int currentPosition) {
        this.libIndex = currentPosition;
        App.setCurrentLib(libIndex);
        tabLayout.setScrollPosition(libIndex, 0f, true);
        fragment = mFragmentList.get(libIndex);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        fragment.setTabIndex(libIndex);
        autoRestore(MementoActivity.this, currentPosition, new FilePickDialog.ImportListener() {
            @Override
            public void onSelect(String path) {
                restoreFromPath(path);
            }

            @Override
            public void onError(String msg) {

            }
        });


      /*  if (ShowHtmlFragment.getTabIndex() == libIndex)
            ShowHtmlFragment.reload(getSupportFragmentManager());*/

    }

    @Override
    protected void onDestroy() {
        //---------------------------------------//
        TTSUtils.getInstance(this).release();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(drawerHolder)) {
            drawerLayout.closeDrawers();
            return;
        }
        //
        if (fragment.getSelectionCount() > 0) {
            fragment.toggleSelection(false);
            return;
        }

       /* if (ShowHtmlFragment.getTabIndex()!=-1) {
            ShowHtmlFragment.remove(getSupportFragmentManager());
            return;
        }*/


        if (fragment.searchResults != null) {
            //  Log.e(TAG, "BaseFragment.previousId=" + BaseFragment.previousId);
            fragment.toggleSelection(false);
            //
            fragment.searchResults = null;

            if (fragment.previousId > 0) {
                fragment.setCategoryId(fragment.previousId);
            } else fragment.setCategoryId(DatabaseModel.NEW_MODEL_ID);

            fragment.loadItems();
            return;
        }

        if (fragment.getCategoryId() > 0) {
            fragment.setCategoryId(DatabaseModel.NEW_MODEL_ID);
            fragment.toggleSelection(false);
            fragment.loadItems();
            return;
        }

        if (exitStatus) {
            try {
                autoSave(MementoActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finishAndRemoveTask();
        } else {
            exitStatus = true;
            //  dbChoice();
            Snackbar.make(fragment.fab != null ? fragment.fab : toolbar, R.string.exit_message, Snackbar.LENGTH_LONG).show();

            handler.postDelayed(runnable, 2000);
        }
    }

    private void setupDrawer() {
        // Set date in drawer
        ((TextView) findViewById(R.id.drawer_date)).setText(NyFormatter.formatDate());

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerHolder = findViewById(R.id.drawer_holder);
        ListView drawerList = (ListView) findViewById(R.id.drawer_list);

        // Navigation menu button
        findViewById(R.id.nav_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Settings button
        findViewById(R.id.settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickDrawer(TYPE_SETTINGS);
            }
        });

        // Set adapter of drawer
        drawerList.setAdapter(new DrawerAdapter(
                getApplicationContext(),
                new DrawerAdapter.ClickListener() {
                    @Override
                    public void onClick(int type) {
                        onClickDrawer(type);
                    }
                }
        ));
    }

    private void onClickDrawer(final int type) {
        drawerLayout.closeDrawers();

        try {
            handler.removeCallbacks(runnable);
        } catch (Exception ignored) {
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    // wait for completion of drawer animation
                    sleep(500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent;
                            switch (type) {
                                case TYPE_GESTURE:
                                    intent = new Intent(MementoActivity.this, EditGestureActivity.class);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    startActivity(intent);
                                    break;
                                case TYPE_DRAW:
                                   /* intent = new Intent(MementoActivity.this, DrawingActivity.class);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    startActivity(intent);*/
                                    break;
                                case TYPE_BACKUP:
                                    backupData(MementoActivity.this);
                                    break;
                                case TYPE_RESTORE:
                                    restoreData(MementoActivity.this, new FilePickDialog.ImportListener() {
                                        @Override
                                        public void onSelect(String path) {
                                            restoreFromPath(path);
                                        }

                                        @Override
                                        public void onError(String msg) {

                                        }
                                    });
                                    break;
                                case TYPE_JSON_VIEW:
                                    viewJson(MementoActivity.this);
                                    break;
                                case TYPE_TEXT:
                                   /* intent = new Intent(MementoActivity.this, TextEditorActivity.class);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    startActivity(intent);*/
                                    break;

                                case TYPE_SETTINGS:
                                    RestoreOrinalKeywords(MementoActivity.this);
                                    break;

                                case TYPE_SYNCHRONIZE:
                                    synchronizeTask(MementoActivity.this);
                                    break;

                                case TYPE_JSON_TO_DB:
                                    dbToJson(MementoActivity.this);
                                    break;
                            }
                        }
                    });

                    interrupt();
                } catch (Exception ignored) {
                }
            }
        }.start();
    }

    public void restoreFromPath(String path) {
        new Thread() {
            @Override
            public void run() {
                try {
                    readBackupFile(MementoActivity.this, path);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment.loadItems();
                            Snackbar.make(fragment.fab != null ? fragment.fab : toolbar, R.string.data_restored, Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MementoActivity.this)
                                    .setTitle(R.string.restore_error)
                                    .setMessage(e.getMessage())
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    });
                } finally {
                    interrupt();
                }
            }
        }.start();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideStatusBar(this);
    }

    private void initGestureOverlay() {
        GestureLibrary mGestureLib = GestureManager.getInstance(this).getGestureLib();
        if (mGestureview == null) return;
        mGestureview.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE);
        mGestureview.setFadeOffset(0);
        mGestureview.setGestureStrokeWidth(10);
        mGestureview.setEventsInterceptionEnabled(false);
        mGestureview.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {

                //get rid of the selection bar before gesture
                if (fragment.getSelectionCount() > 0) fragment.toggleSelection(false);

                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    if (prediction.score > 1.0 && prediction.name.equals("back")) {
                        onBackPressed();
                    } else if (prediction.score > 1.0 && prediction.name.equals("refresh")) {
                        fragment.loadItems();
                    } else if (prediction.score > 1.0 && prediction.name.equals("left") /*&& ShowHtmlFragment.getTabIndex() != libIndex*/) {
                        libIndex--;
                        if (libIndex < 0) libIndex = App.getAllLibs().size() - 1;
                        changeFragment(libIndex);
                    } else if (prediction.score > 1.0 && prediction.name.equals("right")/* && ShowHtmlFragment.getTabIndex() != libIndex*/) {
                        libIndex++;
                        if (libIndex == App.getAllLibs().size()) libIndex = 0;
                        changeFragment(libIndex);
                    }
                }
            }
        });
    }


}
