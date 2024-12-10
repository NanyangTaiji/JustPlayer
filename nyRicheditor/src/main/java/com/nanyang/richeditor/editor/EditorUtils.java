package com.nanyang.richeditor.editor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Category;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.Note;
import com.nanyang.richeditor.memento.BaseFragment;
import com.nytaiji.nybase.utils.NyFormatter;
//import com.nanyang.richeditor.text.TextEditorActivity;
import com.nanyang.richeditor.view.ExpandListAdapter;
import com.nytaiji.nybase.utils.NyFileUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.view.View.GONE;
import static com.nytaiji.nybase.crypt.EncryptUtil.getActivity;
import static com.nytaiji.nybase.utils.NyFileUtil.getSavedDir;
import static com.nytaiji.nybase.utils.VideoJsonUtil.readListFromFile;
import static com.nytaiji.nybase.utils.VideoJsonUtil.saveListToFile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;

public final class EditorUtils {

    //  public static String KEYPATH = new File(NyFileUtil.getAppDirectory(g), "keywords.txt").getAbsolutePath();

    public static final int REQUEST_CODE = 1;
    public static final int RESULT_CHANGE = 100;
    public static final int RESULT_NEW = 101;
    public static final int RESULT_EDIT = 102;
    public static final int RESULT_DELETE = 103;
    public static final int RESULT_CANCEL = 104;

    public static String[] sorts = {"Title ASC", "Title DESC", "Date ASC", "Date DESC"};

    private EditorUtils() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    /**
     * 获取html本地的地址 方便上传的时候转为在线的地址
     *
     * @param html
     * @return
     */
    public static List<String> getHtmlSrcOrHrefList(String html) {

        if (TextUtils.isEmpty(html)) {
            return null;
        }
        Document doc = Jsoup.parse(html);
        List<String> listData = new ArrayList();

        Elements elementsSrc = new Elements();
        Elements elementsImg = doc.select("img[src]");
        Elements elementsAudio = doc.select("audio[src]");
        Elements elementsVideo = doc.select("video[src]");
        Elements elementsFiles = doc.select("a[href]");
        Elements elementsPosters = doc.select("video[poster]");

        elementsSrc.addAll(elementsImg);
        elementsSrc.addAll(elementsAudio);
        elementsSrc.addAll(elementsVideo);
        for (Element element : elementsSrc) {
            String src = element.attr("src");
            if (!TextUtils.isEmpty(src) && !src.contains("http")) {
                listData.add(src);
            }
        }

        for (Element element : elementsFiles) {
            String src = element.attr("href");
            if (!TextUtils.isEmpty(src) && !src.contains("http")) {
                listData.add(src);
            }
        }
        for (Element element : elementsPosters) {
            String src = element.attr("poster");
            if (!TextUtils.isEmpty(src) && !src.contains("http")) {
                listData.add(src);
            }
        }

        return listData;
    }


    public static void openMenu(ImageView openMenu, final ImageView delete, final ImageView rotate) {
        // isMenuShow = true;
        int distance1 = 150;
        int x = (int) openMenu.getX();
        int y = (int) openMenu.getY();
        ValueAnimator v1 = ValueAnimator.ofInt(x, x - distance1);
        v1.setDuration(300);
        v1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) delete.getY();
                int r = delete.getWidth() + l;
                int b = delete.getHeight() + t;
                delete.layout(l, t, r, b);
            }
        });
        int distance2 = 300;
        ValueAnimator v2 = ValueAnimator.ofInt(x, x - distance2);
        v2.setDuration(300).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) rotate.getY();
                int r = rotate.getWidth() + l;
                int b = rotate.getHeight() + t;
                rotate.layout(l, t, r, b);
            }
        });
        v1.start();
        v2.start();
    }

    public static void hideMenu(ImageView openMenu, final ImageView delete, final ImageView rotate) {
        //  isMenuShow = false;
        int x = (int) delete.getX();
        ValueAnimator v1 = ValueAnimator.ofInt(x, (int) openMenu.getX());
        v1.setDuration(300);
        v1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) delete.getY();
                int r = delete.getWidth() + l;
                int b = delete.getHeight() + t;
                delete.layout(l, t, r, b);
            }
        });
        x = (int) rotate.getX();
        ValueAnimator v2 = ValueAnimator.ofInt(x, (int) openMenu.getX());
        v2.setDuration(300).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int l = (int) animation.getAnimatedValue();
                int t = (int) rotate.getY();
                int r = rotate.getWidth() + l;
                int b = rotate.getHeight() + t;
                rotate.layout(l, t, r, b);
            }
        });

        v1.start();
        v2.start();
        v1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                delete.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        v2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rotate.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }


    public static List<String> getInitGroupData() {
        return Arrays.asList("功法", "劲法", "身法", "肢体", "应用", "拳理", "科学", "进阶", "拳架");
    }

    public static void RestoreOrinalKeywords(Context context) {
        String keypath = new File(NyFileUtil.getAppDirectory(context), "keywords.txt").getAbsolutePath();
        List<String> allKeyWords = new ArrayList<>();
        allKeyWords.add(getInitGroupData().toString().replace(",", " "));
        for (int i = 1; i < getInitGroupData().size(); i++) {
            allKeyWords.add(initlinkageSecondData(i).toString().replace(",", " "));
        }
        if (saveListToFile(allKeyWords, keypath)) {
            //TODO ny 2024-12-11 tmp out
          /*  Intent intent = new Intent(context, TextEditorActivity.class);
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_REFERRER_NAME, keypath);
            context.startActivity(intent);*/
        }
    }

    public static List<String> initlinkageSecondData(int index) {
        switch (index) {
            case 0:
                return Arrays.asList(
                        "松功", "桩功", "内功", "气功", "硬功");
            case 1:
                return Arrays.asList(
                        "掤劲", "捋劲", "挤劲", "按劲", "採劲", "挒劲", "肘劲", "靠劲",
                        "化劲", "拿劲", "发劲", "打劲", "点劲", "线劲", "面劲", "体劲",
                        "沾粘", "连随", "拍击", "惊弹", "缠丝", "鼓荡", "凌空");
            case 2:
                return Arrays.asList(
                        "中正", "虚实", "进退", "折叠", "开合", "转换", "关节", "皮毛",
                        "三节", "贯通", "均匀", "展筋", "紧凑", "螺旋", "神意", "意气");
            case 3:
                return Arrays.asList(
                        "头颈", "五官", "上肢", "下肢", "中节", "肩背", "肘臂", "腕掌", "掌指", "脊椎",
                        "胸腹", "腰胯", "臀裆", "皮毛", "外球", "至阴", "腿膝", "踝足", "筋经", "穴位");
            case 4:
                return Arrays.asList(
                        "推手", "四正", "俯仰", "大履", "九宫", "圆环", "定步", "活步", "散推", "散手", "防身", "擒拿", "点穴", "跌摔", "解困", "生活");
            case 5:
                return Arrays.asList(
                        "借力打力", "随曲就伸", "引进落空", "抗力为均", "归零得中", "动静虚实", "避重就虚", "圆球变点", "连消带打", "反者道动");
            case 6:
                return Arrays.asList(
                        "杠杆", "旋转", "向心", "离心", "重量", "势能", "场能");
            case 7:
                return Arrays.asList(
                        "交重", "轻灵", "转关", "截劲", "虚空", "隔山", "挪移");
            case 8:
                return Arrays.asList(
                        "起势", "提手上势", "手挥琵琶", "揽雀尾", "单鞭", "斜飞势", "白鹤亮翅", "搂膝拗步",
                        "搬拦捶", "如风似闭", "抱虎归山", "十字手", "转身搂膝", "海底针", "闪通背",
                        "肘底捶", "倒撵猴", "云手", "高探马", "分手脚", "转身蹬脚", "撇身捶", "一二起脚", "栽捶",
                        "退步打虎", "双峰贯耳", "披身踢脚", "野马分鬃", "玉女穿梭", "下势", "金鸡独立", "穿掌", "扑面掌", "单摆莲",
                        "退步跨虎", "左单鞭", "转身扑面", "双摆莲", "打虎势", "收势"
                );
        }
        return new ArrayList<>();
    }

    public static List<String> allKeyWords = new ArrayList<>();


    public static List<String> loadKeywordsData(Context context) {
        File keywordFile = new File(NyFileUtil.getAppDirectory(context), "keywords.txt");

        if (keywordFile.exists() && keywordFile.length() > 0) {
            allKeyWords = readListFromFile(keywordFile);
        } else {
            //First time create and save
            allKeyWords.add(getInitGroupData().toString().replace(",", " "));
            for (int i = 1; i < getInitGroupData().size(); i++) {
                allKeyWords.add(initlinkageSecondData(i).toString().replace(",", " "));
            }
            new Thread() {
                @Override
                public void run() {
                    saveListToFile(allKeyWords, keywordFile);
                }
            }.start();
        }

        String tmp = allKeyWords.get(0);
        tmp = tmp.substring(1, tmp.length() - 1);
        tmp = tmp.replace("  ", " ");
        return new ArrayList<String>(Arrays.asList(tmp.split(" ")));
    }

    public static List<String> linkageSecondData(int index) {
        String tmp = allKeyWords.get(index);
        tmp = tmp.substring(1, tmp.length() - 1);
        tmp = tmp.replace("  ", " ");
        return new ArrayList<String>(Arrays.asList(tmp.split(" ")));
    }


    public static void pickupKeyWordsDialog(Activity context, KeywordListener keywordListener) {
        final View root = context.getLayoutInflater().inflate(R.layout.dialog_expand_list, null);
        ExpandableListView expandList = root.findViewById(R.id.expList);

        ExpandListAdapter listAdapter;
        List<String> listDataHeader = loadKeywordsData(context);
        //  Log.e("Utils", "listDataHeader.size():  " + listDataHeader.size());
        HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();

        for (int index = 0; index < listDataHeader.size(); index++) {
            listDataChild.put(listDataHeader.get(index), linkageSecondData(index));
        }

        listAdapter = new ExpandListAdapter(context, listDataHeader, listDataChild);
        listAdapter.setmListener(keywordListener);
        expandList.setAdapter(listAdapter);

        new AlertDialog.Builder(context)
                .setView(root)
                .setTitle("Select keywords")
                .setPositiveButton("Add", (dialog, which) -> {
                    keywordListener.onListChanged(listAdapter.currentChildren, true);
                    dialog.dismiss();
                })
                .setNegativeButton("Replace", (dialog, which) -> {
                    keywordListener.onListChanged(listAdapter.currentChildren, false);
                    dialog.dismiss();
                })
                .show();
    }

    public static int KEYWORDS = 0;
    public static int DATEMINUS = 1;
    public static int DATEPLUS = 2;
    public static int STAR = 3;

    public static void searchDialog(Activity context, BaseFragment.searchCallback callback, long specifiedCategory) {
        final View root = context.getLayoutInflater().inflate(R.layout.dialog_search, null);
        RadioGroup option = root.findViewById(R.id.search_option);
        EditText inputKey = ((EditText) root.findViewById(R.id.search_key));
        inputKey.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickupKeyWordsDialog(context, new KeywordListener() {
                    @Override
                    public void onListChanged(ArrayList<String> chosenChildren, boolean toAppend) {
                        String temp = chosenChildren.toString();
                        temp = temp.substring(1, temp.length() - 1);
                        temp = temp.replace(",", " ");
                        inputKey.setText(temp);
                    }
                });
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(root).setTitle("Search options:");

        ArrayList<Long> categories = new ArrayList<>();

        ArrayList<Category> all = new Controller(App.instance).findAllCategories();
        final int length = all.size();
        String[] titles = new String[length];
        HashMap<String, Long> map = new HashMap<>();

        boolean[] checkedItems = new boolean[length];

        for (int i = 0; i < length; i++) {
            map.put(all.get(i).title, all.get(i).id);
            titles[i] = all.get(i).title;
            if (specifiedCategory == -1) checkedItems[i] = true;
            else if (all.get(i).id == specifiedCategory) checkedItems[i] = true;
            if (checkedItems[i]) categories.add(map.get(all.get(i).title));
        }

        builder.setMultiChoiceItems(titles, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) categories.add(map.get(all.get(which).title));
                else categories.remove(map.get(all.get(which).title));
            }
        });


        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                    String key = inputKey.getText().toString().trim().toLowerCase();
                    int type = KEYWORDS;
                    if (option.getCheckedRadioButtonId() == R.id.date_minus) type = DATEMINUS;
                    else if (option.getCheckedRadioButtonId() == R.id.date_plus) type = DATEPLUS;
                    else if (option.getCheckedRadioButtonId() == R.id.search_star) type = STAR;
                    boolean includeBody = ((SwitchCompat) root.findViewById(R.id.swtich_body)).isChecked();
                    proceedSearch(context, key, type, includeBody, callback, categories);

                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AppCompatActivity activity = getActivity(context);
                        //TODO ny To handdle cancel in search
                        if (activity instanceof EditorActivity) activity.onBackPressed();
                    }
                })
                .setCancelable(false);

        //the following is essential for showing a long list
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            AlertDialog adlg = (AlertDialog) dlg;
            View v = adlg.getWindow().findViewById(R.id.contentPanel);
            if (v != null) {
                ((LinearLayoutCompat.LayoutParams) v.getLayoutParams()).weight = 1;
                v.setBackgroundResource(R.color.colorSilver);
            }
        });
        dialog.show();

    }

    public static void proceedSearch(Context context,
                                     String key,
                                     int type,
                                     boolean includeBody,
                                     BaseFragment.searchCallback callback,
                                     ArrayList<Long> categories) {
        ArrayList<Note> items;
        if (type == STAR) {
            key = "star";
            items = new Controller(App.instance).starSearch();
        } else if (type == DATEMINUS || type == DATEPLUS) {
            String dataT = String.valueOf(NyFormatter.getDateLong(key));
            items = new Controller(App.instance).dateSearch(dataT, (type == DATEMINUS));
        } else {
            if (includeBody) items = new Controller(App.instance).deepSearch(key);
            else items = new Controller(App.instance).simpleSearch(key);
            //TODO ny no filter for all selected but redundant for an already specified single category
        }

        if (items != null && new Controller(App.instance).findAllCategories().size() > categories.size())
            items = categoryFilter(items, categories);

       // if (items == null) getActivity(context).onBackPressed();
        key = type == DATEMINUS ? "<" : ">" + key;
        callback.returnKey(key);
        callback.returnResult(items);
    }

    private static ArrayList<Note> categoryFilter(ArrayList<Note> searched, ArrayList<Long> categoryId) {
        ArrayList<Note> filteredItem = new ArrayList<Note>();
        for (Note item : searched) {
            if (categoryId.contains(item.parentId)) filteredItem.add(item);
        }
        return filteredItem;
    }


    public static void searchDialog(Activity context, BaseFragment.searchCallback callback, ArrayList<Note> specified) {
        final View root = context.getLayoutInflater().inflate(R.layout.dialog_search, null);
        RadioGroup option = root.findViewById(R.id.search_option);
        EditText inputKey = ((EditText) root.findViewById(R.id.search_key));
        inputKey.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickupKeyWordsDialog(context, new KeywordListener() {
                    @Override
                    public void onListChanged(ArrayList<String> chosenChildren, boolean toAppend) {
                        String temp = chosenChildren.toString();
                        temp = temp.substring(1, temp.length() - 1);
                        temp = temp.replace(",", " ");
                        inputKey.setText(temp);
                    }
                });
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(root).setTitle("Search options:");

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                    String key = inputKey.getText().toString().trim().toLowerCase();
                    int type = KEYWORDS;
                    if (option.getCheckedRadioButtonId() == R.id.date_minus) type = DATEMINUS;
                    else if (option.getCheckedRadioButtonId() == R.id.date_plus) type = DATEPLUS;
                    else if (option.getCheckedRadioButtonId() == R.id.search_star) type = STAR;
                    boolean includeBody = ((SwitchCompat) root.findViewById(R.id.swtich_body)).isChecked();
                    specifiedSearch(context, key, type, includeBody, callback, specified);

                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AppCompatActivity activity = getActivity(context);
                        //To handdle cancel in search
                        if (activity instanceof EditorActivity) activity.onBackPressed();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            AlertDialog adlg = (AlertDialog) dlg;
            View v = adlg.getWindow().findViewById(R.id.contentPanel);
            if (v != null) {
                ((LinearLayoutCompat.LayoutParams) v.getLayoutParams()).weight = 1;
                v.setBackgroundResource(R.color.colorSilver);
            }
        });
        dialog.show();
    }


    public static void specifiedSearch(Context context,
                                       String key,
                                       int type,
                                       boolean includeBody,
                                       BaseFragment.searchCallback callback,
                                       ArrayList<Note> specified) {

        if (specified == null) return;
        Set<Note> set = new HashSet<Note>();
        for (Note item : specified) {
            if (type == STAR && item.isStard) set.add(item);
            else if (type == DATEMINUS && item.datelong < NyFormatter.getDateLong(key))
                set.add(item);
            else if (type == DATEPLUS && item.datelong> NyFormatter.getDateLong(key))
                set.add(item);
            else if (!key.trim().isEmpty()) {
                Note newNote = new Controller(App.instance).findNote(item.id);
                if (newNote.keywords != null && newNote.keywords.contains(key)) set.add(item);
                if (newNote.title.contains(key)) set.add(item);
                if (newNote.remark != null && newNote.remark.contains(key)) set.add(item);
                if (includeBody && newNote.body != null && newNote.body.contains(key))
                    set.add(item);
            } else set.add(item);
        }

        if (!set.isEmpty() && callback != null) {
            callback.returnKey(key);
            callback.returnResult(new ArrayList<>(set));
        } else Toast.makeText(context, "Search results empty", Toast.LENGTH_SHORT).show();
    }

    public static void hideSoftInput(Context context) {
        try {//   w  w  w .  d   e m   o 2    s.   c   om
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                View currentView = activity.getCurrentFocus();
                if (currentView != null) {
                    inputMethodManager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
                    currentView.clearFocus();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File formatSaveFile(String filename, String ext) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        //TODO correction for Calendar.MONTH index fron 0 to 11
        String filename_prefix;
        filename_prefix = String.format(Locale.US, "%s-%d-%02d-%02d", filename, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        File save_path = new File(getSavedDir(), String.format("%s.%s", filename_prefix, ext));

        int i = 2;
        while (save_path.exists()) {
            save_path = new File(getSavedDir(), String.format(Locale.US, "%s(%d).%s", filename_prefix, i, ext));
            i++;
        }

        return save_path;
    }

    public static View codeEditDialog(Activity context, Note item) {
        final View root = context.getLayoutInflater().inflate(R.layout.dialog_editor, null);
        Note note = new Controller(App.instance).findNote(item.id);
        EditText eTitle = root.findViewById(R.id.editor_title);
        eTitle.setText(note.title);

        EditText eDate = root.findViewById(R.id.editor_date);
        eDate.setText(NyFormatter.getDateString(note.datelong));
        EditText eKeywords = root.findViewById(R.id.txt_keywords);
        eKeywords.setText(note.keywords.replace("  ", " "));
        EditText eMainBody = root.findViewById(R.id.txt_body);
        if (note.type == DatabaseModel.TYPE_NOTE) {
            String mbody = note.body.substring(1, note.body.length() - 1);
            eMainBody.setText(mbody.replace(",", "\n"));
        } else eMainBody.setVisibility(View.GONE);
        //  EditText eReference = root.findViewById(R.id.txt_body);
        EditText eRemark = root.findViewById(R.id.txt_remark);
        eRemark.setText(note.remark);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogTheme)
                .setView(root)
                .setIcon(R.drawable.ic_edit)
                .setTitle("Note Code Edit")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                //  .setCancelable(true)
                .setPositiveButton("Code saving and leave!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note.title = eTitle.getText().toString();
                        note.datelong = NyFormatter.getDateLong(eDate.getText().toString().trim());
                        note.keywords = eKeywords.getText().toString();
                        note.remark = eRemark.getText().toString();
                        if (note.type != DatabaseModel.TYPE_WEBSITE) {
                            note.body = eMainBody.getText().toString();
                            note.body = "[" + note.body.replace("\n", ",") + "]";
                        }
                        note.save();
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
    }

    public static String insertStringIntoBracket(String newString, String bracket1) {
        // [bracket1], newString become [bracket1, newString];
        String joined = bracket1.replace("]", " ,") + newString + "]";

        //  Log.e(TAG, "joinTwo  " + joined);
        return joined;

    }

    public static List<String> removeRedundant(List<String> list) {
        //Adding elements of the ArrayList to the Set object
        Set<String> set = new HashSet<String>(list);
        //Removing all the elements from the ArrayList
        list.clear();
        //Adding elements of the set back to the list
        list.addAll(set);
        return list;
    }

    public static String convertVideoToHtml(String httpUrl) {
        StringBuilder content = new StringBuilder("<p></p><hr><div style=\"text-align:center;\"><video poster=\"\" src=" + httpUrl+
                " controls=\"controls\"></video></div>");
        return content.toString();
    }

    public static String extractUrlFromContent(String content) {
        String url = content.substring(content.indexOf("src=") + 5, content.indexOf("width") - 1);
        if (url.contains("?download=")) url = url.substring(0, url.indexOf("?download="));
        //  url = url.substring(5, url.indexOf("\""));
        return url;
    }
}
