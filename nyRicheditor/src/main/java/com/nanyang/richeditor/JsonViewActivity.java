package com.nanyang.richeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.nanyang.richeditor.view.JsonViewLayout;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonViewActivity extends AppCompatActivity {

    private JsonViewLayout jsonViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_view);
        jsonViewLayout = findViewById(R.id.json);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        final File file = new File(NyFileUtil.getPath(this, uri));
        if (!file.exists()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    is = new FileInputStream(file);
                  //  is = getAssets().open("json.json");
                    int lenght = is.available();
                    byte[] buffer = new byte[lenght];
                    is.read(buffer);
                    final String result = new String(buffer, "utf8");
                    is.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            jsonViewLayout.bindJson(result);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_json, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_expend:
                jsonViewLayout.expandAll();
                break;
            case R.id.action_collapse:
                jsonViewLayout.collapseAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
