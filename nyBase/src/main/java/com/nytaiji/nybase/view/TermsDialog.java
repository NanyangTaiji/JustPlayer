package com.nytaiji.nybase.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.nytaiji.nybase.R;
import com.nytaiji.nybase.utils.PreferenceHelper;


public class TermsDialog {
    Context activity;
    AlertDialog dialog;

    TermCallback termCallback;

    public TermsDialog(Context context, TermCallback termCallback) {
        this.activity = context;
        this.termCallback = termCallback;
    }

    public void show() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        View v = ((FragmentActivity) activity).getLayoutInflater().inflate(R.layout.dialog_terms, null);
        dialogBuilder.setView(v);
        dialogBuilder.setCancelable(false);
        Button close = v.findViewById(R.id.term_close);
        Button agree = v.findViewById(R.id.term_agree);
        agree.setAlpha(.75f);
        agree.setEnabled(false);
        CheckBox checkBox = v.findViewById(R.id.term_checkbox);
        Toast.makeText(activity, "Scroll and read to end to enable \"Agree", Toast.LENGTH_LONG).show();

        ScrollView scrollView = v.findViewById(R.id.term_content);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
            int topDetector = scrollView.getScrollY();
            int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
            if (bottomDetector == 0) {
                checkBox.setEnabled(true);
            }
        });
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkBox.isChecked()) {
                agree.setAlpha(1);
                agree.setEnabled(true);
            } else {
                agree.setAlpha(.75f);
                agree.setEnabled(false);
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PreferenceHelper.getInstance().setBoolean(R.string.key_terms_con, true);
                termCallback.agreedTerms(true);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                termCallback.agreedTerms(false);
            }
        });
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public interface TermCallback {
        void agreedTerms(boolean yesOrno);
    }
}
