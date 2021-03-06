package com.afordev.todomanagermini;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afordev.todomanagermini.Dialog.DialogExpandMenu;
import com.afordev.todomanagermini.Manager.DBManager;
import com.afordev.todomanagermini.Manager.Manager;
import com.afordev.todomanagermini.Manager.TodoRcvAdapter;
import com.afordev.todomanagermini.SubItem.DataTodo;
import com.afordev.todomanagermini.SubItem.DateForm;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private Toolbar mToolbar;
    private RecyclerView rcvTodo;
    private TodoRcvAdapter todoRcvAdapter;
    private SwipeRefreshLayout mSwipe;
    private DateForm date;
    private DBManager dbManager = DBManager.getInstance(this);
    private boolean isToday;
    private SharedPreferences prefs;
    private InputMethodManager imm;
    private TextView version;

    public View viewBottom;
    private DataTodo temp;
    private ConstraintLayout layoutNew;
    private LinearLayout layoutEdit;
    private Button btnDelete, btnCancel, btnExpandMenu;
    private EditText etTitle;
    private ImageView ivEditLeft, ivEditSave;
    private TextView tvTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSet();
        Manager.checkService(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        rcvTodo = findViewById(R.id.today_rcv);
        mSwipe = findViewById(R.id.today_swipe);
        version = findViewById(R.id.main_tv_version);
        version.setText(Manager.VERSIONNAME + " b" + Manager.VERSIONCODE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rcvTodo.setLayoutManager(llm);
        mSwipe.setOnRefreshListener(this);
        setData();
    }

    public void initSet() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Manager.VERSIONCODE = pInfo.versionCode;
        Manager.VERSIONNAME = pInfo.versionName;

        // Bottom item
        viewBottom = findViewById(R.id.today_bottom);
        temp = null;
        layoutNew = findViewById(R.id.item_todo_layout_new);
        layoutEdit = findViewById(R.id.item_todo_layout_edit);
        btnDelete = findViewById(R.id.item_todo_btn_delete);
        btnExpandMenu = findViewById(R.id.item_todo_btn_expandmenu);
        btnCancel = findViewById(R.id.item_todo_btn_cancel);
        etTitle = findViewById(R.id.item_todo_et_edit_title);
        ivEditLeft = findViewById(R.id.item_todo_iv_edit_left);
        ivEditSave = findViewById(R.id.item_todo_iv_edit_save);
        tvTags = findViewById(R.id.item_todo_tv_edit_tag);
        layoutNew.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        ivEditLeft.setOnClickListener(this);
        ivEditSave.setOnClickListener(this);
        btnExpandMenu.setOnClickListener(this);
        btnDelete.setVisibility(View.GONE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void setData() {
        Manager.checkService(this);
        try {
            date = new DateForm(getIntent().getLongExtra("date", Calendar.getInstance().getTimeInMillis() / 60000));
        } catch (NullPointerException e) {
            date = new DateForm(Calendar.getInstance());
        }
        Calendar cal = Calendar.getInstance();
        if (date.getYear() == cal.get(Calendar.YEAR) &&
                date.getMonth() == cal.get(Calendar.MONTH) &&
                date.getDay() == cal.get(Calendar.DATE)) {
            isToday = true;
            mToolbar.setTitle("오늘의 할 일");
        } else {
            isToday = false;
            mToolbar.setTitle(Manager.getDateForm(this, date));
        }
        dbManager.checkToday();
        todoRcvAdapter = new TodoRcvAdapter(this, dbManager, date);
        rcvTodo.setAdapter(todoRcvAdapter);
        onRefreshBottom();
    }

    public void onRefreshBottom() {
        if (temp == null) {
            layoutNew.setVisibility(View.VISIBLE);
            layoutEdit.setVisibility(View.GONE);
        } else {
            layoutNew.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
            etTitle.setText(temp.getTitle());
            etTitle.requestFocus();

            StringBuilder sb = new StringBuilder();
            if (temp.getTimeDead().compareTo(new DateForm(Calendar.getInstance())) == 0) {
                sb.append(Manager.getTimeForm(temp.getTimeDead()));
            }
            if (!temp.getTags().equals("")) {
                if (!sb.toString().equals("")) {
                    sb.append(", ");
                }
                ArrayList<String> st = temp.getTagList();
                for (int i = 0; i < st.size(); i++) {
                    sb.append("#" + st.get(i) + " ");
                }
            }
            if (sb.toString().equals("")) {
                tvTags.setVisibility(View.GONE);
            } else {
                tvTags.setVisibility(View.VISIBLE);
                tvTags.setText(sb.toString());
            }

            switch (temp.getImportance()) {
                case (1):
                    ivEditLeft.setImageResource(R.drawable.ic_star_half);
                    break;
                case (2):
                    ivEditLeft.setImageResource(R.drawable.ic_star_true);
                    break;
                case (0):
                default:
                    ivEditLeft.setImageResource(R.drawable.ic_star_false);
                    break;
            }
        }
    }

    public void setViewBottom(final boolean view) {
        final Handler mHandler = new Handler();
        final Animation animBtC = AnimationUtils.loadAnimation(
                this, R.anim.bottom_to_center);
        if (view) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    viewBottom.setVisibility(View.VISIBLE);
                    viewBottom.setAnimation(animBtC);
                }
            }, 200);
        } else {
            viewBottom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.item_todo_layout_new):
                temp = new DataTodo(-1, date);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                onRefreshBottom();
                break;

            case (R.id.item_todo_iv_edit_left):
                switch (temp.getImportance()) {
                    case (0):
                        temp.setImportance(1);
                        break;
                    case (1):
                        temp.setImportance(2);
                        break;
                    case (2):
                        temp.setImportance(0);
                        break;
                }
                onRefreshBottom();
                break;

            case (R.id.item_todo_iv_edit_save):
                temp.setTitle(etTitle.getText().toString());
                dbManager.insertTodo(temp);
                imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
                temp = null;
                onRefresh();
                rcvTodo.scrollToPosition(todoRcvAdapter.getItemCount() - 1);
                break;

            case (R.id.item_todo_btn_cancel):
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("작업 중인 정보가 사라집니다.");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
                        temp = null;
                        onRefreshBottom();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;

            case (R.id.item_todo_btn_expandmenu):
                DialogExpandMenu dialogExpandMenu = new DialogExpandMenu(MainActivity.this, temp, null, -1);
                dialogExpandMenu.show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menuxml_today, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.menu_cal):
                dateSelectOption();
                return true;
            case (R.id.menu_today):
                if (!isToday) {
                    finish();
                }
                return true;
            case (R.id.menu_setting):
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, Manager.RC_MAIN_TO_SETTING);
                return true;
            case (R.id.menu_search):
                intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, Manager.RC_MAIN_TO_SEARCH);
                return true;
            case (R.id.menu_multi):
                intent = new Intent(MainActivity.this, MultiActivity.class);
                intent.putExtra("list", todoRcvAdapter.getDataList());
                startActivityForResult(intent, Manager.RC_MAIN_TO_MULTI);
                return true;
            case (R.id.menu_pattern):
                intent = new Intent(MainActivity.this, PatternActivity.class);
                startActivityForResult(intent, Manager.RC_MAIN_TO_PATTERN);
                return true;
            case (R.id.menu_lock):
                intent = new Intent(MainActivity.this, LockActivity.class);
                startActivity(intent);
                return true;
            case(R.id.menu_filter):
                intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        todoRcvAdapter.onRefresh();
        mSwipe.setRefreshing(false);
        onRefreshBottom();
    }

    @Override
    public void onBackPressed() {
        if (todoRcvAdapter.temp == null && this.temp == null) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("작업 중인 정보가 사라집니다.");
            dialog.setCancelable(true);
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (todoRcvAdapter.temp != null) {
                        todoRcvAdapter.temp = null;
                        todoRcvAdapter.notifyItemChanged(todoRcvAdapter.editPosition);
                    } else {
                        MainActivity.this.temp = null;
                        onRefreshBottom();
                    }

                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public void dateSelectOption() {
        DatePickerDialog dpDialog = new DatePickerDialog(this, listenerDate,
                date.getYear(),
                date.getMonth(),
                date.getDay());
        dpDialog.show();
    }

    private DatePickerDialog.OnDateSetListener listenerDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DateForm temp = new DateForm(year, monthOfYear, dayOfMonth);
            if (temp.compareTo(date) == 0 && date.compareTo(new DateForm(Calendar.getInstance())) == 0) {
                if (!isToday) {
                    finish();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("date", temp.getTime());
                if (!isToday) {
                    finish();
                }
                startActivityForResult(intent, Manager.RC_MAIN_TO_MAIN);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Manager.RC_MAIN_TO_SETTING) {
            if (resultCode == RESULT_OK) {
                setData();
            }
        }
        if (requestCode == Manager.RC_MAIN_TO_SEARCH) {
            if (resultCode == RESULT_OK) {
                setData();
            }
        }
        if (requestCode == Manager.RC_MAIN_TO_MAIN) {
            setData();
        }
        if (requestCode == Manager.RC_MAIN_TO_MULTI) {
            if (resultCode == RESULT_OK) {
                setData();
            }
        }
        if (requestCode == Manager.RC_MAIN_TO_PATTERN) {
            if (resultCode == RESULT_OK) {
                setData();
            }
        }
    }

    public DataTodo getTemp() {
        return temp;
    }

}
