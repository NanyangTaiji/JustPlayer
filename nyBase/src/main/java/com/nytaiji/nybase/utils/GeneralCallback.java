package com.nytaiji.nybase.utils;


import java.util.ArrayList;

public interface GeneralCallback {

    void SingleString(String path);

    void SingleBoolean(boolean yesOrNo);

    void MultiStrings(ArrayList<String> paths);
}

/*   usages
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example usage
        GeneralCallback callback = new GeneralCallback() {
            @Override
            public void SingleString(String path) {
                if (path != null) {
                    // Handle the callback
                    // Do something with the path
                }
            }

            @Override
            public void SingleBoolean(boolean yesOrNo) {
                // Handle the callback
                // Do something with the boolean
            }

            @Override
            public void MultiStrings(ArrayList<String> paths) {
                if (paths != null) {
                    // Handle the callback
                    // Do something with the list of paths
                }
            }
        };

        // Example method calls
        someMethod(callback, "example/path", true, new ArrayList<String>());
    }

    private void someMethod(GeneralCallback callback, String path, boolean flag, ArrayList<String> paths) {
        if (callback != null) {
            if (path != null) {
                callback.SingleString(path);
            }
            callback.SingleBoolean(flag);
            if (paths != null) {
                callback.MultiStrings(paths);
            }
        }
    }
}

 */
