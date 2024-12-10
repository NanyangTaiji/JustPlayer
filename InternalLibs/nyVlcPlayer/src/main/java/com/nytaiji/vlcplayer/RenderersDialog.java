package com.nytaiji.vlcplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.nytaiji.nybase.utils.AppContextProvider;

import org.videolan.libvlc.RendererItem;

public class RenderersDialog extends DialogFragment {

    private static final String ARG_RENDERERS = "arg_renderers";

    private SharedPreferences pref;
    private RendererItem[] renderers;
    private String selectedRenderName;

    // Callback interface for selected item
    public interface RenderersDialogListener {
        void onRendererSelected(RendererItem selectedRenderer);
    }

    private RenderersDialogListener listener;

    // Factory method to create a new instance of the fragment with renderers as a parameter
    public static RenderersDialog newInstance(RendererItem[] renderers) {
        RenderersDialog fragment = new RenderersDialog();
        Bundle args = new Bundle();
        args.putParcelableArray(ARG_RENDERERS, (Parcelable[]) renderers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        RendererDelegate rendererDelegate = RendererDelegate.getInstance(AppContextProvider.getAppContext());
        renderers = rendererDelegate.getRenderers().toArray(new RendererItem[0]);

        if (renderers == null || renderers.length == 0) {
            Toast.makeText(AppContextProvider.getAppContext(), "Not available, please try again later!", Toast.LENGTH_LONG).show();
            dismiss();
        }// Set the default selected renderer name
        else selectedRenderName = pref.getString("CURRENT_RENDER", renderers[0].displayName);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("ChromeCast Renderers")
                .setSingleChoiceItems(createRendererNamesArray(), findSelectedRendererIndex(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle item click
                        connect(renderers[which]);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private CharSequence[] createRendererNamesArray() {
        CharSequence[] rendererNames = new CharSequence[renderers.length];
        for (int i = 0; i < renderers.length; i++) {
            Log.e("RendererDialog", i+" = "+renderers[i].name+" "
                    +renderers[i].displayName+" "+renderers[i].type
            );
            rendererNames[i] = renderers[i].displayName;
        }
        return rendererNames;
    }

    private int findSelectedRendererIndex() {
        for (int i = 0; i < renderers.length; i++) {
            if (renderers[i].displayName.equals(selectedRenderName)) {
                return i;
            }
        }
        return -1;
    }

    public void connect(RendererItem item) {
        if (item != null) {
            if (listener != null) {
                listener.onRendererSelected(item);
            }

            if (!item.displayName.equals(selectedRenderName)) {
                selectedRenderName = item.displayName;
                pref.edit().putString("CURRENT_RENDER", selectedRenderName).apply();
            }
        }
    }

    // Method to set the listener
    public void setRenderersDialogListener(RenderersDialogListener listener) {
        this.listener = listener;
    }
}



