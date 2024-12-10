package com.nanyang.richeditor.memento;


import static com.nanyang.richeditor.database.DatabaseModel.TYPE_WEBSITE;
import static com.nanyang.richeditor.editor.EditorUtils.REQUEST_CODE;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_CANCEL;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_DELETE;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_EDIT;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_NEW;
import static com.nanyang.richeditor.editor.EditorUtils.codeEditDialog;
import static com.nanyang.richeditor.editor.EditorUtils.hideSoftInput;
import static com.nanyang.richeditor.editor.EditorUtils.searchDialog;
import static com.nanyang.richeditor.editor.EditorUtils.sorts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.nanyang.richeditor.App;
import com.nanyang.richeditor.MementoActivity;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Category;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.OpenHelper;
import com.nanyang.richeditor.database.Note;
import com.nanyang.richeditor.util.TTSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class NoteFragment extends BaseFragment<Note, NoteAdapter> {
    private static final String TAG = "ShowNoteFragment";
    private int checkedItem = App.sortNotesBy;
    private View selectedSpeech;

    public static void show(FragmentManager fm, long id) {
        Bundle arguments = new Bundle();
        NoteFragment fragment = new NoteFragment();
        fragment.setCategoryId(id);
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static NoteFragment get(FragmentManager fm) {
        return (NoteFragment) fm.findFragmentByTag(TAG);
    }

    public static void remove(FragmentManager fm) {
        NoteFragment noteFragment = get(fm);

        if (null != noteFragment) {
            //  showNoteFragment.Destroy();
            noteFragment.onDestroy();
            fm.beginTransaction().remove(noteFragment).commitAllowingStateLoss();
        }
    }

    public NoteFragment() {
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_show_note;
    }

    @Override
    public String getItemName() {
        return "note";
    }

    @Override
    public Class<NoteAdapter> getAdapterClass() {
        return NoteAdapter.class;
    }

    @Override
    public BaseAdapter.ClickListener getListener() {
        return listener;
    }

    private final BaseAdapter.ClickListener listener = new BaseAdapter.ClickListener() {
        @Override
        public void onClick(DatabaseModel item, int position) {
            mItem = item;
            mPosition = position;
            checkedOpen(item, position);
        }

        @Override
        public void onChangeSelection(boolean haveSelected) {
            if (getCategoryId() == DatabaseModel.SEARCH_CLICK || getCategoryId() == DatabaseModel.SEARCH_SELECT)
                return;
            toggleSelection(haveSelected);
        }

        @Override
        public void onCountSelection(int count) {
            if (getCategoryId() == DatabaseModel.SEARCH_CLICK || getCategoryId() == DatabaseModel.SEARCH_SELECT)
                return;
            if (selectedSpeech != null)
                selectedSpeech.setVisibility(count == 1 ? View.VISIBLE : View.GONE);
            onSelectonCounterChange(count);
        }
    };

    @Override
    public void setCategoryId(long id) {
        if (id > 0) {
            previousId = id;
            Category category = new Controller(App.instance).findCategoryById(id);
            toolBarTitle = category.title;
            categoryTheme = category.theme;
            categoryKeywords = category.keywords;
            categoryPosition = category.position;
        } else {
            toolBarTitle = "Search";
        }
    }

    @Override
    public void initExtraViews(View view) {

        ((MementoActivity) requireActivity()).fragment = this;

        selectionToolbar.findViewById(R.id.selection_copy).setVisibility(View.VISIBLE);
        selectionToolbar.findViewById(R.id.selection_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotes = new ArrayList<>(selected);
                if (containProtectedItem()) unlockDialog(JOB_COPY, null);
                else {
                    noteCopyDialog(false);
                }

            }
        });
        selectionToolbar.findViewById(R.id.selection_move).setVisibility(View.VISIBLE);
        selectionToolbar.findViewById(R.id.selection_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNotes = new ArrayList<>(selected);
                if (containProtectedItem()) unlockDialog(JOB_MOVE, null);
                else {
                    noteCopyDialog(true);
                }

            }
        });

        //  selectionToolbar.findViewById(R.id.selection_merge_up).setVisibility(View.GONE);
        //  selectionToolbar.findViewById(R.id.selection_merge_down).setVisibility(View.GONE);

        selectedSpeech = selectionToolbar.findViewById(R.id.selection_speech);
        selectedSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadSelected();
            }
        });

        requireActivity().findViewById(R.id.app_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(false);
                if (searchResults != null)
                    searchDialog(requireActivity(), searchCallback, searchResults);
                else
                    searchDialog(requireActivity(), searchCallback, getCategoryId());
            }
        });

        requireActivity().findViewById(R.id.app_sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity(), R.style.DialogTheme);

                dialog.setSingleChoiceItems(sorts, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Category category = new Controller(App.instance).findCategoryById(getCategoryId());
                        category.sortBy = String.valueOf(which);
                        category.save();
                        dialog.dismiss();
                        loadItems();
                    }
                });
                dialog.setTitle("Sort by");
                dialog.setCancelable(true)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog.show();
            }
        });

        //only applicable after init
        showToolbarTitle();
    }

    @Override
    protected void pickupAction(int action) {
        super.pickupAction(action);
        if (action == JOB_COPY) noteCopyDialog(false);
        if (action == JOB_MOVE) noteCopyDialog(true);
        if (action == JOB_MERGE_UP) processMerge(true);
        if (action == JOB_MERGE_DOWN) processMerge(false);
    }


    public void startSearch() {    //be directly called from EditorActivity
        toggleSelection(false);
        if (searchResults != null)
            searchDialog(requireActivity(), searchCallback, searchResults);
        else
            searchDialog(requireActivity(), searchCallback, -1);
    }


    @Override
    public void onEditSelected() {
        if (!selected.isEmpty()) {//editing
            mItem = selected.remove(0);
            mPosition = items.indexOf(mItem);
            toggleSelection(false);
            //  categoryDialogTheme = mItem.theme;
            if (needOpenCheck((Note) mItem)) {
                unlockDialog(JOB_EDIT_NOTE, mItem.secureKey);
            } else proceedEdit();
        }
    }

    @Override
    public void proceedEdit() {
        refreshItem(mPosition);
        codeEditDialog(requireActivity(), (Note) mItem);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (data == null) {
            //to handle activity crash
            // Log.e(TAG, "resultCode ========================="+resultCode);
            requireActivity().onBackPressed();
            // refreshAll();
            return;
        }

        if (requestCode == REQUEST_CODE) {
            final int position = data.getIntExtra("position", 0);

            switch (resultCode) {
                case RESULT_CANCEL:
                    break;
                case RESULT_NEW:
                    Note note = new Note();
                    note.title = data.getStringExtra(OpenHelper.COLUMN_TITLE);
                    note.keywords = data.getStringExtra(OpenHelper.COLUMN_KEYWORDS);
                    note.type = data.getIntExtra(OpenHelper.COLUMN_TYPE, DatabaseModel.TYPE_NOTE);
                    note.remark = data.getStringExtra(OpenHelper.COLUMN_REMARK);
                    note.datelong = data.getLongExtra(OpenHelper.COLUMN_DATE, System.currentTimeMillis());
                    note.parentId = getCategoryId();
                    note.id = data.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID);
                    addItem(note, position);
                    //TODO  gurantee to show the new item created by saving + cancel in EditorActivity
                    loadItems();
                    break;
                case RESULT_EDIT:
                    if (items != null) {  //to handle the case of editing during reference viewing
                        Note item = items.get(position);
                        item.title = data.getStringExtra(OpenHelper.COLUMN_TITLE);
                        item.keywords = data.getStringExtra(OpenHelper.COLUMN_KEYWORDS);
                        item.datelong = data.getLongExtra(OpenHelper.COLUMN_DATE, 0L);
                        item.parentId = getCategoryId();
                        refreshItem(position);
                    }
                    break;
                case RESULT_DELETE:
                    new Thread() {
                        @Override
                        public void run() {
                            new Controller(App.instance).deleteNotes(
                                    new String[]{
                                            String.format(Locale.US, "%d", data.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID))
                                    },
                                    getCategoryId()
                            );

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final Note deletedItem = deleteItem(position);
                                    loadItems();
                                    Snackbar.make(fab != null ? fab : selectionToolbar, "1 note was deleted", 7000)
                                            .setAction(R.string.undo, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            new Controller(App.instance).undoDeletion();
                                                            new Controller(App.instance).addCategoryCounter(deletedItem.parentId, 1);

                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    addItem(deletedItem, position);
                                                                }
                                                            });

                                                            interrupt();
                                                        }
                                                    }.start();
                                                }
                                            })
                                            .show();
                                }
                            });
                            interrupt();
                        }
                    }.start();
                    break;
            }

            refreshAll();
        }
    }

    @Override
    public void onClickFab() {
        if (searchResults != null && selected != null) {
            //   Log.e("ShowCategoryFragment", "selected =" + selected.toString());
            selectionListener.onItemSelected(selected);
        } else if (!toolBarTitle.equals("Reference"))
            startNoteActivity(DatabaseModel.TYPE_NOTE, DatabaseModel.NEW_MODEL_ID, 0);

    }

    @Override
    public boolean onLongClickFab() {
        onCreateWebLink(getContext());
        return true;
    }

    public void onCreateWebLink(Context context) {
        //TODO ny no effect for the next line
        // if (mNyRichEditor==null) Toast.makeText(context, "Insert a Richtext item first!", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_edit_hyperlink, null);
        EditText etAddress = rootView.findViewById(R.id.et_address);
        EditText etDisplayText = rootView.findViewById(R.id.et_display_text);

        new android.app.AlertDialog.Builder(context)
                .setTitle(R.string.edit_hyperlink)
                .setView(rootView)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Note note = new Note();
                        note.keywords = etAddress.getText().toString();
                        note.type = TYPE_WEBSITE;
                        note.datelong = System.currentTimeMillis();
                        note.id = DatabaseModel.NEW_MODEL_ID;
                        if (!etDisplayText.getText().toString().isEmpty())
                            note.title = etDisplayText.getText().toString();
                        else
                            note.title = etAddress.getText().toString();
                        note.parentId = getCategoryId();
                        note.id = note.save();
                        note.body = "[]";
                        if (note.id != DatabaseModel.NEW_MODEL_ID) note.save();
                        dialog.dismiss();
                    }
                })
                .show();
        hideSoftInput((Activity) context);
        refreshAll();
    }


    @Override
    public void processMerge(boolean isUp) {
        ArrayList<Note> selectedNotes = new ArrayList<Note>((Collection<? extends Note>) selected);
        toggleSelection(false);
        final int length = selectedNotes.size();
        //Type checking
        for (int i = 0; i < length; i++) {
            if (selectedNotes.get(i).type != DatabaseModel.TYPE_NOTE) {
                Toast.makeText(getContext(), "Only notes can be merged!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        final Note note = new Note();
        note.id = DatabaseModel.NEW_MODEL_ID;
        //only TYPE_NOTE is allowed
        note.type = DatabaseModel.TYPE_NOTE;

        note.isArchived = false;
        note.title = isUp ? selectedNotes.get(0).title : selectedNotes.get(length - 1).title;
        note.title = note.title + "_m";
        // note.datelong = System.currentTimeMillis();
        note.datelong = isUp ? selectedNotes.get(0).datelong : selectedNotes.get(length - 1).datelong;
        note.isProtected = containProtectedItem();
        //security key not merged
        note.parentId = selectedNotes.get(0).parentId;
        note.theme = selectedNotes.get(0).theme;
        final long id = note.save();
        if (id != DatabaseModel.NEW_MODEL_ID) note.id = id;

        StringBuilder keywords = new StringBuilder();
        StringBuilder reference = new StringBuilder();
        StringBuilder remark = new StringBuilder();
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Note oNote = new Controller(App.instance).findNote(selectedNotes.get(i).id);
            keywords.append(" ").append(oNote.keywords);
            reference.append(" ").append(oNote.reference);
            remark.append(" ").append(oNote.remark);
            body.append(oNote.body.substring(1, oNote.body.length() - 1)).append(",");
        }
        note.keywords = keywords.toString().trim();
        note.reference = reference.toString().trim();
        note.remark = remark.toString().trim();
        String temp = body.toString();
        temp = "[" + temp.substring(0, temp.length() - 1) + "]"; //remove last ","
        note.body = temp;
        note.save();
        deleteDialog(selectedNotes);
    }


    //-----------//

    public void onReadSelected() {
        if (!selected.isEmpty()) {
            if (TTSUtils.getInstance(requireActivity()).isPlaying) {
                TTSUtils.getInstance(requireActivity()).stopSpeech();
                toggleSelection(false);
                return;
            }
            mItem = selected.remove(0);
            id = mItem.id;
            mPosition = items.indexOf(mItem);
            toggleSelection(false);
            if (needOpenCheck(mItem)) {
                unlockDialog(JOB_READ_NOTE, mItem.secureKey);
            } else speechNote(id);
        }
    }

    @Override
    public void speechNote(long id) {
        Note note = new Controller(App.instance).findNote(id);
        if (note == null) {
            Log.e(TAG, "speechNote null");
            return;
        }
        String[] result = note.body.substring(1, note.body.length() - 1).split(",");
        List<String> list = Arrays.asList(result);
        StringBuilder richText = new StringBuilder();
        for (int index = 0; index < list.size(); index++) {
            String item = list.get(index).trim();
            if (item.contains("richtext:")) {
                item = item.replace("richtext:", "");
                richText.append(item.replace(",", "\n"));
            }
        }
        if (!richText.toString().isEmpty()) {
            String text = Html.fromHtml(richText.toString()).toString();
            //  Log.e(TAG, "speechNote text = "+text);
            TTSUtils.getInstance(requireActivity()).startSpeech(text);
        }
    }

}



   /* public void toggleFab(boolean forceClose) {
        if (isFabOpen) {
            isFabOpen = false;

            Animator.create(getContext())
                    .on(protector)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fade_out);

            Animator.create(getContext())
                    .on(fab)
                    .animate(R.anim.fab_rotate_back);

            Animator.create(getContext())
                    .on(fab_type)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);

            Animator.create(getContext())
                    .on(fab_drawing)
                    .setDelay(50)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_out);
        } else if (!forceClose) {
            isFabOpen = true;

            Animator.create(getContext())
                    .on(protector)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fade_in);

            Animator.create(getContext())
                    .on(fab)
                    .animate(R.anim.fab_rotate);

            Animator.create(getContext())
                    .on(fab_type)
                    .setDelay(80)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);

            Animator.create(getContext())
                    .on(fab_drawing)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_in);
        }
    }*/



