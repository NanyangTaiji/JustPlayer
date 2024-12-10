package com.nytaiji.nybase.playlist;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.amaze.filemanager.fileoperations.filesystem.OpenMode;
import com.nytaiji.nybase.NyDisplayFragment;
import com.nytaiji.nybase.R;
import com.nytaiji.nybase.model.NyHybrid;
import com.nytaiji.nybase.utils.NyFileUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDialog {

    private Context context;
    private PlaylistHelper playlistHelper;

    private AlertDialog dialog;
    private View dialogView;
    private RadioGroup securityRadioGroup;

    private List<Playlist> allPlaylist;
    private int positionSelected = 0;

    private String currentFile = null;

    private long playlistId;

    private ListFileListner listFileListner = null;

    private OpenMode currentMode = null;

    public PlaylistDialog(Context context) {
        this.context = context;
        playlistHelper = PlaylistHelper.getInstance(context.getApplicationContext());
    }

    public void setListFileListner(ListFileListner listFileListner) {
        this.listFileListner = listFileListner;
    }


    public void showDialog(NyHybrid file) {
        showDialog(file.getPath(), file.getMode(), null);
    }

    public void showDialog(NyHybrid file, ListFileListner listFileListner) {
        showDialog(file.getPath(), file.getMode(), listFileListner);
    }

    public void showDialog(final String fileUri, final OpenMode openMode) {
        showDialog(fileUri, openMode, null);
    }


    public void showDialog(final String fileUri, final OpenMode openMode, ListFileListner listFileListner) {
        this.listFileListner = listFileListner;
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.dialog_playlist, null);
        final LinearLayout inputPanel = dialogView.findViewById(R.id.input_panel);
        final EditText playlistNameEditText = dialogView.findViewById(R.id.playlist_name_edittext);
        final ListView playlistsListView = dialogView.findViewById(R.id.playlists_listview);
        securityRadioGroup = dialogView.findViewById(R.id.security_radio_group);
        currentFile = fileUri;
        currentMode = openMode;
        // Populate the ListView with existing playlists
        allPlaylist = playlistHelper.getAllPlaylists();
        List<String> playlists = new ArrayList<>();
        for (Playlist file : allPlaylist) {
            playlists.add(file.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, playlists);
        playlistsListView.setAdapter(adapter);

        // Handle item click on the ListView to add the file to the selected playlist
        playlistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //disable if the input panel is still waiting
                if (inputPanel.getVisibility() != View.GONE) return;

                String selectedPlaylistName = playlists.get(position);
                playlistId = playlistHelper.getPlaylistIdByName(selectedPlaylistName);
                if (currentFile == null || currentFile.isEmpty()) {
                    //display all files in the playlist
                    showFilesInPlaylistDialog(playlistId);
                    dialog.dismiss();
                } else {  //add path to the playlst
                    // Check if the file already exists in the selected playlist
                    if (!playlistHelper.fileExistsInPlaylist(playlistId, currentFile)) {
                        // File doesn't exist in the playlist, proceed with adding the file
                        playlistHelper.addFileToPlaylist(playlistId, currentFile, getSelectedSecurity(), currentMode);
                        Toast.makeText(context, "File added to " + selectedPlaylistName, Toast.LENGTH_SHORT).show();
                    } else {
                        // File already exists in the playlist, display error message
                        Toast.makeText(context, "File already exists in " + selectedPlaylistName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // Add onItemLongClick listener to display a menu of playlists
        playlistsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //disable if the input panel is still waiting
                if (inputPanel.getVisibility() != View.GONE) return false;
                String selectedPlaylistName = playlists.get(position);
                playlistId= playlistHelper.getPlaylistIdByName(selectedPlaylistName);
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.playlist_menu); // Inflate the playlist menu XML
                popupMenu.getMenu().findItem(R.id.menu_share).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_webShare).setVisible(false);
                //  popupMenu.getMenu().findItem(R.id.add_to_playlist).setVisible(false);
                // Set listener for delete item
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_delete) {
                            playlistHelper.deletePlaylist(playlistId);
                            allPlaylist = playlistHelper.getAllPlaylists();
                            playlists.remove(position);
                            adapter.notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.menu_edit) {
                            positionSelected = position;
                            inputPanel.setVisibility(View.VISIBLE);
                            playlistNameEditText.setText(playlists.get(position));
                            setSecurityRadioGroup(allPlaylist.get(position).isSecure());
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                            return true;
                        } else if (itemId == R.id.add_to_playlist) {
                            currentFile = playlists.get(position);
                            currentMode = OpenMode.PLAY_LIST;
                            playlists.remove(position);
                            adapter.notifyDataSetChanged();
                            dialog.setTitle("Add to Playlist");
                            //  showDialog(currentFile, currentMode);
                            return true;
                        }
                        return false;
                    }
                });

                // Show the PopupMenu at the clicked position
                popupMenu.show();
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setNegativeButton("Cancel", null);

        builder.setPositiveButton("Add Playlist", null); // Set an empty OnClickListener

        builder.setNeutralButton("Edit Confirm", null); // Set an empty OnClickListener

        if (currentFile == null || currentFile.isEmpty()) builder.setTitle("Playlists");
        else builder.setTitle("Add to Playlist");

        dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
        // Now, you can handle the positive button click separately
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = dialogView.findViewById(R.id.playlist_name_edittext);
                // RadioGroup radioGroup = dialogView.findViewById(R.id.security_radio_group);
                if (inputPanel.getVisibility() == View.GONE) {
                    inputPanel.setVisibility(View.VISIBLE);
                    return;
                }

                String playlistName = editText.getText().toString().trim();

                if (!playlistName.isEmpty()) {
                    // Check if the playlist name already exists
                    if (!playlistHelper.playlistExists(playlistName)) {
                        // Playlist name doesn't exist, proceed with adding the new playlist
                        playlistId = playlistHelper.createPlaylist(playlistName, getSelectedSecurity());

                        if (currentFile != null && !currentFile.isEmpty()) {
                            playlistHelper.addFileToPlaylist(playlistId, currentFile, getSelectedSecurity(), currentMode);
                        }
                        inputPanel.setVisibility(View.GONE);
                        allPlaylist = playlistHelper.getAllPlaylists();
                        // Refresh the ArrayAdapter to update new additions
                        editText.setText("");
                        playlists.add(playlistName);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(context, playlistName + " added to PlayLists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Playlist name already exists, display error message
                        Toast.makeText(context, playlistName + " already exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Please enter playlist name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = dialogView.findViewById(R.id.playlist_name_edittext);
                // editText.setText(playlists.get(positionSelected));
                String newName = editText.getText().toString().trim();
                if (!newName.isEmpty()) {
                    playlistHelper.updatePlaylist(playlistId, newName, getSelectedSecurity());
                    allPlaylist = playlistHelper.getAllPlaylists();
                    editText.setText("");
                    playlists.set(positionSelected, newName);
                    adapter.notifyDataSetChanged();
                    inputPanel.setVisibility(View.GONE);
                   // Toast.makeText(context, newName + " is updated!", Toast.LENGTH_SHORT).show();
                    dialogView.findViewById(R.id.security_radio_group).setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(context, "Please enter playlist name", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean getSelectedSecurity() {
        RadioButton secureRadioButton = dialogView.findViewById(securityRadioGroup.getCheckedRadioButtonId());
        return secureRadioButton.getId() == R.id.secure_yes_radio;
    }

    public void setSecurityRadioGroup(boolean isSecure) {
        RadioButton secureRadioButton = dialogView.findViewById(R.id.secure_yes_radio);
        RadioButton normalRadioButton = dialogView.findViewById(R.id.secure_no_radio);
        if (isSecure) {
            secureRadioButton.setChecked(true);
            normalRadioButton.setChecked(false);
        } else {
            secureRadioButton.setChecked(false);
            normalRadioButton.setChecked(true);
        }
    }


    //--------------------//
    private AlertDialog filesDialog;

    private List<PlaylistFile> filesInPlaylist;
    private List<String> fileNamesInPlaylist = new ArrayList<>();


    public void showFilesInPlaylistDialog(long playlistId) {
        // Retrieve files in the playlist from the database
        PlaylistHelper playlistHelper = PlaylistHelper.getInstance(context);
        filesInPlaylist = playlistHelper.getFilesInPlaylist(playlistId);

        for (PlaylistFile file : filesInPlaylist) {
            fileNamesInPlaylist.add(NyFileUtil.getLastSegmentFromString(file.getPath()));
        }
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_files_in_playlist, null);
        builder.setView(dialogView);

        ListView filesListView = dialogView.findViewById(R.id.files_in_playlist);
        ArrayAdapter<String> filesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, fileNamesInPlaylist);
        filesListView.setAdapter(filesAdapter);
        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = filesInPlaylist.get(position).getPath();
                OpenMode mode = filesInPlaylist.get(position).getOpenMode();
                NyHybrid file = new NyHybrid(mode, filePath);
                if (mode != OpenMode.PLAY_LIST) {
                    if (!file.isDirectory(context)) {
                        proccesFile(filesInPlaylist.get(position));
                        filesDialog.dismiss();
                    } else {//is a directory
                        ArrayList<NyHybrid> listFile = file.listChildrenFiles(context);
                        filesInPlaylist.remove(position);
                        fileNamesInPlaylist.remove(position);
                        for (NyHybrid hybridParcelable : listFile) {
                            filesInPlaylist.add(new PlaylistFile(hybridParcelable.getPath(), false, hybridParcelable.getMode()));
                            fileNamesInPlaylist.add(NyFileUtil.getLastSegmentFromString(hybridParcelable.getPath()));
                        }
                        filesAdapter.notifyDataSetChanged();
                    }
                } else if (mode == OpenMode.PLAY_LIST) {
                    long currentId = playlistHelper.getPlaylistIdByName(fileNamesInPlaylist.get(position));
                    List<PlaylistFile> filesInPlaylistNew = playlistHelper.getFilesInPlaylist(currentId);

                    filesInPlaylist.remove(position);
                    fileNamesInPlaylist.remove(position);
                    for (PlaylistFile file1 : filesInPlaylistNew) {
                        fileNamesInPlaylist.add(NyFileUtil.getLastSegmentFromString(file1.getPath()));
                        filesInPlaylist.add(file1);
                    }
                    filesAdapter.notifyDataSetChanged();
                    // showFilesInPlaylistDialog(currentId);
                } else {
                    if (mode == OpenMode.FILE) {
                        playlistHelper.removeFileFromPlaylist(playlistId, filePath);
                        fileNamesInPlaylist.remove(position);
                        filesAdapter.notifyDataSetChanged();
                        Toast.makeText(context, "The file does not exist anymore", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Add onItemLongClick listener to display a menu of playlists
        filesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.playlist_menu); // Inflate the playlist menu XML
                popupMenu.getMenu().findItem(R.id.menu_edit).setVisible(false);
                // Set listener for delete item
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_delete) {
                            playlistHelper.removeFileFromPlaylist(playlistId, filesInPlaylist.get(position).getPath());
                            fileNamesInPlaylist.remove(position);
                            filesAdapter.notifyDataSetChanged();
                            return true;
                        } else if (itemId == R.id.menu_share) {// Handle share action
                            return true;
                        } else if (itemId == R.id.menu_webShare) {// Handle web share action
                            return true;
                        } else if (itemId == R.id.add_to_playlist) {
                            filesDialog.dismiss();
                            new PlaylistDialog(context).showDialog(filesInPlaylist.get(position).getPath(), filesInPlaylist.get(position).getOpenMode(),null);
                            return true;
                        }
                        return false;
                    }
                });

                // Show the PopupMenu at the clicked position
                popupMenu.show();
                return true;
            }
        });

        builder.setTitle("Files in Playlist")
                .setNegativeButton("Close", null);

        filesDialog = builder.create();
        filesDialog.show();
    }


    private void proccesFile(PlaylistFile file) {
        if (listFileListner==null) {
            NyDisplayFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), new NyHybrid(file.getOpenMode(), file.getPath()));
        } else{
            listFileListner.onFileFound(file);
        }
    }

    public interface ListFileListner {
        void onFileFound(PlaylistFile file);
    }

}

