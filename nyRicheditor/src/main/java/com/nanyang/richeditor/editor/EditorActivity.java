package com.nanyang.richeditor.editor;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.blankj.utilcode.util.ConvertUtils.px2dp;
import static com.nanyang.richeditor.database.DatabaseModel.TYPE_NOTE;
import static com.nanyang.richeditor.database.DatabaseModel.TYPE_WEBSITE;
import static com.nanyang.richeditor.database.DatabaseModel.TYPE_YOUTUBE;
import static com.nanyang.richeditor.database.OpenHelper.POSITION;
import static com.nanyang.richeditor.editor.EditorContainer.CODESHOW;
import static com.nanyang.richeditor.editor.EditorContainer.COMMA;
import static com.nanyang.richeditor.editor.EditorContainer.EDIT_PADDING;
import static com.nanyang.richeditor.editor.EditorContainer.EXPORT;
import static com.nanyang.richeditor.editor.EditorContainer.ITEMCHANGE;
import static com.nanyang.richeditor.editor.EditorContainer.KEYWORDBUTTON;
import static com.nanyang.richeditor.editor.EditorContainer.KEYWORDITEM;
import static com.nanyang.richeditor.editor.EditorContainer.REFERENCEBUTTON;
import static com.nanyang.richeditor.editor.EditorContainer.REFERENCEITEM;
import static com.nanyang.richeditor.editor.EditorContainer.RELOAD;
import static com.nanyang.richeditor.editor.EditorContainer.RICHTEXT_NUM;
import static com.nanyang.richeditor.editor.EditorContainer.UNDOJOB;
import static com.nanyang.richeditor.editor.EditorContainer.VIDEOSHOW;
import static com.nanyang.richeditor.editor.EditorUtils.KEYWORDS;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_CANCEL;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_EDIT;
import static com.nanyang.richeditor.editor.EditorUtils.RESULT_NEW;
import static com.nanyang.richeditor.editor.EditorUtils.extractUrlFromContent;
import static com.nanyang.richeditor.editor.EditorUtils.hideSoftInput;
import static com.nanyang.richeditor.editor.EditorUtils.searchDialog;
import static com.nanyang.richeditor.editor.EditorUtils.specifiedSearch;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getMessageHandler;
import static com.nytaiji.nybase.httpShare.WifiShareUtil.getPreferredServerUrl;
import static com.nytaiji.nybase.utils.BitmapUtil.getVideoThumbnail;
import static com.nytaiji.nybase.utils.BitmapUtil.saveBitmap;
import static com.nytaiji.nybase.filePicker.MediaSelection.getMediaLinkDialog;
import static com.nytaiji.nybase.filePicker.MediaSelection.getSavedMediaDialog;
import static com.nytaiji.nybase.utils.NyFileUtil.FileToStr;
import static com.nytaiji.nybase.utils.NyFileUtil.copyAsset;
import static com.nytaiji.nybase.utils.NyFileUtil.getParentPath;
import static com.nytaiji.nybase.utils.NyFileUtil.isAudio;
import static com.nytaiji.nybase.utils.NyFileUtil.isDocument;
import static com.nytaiji.nybase.utils.NyFileUtil.isImage;
import static com.nytaiji.nybase.utils.NyFileUtil.isMedia;
import static com.nytaiji.nybase.utils.NyFileUtil.isOnline;
import static com.nytaiji.nybase.utils.NyFileUtil.isVideo;
import static com.nytaiji.nybase.utils.SystemUtils.hideInputMethod;
import static com.nytaiji.nybase.utils.SystemUtils.hideStatusBar;
import static com.nytaiji.nybase.utils.VideoJsonUtil.readListFromPath;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.OpenHelper;
import com.nanyang.richeditor.database.Note;
import com.nanyang.richeditor.memento.BaseAdapter;
import com.nanyang.richeditor.memento.BaseFragment;
import com.nanyang.richeditor.memento.NoteFragment;
//import com.nanyang.richeditor.text.TextEditorActivity;
//import com.nanyang.richeditor.youtube.YoutubeFragment;
import com.nytaiji.nybase.permisssion.PermissionActivity;
import com.nytaiji.nybase.utils.GeneralCallback;
import com.nytaiji.nybase.httpShare.WifiShareUtil;
import com.nytaiji.nybase.utils.NyFileUtil;
import com.nytaiji.nybase.view.ColorPaletteView;
//import com.nytaiji.drawview.DrawingActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;


public class EditorActivity extends PermissionActivity implements View.OnClickListener, EditorContainer.ButtonCallout {
    public static final int REQUEST_CODE_CHOOSE_HTML = 100900;
    public static final int REQUEST_CODE_GET_DRAWING = 100892;
    private static final String TAG = "EditorActivity";
    //---------------------//

    private static final int CAMERA_REQUEST = 500000;

    //  private View openedDialog;

    private final String tempH = new File(NyFileUtil.getCacheFolder(), "temp").getAbsolutePath();
    private Toolbar toolbar;
    private HorizontalScrollView editorbar;
    public Note note = null;

    public Handler shareHandle;
    public String mediaLink = null;
    private int noteResult = 0;
    private int position;
    private boolean editable = true;
    private boolean toMove = false;
    private int requestCode = 0;
    private EditorContainer editorContainer;
    private ColorPaletteView colorPicker;
    private NyRichEditor mNyRichEditor;

    private LinearLayout bottom_action_bar, actionBarContainer, richtextBarContainer;
    private List<String> editList = null;       //for output
    private String passWord;
    private boolean loadingError = false;
    private boolean isNewNote = true;
    private String defaultType = "IMAGE";
   // private ArrayList<String> allMedias = new ArrayList<>();
    private boolean noChange = true;
    private static FragmentActivity editorActivity;
    private FrameLayout fragmentContainer;
    private View mainFrame, navButton, youtube;
    private Fragment fragment;
    private Dialog mediaDialog;
    private boolean insertBox = false;
    private boolean searchLocal = true;
    private HashMap<String, Long> mReference = new HashMap<String, Long>();
    private EditorActivityHelper editorActivityHelper;
    private boolean directWeb = false;
    private boolean goBackInWeb = false;
    private final List<String> missPathList = new ArrayList<String>();
    private String saveLink = null;
    private ImageView searchButton;
    private Toolbar youtubeTool;

    public static FragmentActivity geInstance() {
        return editorActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postPermissionGranted();
    }

  //  @Override
    protected void postPermissionGranted() {
        editorActivity = this;
        Intent intent = getIntent();
        // setTheme(Category.getStyle(intent.getIntExtra(DbHelper.COLUMN_THEME, Category.THEME_GREEN)));
        setContentView(R.layout.activity_editor);
        hideStatusBar(this);
        initView();
        shareHandle = getMessageHandler(findViewById(R.id.fl_container));

        //-------//
        long noteId = intent.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID);
        int type = intent.getIntExtra(OpenHelper.COLUMN_TYPE, TYPE_NOTE);
        if (type == TYPE_WEBSITE) {
            directWeb = true;
            final String link = new Controller(App.instance).findNote(noteId).keywords;
            showMain(false);
            youtubeTool.setVisibility(VISIBLE);
            fragment = new ShowHtmlFragment();
            Bundle arguments = new Bundle();
            arguments.putString("link", link);
            fragment.setArguments(arguments);
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, "NoteFragment");
            ft.commitAllowingStateLoss();
        } else if (type == TYPE_YOUTUBE) {
            Toast.makeText(this, "Tmp out", Toast.LENGTH_LONG).show();
           /* directWeb = true;
            showMain(false);
            searchButton.setVisibility(GONE);
            youtubeTool.setVisibility(VISIBLE);
            // toolbar.setVisibility(GONE);
            fragment = new YoutubeFragment();
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, TAG);
            ft.commitAllowingStateLoss();*/
        } else {
            // TODO ny need to be initiate first
            position = intent.getIntExtra(POSITION, 0);
            final long categoryId = intent.getLongExtra(OpenHelper.COLUMN_PARENT_ID, DatabaseModel.NEW_MODEL_ID);

            if (noteId != DatabaseModel.NEW_MODEL_ID) {
                note = new Controller(App.instance).findNote(noteId);
            }

            if (note == null) {
                note = new Note();
                setNoteResult(RESULT_NEW, false);
                note.parentId = categoryId;
                note.isArchived = false;
                note.type = TYPE_NOTE;
                isNewNote = true;
            } else {
                isNewNote = false;
                setNoteResult(RESULT_EDIT, false);
                importNote(note);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideStatusBar(this);
    }

    private void initView() {
        editorActivityHelper = new EditorActivityHelper();
        editorbar = findViewById(R.id.hsv_action_bar);
        mainFrame = findViewById(R.id.main_frame);
        navButton = findViewById(R.id.nav_btn);
     //   youtube = findViewById(R.id.iv_youtube);
        youtubeTool = findViewById(R.id.youtube_toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        editorContainer = findViewById(R.id.mixed_container);
        fragmentContainer = findViewById(R.id.fragment_container);
        actionBarContainer = (LinearLayout) findViewById(R.id.action_bar_container);
        richtextBarContainer = (LinearLayout) findViewById(R.id.richtext_bar_container);
        bottom_action_bar = (LinearLayout) findViewById(R.id.bottom_action_bar);
        searchButton = findViewById(R.id.app_search);
        colorPicker = findViewById(R.id.cpv_color);
        colorPicker.setOnColorChangeListener(color -> {
            if (editorActivityHelper.getActionType() == ActionType.FORE_COLOR) {
                mNyRichEditor.setTextColor(Color.parseColor(color));
            } else mNyRichEditor.setTextBackgroundColor(Color.parseColor(color));
            colorPicker.setVisibility(GONE);
        });

        editorActivityHelper.setColorPicker(colorPicker);

        initOnClickListener();

        editable = true;
        toMove = false;
        showEditor(editable, toMove);

        //----------very important---
        editorContainer.setFocusChangeCallback(new FocusChangeCallback() {
            @Override
            public void onFocusChanged(boolean changed) {
                editorContainer.clearRichBackGround();
                mNyRichEditor = editorActivityHelper.setRichEditor(EditorActivity.this,
                        editorContainer.lastFocusEdit,
                        richtextBarContainer,
                        new MRichEditorCallback());
                mNyRichEditor.setEditorBackgroundColor(Color.parseColor("#ededed"));
                // mNyRichEditor.setTextColor(Color.WHITE);
                // richtextBarContainer.setVisibility(View.GONE);
            }
        });
        editorContainer.setOnTextChangeListener(new NyRichEditor.OnTextChangeListener() {
            //for all richtexts
            @Override
            public void onTextChange(String text) {
                noChange = false;
            }
        });

        mNyRichEditor = editorActivityHelper.setRichEditor(this, editorContainer.lastFocusEdit, richtextBarContainer, new MRichEditorCallback());

        editorContainer.setButtonCallout(this);
    }


    //ButtonCallout from EditorContainer
    @Override
    public void trigged(int type, String content) {
        Intent intent;
        String link;
        Bundle arguments = new Bundle();
        FragmentTransaction ft;
        switch (type) {
            case UNDOJOB:
                noChange = false;
                findViewById(R.id.iv_action_undo).setVisibility(VISIBLE);
                break;
            case RICHTEXT_NUM:
                // Log.e(TAG,"REMOVERICHTEXT No ="+content);
                noChange = !editable;
                if (Integer.parseInt(content) < 1) {
                    mNyRichEditor = null;
                    bottom_action_bar.setVisibility(GONE);
                } else if (editable) bottom_action_bar.setVisibility(VISIBLE);
                break;
            case CODESHOW:
                // Log.e(TAG, "CODESHOW: =" + content);
                String url = extractUrlFromContent(content);
                Log.e(TAG, "url =" + url);
                link = getPreferredServerUrl(false);
                WifiShareUtil.httpShare(this.getApplicationContext(), Uri.parse(url), link, shareHandle);
                Toast.makeText(this, "Http Server to: " + link, Toast.LENGTH_LONG).show();
                break;
            case VIDEOSHOW:
                Log.e(TAG, "VIDEOSHOW: =" + content);
                link = getPreferredServerUrl(false);
                WifiShareUtil.httpShare(this.getApplicationContext(), Uri.parse(content), link, getMessageHandler(findViewById(R.id.fl_container)));
                Toast.makeText(this, "Http Server to: " + link, Toast.LENGTH_LONG).show();
                break;
            case EXPORT:
                Toast.makeText(this, "Tmp out", Toast.LENGTH_LONG).show();

               /* String text = Html.fromHtml(content).toString();
                intent = new Intent(this, TextEditorActivity.class);
                // intent.setType();
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setAction(Intent.ACTION_SEND);
                startActivity(intent);*/
                break;
            case KEYWORDBUTTON:
                noChange = !editable;
                EditorUtils.pickupKeyWordsDialog(EditorActivity.this, new KeywordListener() {
                    @Override
                    public void onListChanged(ArrayList<String> chosenChildren, boolean toAppend) {
                        editorContainer.setKeywords(chosenChildren, toAppend);
                    }
                });
                break;
            case KEYWORDITEM:
                showMain(false);
                //  if (content.equals("local"))searchLimit = true;else searchLimit = false;
                fragment = new NoteFragment();
                ((NoteFragment) fragment).setCategoryId(DatabaseModel.SEARCH_CLICK);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
                specifiedSearch(this, content, KEYWORDS, true,
                        ((BaseFragment) fragment).searchCallback,
                        new Controller(App.instance).findAllNotesInCategory(note.parentId));
                //  Toast.makeText(EditorActivity.this, "KEYWORDITEM = "+content, Toast.LENGTH_SHORT).show();
                //  search(content, false);
                break;
            case REFERENCEBUTTON:
                noChange = !editable;
                searchLocal = content.equals("local");
                showMain(false);
                //continuous search
                fragment = null;
                fragment = new NoteFragment();
                ((NoteFragment) fragment).setCategoryId(DatabaseModel.SEARCH_SELECT);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
                search("", true);
                break;
            case REFERENCEITEM:
                fragment = new NoteFragment();
                ((NoteFragment) fragment).setCategoryId(DatabaseModel.SEARCH_CLICK);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
                Note newnote = new Controller(App.instance).findNote(new Controller(App.instance).titleToId(content));
                if (newnote != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((NoteFragment) fragment).checkedOpen(newnote, 0);
                                }
                            });
                            interrupt();
                        }
                    }.start();
                } else
                    Toast.makeText(EditorActivity.this, content + "is not a valid link", Toast.LENGTH_SHORT).show();
                break;
            case ITEMCHANGE:
                noChange = !editable;
                break;

            case RELOAD:
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        note.body = editorContainer.exportEditString().toString();
                        reload();
                        editable = true;
                        toMove = true;
                        showEditor(true, true);
                    }
                }, 100);

                break;
        }

    }

  /*  private void startMediaPlayerActivity(Uri videoUri, Uri subtitleUri) {
        Intent intent = new Intent((Context) this, MediaPlayerActivity.class);
        intent.putExtra(MediaPlayerActivity.Companion.getMediaUri(), (Parcelable) videoUri);
        intent.putExtra(MediaPlayerActivity.Companion.getSubtitleUri(), (Parcelable) subtitleUri);
        intent.putExtra(MediaPlayerActivity.Companion.getSubtitleDestinationUri(), (Parcelable) Uri.fromFile(this.getCacheDir()));
        intent.putExtra(MediaPlayerActivity.Companion.getOpenSubtitlesUserAgent(), "TemporaryUserAgent");
        intent.putExtra(MediaPlayerActivity.Companion.getSubtitleLanguageCode(), "rus");
        this.startActivity(intent);
    }*/

    private void showMain(boolean show) {
        if (show) {
            toolbar.setVisibility(VISIBLE);
            editorbar.setVisibility(VISIBLE);
            mainFrame.setVisibility(VISIBLE);
            if (fragment != null) fragment = null;
            fragmentContainer.removeAllViews();
            fragmentContainer.setVisibility(GONE);
            //the next back from youtube
            searchButton.setVisibility(VISIBLE);
            youtubeTool.setVisibility(GONE);
            //  ((TextView) appTitle).setText(R.string.app_name);
        } else {
            //visibility of toolbar relies on indivisual fragment
            editorbar.setVisibility(View.INVISIBLE);
            mainFrame.setVisibility(View.INVISIBLE);
            fragmentContainer.setVisibility(VISIBLE);
            fragmentContainer.removeAllViews();
            //   ((TextView) appTitle).setText(R.string.search);
        }
    }


    private void search(String key, boolean allowEmpty) {
        if (key.trim().isEmpty() && !allowEmpty) {
            Toast.makeText(this, "Search key should not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }


        ((BaseFragment) fragment).setSelectionListener(new BaseAdapter.SelectionListener() {
            @Override
            public void onItemSelected(ArrayList selected) {
                //  mSelected = (ArrayList<Note>) selected;
                updateReferenceFromNote(selected);
                showMain(true);
            }
        });

        searchDialog(this, ((BaseFragment) fragment).searchCallback, searchLocal ? note.parentId : -1);
    }


    private void initOnClickListener() {
        int[] viewIds = new int[]{
                R.id.nav_btn,
                R.id.iv_action_open,
                R.id.iv_edit_toggle,
                R.id.iv_action_save,
             //   R.id.iv_youtube,
                R.id.iv_insert_text,
                R.id.iv_action_undo,
                R.id.iv_text_redo,
                R.id.iv_text_undo,
                R.id.mixed_container,
                R.id.app_search,
                R.id.iv_text_search
                //  R.id.iv_insert_link
                //  R.id.txt_reference
        };

        for (int viewId : viewIds) {
            //  Log.e(TAG, "viewID  " + viewId);
            findViewById(viewId).setOnClickListener(this);
        }
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.nav_btn:
                onBackPressed();
                break;
            case R.id.iv_youtube:
                Toast.makeText(this, "Tmp out ", Toast.LENGTH_LONG).show();
               /* showMain(false);
                searchButton.setVisibility(GONE);
                youtubeTool.setVisibility(VISIBLE);
                // toolbar.setVisibility(GONE);
                fragment = new YoutubeFragment();
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TAG);
                ft.commitAllowingStateLoss();*/
                break;
            // VoiceRecognition.getInstance(this).start();
            case R.id.iv_action_save:
                if (editable || toMove) {
                    note.body = editorContainer.exportEditString().toString();
                    saveNoteChange(note.body, false);
                    noChange = true;
                }
                break;
            case R.id.iv_action_open:
                getMediaLinkDialog(EditorActivity.this, mediaLink, new GeneralCallback() {
                            @Override
                            public void SingleString(String path) {
                                smartOpenLink(path);
                            }

                            @Override
                            public void SingleBoolean(boolean yesOrNo) {
                                insertBox = yesOrNo;
                            }

                            @Override
                            public void MultiStrings(ArrayList<String> paths) {

                            }

                        },
                        new GeneralCallback() {
                            @Override
                            public void SingleString(String path) {
                                mediaLink = path;
                                if (isMedia(mediaLink) || isDocument(mediaLink)) {
                                    smartOpenLink(mediaLink);
                                } else if (mediaLink.endsWith("lnk")) {
                                    ArrayList<String> allMedias = new ArrayList<>();
                                    saveLink = mediaLink;
                                    Scanner s = null;
                                    try {
                                        s = new Scanner(new File(mediaLink));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    while (s.hasNext()) {
                                        //  Log.e(TAG, "filePath =" + s.next());
                                        allMedias.add(s.next());
                                    }
                                    s.close();
                                    //  List<String> savedLinks = readListFromPath(this, filePath);
                                    // Log.e(TAG, "savedLinks=" + savedLinks.toString());
                                    getSavedMediaDialog(EditorActivity.this, allMedias, new GeneralCallback(){
                                        @Override
                                        public void SingleString(String path) {
                                            mediaLink = path;
                                                smartOpenLink(mediaLink);
                                        }

                                        @Override
                                        public void SingleBoolean(boolean yesOrNo) {

                                        }

                                        @Override
                                        public void MultiStrings(ArrayList<String> paths) {

                                        }

                                    });
                                }

                            }

                            @Override
                            public void SingleBoolean(boolean yesOrNo) {

                            }

                            @Override
                            public void MultiStrings(ArrayList<String> paths) {

                            }


                        });
                //
                //  EditorUtils.openDirChooseFile(EditorActivity.this, "*/*", requestCode);
                //TODO ny
                //  editorContainer.setKeywords(keywords);

                break;
            case R.id.iv_edit_toggle:
                // noChange = false; //regardless whether you change or not so long you toggle it
                if (!loadingError) {
                    if (!editable && !toMove) {
                        editable = true;
                      //  youtube.setVisibility(GONE);
                        Toast.makeText(this, "Editor enabled", Toast.LENGTH_LONG).show();
                    } else if (editable && !toMove) {
                      //  youtube.setVisibility(GONE);
                        toMove = true;
                        Toast.makeText(this, "Moving/delete enabled", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Editor disabled!", Toast.LENGTH_LONG).show();
                        editable = false;
                        toMove = false;
                        editorContainer.clearRichBackGround();
                      //  youtube.setVisibility(VISIBLE);
                    }
                    showEditor(editable, toMove);

                } else {
                    fixMissingLinksDialog();
                    // Toast.makeText(this, "Non-editable due to loading error!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.iv_insert_text:
                //TODO ny
                // editorContainer.setKeywords(keywords);
                if (editable) {
                    onClickInsertRichText(null);
                }
                break;

            case R.id.iv_action_undo:
                importEditList(editorContainer.getUndoList());
                findViewById(R.id.iv_action_undo).setVisibility(GONE);
                break;

            //TODO ny the following two are from the bottom bar
            case R.id.iv_text_undo:
                if (editable) mNyRichEditor.undo();
                break;
            case R.id.iv_text_redo:
                if (editable) {
                    //  richtextBarContainer.setVisibility(VISIBLE);
                    mNyRichEditor.redo();
                }
                break;
            case R.id.app_search:
                if (fragment != null && fragment instanceof NoteFragment)
                    ((NoteFragment) fragment).startSearch();
                else if (editable) findReplace(this, false);
                break;

            case R.id.iv_text_search:
                if (editable) findReplace(this, true);
                break;
        }

        hideInputMethod(this, view);
    }


    private void showEditor(boolean editable, boolean movable) {
        actionBarContainer.setVisibility(editable || movable ? VISIBLE : GONE);
        bottom_action_bar.setVisibility(editable ? VISIBLE : GONE);
        editorContainer.setEditable(editable, movable);
        editorContainer.refreshDrawableState();
        //TODO ny
        if (!editable)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //else
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getData() != null) {
            ArrayList<String> allMedias=new ArrayList<>();

            String filePath = NyFileUtil.getPath(this, data.getData());
            Log.e(TAG, "data.getData()= " + data.getData().getPath());
            Log.e(TAG, "filePath = " + filePath);

            int index = filePath.indexOf("/storage/");
            if (index > -1) filePath = filePath.substring(index);
            if (isMedia(filePath) || isDocument(filePath)) {
                String mediaLink = filePath;
                smartOpenLink(mediaLink);
            } else if (filePath.endsWith("lnk")) {
                saveLink = filePath;
                Scanner s = null;
                try {
                    s = new Scanner(new File(filePath));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                allMedias.clear();
                while (s.hasNext()) {
                    //  Log.e(TAG, "filePath =" + s.next());
                    allMedias.add(s.next());
                }
                s.close();
                //  List<String> savedLinks = readListFromPath(this, filePath);
                // Log.e(TAG, "savedLinks=" + savedLinks.toString());
                getSavedMediaDialog(EditorActivity.this, allMedias, new GeneralCallback() {
                    @Override
                    public void SingleString(String path) {
                        mediaLink=path;
                        smartOpenLink(mediaLink);
                    }

                    @Override
                    public void SingleBoolean(boolean yesOrNo) {

                    }

                    @Override
                    public void MultiStrings(ArrayList<String> paths) {

                    }

                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void smartOpenLink(final String mediaLink) { //handling unknown type of links and mistakely loaded medias
        noChange = !editable;
        editorContainer.clearRichBackGround();
        if (mNyRichEditor == null) insertBox();
        if (mediaLink.contains(".html")) {
            //open html
            editList = readListFromPath(this, mediaLink);
            importEditList(editList);
        } else if (mediaLink.contains(".txt") || mediaLink.contains(".rtf")) {
            insertText(mediaLink);
        } else if (mediaLink.contains("youtu.be") || mediaLink.contains("youtube.com")) {
            String VID = NyFileUtil.getLastSegmentFromString(mediaLink);
            VID = VID.replace("watch?=", "");
            String youtubeLink = "https://www.youtube.com/embed/" + VID;
            if (mNyRichEditor == null) insertBox();
            mNyRichEditor.insertYoutubeVideo(youtubeLink, px2dp(App.DEVICE_WIDTH), 2 * px2dp(App.DEVICE_WIDTH) / 3);
        } else {
            //TODO ny
            if (mediaLink.contains("_NY") || isOnline(mediaLink)) {
                final String path = mediaLink;
                new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isImage(path))
                                    editorContainer.insertServerImage(path);
                                else if (isVideo(mediaLink))
                                    editorContainer.insertServerVideo(path);
                                else if (defaultType.equals("IMAGE"))
                                    editorContainer.insertServerImage(path);
                                else if (defaultType.equals("VIDEO"))//video
                                    editorContainer.insertServerVideo(path);
                                else openUnknownLink(mediaLink);
                            }
                        });
                        interrupt();
                    }
                }.start();

            } else {
                //
                if (insertBox) insertBox();
                else smartInsertText();

                if (isImage(mediaLink))
                    mNyRichEditor.insertImage(mediaLink, null, px2dp(App.DEVICE_WIDTH));
                else if (isVideo(mediaLink)) {
                    mNyRichEditor.insertVideo(mediaLink, px2dp(App.DEVICE_WIDTH));
                } else if (isAudio(mediaLink))
                    mNyRichEditor.insertAudio(mediaLink);
                else if (defaultType == null)//to open un-identified medias
                    openUnknownLink(mediaLink);
                else if (defaultType.equals("IMAGE"))
                    mNyRichEditor.insertImage(mediaLink, null, px2dp(App.DEVICE_WIDTH));
                else if (defaultType.equals("VIDEO"))//video
                    mNyRichEditor.insertVideo(mediaLink, px2dp(App.DEVICE_WIDTH), getThumbnail(mediaLink));
                else
                    mNyRichEditor.insertAudio(mediaLink);
            }
        }
    }


    private void openUnknownLink(String mediaLink) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select media type:")
                .setCancelable(false)
                .setNegativeButton("Video/Audio", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaLink.contains("_NY") || isOnline(mediaLink))
                            editorContainer.insertServerVideo(mediaLink);
                        else
                            mNyRichEditor.insertVideo(mediaLink, px2dp(App.DEVICE_WIDTH), getThumbnail(mediaLink));
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaLink.contains("_NY") || isOnline(mediaLink))
                            editorContainer.insertServerImage(mediaLink);
                        mNyRichEditor.insertImage(mediaLink, null, px2dp(App.DEVICE_WIDTH));
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void smartInsertText() {
        if (mNyRichEditor == null || editorContainer.getLastRichtextIndex() + 1 < editorContainer.getNextIndex()) {
            insertBox();
        }
    }

    private void insertBox() {
        editorContainer.addEmptyAtIndex(editorContainer.getNextIndex());
        mNyRichEditor = editorActivityHelper.setRichEditor(this, editorContainer.lastFocusEdit, richtextBarContainer, new MRichEditorCallback());
        noChange = !editable;
        showEditor(editable, toMove);
    }


    private void insertText(String path) {
        String content = FileToStr(path, StandardCharsets.UTF_8);
        //  content=content.replace(",","\n");
        onClickInsertRichText(content);
    }

    private void onClickInsertRichText(String html) {
        editorContainer.addRichTextAtIndex(editorContainer.getNextIndex(), html);
        mNyRichEditor = editorActivityHelper.setRichEditor(this, editorContainer.lastFocusEdit, richtextBarContainer, new MRichEditorCallback());
        noChange = !editable;
        showEditor(editable, toMove);
    }

    private String getThumbnail(String mediaLink) {
        String newF = new File(getParentPath(mediaLink), NyFileUtil.getFileNameWithoutExtFromPath(mediaLink) + ".vng").getAbsolutePath();
        if (new File(newF).exists()) return newF;
        Bitmap bitmap = getVideoThumbnail(mediaLink);
        if (bitmap != null) saveBitmap(newF, bitmap, Bitmap.CompressFormat.JPEG, 20);
        else copyAsset(this, "ntu.vng", newF);
        return newF;
    }

  /*  private void onClickInsertDrawing(String path) {
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.putExtra("imagePath", path);
        startActivityForResult(intent, REQUEST_CODE_GET_DRAWING);
        // editorContainer.addDrawingAtIndex(editorContainer.getLastIndex(), path);
    }*/

    private void echoMediaMissing(String path) {
        Toast.makeText(this, "Media files missing!", Toast.LENGTH_SHORT).show();
        loadingError = true;
        editable = false;
        missPathList.add(path);
    }



    private String pathWrong = null;

    private void fixMissingLinksDialog() {
        final View root = this.getLayoutInflater().inflate(R.layout.layout_replace_media_url, null);
        EditText dialogUrlEdittext = root.findViewById(R.id.dialog_url_edittext);


        ListView lvMain = (ListView) root.findViewById(R.id.lvMain);
        lvMain.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, missPathList);

        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pathWrong = missPathList.get(position);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(root)
                .setTitle("Fix missing local medias")
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("Reload", (dialog, which) -> {
            reload();
        });

        mediaDialog = builder.create();
        mediaDialog.show();

        root.findViewById(R.id.dialog_url_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clearClipboard(EditorActivity.this);
                dialogUrlEdittext.setText("");
            }
        });

        root.findViewById(R.id.dialog_url_replace_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplaceMissingLink(pathWrong, dialogUrlEdittext.getText().toString());
            }
        });
    }

    private void ReplaceMissingLink(String pathWrong, String pathCorrect) {
        //  Log.e(TAG, "pathWrong= " + pathWrong);
        //  Log.e(TAG, "pathCorrect= " + pathCorrect);
        if (pathWrong != null && pathCorrect != null && !pathWrong.equals(pathCorrect)) {
            note.body = note.body.replace(pathWrong, pathCorrect);
            Toast.makeText(this, "Media link is replaced", Toast.LENGTH_SHORT).show();
        }
    }

    private void reload() {
        saveNoteChange(note.body, false);
        importNote(note);
    }

    public interface SaveListener {
        void onSave();
    }


    //-------------------------
//  @Override
    private void setNoteResult(int result, boolean closeActivity) {
        noteResult = result;
        if (closeActivity) {
            Intent data = new Intent();
            data.putExtra(POSITION, position);
            data.putExtra(OpenHelper.COLUMN_ID, note.id);
            setResult(result, data);
            finish();
        }
    }

    private void importNote(Note note) {
        editorContainer.dragLinearLayout.removeAllViews();
        editorContainer.resetIndex();
        // Log.e(TAG, "importEditList" + list.toString());
        editorContainer.createTop(EDIT_PADDING);
        //important to
        editorContainer.setTitle(note.title);
        editorContainer.setDate(note.datelong);
        editorContainer.setKeywords(new ArrayList<String>(Arrays.asList(note.keywords.split(" "))), false);

        if (note.body == null) {
            editable = false;
            Toast.makeText(this, note.title + " body empty!", Toast.LENGTH_SHORT).show();
            setNoteResult(RESULT_CANCEL, true);
            return;
        }

        if (note.remark != null && !note.remark.isEmpty()) editorContainer.setRemark(note.remark);
        // Log.e(TAG, "note.reference=" + note.reference);
        if (note.reference != null) {

            List<String> referenceId = new ArrayList<String>(Arrays.asList(note.reference.split(" ")));
            if (referenceId.size() > 0) {
                ArrayList<String> selected = new ArrayList<>();
                for (String idS : referenceId) {
                    idS = idS.trim();
                    // Log.e(TAG, "importEditList idS=" + idS);
                    if (idS.length() > 0) {
                        try {
                            long nId = Long.parseLong(idS);
                            if (nId > 0L) {
                                Note noteS = new Controller(App.instance).findNote(nId);
                                if (noteS != null) {
                                    mReference.put(noteS.title, nId);
                                    selected.add(noteS.title);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // e.printStackTrace();
                        }
                    }
                }
                editorContainer.addReference(selected, true);
            }
        }
        String[] result = note.body.substring(1, note.body.length() - 1).split(",");
        editList = Arrays.asList(result);

        editable = false;
        importEditList(editList);
    }

    private void importEditList(List<String> list) {
        //hide all buttons
        int num = list.size();
        for (int index = 0; index < num; index++) {
            String item = list.get(index).trim();
            if (item.contains("video:")) {
                String input = item.replace("video:", "");
                if (isOnline(input) || new File(input).exists()) {
                    mediaLink = input;
                    new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    editorContainer.addSeverVideoViewAtIndex(editorContainer.getNextIndex(), input);
                                    editable = false;
                                    toMove = false;
                                    showEditor(editable, toMove);
                                }
                            });
                            interrupt();
                        }
                    }.start();

                    // delay(150);
                } else echoMediaMissing(input);
            } else if (item.contains("image:")) {
                String input = item.replace("image:", "");
                if (isOnline(input) || new File(input).exists()) {
                    mediaLink = input;
                    new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    editorContainer.addServerImageAtIndex(editorContainer.getNextIndex(), input);
                                    editable = false;
                                    toMove = false;
                                    showEditor(editable, toMove);
                                }
                            });
                            interrupt();
                        }
                    }.start();
                } else echoMediaMissing(input);
            } else if (item.contains("richtext:")) {
                String input = item.replace("richtext:", "").replace(COMMA, ",");
                new Thread() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onClickInsertRichText(input);
                                editable = false;
                                toMove = false;
                                showEditor(editable, toMove);
                            }
                        });
                        interrupt();
                    }
                }.start();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editorContainer.clearAllLayout();
        editorContainer.removeAllViews();
        editorContainer.destroyDrawingCache();
        NyFileUtil.cleanCache(this);
    }

    //--------------------------------//

    private void saveNoteChange(String body, boolean closeActivity) {
        saveNote(body, new SaveListener() {
            @Override
            public void onSave() {
                final Intent data = new Intent();
                data.putExtra(POSITION, position);
                data.putExtra(OpenHelper.COLUMN_ID, note.id);

                switch (noteResult) {
                    case RESULT_NEW:
                        data.putExtra(OpenHelper.COLUMN_TYPE, note.type);
                    case RESULT_EDIT:
                        data.putExtra(OpenHelper.COLUMN_TITLE, note.title);
                }

                data.putExtra(OpenHelper.COLUMN_DATE, note.datelong);
                data.putExtra(OpenHelper.COLUMN_KEYWORDS, note.keywords);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setResult(noteResult, data);
                        if (closeActivity) finish();
                        else
                            Toast.makeText(EditorActivity.this, "Change has been saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void saveNote(final String body, final SaveListener listener) {
        String inputTitle = editorContainer.getTitle();
        if (inputTitle.isEmpty()) inputTitle = "nyNote";
        // Log.e(TAG, "inputTitle=" + inputTitle + "note.title=" + note.title);
        //to enforce a unique name in the same category
        if (!Objects.equals(note.title, inputTitle))
            note.title = new Controller(App.instance).formatTitle(note.parentId, inputTitle);
        note.datelong = editorContainer.getDate();
        String keywords = editorContainer.getKeywords().toString().replace(",", " ");
        note.keywords = keywords.substring(1, keywords.length() - 1);
        //join body and reference
        note.body = body;
        note.reference = getReference();

        note.remark = editorContainer.getRemark();
        new Thread() {
            @Override
            public void run() {
                if (!loadingError) {
                    // Log.e(TAG, "note.reference=" + note.reference);
                    long id = note.save();
                    if (note.id == DatabaseModel.NEW_MODEL_ID) {
                        note.id = id;
                        //  note.save();
                    }
                }
                listener.onSave();
                interrupt();
            }
        }.start();
    }

    public String getReference() {
        String reference = "";
        // Log.e(TAG, "mReference=" + mReference.toString());
        if (!mReference.isEmpty()) {
            ArrayList<String> referenceTitle = editorContainer.getReference();
            // Log.e(TAG, "referenceTitle=" + referenceTitle.toString());
            for (int i = 0; i < referenceTitle.size(); i++) {
                reference = reference + String.valueOf(mReference.get(referenceTitle.get(i))) + " ";
            }
        }
        return reference.trim();

    }

    private void updateReferenceFromNote(ArrayList<Note> selectedNote) {
        ArrayList<String> selected = new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Note snote : selectedNote) {
                            selected.add(snote.title);
                            mReference.put(snote.title, snote.id);
                            Note noteS = new Controller(App.instance).findNote(snote.id);
                            if (noteS.reference == null || !noteS.reference.contains(String.valueOf(note.id))) {
                                noteS.reference = String.valueOf(note.id) + " " + noteS.reference;
                                noteS.reference = noteS.reference.trim();
                                noteS.save();
                            }
                        }
                        editorContainer.addReference(selected, true);
                    }
                });
            }
        }.start();
    }

    class MRichEditorCallback extends RichEditorCallback {
        @Override
        public void notifyFontStyleChange(ActionType type, final String value) {
            ActionImageView actionImageView =
                    (ActionImageView) richtextBarContainer.findViewWithTag(type);
            if (actionImageView != null) {
                actionImageView.notifyFontStyleChange(type, value);
            }
        }

    }

    @Override
    public void onBackPressed() {
        // Log.e(TAG, "mNyRichEditor.canGoBack()="+mNyRichEditor.canGoBack());
        super.onBackPressed();
        if (mNyRichEditor != null && mNyRichEditor.canGoBack()) {
            goBackInWeb = true;
            mNyRichEditor.goBack();
            return;
        }


        if (fragment instanceof ShowHtmlFragment) {
            if (((ShowHtmlFragment) fragment).onBackPressed()) {
                return;
            } else if (directWeb) finish();
            else {
                showMain(true);
            }
            return;
        }

      /*  if (fragment instanceof YoutubeFragment) {
            if (((YoutubeFragment) fragment).onBackPressed()) {
                return;
            } else if (directWeb) finish();
            else {
                showMain(true);
            }
            return;
        }*/

        if (fragmentContainer.getVisibility() == VISIBLE) {
            showMain(true);
            return;
        }

        // Log.e(TAG, "goBackInWeb="+goBackInWeb);
        //reload to enable normal display of the editor
        if (goBackInWeb) {
            goBackInWeb = false;
            //avoid loss of editing during show web??
            note.body = editorContainer.exportEditString().toString();
            reload();
            return;
        }
        //TODO ny
        if (noChange) {
            setNoteResult(RESULT_CANCEL, true);
            // finish();
        }//just leave
        else exitDialog();
    }

    private void exitDialog() {
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leaving?")
                .setMessage("Save changes before leaving!")
                .setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setNoteResult(RESULT_CANCEL, true);//to avoid create a new entry
                    }
                })
                .setCancelable(true);
        builder.setPositiveButton("Saved leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveNoteChange(editorContainer.exportEditString().toString(), true);
                dialog.dismiss();
            }

        });
        builder.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            View v = getCurrentFocus();
          //  if (v==null) return false;
            if (isShouldHideInput(v, ev)) {
                if (hideInputMethod(this, v)) {
                    return true; //隐藏键盘时，其他控件不响应点击事件==》注释则不拦截点击事件
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        //TODO ny
      //  if (v==null) return false;
        if (v instanceof EditText || v instanceof WebView || v == bottom_action_bar) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0], top = leftTop[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            // 保留点击EditText的事件
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        return false;
    }

    private void findReplace(Context context, boolean toReplace) {
        hideSoftInput((Activity) context);
        //TODO ny no effect for the next line
        // if (mNyRichEditor==null) Toast.makeText(context, "Insert a Richtext item first!", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_find_replace, null);
        EditText findKey = rootView.findViewById(R.id.txt_search);
        EditText replaceKey = rootView.findViewById(R.id.txt_replacement);
        if (toReplace) replaceKey.setVisibility(VISIBLE);
        else replaceKey.setVisibility(GONE);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                .setTitle(R.string.find)
                .setView(rootView)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.findonly, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = findKey.getText().toString().trim();
                        if (!toReplace) editorContainer.findKeyinAllText(key);
                        else editorContainer.findKeyinCurrentText(key);
                        dialog.dismiss();
                    }
                });

        if (toReplace)
            builder.setNeutralButton(R.string.replaceall, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String key = findKey.getText().toString().trim();
                    String replace = replaceKey.getText().toString().trim();
                    if (key.isEmpty() || replace.isEmpty())
                        Toast.makeText(EditorActivity.this, "Please input a search key and a replace key!", Toast.LENGTH_SHORT).show();
                    else editorContainer.replaceKeyinCurrentText(key, replace);
                    dialog.dismiss();
                }
            });

        builder.show();
    }


    /*

    private void cleanCode() {
        editorContainer.clearAllLayout();
        editorContainer.createTop(VISIBLE);
        editorContainer.setTitle(note.title);
        editorContainer.setDate(note.datelong);

        onClickInsertRichText(null);
    }

    private void saveTemp(List<String> editList) {
        saveListToFile(editList, tempH);
    }

    private void loadTemp() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editList = VideoJsonUtil.readListFromPath(EditorActivity.this, tempH);
                importEditList(editList);
            }
        }, 200);
    }
     */


  /*  private View readDialog() {
        final View root = getLayoutInflater().inflate(R.layout.dialog_read_text, null);
        TextView codeView = root.findViewById(R.id.show_text);
        String code;
        if (!loadingError) {
            code = editorContainer.exportRichText().toString();
            code = code.substring(1, code.length() - 1);
        } else code = note.body.substring(1, note.body.length() - 1);
        code = code.replace(",", "\n");
        code = code.replace("richtext:", "");
        String text = Html.fromHtml(code).toString();
        if (text.isEmpty() || !text.contains("\n"))
            text = note.body.substring(1, note.body.length() - 1).replace(",", "\n");
        codeView.setText(text);
        //  copyToClipboard(EditorActivity.this,code);
        //  onInterceptClipDataToPlainText(EditorActivity.this);
        // codeView.setText(pasteFromClipboard(EditorActivity.this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setView(root)
                .setIcon(R.drawable.exo_ic_audiotrack)
                .setTitle("Read Text")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Set Voice", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        speechHelper = new SpeechHelper(EditorActivity.this, tts, new SpeechCallback() {
                            @Override
                            public void voiceChosen(Voice mvoice) {
                                if (mvoice == null) speechHelper.SpeechDialog();
                                else voice = mvoice;
                            }
                        });
                        speechHelper.SpeechDialog();
                    }
                })
                .setCancelable(true)
                .setPositiveButton("Start Read", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  Log.e(TAG, codeView.getText().toString());
                        SpeechHelper.speak(EditorActivity.this, tts, voice, codeView.getText().toString());
                    }

                })
                .setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {

                            dialog.dismiss();
                        }
                        return false;
                    }
                });

        builder.show();
        return root;
    }*/

    private void adjustSize() {
       /* int videoWidth = getPlayer().getVideoWidth();
        int videoHeight = getPlayer().getVideoHeight();
        int floatHeight, floatWidth;
        if (videoWidth > videoHeight) {
            floatWidth = isFullScreen() ? (int) screenWidth / 3 : (int) screenWidth / 2;
            floatHeight = (int) floatWidth * videoHeight / videoWidth;
        } else {
            floatHeight = (int) screenHeight / 3;
            floatWidth = (int) floatHeight * videoWidth / videoHeight;
        }*/
    }

}



