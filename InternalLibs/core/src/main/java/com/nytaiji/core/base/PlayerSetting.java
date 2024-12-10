package com.nytaiji.core.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;


import com.nytaiji.core.R;

import static android.view.View.GONE;
import static com.nytaiji.nybase.model.Constants.KEY_CACHE;
import static com.nytaiji.nybase.model.Constants.KEY_CAST;
import static com.nytaiji.nybase.model.Constants.KEY_ENCRYPT;
import static com.nytaiji.nybase.model.Constants.KEY_ENDING;
import static com.nytaiji.nybase.model.Constants.KEY_MEMBER;
import static com.nytaiji.nybase.model.Constants.KEY_PIP;
import static com.nytaiji.nybase.model.Constants.KEY_PLAYER;
import static com.nytaiji.nybase.model.Constants.KEY_RENDER;
import static com.nytaiji.nybase.model.Constants.LEVEL0_DEFAULT;
import static com.nytaiji.nybase.model.Constants.MAIN_SETTINGS;

public class PlayerSetting {
    private final Context context;
    private final SharedPreferences sharedPrefs;

    public PlayerSetting(Context context) {
        this.context = context;
           sharedPrefs = context.getSharedPreferences(MAIN_SETTINGS, Context.MODE_PRIVATE);
        // sharedPrefs = App.generalPrefs;
    }

    public void playerDialog() {

        LayoutInflater li = LayoutInflater.from(context);
        View root = li.inflate(com.nytaiji.core.R.layout.player_external_setting_layout, null);

        RelativeLayout verify = root.findViewById(com.nytaiji.core.R.id.verify);
        RadioGroup rga = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_cache_group);
        RadioGroup rgi = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_pip_group);
        RadioGroup rgc = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_cast_group);
     //   RadioGroup rge = (RadioGroup) root.findViewById(R.id.setting_encrypt_group);
        RadioGroup rgr = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_render_group);
        RadioGroup rgp = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_player_group);
        RadioGroup rgv = (RadioGroup) root.findViewById(com.nytaiji.core.R.id.setting_video_group);

        EditText encryptCode = root.findViewById(com.nytaiji.core.R.id.encryptCode);

      //  if (MainActivity.developerMode) {
     //       rge.setVisibility(VISIBLE);

     //   } else {
        //    rge.setVisibility(GONE);
   //     }

       // AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(com.nytaiji.core.R.string.playerSetting)
                .setPositiveButton(com.nytaiji.core.R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })

                .setNegativeButton(com.nytaiji.core.R.string.defaultSetting, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPrefs.edit().putInt(KEY_CAST, 0).apply();
                        rgc.check(com.nytaiji.core.R.id.Cast_No);

                        sharedPrefs.edit().putInt(KEY_PLAYER, 0).apply();
                        rgp.check(R.id.exoplayer);

                        sharedPrefs.edit().putInt(KEY_RENDER, 1).apply();
                        rgr.check(R.id.surface);

                      //  sharedPrefs.edit().putInt(KEY_MEMBER, 1).apply();
                      //  rge.check(R.id.grade1);

                        sharedPrefs.edit().putInt(KEY_PIP, 0).apply();
                        rgi.check(com.nytaiji.core.R.id.pip_No);

                        sharedPrefs.edit().putInt(KEY_ENDING, 2).apply();
                        rgv.check(com.nytaiji.core.R.id.endOut);

                        sharedPrefs.edit().putBoolean(KEY_CACHE, false).apply();
                        rga.check(com.nytaiji.core.R.id.Cast_No);

                        sharedPrefs.edit().putString(KEY_ENCRYPT, LEVEL0_DEFAULT).apply();
                        encryptCode.setText(LEVEL0_DEFAULT);
                    }
                })

                .create();

        builder.setView(root);

        //Casting choice
        int castable = sharedPrefs.getInt(KEY_CAST, 0);
        rgc.check(castable == 0 ? com.nytaiji.core.R.id.Cast_No : com.nytaiji.core.R.id.Cast_Yes);
        rgc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == com.nytaiji.core.R.id.Cast_No) {
                    sharedPrefs.edit().putInt(KEY_CAST, 0).apply();
                } else {
                    sharedPrefs.edit().putInt(KEY_CAST, 1).apply();
                }
            }
        });

        int PiPtable = sharedPrefs.getInt(KEY_PIP, 0);
        rgi.check(PiPtable == 0 ? com.nytaiji.core.R.id.pip_No : com.nytaiji.core.R.id.pip_Yes);
        rgi.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == com.nytaiji.core.R.id.pip_No) {
                    sharedPrefs.edit().putInt(KEY_PIP, 0).apply();
                } else {
                    sharedPrefs.edit().putInt(KEY_PIP, 1).apply();
                }
            }
        });

        boolean onlineCache = sharedPrefs.getBoolean(KEY_CACHE, false);
        rga.check(onlineCache ? com.nytaiji.core.R.id.cache_Yes : com.nytaiji.core.R.id.cache_No);
        rga.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == com.nytaiji.core.R.id.cache_No) {
                    sharedPrefs.edit().putBoolean(KEY_CACHE, false).apply();
                } else {
                    sharedPrefs.edit().putBoolean(KEY_CACHE, true).apply();
                }
            }
        });

        //playmode
        int playmode = sharedPrefs.getInt(KEY_ENDING, 2);
            if (playmode == 0) {
                rgv.check(com.nytaiji.core.R.id.tocontinue);
            } else if (playmode == 1) {
                rgv.check(com.nytaiji.core.R.id.repeat);
            } else if (playmode == 2) {
                rgv.check(com.nytaiji.core.R.id.endOut);
            }

            rgv.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == com.nytaiji.core.R.id.tocontinue) {
                        sharedPrefs.edit().putInt(KEY_ENDING, 0).apply();
                    } else if (checkedId == com.nytaiji.core.R.id.repeat) {
                        sharedPrefs.edit().putInt(KEY_ENDING, 1).apply();
                    } else if (checkedId == com.nytaiji.core.R.id.endOut) {
                        sharedPrefs.edit().putInt(KEY_ENDING, 2).apply();
                    }
                }
            });

        encryptCode.setText(sharedPrefs.getString(KEY_ENCRYPT, LEVEL0_DEFAULT));
        Button ecConfirm = root.findViewById(com.nytaiji.core.R.id.ec_confirm);
        ecConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String encryptDefault = encryptCode.getText().toString().trim();
                if (encryptDefault.length() == 16) {
                    sharedPrefs.edit().putString(KEY_ENCRYPT, encryptDefault).apply();
                } else {
                    encryptCode.setText(sharedPrefs.getString(KEY_ENCRYPT, LEVEL0_DEFAULT));
                    //  sharedPrefs.edit().putString(KEY_ENCRYPT, ENCRYPT_DEFAULT).apply();
                    Toast.makeText(context, com.nytaiji.core.R.string.restoreCode, Toast.LENGTH_LONG).show();
                }
            }
        });

        //Encryption choice
        int encrypted = sharedPrefs.getInt(KEY_MEMBER, 1);

        verify.setVisibility(GONE);

        /*
        if (MainActivity.developerMode) {
            if (encrypted == 0) {
                rge.check(R.id.grade0);
                verify.setVisibility(VISIBLE);
            } else if (encrypted == 1) {
                rge.check(R.id.grade1);
            } else if (encrypted == 2) {
                rge.check(R.id.grade2);
            } else if (encrypted == 3) {
                rge.check(R.id.grade3);
            } else if (encrypted == 4) {
                rge.check(R.id.grade4);
            } else if (encrypted == 5) {
                rge.check(R.id.grade5);
            }


            rge.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    verify.setVisibility(GONE);
                    if (checkedId == R.id.grade0) {
                        verify.setVisibility(VISIBLE);
                        //passwordInput(root);
                    } else if (checkedId == R.id.grade1) {
                        sharedPrefs.edit().putInt(KEY_MEMBER, 1).apply();
                    } else if (checkedId == R.id.grade2) {
                        sharedPrefs.edit().putInt(KEY_MEMBER, 2).apply();
                    } else if (checkedId == R.id.grade3) {
                        sharedPrefs.edit().putInt(KEY_MEMBER, 3).apply();
                    } else if (checkedId == R.id.grade4) {
                        sharedPrefs.edit().putInt(KEY_MEMBER, 4).apply();
                    } else if (checkedId == R.id.grade5) {
                        verify.setVisibility(VISIBLE);
                        passwordOK(root, "20090418");
                        //  securityHelper.authenticate("Nanyang Explorer", "Use device pattern to continue");
                        //  if(securityHelper.isDeviceSecure()) sharedPrefs.edit().putInt(KEY_ID, 5).apply();
                    }
                }
            });

        }*/

        //render choice
        int render = sharedPrefs.getInt(KEY_RENDER, 2);
        int renderid = com.nytaiji.core.R.id.glsurface;

        if (render == 0) {
            renderid = com.nytaiji.core.R.id.texture;
        } else if (render == 1) {
            renderid = com.nytaiji.core.R.id.surface;
        } else if (render == 2) {
            renderid = com.nytaiji.core.R.id.glsurface;
            //  } else if (render == 3) {
            //     renderid = R.id.sphere;
        }
        rgr.check(renderid);
        rgr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == com.nytaiji.core.R.id.texture) {
                    sharedPrefs.edit().putInt(KEY_RENDER, 0).apply();

                } else if (checkedId == com.nytaiji.core.R.id.surface) {
                    sharedPrefs.edit().putInt(KEY_RENDER, 1).apply();

                } else if (checkedId == com.nytaiji.core.R.id.glsurface) {
                    sharedPrefs.edit().putInt(KEY_RENDER, 2).apply();
                    //    } else if (checkedId == R.id.sphere) {
                    //       sharedPrefs.edit().putInt(KEY_RENDER, 3).apply();
                }
            }
        });
        //render choice

        int player = sharedPrefs.getInt(KEY_PLAYER, 0);

        rgp.check(player == 0 ? com.nytaiji.core.R.id.exoplayer : player == 1 ? com.nytaiji.core.R.id.mediaplayer : player == 2 ? R.id.ijkplayer :com.nytaiji.core.R.id.vlcplayer);
        rgp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == com.nytaiji.core.R.id.exoplayer) {
                    sharedPrefs.edit().putInt(KEY_PLAYER, 0).apply();
                } else if (checkedId == com.nytaiji.core.R.id.mediaplayer) {
                    sharedPrefs.edit().putInt(KEY_PLAYER, 1).apply();
                } else if (checkedId == com.nytaiji.core.R.id.ijkplayer) {
                    sharedPrefs.edit().putInt(KEY_PLAYER, 2).apply();
                } else if (checkedId == com.nytaiji.core.R.id.vlcplayer) {
                    sharedPrefs.edit().putInt(KEY_PLAYER, 3).apply();
                }
            }
        });

        builder.show();
    }

    private void passwordOK(View view, String password) {
        EditText inputPW = (EditText) view.findViewById(com.nytaiji.core.R.id.password);
        Button confirm = (Button) view.findViewById(com.nytaiji.core.R.id.input_confirm);
        RelativeLayout verify = view.findViewById(com.nytaiji.core.R.id.verify);
        RadioGroup rge = (RadioGroup) view.findViewById(com.nytaiji.core.R.id.setting_encrypt_group);

        final boolean verifiedOk;
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputPW.getText().toString().trim().equals(password)) {
                    sharedPrefs.edit().putInt(KEY_MEMBER, 5).apply();
                } else {
                    rge.check(com.nytaiji.core.R.id.grade1);
                    Toast.makeText(context, "Wrong Code", Toast.LENGTH_LONG).show();
                }
                verify.setVisibility(GONE);
            }
        });

    }
}
