package com.nanyang.richeditor.memento;

import static com.nanyang.richeditor.database.DatabaseModel.SEARCH_GLOBAL;
import static com.nanyang.richeditor.database.DatabaseModel.TYPE_WEBSITE;
import static com.nanyang.richeditor.database.OpenHelper.POSITION;
import static com.nanyang.richeditor.editor.EditorUtils.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.OpenHelper;
import com.nanyang.richeditor.editor.EditorActivity;
import com.nanyang.richeditor.database.Category;
import com.nanyang.richeditor.database.Note;
import com.nanyang.richeditor.editor.ShowHtmlFragment;
import com.nytaiji.nybase.view.GestureUnlockView;
import com.nytaiji.nybase.filePicker.Animator;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

abstract public class BaseFragment<T extends DatabaseModel, A extends BaseAdapter> extends Fragment {
    private static final String TAG = "BaseFragment";

    public static int JOB_UNLOCK = 0;
    public static int JOB_DELETE = 1;
    public static int JOB_OPEN_CATEGORY = 2;
    public static int JOB_OPEN_NOTE = 3;
    public static int JOB_EDIT_CATEGORY = 4;
    public static int JOB_READ_NOTE = 5;
    public static int JOB_EDIT_NOTE = 6;
    public static int JOB_COPY = 7;
    public static int JOB_MOVE = 8;
    public static int JOB_MERGE_UP = 9;
    public static int JOB_MERGE_DOWN = 10;

    public View fab, lockToggle, selectionEdit;
    private RecyclerView recyclerView;

    protected boolean allowOpen = true;
    private View empty;
    public Toolbar toolbar, selectionToolbar;
    private TextView selectionCounter, titleView;
    public boolean selectionState = false;
    public View undelete, mergeUp, mergeDown;
    private A adapter;
    public ArrayList<T> items;
    public ArrayList<Note> searchResults;
    public ArrayList<T> selected = new ArrayList<>();
    protected ArrayList<T> selectedNotes;

    public long previousId = DatabaseModel.NEW_MODEL_ID;
    private long categoryId = DatabaseModel.NEW_MODEL_ID;
    public String libTitle;
    public String toolBarTitle = "Search";
    public String categoryKeywords;
    public int categoryTheme;
    public int categoryPosition = 0;
    public int selectedType = 0;
    protected BaseAdapter.SelectionListener selectionListener;

    protected DatabaseModel mItem;
    protected int mPosition;
    private ArrayList<T> undos;

    //-----------------------------//
    protected int type, post;
    protected long id;
    private String keySetted = null;
    private String searchKey = null;
    protected int selectionCount = 0;
    protected int tabIndex;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        titleView = (TextView) getActivity().findViewById(R.id.app_title);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        empty = view.findViewById(R.id.empty);


        fab = view.findViewById(R.id.fab);
        //TODO ny  non-search
        fab.setVisibility(categoryId > SEARCH_GLOBAL ? View.VISIBLE : View.GONE);
        //  }
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (allowOpen) {
                        allowOpen = false;
                        onClickFab();
                        //TODO ny to avoid multi-opening the same file
                        new Handler()
                                .postDelayed(
                                        () -> {
                                            allowOpen = true;
                                        }, 2000);
                    }
                }
            });

            //TODO ny add website
            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onLongClickFab();
                }
            });
        }

        undelete = getActivity().findViewById(R.id.undelete);
        if (undelete != null) {
            undelete.setVisibility(View.GONE);
            undelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread() {
                        @Override
                        public void run() {
                            proceedUndodelete();
                            interrupt();
                        }
                    }.start();
                }
            });
        }


        if (getActivity().findViewById(R.id.selection_toolbar) != null) {
            selectionToolbar = (Toolbar) getActivity().findViewById(R.id.selection_toolbar);
            selectionCounter = (TextView) selectionToolbar.findViewById(R.id.selection_counter);


            selectionToolbar.findViewById(R.id.selection_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleSelection(false);
                }
            });

            mergeUp = selectionToolbar.findViewById(R.id.selection_merge_up);
            mergeUp.setVisibility(categoryId >= 0 ? View.VISIBLE : View.GONE);
            mergeUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedNotes = new ArrayList<>(selected);
                    if (containProtectedItem()) unlockDialog(JOB_MERGE_UP, null);
                    else processMerge(true);
                }
            });
            mergeDown = selectionToolbar.findViewById(R.id.selection_merge_down);
            mergeDown.setVisibility(categoryId >= 0 ? View.VISIBLE : View.GONE);
            mergeDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedNotes = new ArrayList<>(selected);
                    if (containProtectedItem()) unlockDialog(JOB_MERGE_DOWN, null);
                    else processMerge(false);
                }
            });


            lockToggle = selectionToolbar.findViewById(R.id.locker);
            lockToggle.setVisibility(View.VISIBLE);
            lockToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedNotes = new ArrayList<>(selected);
                    if (!containProtectedItem()) {
                        if (selectedNotes.size() > 1) processUnlock();
                        else setKeyCodeDialog(getContext(), selectedNotes.get(0));
                        //
                    } else {
                        if (selectedNotes.size() > 1)
                            unlockDialog(JOB_UNLOCK, null);
                        else
                            unlockDialog(JOB_UNLOCK, selectedNotes.get(0).secureKey);
                    }
                }
            });

            selectionToolbar.findViewById(R.id.thumb_up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedNotes = new ArrayList<>(selected);
                    processStar();
                }
            });


            selectionToolbar.findViewById(R.id.selection_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedNotes = new ArrayList<>(selected);
                    deleteDialog();
                }
            });


            selectionEdit = selectionToolbar.findViewById(R.id.selection_edit);
            selectionEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEditSelected();
                }
            });

            initExtraViews(view);
        }


        if (getActivity() instanceof EditorActivity) {
            new Thread() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    if (searchResults != null) {
                        items = (ArrayList<T>) searchResults;
                        proceedLoad();
                    }
                    interrupt();
                }

            }.start();
            return;
        }

        libTitle = this.getArguments().getString("LIBTITLE");
        toolBarTitle = libTitle;
        loadItems();
    }

    public void setTabIndex(int libIndex) {
        tabIndex = libIndex;
        setToolbarTitle(App.getAllLibs().get(libIndex).getTitle());
    }

    public void setSelectionListener(BaseAdapter.SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    private void processStar() {
        final int length = selectedNotes.size();
        new Thread() {
            @Override
            public void run() {

                for (int i = 0; i < selectedNotes.size(); i++) {
                    Note newNote = new Note();
                    T note = selectedNotes.get(i);
                    if (note.type > 0) {
                        long id = note.id;
                        newNote = new Controller(App.instance).findNote(id);
                        newNote.isStard = !newNote.isStard;
                    }

                    note.isStard = !note.isStard;
                    note.save();

                    if (note.type > 0) newNote.save(); //other wise the note.body will be empty
                }
                interrupt();
            }
        }.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleSelection(false);
            }
        }, length * 50L);

    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Are you sure to delete all the selection?")
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
                        if (!containProtectedItem()) proceessDelete(new ArrayList<>(selected));
                        else {
                            unlockDialog(JOB_DELETE, null);
                        }
                    }
                });
        builder.show();
    }


    protected void unlockDialog(int action, String key) {
        final View root = requireActivity().getLayoutInflater().inflate(R.layout.dialog_unlock, null);
        if (key != null) keySetted = key;
        else keySetted = null;
        //  Log.e(TAG, "keySetted " + keySetted);
        if (keySetted == null || keySetted.isEmpty()) keySetted = "2145698";
        //  Log.e(TAG, "unlockDialog keySetted " + keySetted);

        AlertDialog unlock = new AlertDialog.Builder(requireContext())
                .setView(root)
                .setTitle("Unlock verify!")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

        GestureUnlockView mUnlockView = (GestureUnlockView) root.findViewById(R.id.gesture_unlock_view);

        final int[] trials = {0};

        mUnlockView.setOnGestureDoneListener(new GestureUnlockView.OnGestureDoneListener() {
            @Override
            public boolean isValidGesture(int pointCount) {
                return pointCount > 3;
            }

            @Override
            public void onGestureDone(LinkedHashSet<Integer> numbers) {
                StringBuilder str = new StringBuilder();
                for (Integer num : numbers) {
                    str.append(num);
                }
                if (str.toString().equals(keySetted)) {
                    pickupAction(action);
                    unlock.dismiss();
                }

                if (trials[0]++ > 3) {
                    Toast.makeText(getContext(), "More than three trials failiure!", Toast.LENGTH_SHORT).show();
                    unlock.dismiss();
                }
            }
        });
    }

    private void setKeyCodeDialog(Context context, T model) {
        final View root = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_unlock, null);
        keySetted = null;
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(root)
                .setTitle("Set Unlock Key")
                .setPositiveButton(R.string.new_key, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (keySetted == null) keySetted = "2145698";
                        setProtectionKey(model, keySetted);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.default_key, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        keySetted = "2145698";
                        setProtectionKey(model, keySetted);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();


        GestureUnlockView mUnlockView = (GestureUnlockView) root.findViewById(R.id.gesture_unlock_view);

        mUnlockView.setOnGestureDoneListener(new GestureUnlockView.OnGestureDoneListener() {
            @Override
            public boolean isValidGesture(int pointCount) {
                return pointCount > 3;
            }

            @Override
            public void onGestureDone(LinkedHashSet<Integer> numbers) {
                StringBuilder str = new StringBuilder();
                for (Integer num : numbers) {
                    str.append(num);
                }
                alertDialog.setTitle(str);
                keySetted = str.toString();
            }
        });
    }


    private void delayToggleSelection(int length) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleSelection(false);
                loadItems();
            }
        }, length * 50L);
    }

    private void setProtectionKey(T model, String key) {
        if (model instanceof Note) {
            Note noteN = new Controller(App.instance).findNote(model.id);
            noteN.secureKey = key;
            noteN.isProtected = true;
            noteN.isStard = true;
            noteN.save();
        } else {
            Category noteN = new Controller(App.instance).findCategoryById(model.id);
            noteN.secureKey = key;
            noteN.isProtected = true;
            noteN.isStard = true;
            noteN.save();
        }

        model.secureKey = key;
        model.isProtected = true;
        model.isStard = true;
        Log.e(TAG, "setProtectionKey " + key);
        delayToggleSelection(1);

    }


    private void processUnlock() {
        final int length = selectedNotes.size();
        for (int i = 0; i < selectedNotes.size(); i++) {
            T note = selectedNotes.get(i);
            long id = note.id;
            if (note instanceof Note) {
                Note newNote = new Controller(App.instance).findNote(id);
                newNote.isProtected = !newNote.isProtected;
                if (!newNote.isProtected) newNote.secureKey = "";
                if (newNote.isProtected) newNote.isStard = true;
                newNote.save();
            } else {
                Category newNote = new Controller(App.instance).findCategoryById(id);
                newNote.isProtected = !newNote.isProtected;
                if (!newNote.isProtected) newNote.secureKey = "";
                if (newNote.isProtected) newNote.isStard = true;
                newNote.save();
            }
        }
        delayToggleSelection(length);
    }

    public void proceessDelete(ArrayList<T> undos) {
        this.undos = undos;
        toggleSelection(false);

        new Thread() {
            @Override
            public void run() {
                final int length = undos.size();
                String[] ids = new String[length];
                final int[] sortablePosition = new int[length];

                for (int i = 0; i < length; i++) {
                    T item = undos.get(i);
                    ids[i] = String.format(Locale.US, "%d", item.id);
                    int position = items.indexOf(item);
                    item.position = position;
                    sortablePosition[i] = position;
                }

                try {  //TODO ny to captch exception
                    new Controller(App.instance).deleteNotes(ids, categoryId);
                } catch (Exception ignored) {

                }

                Arrays.sort(sortablePosition);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = length - 1; i >= 0; i--) {
                            items.remove(sortablePosition[i]);
                            adapter.notifyItemRemoved(sortablePosition[i]);
                        }

                        toggleEmpty();

                        StringBuilder message = new StringBuilder();
                        message.append(length).append(" ").append(getItemName());
                        if (length > 1) message.append("s were deleted");
                        else message.append(" was deleted.");

                        //TODO ny very important to refresh;
                        loadItems();
                        undelete.setVisibility(View.VISIBLE);
                        //TODO ny the following are not needed anymore
                        Snackbar.make(fab != null ? fab : selectionToolbar, message.toString(), 7000)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                proceedUndodelete();
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

    }

    private void proceedUndodelete() {
        final int length = undos.size();
        new Controller(App.instance).undoDeletion();
        if (categoryId != DatabaseModel.NEW_MODEL_ID) {
            new Controller(App.instance).addCategoryCounter(categoryId, length);
        }

        Collections.sort(undos, new Comparator<T>() {
            @Override
            public int compare(T t1, T t2) {
                if (t1.position < t2.position)
                    return -1;
                if (t1.position == t2.position)
                    return 0;
                return 1;
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < length; i++) {
                    T item = undos.get(i);
                    addItem(item, item.position);
                }
                undelete.setVisibility(View.GONE);
            }
        });
    }

    public void onSelectonCounterChange(int count) {
        if (count < 1) return;
        if (selectionToolbar != null) {
            selectionCounter.setText(String.format(Locale.US, "%d", count));
            selectionEdit.setVisibility(count == 1 ? View.VISIBLE : View.GONE);
            mergeUp.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
            mergeDown.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
        }
    }

    public void toggleSelection(boolean state) {
        selectionState = state;
        if (categoryId == DatabaseModel.SEARCH_CLICK || categoryId == DatabaseModel.SEARCH_SELECT)
            return;
        if (state) {
            Animator.create(getContext())
                    .on(toolbar)
                    .setEndVisibility(View.INVISIBLE)
                    .animate(R.anim.fade_out);
        } else {
            Animator.create(getContext())
                    .on(toolbar)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fade_in);
            deselectAll();
        }
    }

    private void deselectAll() {
        while (!selected.isEmpty()) {
            adapter.notifyItemChanged(items.indexOf(selected.remove(0)));
            selectionCount = 0;
        }
    }

    public void setToolbarTitle(String title) {
        toolBarTitle = title;
        if (titleView != null) titleView.setText(toolBarTitle);
    }

    public void showToolbarTitle() {
        if (titleView != null) titleView.setText(toolBarTitle);
    }

    public BaseFragment.searchCallback searchCallback = new searchCallback() {
        @Override
        public void returnKey(String key) {
            searchKey = key;
        }

        @Override
        public void returnResult(ArrayList<Note> nitems) {
            searchResults = nitems;
            if (categoryId == DatabaseModel.SEARCH_CLICK || categoryId == DatabaseModel.SEARCH_SELECT)
                loadItems();
            else {
                categoryId = SEARCH_GLOBAL;
                loadItems();
            }
        }
    };

    public void loadItems() {
        new Thread() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                //  Log.e(TAG, "categoryId ==" + categoryId);

                if (categoryId < -1) {
                    toolBarTitle = toolBarTitle + ":" + searchKey;
                    items = (ArrayList<T>) searchResults;
                } else if (categoryId == DatabaseModel.NEW_MODEL_ID) {
                    items = (ArrayList<T>) new Controller(App.instance).findAllCategories();
                } else if (categoryId > 0) {
                    items = (ArrayList<T>) new Controller(App.instance).findAllNotesInCategory(categoryId);
                }
                interrupt();
                proceedLoad();
            }

        }.start();
        //for every uploading
        // if (getActivity()!=null) hideStatusBar(getActivity());
    }


    public void proceedLoad() {
        //---------------//
        FragmentActivity mActivity = getActivity();
        if (mActivity == null) {
            mActivity = EditorActivity.geInstance();
        }
        if (mActivity == null) return;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (categoryId != DatabaseModel.NEW_MODEL_ID) {
                        // Log.e(TAG, "run( categoryId ==" + categoryId);
                        if (categoryId == DatabaseModel.SEARCH_SELECT)
                            fab.setVisibility(View.VISIBLE);
                        adapter = (A) new NoteAdapter((ArrayList<Note>) items, (ArrayList<Note>) selected, getListener());
                    } else {
                        adapter = getAdapterClass().getDeclaredConstructor(
                                ArrayList.class, ArrayList.class, BaseAdapter.ClickListener.class
                        ).newInstance(items, selected, getListener());
                    }
                    toggleEmpty();
                    showToolbarTitle();
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false
                    ));
                } catch (InvocationTargetException | NoSuchMethodException |
                         java.lang.InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void toggleEmpty() {
        if (items != null && items.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshItem(int position) {
        adapter.notifyItemChanged(position);
    }

    public void refreshAll() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public T deleteItem(int position) {
        T item = items.remove(position);
        adapter.notifyItemRemoved(position);
        toggleEmpty();
        return item;
    }

    public void addItem(T item, int position) {
        if (items.isEmpty()) {
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        items.add(position, item);
        adapter.notifyItemInserted(position);
    }


    public void checkedOpen(DatabaseModel item, int position) {
        //TODO ny
        // Log.e(TAG, "toolBarTitle= "+toolBarTitle);
        type = item.type;
        id = item.id;
        post = position;
        if (toolBarTitle.contains("Reference")) return;
        //for Search outcome, need to verify whether its parent is locked or not
        if (needOpenCheck(item))
            unlockDialog(JOB_OPEN_NOTE, item.secureKey);
        else openNote(type, id, post);
    }


    public boolean containProtectedItem() {
        for (int i = 0; i < selectedNotes.size(); i++) {
            T note = selectedNotes.get(i);
            if (note.isProtected || parentLock(note)) {
                return true;
            }
        }
        return false;
    }

    protected boolean parentLock(T note) {
        if (note instanceof Category) return false;
        else
            return new Controller(App.instance).findCategoryById(((Note) note).parentId).isProtected;
    }

    protected boolean needOpenCheck(DatabaseModel item) {
        if (item instanceof Note)
            return item.isProtected
                    || (categoryId < 0 && new Controller(App.instance).findCategoryById(((Note) item).parentId).isProtected);
        else return item.isProtected;
    }


    protected void pickupAction(int action) {
        if (action == JOB_UNLOCK) processUnlock();
        if (action == JOB_DELETE) proceessDelete(new ArrayList<>(selected));
        if (action == JOB_OPEN_NOTE) openNote(type, id, post);
        if (action == JOB_READ_NOTE) speechNote(id);
        if (action == JOB_EDIT_NOTE) proceedEdit();
    }



    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
        if (categoryId > DatabaseModel.NEW_MODEL_ID)
            toolBarTitle = libTitle + "/" + new Controller(App.instance).findCategoryById(categoryId).title;
        else toolBarTitle = libTitle;
    }

    public int getSelectionCount() {
        return selectionCount;
    }

    //--------------------------------abstracts--------------------------------------------//
    public void initExtraViews(View view) {
    }

    public abstract void onClickFab();

    public abstract boolean onLongClickFab();

    public abstract int getLayout();

    public abstract String getItemName();

    public abstract Class<A> getAdapterClass();

    public abstract BaseAdapter.ClickListener getListener();


    //TODO ny

    public abstract void onEditSelected();

    public abstract void proceedEdit();


    public void openNote(int type, long id, int post) {
        if (allowOpen) {
            //TODO ny to avoid multi-opening the same file
            allowOpen = false;
            startNoteActivity(type, id, post);
            //TODO ny to avoid multi-opening the same file
            new Handler()
                    .postDelayed(
                            () -> {
                                allowOpen = true;
                            }, 2000);
        }
    }

    public static interface searchCallback {
        void returnKey(String key);

        void returnResult(ArrayList<Note> items);
    }


    void noteCopyDialog(boolean toDelete) {
        ArrayList<Note> selectedNotes = new ArrayList<Note>((Collection<? extends Note>) selected);
        ArrayList<Category> all = new Controller(App.instance).findAllCategories();
        final int length = all.size();
        HashMap<String, Long> map = new HashMap<>();
        ArrayList<String> titles = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            if (all.get(i).id != categoryId) {
                map.put(all.get(i).title, all.get(i).id);
                titles.add(all.get(i).title);
            }
        }
        String[] titleArray = titles.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose the destination:");

        final long[] destionaId = {0L}; // cow
        builder.setSingleChoiceItems(titleArray, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                destionaId[0] = map.get(titleArray[which]);
            }
        });

        builder.setCancelable(true);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toggleSelection(false);
                noteNcopy(selectedNotes, destionaId[0], toDelete);
                //  dialog.dismiss();
            }
        });


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void noteNcopy(ArrayList<Note> selectedNotes, long targetId, boolean toDelete) {
        new Thread() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int length = selectedNotes.size();
                        String[] ids = new String[length];
                        for (int i = 0; i < length; i++) {
                            Note item = selectedNotes.get(i);
                            ids[i] = String.format(Locale.US, "%d", item.id);
                            item.position = items.indexOf(item);
                            Note origin = new Controller(App.instance).findNote(item.id);
                            Note newNote = new Note();
                            newNote.id = DatabaseModel.NEW_MODEL_ID;
                            newNote.title = origin.title;
                            newNote.keywords = origin.keywords;
                            newNote.remark = origin.remark;
                            newNote.reference = origin.reference;
                            newNote.body = origin.body;
                            newNote.type = origin.type;
                            newNote.datelong = origin.datelong;
                            newNote.parentId = targetId;
                            newNote.isStard = origin.isStard;
                            newNote.isProtected = origin.isProtected;
                            if (origin.isProtected) newNote.secureKey = origin.secureKey;
                            else if (parentLock((T) origin)) {
                                //use category securekey as new securekey
                                newNote.secureKey = new Controller(App.instance).findCategoryById(origin.parentId).secureKey;
                                newNote.isProtected = true;
                                newNote.isStard = true;
                            } //otherwise do not copy security key
                            newNote.id = newNote.save();
                            if (newNote.id != DatabaseModel.NEW_MODEL_ID) newNote.save();
                            if (toDelete) deleteItem(item.position);   //delete recycleview entry
                        }

                        if (toDelete) new Controller(App.instance).deleteNotes(ids, categoryId);
                    }
                });

                interrupt();
            }
        }.start();
    }

    abstract void processMerge(boolean isUp);

    abstract void speechNote(long id);


    void deleteDialog(ArrayList<Note> selectedNotes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("To delete originals?")
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        loadItems();
                    }

                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        proceessDelete((ArrayList<T>) selectedNotes);
                        //loadItems() is imbeded in proceesDelete
                    }
                });
        builder.show();
    }


    public void startNoteActivity(final int type, final long noteId, final int position) {
        //to avoid double click
        new Thread() {
            @Override
            public void run() {
                FragmentActivity mActivity = getActivity();
                if (mActivity == null) {
                    mActivity = EditorActivity.geInstance();
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (type == TYPE_WEBSITE) {
                            String link = new Controller(App.instance).findNote(noteId).keywords;
                            Uri webpage = Uri.parse(link);
                            PackageManager pm = getActivity().getPackageManager();
                            try {
                                pm.getPackageInfo("org.adblockplus.browser", 0);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setPackage("org.adblockplus.browser");
                                intent.putExtra(Intent.EXTRA_TEXT, link);
                                intent.setData(webpage);
                                getActivity().startActivity(intent);
                            } catch (PackageManager.NameNotFoundException e) {
                               // Toast.makeText(getActivity(), "Package Adblock Browser did not installed", Toast.LENGTH_SHORT).show();
                                ShowHtmlFragment.show(getParentFragmentManager(),link);
                                /* Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                                if (intent.resolveActivity(pm) != null) {
                                    getActivity().startActivity(intent);
                                }*/
                            }
                        } else {
                            Log.e(TAG, "getCategoryId() = "+getCategoryId());
                            Intent intent;
                            intent = new Intent(getContext(), EditorActivity.class);
                            intent.putExtra(OpenHelper.COLUMN_TYPE, type);
                            intent.putExtra(POSITION, position);
                            intent.putExtra(OpenHelper.COLUMN_ID, noteId);
                            intent.putExtra(OpenHelper.COLUMN_PARENT_ID, getCategoryId());
                            intent.putExtra(OpenHelper.COLUMN_THEME, categoryTheme);
                            startActivityForResult(intent, REQUEST_CODE);
                        }
                    }
                });

                interrupt();
            }
        }.start();

    }

}
