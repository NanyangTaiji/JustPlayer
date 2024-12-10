package com.nanyang.richeditor.memento;


import static com.nanyang.richeditor.editor.EditorUtils.codeEditDialog;
import static com.nanyang.richeditor.editor.EditorUtils.searchDialog;
import static com.nanyang.richeditor.editor.EditorUtils.sorts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.afollestad.materialdialogs.MaterialDialog;
import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.Note;
import com.nanyang.richeditor.database.Category;

import java.util.ArrayList;
import java.util.Collection;


public class CategoryFragment extends BaseFragment<Category, CategoryAdapter> {
    private static final String TAG = "ShowCategoryFragment";

    private int categoryDialogTheme = Category.THEME_GREEN;
    private int checkedItem = App.sortNotesBy;
    private View selectedSpeech;

    public static void show(FragmentManager fm) {
        Bundle arguments = new Bundle();
        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(arguments);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static CategoryFragment get(FragmentManager fm) {
        return (CategoryFragment) fm.findFragmentByTag(TAG);
    }

    public static void remove(FragmentManager fm) {
        CategoryFragment showNoteFragment = get(fm);

        if (null != showNoteFragment) {
            //  showNoteFragment.Destroy();
            showNoteFragment.onDestroy();
            fm.beginTransaction().remove(showNoteFragment).commitAllowingStateLoss();
        }
    }

    public CategoryFragment() {
    }

    @Override
    public int getLayout() {
        return (R.layout.fragment_show_database);
    }

    @Override
    public String getItemName() {
        return getCategoryId() > 0 ? "note" : "category";
    }

    @Override
    public void initExtraViews(View view) {

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

      /*  selectedSpeech = selectionToolbar.findViewById(R.id.selection_speech);

        //  selectionToolbar.setVisibility(getCategoryId()> 0 ? View.VISIBLE : View.GONE);
        selectedSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadSelected();
            }
        });*/


        requireActivity().findViewById(R.id.app_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the following is essential for delect items from search results
                //however it needs an additional onBack to return to the previous state
                selectionState = true;
                //TODO searchResults shoule not be set to static when change Tab.
                if (searchResults != null) //search from search results
                    searchDialog(requireActivity(), searchCallback, searchResults);
                else { //fresh search
                    previousId = getCategoryId();
                    setCategoryId(DatabaseModel.SEARCH_GLOBAL);
                    searchDialog(requireActivity(), searchCallback, previousId);
                }
            }
        });

        // requireActivity().findViewById(R.id.app_sort).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.app_sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedItem = App.sortCategoriesBy;
                AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity(), R.style.DialogTheme);
                dialog.setSingleChoiceItems(sorts, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (getCategoryId() > 0) {
                            Category editedCategory = new Controller(App.instance).findCategoryById(getCategoryId());
                            editedCategory.sortBy = String.valueOf(which);
                            editedCategory.save();
                        } else App.instance.setSortCategoriesBy(which);
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

        //  setToolbarTitle((libs[App.getCurrentLib()]));
    }

    @Override
    public Class<CategoryAdapter> getAdapterClass() {
        return CategoryAdapter.class;
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
            if (getCategoryId() > 0) checkedOpen(item, position);
            else {
                if (!item.isProtected)
                    openCategory();
                else unlockDialog(JOB_OPEN_CATEGORY, item.secureKey);
            }
        }

        @Override
        public void onChangeSelection(boolean haveSelected) {
            //  if (searchResults != null) return;
            toggleSelection(haveSelected);
        }

        @Override
        public void onCountSelection(int count) {
            selectionCount = count;
            //  if (searchResults != null) return;
            selectionToolbar.findViewById(R.id.selection_copy).setVisibility(getCategoryId() != -1 ? View.VISIBLE : View.GONE);
            selectionToolbar.findViewById(R.id.selection_move).setVisibility(getCategoryId() != -1 ? View.VISIBLE : View.GONE);
            selectionToolbar.findViewById(R.id.selection_speech).setVisibility(getCategoryId() != -1 && count == 1 ? View.VISIBLE : View.GONE);
            onSelectonCounterChange(count);
        }
    };


    @Override
    protected void pickupAction(int action) {
        super.pickupAction(action);
        if (action == JOB_OPEN_CATEGORY) openCategory();
        if (action == JOB_EDIT_CATEGORY) proceedEdit();
        if (action == JOB_MERGE_UP) processMerge(true);
        if (action == JOB_MERGE_DOWN) processMerge(false);
        if (action == JOB_COPY) noteCopyDialog(false);
        if (action == JOB_MOVE) noteCopyDialog(true);
    }


    private void openCategory() {

        if (searchResults == null && getCategoryId() == DatabaseModel.NEW_MODEL_ID) {  //or ||toolbarTitle!="Search"
            setCategoryId(mItem.id);
            //  toolBarTitle=libTitle+":"+mItem.title; //inside setCategoryId
            loadItems();
            // ShowNoteFragment.show(getParentFragmentManager(), mItem.id);
        } else {
            checkedOpen(mItem, mPosition);
        }
    }


    @Override
    public void onClickFab() {
        if (searchResults != null && selected != null) {
            //   Log.e("ShowCategoryFragment", "selected =" + selected.toString());
            selectionListener.onItemSelected(selected);
        } else if (getCategoryId() > 0 && !toolBarTitle.equals("Reference"))
            startNoteActivity(DatabaseModel.TYPE_NOTE, DatabaseModel.NEW_MODEL_ID, 0);
        else {
            categoryDialogTheme = Category.THEME_GREEN;
            categoryEditDialog(
                    R.string.new_category,
                    R.string.create,
                    DatabaseModel.NEW_MODEL_ID,
                    0
            );
        }

    }

    @Override
    public boolean onLongClickFab() {
       // if (getCategoryId() > 0) onCreateWebLink(getContext());
        return true;
    }

    @Override
    public void onEditSelected() {
        if (!selected.isEmpty()) {//editing
            mItem = selected.remove(0);
            mPosition = items.indexOf(mItem);
            toggleSelection(false);
            categoryDialogTheme = mItem.theme;
            if (getCategoryId() == DatabaseModel.NEW_MODEL_ID && mItem.isProtected) {
                unlockDialog(JOB_EDIT_CATEGORY, mItem.secureKey);
            } else if (getCategoryId() > 0 && needOpenCheck((DatabaseModel) mItem)) {
                unlockDialog(JOB_EDIT_NOTE, mItem.secureKey);
            } else proceedEdit();
        }
    }

    @Override
    public void proceedEdit() {
        refreshItem(mPosition);
      //  Log.e(TAG, "getCategoryId() = " + getCategoryId());
        if (getCategoryId() == DatabaseModel.NEW_MODEL_ID) {
            categoryEditDialog(R.string.edit_category, R.string.edit, mItem.id, mPosition);
        } else
            codeEditDialog(requireActivity(), (Note) mItem);

    }


    private void categoryEditDialog(@StringRes int title, @StringRes int positiveText, final long categoryId, final int position) {

        final boolean isEditing = categoryId != DatabaseModel.NEW_MODEL_ID;

        checkedItem = App.sortNotesBy;
        boolean isProtected = false;
        String categoryTitle = null;
        String categoryKeywords = null;
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_category, null);

        if (isEditing) {
            Category editedCategory = new Controller(App.instance).findCategoryById(categoryId);
            categoryTitle = editedCategory.title;
            categoryKeywords = editedCategory.keywords;
            isProtected = editedCategory.isProtected;
            if (!editedCategory.sortBy.isEmpty())
                checkedItem = Integer.parseInt(editedCategory.sortBy);
            //  ((EditText) view.findViewById(R.id.date_title)).setText(categoryTitle);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        dialog.setSingleChoiceItems(sorts, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItem = which;
            }
        });
        dialog.setView(view);
        dialog.setTitle(title)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputTitle = ((EditText) view.findViewById(R.id.item_title)).getText().toString();
                String inputKeyword = ((EditText) view.findViewById(R.id.category_keyword)).getText().toString();
                if (inputTitle.isEmpty()) {
                    inputTitle = "New Category";
                }

                boolean newProtected = ((SwitchCompat) view.findViewById(R.id.c_switch)).isChecked();

                final Category category = new Category();
                category.id = categoryId;

                if (!isEditing) {
                    category.counter = 0;
                    category.type = DatabaseModel.TYPE_CATEGORY;
                    category.datelong = System.currentTimeMillis();
                    category.isArchived = false;
                }

                category.title = inputTitle;
                category.keywords = inputKeyword;
                category.sortBy = "" + checkedItem;
                category.theme = categoryDialogTheme;
                category.isProtected = newProtected;

                new Thread() {
                    @Override
                    public void run() {
                        final long id = category.save();  //--------------------
                        //save then refresh
                        if (id != DatabaseModel.NEW_MODEL_ID) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isEditing) {
                                        refreshItem(position);
                                    } else {
                                        category.id = id;            //new
                                        addItem(category, position);
                                    }
                                }
                            });
                        }
                        interrupt();
                    }
                }.start();
            }

        });

        dialog.show();

        //noinspection ConstantConditions
        ((EditText) view.findViewById(R.id.item_title)).setText(categoryTitle);
        ((SwitchCompat) view.findViewById(R.id.c_switch)).setChecked(isProtected);

        EditText inputKeyword = ((EditText) view.findViewById(R.id.category_keyword));
        inputKeyword.setText(categoryKeywords);

        //TODO ny disable default keyword input temporily
      /*  inputKeyword.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickupKeyWordsDialog(getActivity(), new ListedListener() {
                    @Override
                    public void onListChanged(ArrayList<String> chosenChildren) {
                        String temp = chosenChildren.toString();
                        temp = temp.substring(1, temp.length() - 1);
                        temp = temp.replace(",", " ");
                        inputKeyword.setText(temp);
                    }
                });
                return true;
            }
        });*/

        setCategoryDialogTheme(view, categoryDialogTheme);

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_red).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_RED);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_pink).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_PINK);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_purple).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_PURPLE);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_amber).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_AMBER);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_blue).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_BLUE);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_cyan).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_CYAN);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_orange).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_ORANGE);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_teal).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_TEAL);
                    }
                });

        //noinspection ConstantConditions
        view.findViewById(R.id.theme_green).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setCategoryDialogTheme(view, Category.THEME_GREEN);
                    }
                });
    }

    private void setCategoryDialogTheme(View view, int theme) {
        if (theme != categoryDialogTheme) {
            getThemeView(view, categoryDialogTheme).setImageResource(0);
        }

        getThemeView(view, theme).setImageResource(R.drawable.ic_checked);
        categoryDialogTheme = theme;
    }

    private ImageView getThemeView(View view, int theme) {
        switch (theme) {
            case Category.THEME_AMBER:
                return (ImageView) view.findViewById(R.id.theme_amber);
            case Category.THEME_BLUE:
                return (ImageView) view.findViewById(R.id.theme_blue);
            case Category.THEME_CYAN:
                return (ImageView) view.findViewById(R.id.theme_cyan);
            case Category.THEME_ORANGE:
                return (ImageView) view.findViewById(R.id.theme_orange);
            case Category.THEME_PINK:
                return (ImageView) view.findViewById(R.id.theme_pink);
            case Category.THEME_PURPLE:
                return (ImageView) view.findViewById(R.id.theme_purple);
            case Category.THEME_RED:
                return (ImageView) view.findViewById(R.id.theme_red);
            case Category.THEME_TEAL:
                return (ImageView) view.findViewById(R.id.theme_teal);
            default:
                return (ImageView) view.findViewById(R.id.theme_green);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (selected != null) return;
        //  if (requestCode == REQUEST_CODE && resultCode == RESULT_CHANGE) {
        //TODO ny refresh all to update moving/coping
        loadItems();
    }

    @Override
    public void processMerge(boolean isUp) {
        ArrayList<Category> selectedNotes = new ArrayList<Category>((Collection<? extends Category>) selected);
        toggleSelection(false);
        final int length = selectedNotes.size();

        final Category category = new Category();
        category.id = DatabaseModel.NEW_MODEL_ID;

        category.type = DatabaseModel.TYPE_CATEGORY;

        category.isArchived = false;
        category.title = isUp ? selectedNotes.get(0).title : selectedNotes.get(length - 1).title;
        category.theme = isUp ? selectedNotes.get(0).theme : selectedNotes.get(length - 1).theme;
        category.datelong = isUp ? selectedNotes.get(0).datelong : selectedNotes.get(length - 1).datelong;
        category.isProtected = containProtectedItem();
        StringBuilder keywords = new StringBuilder();
        category.counter = 0;
        for (int i = 0; i < length; i++) {
            category.counter += selectedNotes.get(i).counter;
            keywords.append(" ").append(selectedNotes.get(i).keywords);
        }
        category.keywords = keywords.toString().trim();

        new Thread() {
            @Override
            public void run() {
                final long id = category.save();  //--------------------
                if (id != DatabaseModel.NEW_MODEL_ID) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            category.id = id;            //new
                            addItem(category, 0);
                            for (int i = 0; i < length; i++) {
                                ArrayList<Note> notes = new Controller(App.instance).findAllNotesInCategory(selectedNotes.get(i).id);
                                for (Note note : notes) {
                                    Note newNote = new Controller(App.instance).findNote(note.id);
                                    newNote.parentId = category.id;
                                    newNote.save();
                                }
                            }

                            category.save();
                            toggleSelection(false);
                            proceessDelete(selectedNotes);
                        }
                    });

                }
                interrupt();
            }
        }.start();

    }

    @Override
    void speechNote(long id) {

    }

}
