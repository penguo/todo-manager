package com.afordev.todomanagermini.Manager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afordev.todomanagermini.Dialog.DialogExpandMenu;
import com.afordev.todomanagermini.MainActivity;
import com.afordev.todomanagermini.R;
import com.afordev.todomanagermini.SubItem.DataTodo;
import com.afordev.todomanagermini.SubItem.DateForm;

import java.util.ArrayList;
import java.util.Calendar;

public class TodoRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private DBManager dbManager;
    private ArrayList<ArrayList<DataTodo>> arrayLists;
    private ArrayList<DataTodo> dataList;
    private DateForm date;
    private boolean isToday, isAutoSort, isDoubleClick;
    public DataTodo temp;
    public int itemExpandPosition = -1, editPosition = -1;
    private InputMethodManager imm;
    private int[] headerPos;

    public TodoRcvAdapter(Context mContext, DBManager dbManager, DateForm date) {
        initSet(mContext, dbManager);
        this.date = date;
        setData();
        if (date.compareTo(new DateForm(Calendar.getInstance())) == 0) {
            isToday = true;
        } else {
            isToday = false;
        }
    }

    public void setData() {
        int tempPos = 0;
        dataList = new ArrayList<>();
        this.arrayLists = dbManager.getSortedList(date);
        headerPos = new int[this.arrayLists.size()];
        for (int i = 0; i < headerPos.length; i++) {
            headerPos[i] = -1;
        }
        for (int i = 0; i < headerPos.length; i++) {
            if (arrayLists.get(i).size() != 0) {
                headerPos[i] = tempPos;
                dataList.add(tempPos, new DataTodo(-2, null)); // 더미 데이터
                tempPos += arrayLists.get(i).size();
                tempPos++;
                dataList.addAll(arrayLists.get(i));
            } else {
                headerPos[i] = -1;
            }
        }
    }

    public void initSet(Context mContext, DBManager dbManager) {
        this.mContext = mContext;
        this.dbManager = dbManager;
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        isAutoSort = prefs.getBoolean(Manager.PREF_AUTO_SORT, true);
        isDoubleClick = prefs.getBoolean(Manager.PREF_DOUBLE_CLICK, false);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        for (int i = 0; i < headerPos.length; i++) {
            if (position == headerPos[i]) {
                return -1;
            }
        }
        if (editPosition == position) {
            return 1;
        } else {
            return 0;
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case (-1):
                VHHeader mVHHeader = new VHHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false));
                return mVHHeader;
            case (1):
                VHEdit mVHEdit = new VHEdit(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit, parent, false));
                return mVHEdit;
            case (0):
            default:
                VHItem mVHItem = new VHItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false));
                return mVHItem;
        }
    }

    class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvTitle, tvTags, tvTurn;
        private LinearLayout layout, layoutPlus, layoutText, layoutRight1, layoutRight2;
        private ImageView ivCheck, ivIcon, ivImportance;
        private Button btnPDelete, btnPEdit, btnPDelay, btnPCheck;

        public VHItem(final View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.item_todo_layout);
            tvTitle = itemView.findViewById(R.id.item_todo_tv_title);
            tvTags = itemView.findViewById(R.id.item_todo_tv_tag);
            ivCheck = itemView.findViewById(R.id.item_todo_iv_left);
            ivIcon = itemView.findViewById(R.id.item_todo_iv_icon);
            ivImportance = itemView.findViewById(R.id.item_todo_iv_importance);
            layoutPlus = itemView.findViewById(R.id.item_todo_layout_plus);
            btnPDelete = itemView.findViewById(R.id.item_todo_btn_pdelete);
            btnPEdit = itemView.findViewById(R.id.item_todo_btn_pedit);
            btnPDelay = itemView.findViewById(R.id.item_todo_btn_pdelay);
            btnPCheck = itemView.findViewById(R.id.item_todo_btn_pcheck);
            layoutText = itemView.findViewById(R.id.item_todo_layout_text);
            tvTurn = itemView.findViewById(R.id.item_todo_tv_turn);
            layoutRight1 = itemView.findViewById(R.id.item_todo_layout_right1);
            layoutRight2 = itemView.findViewById(R.id.item_todo_layout_right2);

            layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (isToday) {
                        if (editPosition == -1 && ((MainActivity) mContext).getTemp() == null) {
                            temp = dataList.get(getAdapterPosition()).clone();
                            editPosition = getAdapterPosition();
                            notifyItemChanged(getAdapterPosition());
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        } else {
                            Toast.makeText(mContext, "먼저 항목 수정을 마쳐야 합니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "팝업 메뉴", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            layout.setOnClickListener(this);
            btnPDelete.setOnClickListener(this);
            btnPEdit.setOnClickListener(this);
            btnPDelay.setOnClickListener(this);
            btnPCheck.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            switch (view.getId()) {
                case (R.id.item_todo_layout):
                    if (editPosition == -1 && ((MainActivity) mContext).getTemp() == null) {
                        if (isToday) {
                            if (!isDoubleClick || (temp != null && temp.equals(dataList.get(getAdapterPosition())))) {
                                if (dataList.get(getAdapterPosition()).getTimeChecked().isNull()) {
                                    dataList.get(getAdapterPosition()).setTimeChecked(new DateForm(Manager.getCurrentTime()));
                                } else {
                                    dataList.get(getAdapterPosition()).setTimeChecked(null);
                                }
                                dbManager.updateTodo(dataList.get(getAdapterPosition()));
                                notifyItemChanged(getAdapterPosition());
                                if (isAutoSort) {
//                                notifyItemMoved(getAdapterPosition(), getSortedPosition(getAdapterPosition()));
                                }
                                temp = null;
                            } else {
                                temp = dataList.get(getAdapterPosition());
                            }
                            editPosition = -1;
                        } else {
                            int i = itemExpandPosition;
                            if (itemExpandPosition == getAdapterPosition()) {
                                itemExpandPosition = -1;
                                notifyItemChanged(getAdapterPosition());
                            } else {
                                itemExpandPosition = getAdapterPosition();
                                if (i >= 0) {
                                    notifyItemChanged(i);
                                }
                                notifyItemChanged(itemExpandPosition);
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "먼저 항목 수정을 마쳐야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case (R.id.item_todo_btn_pdelete):
                    dialog.setMessage("정말로 삭제하시겠습니까? 모든 내용이 삭제되며 복구되지 않습니다.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbManager.deleteTodo(dataList.get(getAdapterPosition()).getId());
                            itemExpandPosition = -1;
                            removeItemView(getAdapterPosition());
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

                case (R.id.item_todo_btn_pedit):
                    if (editPosition == -1 && ((MainActivity) mContext).getTemp() == null) {
                        temp = dataList.get(getAdapterPosition()).clone();
                        editPosition = getAdapterPosition();
                        notifyItemChanged(getAdapterPosition());
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    } else {
                        Toast.makeText(mContext, "먼저 항목 수정을 마쳐야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case (R.id.item_todo_btn_pdelay):
                    dateDelayOption(dataList.get(getAdapterPosition()));
                    break;

                case (R.id.item_todo_btn_pcheck):
                    if (editPosition != -1) {
                        Toast.makeText(mContext, "먼저 항목 수정을 마쳐야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (dataList.get(getAdapterPosition()).getTimeChecked().isNull()) {
                            dataList.get(getAdapterPosition()).setTimeChecked(new DateForm(Manager.getCurrentTime()));
                        } else {
                            dataList.get(getAdapterPosition()).setTimeChecked(null);
                        }
                        dbManager.updateTodo(dataList.get(getAdapterPosition()));
                        itemExpandPosition = -1;
                        notifyItemChanged(getAdapterPosition());
                        if (isAutoSort) {
//                            notifyItemMoved(getAdapterPosition(), getSortedPosition(getAdapterPosition()));
                        }
                    }
            }
        }
    }

    private EditText etEditTitle;

    class VHEdit extends RecyclerView.ViewHolder implements View.OnClickListener {
        //        private EditText etTitle;
        private TextView tvTags;
        private ImageView ivEditLeft, ivEditSave;
        private Button btnDelete, btnCancel, btnExpandMenu;
        private ConstraintLayout layoutNew;

        public VHEdit(final View itemView) {
            super(itemView);
            layoutNew = itemView.findViewById(R.id.item_todo_layout_new);
            layoutNew.setVisibility(View.GONE);
            etEditTitle = itemView.findViewById(R.id.item_todo_et_edit_title);
            tvTags = itemView.findViewById(R.id.item_todo_tv_edit_tag);
            ivEditLeft = itemView.findViewById(R.id.item_todo_iv_edit_left);
            ivEditSave = itemView.findViewById(R.id.item_todo_iv_edit_save);
            btnDelete = itemView.findViewById(R.id.item_todo_btn_delete);
            btnCancel = itemView.findViewById(R.id.item_todo_btn_cancel);
            btnExpandMenu = itemView.findViewById(R.id.item_todo_btn_expandmenu);

            ivEditLeft.setOnClickListener(this);
            ivEditSave.setOnClickListener(this);
            btnDelete.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            btnExpandMenu.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            switch (view.getId()) {
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
                    notifyItemChanged(getAdapterPosition());
                    break;

                case (R.id.item_todo_iv_edit_save):
                    temp.setTitle(etEditTitle.getText().toString());
                    dbManager.updateTodo(temp);
                    dataList.set(getAdapterPosition(), temp);
                    temp = null;
                    editPosition = -1;
                    itemExpandPosition = -1;
                    notifyItemChanged(getAdapterPosition());
                    imm.hideSoftInputFromWindow(etEditTitle.getWindowToken(), 0);
                    ((MainActivity) mContext).setViewBottom(true);
                    break;

                case (R.id.item_todo_btn_delete):
                    dialog.setMessage("정말로 삭제하시겠습니까? 모든 내용이 삭제되며 복구되지 않습니다.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imm.hideSoftInputFromWindow(etEditTitle.getWindowToken(), 0);
                            dbManager.deleteTodo(temp.getId());
                            temp = null;
                            editPosition = -1;
                            ((MainActivity) mContext).setViewBottom(true);
                            removeItemView(getAdapterPosition());
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

                case (R.id.item_todo_btn_cancel):
                    dialog.setMessage("작업 중인 정보가 사라집니다.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            temp = null;
                            editPosition = -1;
                            notifyItemChanged(getAdapterPosition());
                            imm.hideSoftInputFromWindow(etEditTitle.getWindowToken(), 0);
                            ((MainActivity) mContext).setViewBottom(true);
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
                    DialogExpandMenu dialogExpandMenu = new DialogExpandMenu(mContext, temp, TodoRcvAdapter.this, getAdapterPosition());
                    dialogExpandMenu.show();
                    break;
            }
        }
    }

    class VHHeader extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public VHHeader(final View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_header_tv_title);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        DataTodo data;
        if (holder instanceof VHItem) {
            data = dataList.get(position);
            if (itemExpandPosition == position) {
                ((VHItem) holder).layoutPlus.setVisibility(View.VISIBLE);
            } else {
                ((VHItem) holder).layoutPlus.setVisibility(View.GONE);
            }
            ((VHItem) holder).tvTitle.setText(data.getTitle());

            StringBuilder sb = new StringBuilder();
            if (data.getTimeDead().compareTo(new DateForm(Calendar.getInstance())) == 0) {
                sb.append(Manager.getTimeForm(data.getTimeDead()));
            }
            if (!data.getTags().equals("")) {
                if (!sb.toString().equals("")) {
                    sb.append(", ");
                }
                ArrayList<String> st = data.getTagList();
                for (int i = 0; i < st.size(); i++) {
                    sb.append("#" + st.get(i) + " ");
                }
            }
            if (sb.toString().equals("")) {
                ((VHItem) holder).tvTags.setVisibility(View.GONE);
            } else {
                ((VHItem) holder).tvTags.setVisibility(View.VISIBLE);
                ((VHItem) holder).tvTags.setText(sb.toString());
            }

            switch (data.getImportance()) {
                case (1):
                    ((VHItem) holder).layoutRight1.setVisibility(View.VISIBLE);
                    ((VHItem) holder).layout.setBackgroundResource(R.drawable.btn_star_half);
                    ((VHItem) holder).ivImportance.setImageResource(R.drawable.ic_star_half);
                    break;
                case (2):
                    ((VHItem) holder).layoutRight1.setVisibility(View.VISIBLE);
                    ((VHItem) holder).layout.setBackgroundResource(R.drawable.btn_star);
                    ((VHItem) holder).ivImportance.setImageResource(R.drawable.ic_star_true);
                    break;
                case (0):
                default:
                    ((VHItem) holder).layoutRight1.setVisibility(View.GONE);
                    ((VHItem) holder).layout.setBackgroundResource(R.drawable.btn_basic);
                    break;
            }

            int i = (int) (data.getTimeDead().getTime() - data.getTimePivot().getTime());
            if (i >= 1440) {
                i /= 1440;
                ((VHItem) holder).layoutRight2.setVisibility(View.VISIBLE);
                ((VHItem) holder).ivIcon.setImageResource(R.drawable.ic_calendar);
                ((VHItem) holder).tvTurn.setText("D-" + i);
            } else if (i >= 0) {
                ((VHItem) holder).layoutRight2.setVisibility(View.VISIBLE);
                ((VHItem) holder).ivIcon.setImageResource(R.drawable.ic_time);
                if (i >= 60) {
                    i /= 60;
                    ((VHItem) holder).tvTurn.setText("H-" + i);
                } else {
                    ((VHItem) holder).tvTurn.setText("M-" + i);
                }
            } else {
                ((VHItem) holder).layoutRight2.setVisibility(View.GONE);
            }
            if (data.getTimeChecked().isNull()) {
                ((VHItem) holder).tvTitle.setPaintFlags(((VHItem) holder).tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                ((VHItem) holder).layoutText.setAlpha(1);
                if (data.getTimeDead().compareTo(data.getTimePivot()) >= 0) {
                    if (data.getTimePivot().compareTo(new DateForm(Calendar.getInstance())) >= 0) {
                        ((VHItem) holder).ivCheck.setImageResource(R.drawable.ic_check_false);
                    } else {
                        ((VHItem) holder).ivCheck.setImageResource(R.drawable.ic_delay);
                    }
                } else {
                    ((VHItem) holder).ivCheck.setImageResource(R.drawable.ic_close);
                }
            } else {
                ((VHItem) holder).layout.setBackgroundResource(R.drawable.btn_basic);
                ((VHItem) holder).tvTitle.setPaintFlags(((VHItem) holder).tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                ((VHItem) holder).layoutText.setAlpha((float) 0.5);
                if (data.getTimeChecked().compareTo(data.getTimePivot()) == 0) {
                    ((VHItem) holder).ivCheck.setImageResource(R.drawable.ic_check_true);
                } else {
                    ((VHItem) holder).ivCheck.setImageResource(R.drawable.ic_delay);
                }
            }
        } else if (holder instanceof VHEdit) {
            ((MainActivity) mContext).setViewBottom(false);
            if (temp != null) {
                data = temp;
            } else {
                data = new DataTodo(-1, null);
                data.setTitle("유효기간이 만료되어 초기화되었습니다. 취소 후 시작해주세요.");
                // TODO: 2018-03-21 초기화될 경우 해결방안 - 초기화 안되게 하면 더 좋음.
            }
            etEditTitle.setText(data.getTitle());
            etEditTitle.requestFocus();

            StringBuffer sb = new StringBuffer();
            if (data.getTimeDead().compareTo(new DateForm(Calendar.getInstance())) == 0) {
                sb.append(Manager.getTimeForm(data.getTimeDead()));
            }
            if (!data.getTags().equals("")) {
                if (!sb.toString().equals("")) {
                    sb.append(", ");
                }
                ArrayList<String> st = data.getTagList();
                for (int i = 0; i < st.size(); i++) {
                    sb.append("#" + st.get(i) + " ");
                }
            }
            if (sb.toString().equals("")) {
                ((VHEdit) holder).tvTags.setVisibility(View.GONE);
            } else {
                ((VHEdit) holder).tvTags.setVisibility(View.VISIBLE);
                ((VHEdit) holder).tvTags.setText(sb.toString());
            }
            switch (data.getImportance()) {
                case (1):
                    ((VHEdit) holder).ivEditLeft.setImageResource(R.drawable.ic_star_half);
                    break;
                case (2):
                    ((VHEdit) holder).ivEditLeft.setImageResource(R.drawable.ic_star_true);
                    break;
                case (0):
                default:
                    ((VHEdit) holder).ivEditLeft.setImageResource(R.drawable.ic_star_false);
                    break;
            }
        } else if (holder instanceof VHHeader) {
            String title = "";
            for (int i = 0; i < headerPos.length; i++) {
                if (position == headerPos[i]) {
                    switch (i) {
                        case (0):
                            title = "매우 중요!";
                            break;
                        case (1):
                            title = "할 일";
                            break;
                        case (2):
                            title = "오늘의 퍼즐";
                            break;
                        case (3):
                            title = "완료";
                            break;
                        default:
                            title = "";
                            break;
                    }
                }
            }
            ((VHHeader) holder).tvTitle.setText(title);
        }
    }

    private void removeItemView(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
        for (int i = 0; i < headerPos.length; i++) {
            if (position < headerPos[i]) {
                headerPos[i]--;
            }
        }
        notifyItemRangeChanged(position, dataList.size()); // 지워진 만큼 다시 채워넣기.
    }

    public void onRefresh() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        isAutoSort = prefs.getBoolean(Manager.PREF_AUTO_SORT, true);
        itemExpandPosition = -1;
        setData();
        notifyDataSetChanged();
    }

//    public int getSortedPosition(int position) {
//        ArrayList<DataTodo> list = dataList;
//        list.remove(0);
//        ArrayList<DataTodo> list0 = new ArrayList<>();
//        ArrayList<DataTodo> list1 = new ArrayList<>();
//        ArrayList<DataTodo> list2 = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            switch (list.get(i).getImportance()) {
//                case (0):
//                    list0.add(list.get(i));
//                    break;
//                case (1):
//                    list1.add(list.get(i));
//                    break;
//                case (2):
//                    list2.add(list.get(i));
//                    break;
//            }
//        }
//        list = new ArrayList<>();
//        list.addAll(list2);
//        list.addAll(list1);
//        list.addAll(list0);
//        list0 = new ArrayList<>();
//        list1 = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            switch (list.get(i).getChecked()) {
//                case (0):
//                    list0.add(list.get(i));
//                    break;
//                case (1):
//                    list1.add(list.get(i));
//                    break;
//            }
//        }
//        list = new ArrayList<>();
//        list.addAll(list0);
//        list.addAll(list1);
//
//        for (int i = 0; i < list.size(); i++) {
//            if (dataList.get(position).getId() == list.get(i).getId()) {
//                dataList = list;
//                this.dataList.add(0, new DataTodo());
//                return i;
//            }
//        }
//        return 0;
//    }

    public void dateDelayOption(final DataTodo temp) {
        DatePickerDialog.OnDateSetListener listenerDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                temp.getTimeDead().set(year, monthOfYear, dayOfMonth);
                dbManager.updateTodo(temp);
                onRefresh();
            }
        };
        DatePickerDialog dpDialog = new DatePickerDialog(mContext, listenerDate,
                temp.getTimeDead().getYear(),
                temp.getTimeDead().getMonth(),
                temp.getTimeDead().getDay());
        dpDialog.show();
    }

    public ArrayList<DataTodo> getDataList() {
        ArrayList<DataTodo> list = new ArrayList<>();
        list.addAll(dataList);
        int tempMinus = 0;
        for (int i = 0; i < headerPos.length; i++) {
            if (headerPos[i] != -1) {
                list.remove(headerPos[i] - tempMinus);
                tempMinus++;
            }
        }
        return list;
    }

    public DataTodo getTemp() {
        return temp;
    }

    public void saveTemp() {
        temp.setTitle(etEditTitle.getText().toString());
    }
}