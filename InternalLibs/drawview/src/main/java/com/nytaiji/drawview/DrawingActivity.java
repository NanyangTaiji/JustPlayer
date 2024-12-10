package com.nytaiji.drawview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nytaiji.drawview.dialogs.SaveBitmapDialog;
import com.nytaiji.drawview.enums.BackgroundScale;
import com.nytaiji.drawview.enums.BackgroundType;
import com.nytaiji.drawview.enums.DrawingCapture;
import com.nytaiji.drawview.views.AdvDrawView;

import java.io.File;

import static com.nytaiji.drawview.utils.ImageLoader.REQUEST_CODE_CHOOSE_HTML;
import static com.nytaiji.drawview.utils.ImageLoader.REQUEST_CODE_CHOOSE_IMAGE;
import static com.nytaiji.drawview.utils.ImageLoader.getClipboardItem;
import static com.nytaiji.drawview.utils.ImageLoader.getPath;
import static com.nytaiji.drawview.utils.ImageLoader.openDirChooseFile;


public class DrawingActivity extends AppCompatActivity {

    //region CONSTANTS
    private final int STORAGE_PERMISSIONS = 1000;
    private final int STORAGE_PERMISSIONS2 = 2000;
    //endregion

    //region VIEWS
    private AdvDrawView mDrawView;
    private int requestCode;

    //region EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        mDrawView = findViewById(R.id.advdraw_view);
        // setupToolbar
        //region VIEWS
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.app_Name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_draw_save) {
            requestPermissions(0);
      /*  } else if (itemId == R.id.action_view_camera_option) {
           /* Intent i = new Intent(this, CameraActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();*/
        } else if (itemId == R.id.action_draw_background) {
            requestPermissions(1);
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestPermissions(int option) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (option == 0 || option == 1) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            option == 1 ? STORAGE_PERMISSIONS : STORAGE_PERMISSIONS2);
                } else {
                    if (option == 0) saveDraw();
                    else chooseBackgroundImage("image/*");
                }
            }
        } else {
            switch (option) {
                case 0:
                    saveDraw();
                    break;
                case 1:
                    chooseBackgroundImage("image/*");
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == grantResults.length) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switch (requestCode) {
                        case STORAGE_PERMISSIONS:
                            saveDraw();
                            break;
                        case STORAGE_PERMISSIONS2:
                            chooseBackgroundImage("image/*");
                            break;
                    }
                }
            }, 300);
        }
    }
    //endregion

    private void saveDraw() {
       /* final File file = new File(Environment.getExternalStorageDirectory(),"bundle.txt");
        String state = BundleToString((Bundle) mDrawView.getCurrentState());
        saveStrToNormalFile(state, file);*/
        SaveBitmapDialog saveBitmapDialog = SaveBitmapDialog.newInstance();
        //TODO
        Object[] createCaptureResponse = mDrawView.getPaint().createCapture(DrawingCapture.BITMAP);
        saveBitmapDialog.setPreviewBitmap((Bitmap) createCaptureResponse[0]);
        saveBitmapDialog.setPreviewFormat(String.valueOf(createCaptureResponse[1]));
        saveBitmapDialog.setOnSaveBitmapListener(new SaveBitmapDialog.OnSaveBitmapListener() {
            @Override
            public void onSaveBitmapCompleted() {
                Toast.makeText(DrawingActivity.this, "Capture saved succesfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSaveBitmapCanceled() {
                Toast.makeText(DrawingActivity.this, "Capture saved cancelled!", Toast.LENGTH_SHORT).show();
            }
        });
        saveBitmapDialog.show(getSupportFragmentManager(), "Save Bitmap");
    }


    String mediaLink = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("DrawingActivity", "data= " + data.toString());

        mediaLink = getPath(this, data.getData());
        if (mediaLink != null) {
            //for Huawei pad
            mediaLink = mediaLink.replace("content://com.android.externalstorage.documents/document/", "/storage/");
            mediaLink = mediaLink.replace(":", "/");
            openLink(mediaLink);
        } else return;
        openLink(mediaLink);

    }

    private void openLink(String mediaLink) {
        mDrawView.getPaint().setBackgroundImage(new File(mediaLink), BackgroundType.FILE, BackgroundScale.CENTER_INSIDE);
        Bitmap bitmap = (Bitmap) mDrawView.getBackgroundImage();
        if (bitmap != null)
            Log.e("DrawingActivity", "mDrawView.getBackgroundImage() " + bitmap.getRowBytes());
    }

    private void chooseBackgroundImage(String mimeType) {
        final View root = this.getLayoutInflater().inflate(R.layout.dialog_enter_media_url, null);
        EditText dialogUrlEdittext = root.findViewById(R.id.dialog_url_edittext);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(root)
                .setTitle("Enter Media")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // get the inputted URL string
                    mediaLink = dialogUrlEdittext.getText().toString();
                    mDrawView.getPaint().setBackgroundImage(
                            mediaLink,
                            BackgroundType.URL,
                            BackgroundScale.CENTER_CROP);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

        // paste whatever there is in the clipboard (hopefully it is a video url)
        if (getClipboardItem(this) != null) {
            String charSequence = getClipboardItem(this).toString();
            if (charSequence.contains("http")) {
                dialogUrlEdittext.setText(charSequence);
            }
        } else dialogUrlEdittext.setText("");
        // clear URL edittext button
        root.findViewById(R.id.dialog_url_clear_button).setOnClickListener(v ->
                dialogUrlEdittext.setText("")
        );

        View localFile = root.findViewById(R.id.dialog_file_selection);
        localFile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        requestCode = REQUEST_CODE_CHOOSE_HTML;
                        openDirChooseFile(DrawingActivity.this, mimeType, requestCode);

                    }
                }
        );

        View localMedia = root.findViewById(R.id.dialog_uri_selection);
        //  localMedia.setVisibility(requestCode == REQUEST_CODE_EDITOR_YOUTUBE || requestCode == REQUEST_CODE_CHOOSE_HTML ? View.GONE : VISIBLE);
        localMedia.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        requestCode = REQUEST_CODE_CHOOSE_IMAGE;
                        openDirChooseFile(DrawingActivity.this, mimeType, requestCode);
                    }
                }
        );
    }

}
