package com.example.budgetreceipt.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetreceipt.R;
import com.example.budgetreceipt.adapters.BillAdapter;
import com.example.budgetreceipt.adapters.CategorySelectAdapter;
import com.example.budgetreceipt.adapters.NothingSelectedSpinnerAdapter;
import com.example.budgetreceipt.controllers.NumberTextController;
import com.example.budgetreceipt.controllers.SwipeController;
import com.example.budgetreceipt.controllers.SwipeControllerActions;
import com.example.budgetreceipt.database.DBcatch;
import com.example.budgetreceipt.global.GlobalProperties;
import com.example.budgetreceipt.models.Bills;
import com.example.budgetreceipt.models.Category;
import com.example.budgetreceipt.validation.InputValidation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class BillsListActivity extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    final Context context = this;

    private RecyclerView recyclerViewBills;

    private TextInputEditText textInputEditTextDate;
    private AppCompatActivity activity = BillsListActivity.this;

    private TextInputLayout textInputLayoutAmount;
    private TextInputLayout textInputLayoutDescription;
    private TextInputLayout textInputLayoutDate;
    private TextInputLayout textInputLayoutCompany;
    private TextInputLayout textInputLayoutCategory;

    private AppCompatButton appCompatButtonModify;
    private TextInputEditText textInputEditTextAmount;
    private TextInputEditText textInputEditTextDescription;
    private TextInputEditText textInputEditTextCompany;
    private Spinner textInputEditTextCategory;
    private AppCompatTextView appCompatTextViewCancelLink;
    private AppCompatButton appCompatButtonAdd;

    SwipeController swipeController = null;

    private Dialog dialog;
    private InputValidation inputValidation;
    private BillAdapter billAdapter;
    private DBcatch dbCatch;
    private List<Bills> listBills;
    private Category category;
    private Bills bills;
    private String getTextAmount;
    private String refactorAmount;
    private List<Category> categoryList;
    private CategorySelectAdapter categorySelectAdapter;
    private FloatingActionButton floatingActionButton;
    private int user_id, bill_ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_list);
        getSupportActionBar().setTitle("");

        floatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab);

        callDialog();
        initViews();
        initObjects();
    }

    private void callDialog() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.activity_popup_bill);
                dialog.setTitle("Add Bill");
                //-
                initDialogViews();
                //create an adapters to describe how the items are displayed, adapters are used in several places in android.
                ArrayAdapter<Category> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.contact_spinner_row_nothing_selected, categoryList);
                //set the spinners adapters to the previously created one.'
                textInputEditTextCategory.setAdapter(
                        new NothingSelectedSpinnerAdapter(
                                adapter,
                                R.layout.contact_spinner_row_nothing_selected,
                                BillsListActivity.this));

                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };

                textInputEditTextDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        new DatePickerDialog(BillsListActivity.this, R.style.DialogTheme, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                //to make layout behind routed corners transparent
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // if button is clicked, close the custom dialog
                appCompatButtonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postDataToSQLite();
                    }
                });
                // if button is clicked, close the custom dialog
                appCompatTextViewCancelLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void initDialogViews() {
        // initViews of popup
        appCompatButtonModify = (AppCompatButton) dialog.findViewById(R.id.appCompatButtonModify);
        textInputLayoutAmount = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutAmount);
        textInputLayoutDescription = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutDescription);
        textInputLayoutDate = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutDate);
        textInputLayoutCompany = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutCompany);
        textInputLayoutCategory = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutCategory);

        textInputEditTextAmount = (TextInputEditText) dialog.findViewById(R.id.textInputEditTextAmount);
        textInputEditTextAmount.addTextChangedListener(new NumberTextController(textInputEditTextAmount, "#,###.##"));

        textInputEditTextDescription = (TextInputEditText) dialog.findViewById(R.id.textInputEditTextDescription);
        textInputEditTextDate = (TextInputEditText) dialog.findViewById(R.id.textInputEditTextDate);
        textInputEditTextCompany = (TextInputEditText) dialog.findViewById(R.id.textInputEditTextCompany);
        textInputEditTextCategory = (Spinner) dialog.findViewById(R.id.textInputEditTextCategory);

        appCompatButtonAdd = (AppCompatButton) dialog.findViewById(R.id.appCompatButtonAdd);
        appCompatTextViewCancelLink = (AppCompatTextView) dialog.findViewById(R.id.appCompatTextViewCancelLink);


    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        textInputEditTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    /**
     * This method is to initialize views
     */

    private void initViews() {
        recyclerViewBills = (RecyclerView) findViewById(R.id.recyclerViewBills);
    }

    /**
     * This method is to initialize objects to be used
     */
    private void initObjects() {
        inputValidation = new InputValidation(activity);
        dbCatch = new DBcatch(activity);
        listBills = new ArrayList<>();
        billAdapter = new BillAdapter(listBills);

        categoryList = new ArrayList<>();
        categorySelectAdapter = new CategorySelectAdapter(categoryList);
        category = new Category();
        bills = new Bills();

        user_id = ((GlobalProperties) this.getApplication()).getUserTokenVariable();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewBills.setLayoutManager(mLayoutManager);
        recyclerViewBills.setHasFixedSize(true);
        recyclerViewBills.setItemAnimator(new DefaultItemAnimator());
        recyclerViewBills.smoothScrollToPosition(0);
        recyclerViewBills.setAdapter(billAdapter);
        billAdapter.notifyItemInserted(0);
        recyclerViewBills.scrollToPosition(0);
        recyclerViewBills.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && floatingActionButton.getVisibility() == View.VISIBLE) {
                    floatingActionButton.hide();
                } else if (dy < 0 && floatingActionButton.getVisibility() != View.VISIBLE) {
                    floatingActionButton.show();
                }
            }
        });
        //attach Controller to RecyclerViewBills
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                DBcatch dbCatch = new DBcatch(BillsListActivity.this);
                dbCatch.deleteBill(listBills.get(position).getId());
                listBills.remove(position);
                billAdapter.notifyItemRemoved(position);
                billAdapter.notifyItemRangeChanged(position, billAdapter.getItemCount());
            }

            @Override
            public void onLeftClicked(int position) {
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.activity_popup_edit_bill);
                dialog.setTitle("Edit Bill");
                //-
                ArrayList<Bills> getBill = dbCatch.getBillByID(listBills.get(position).getId());
                initDialogViews();
                for (Bills b : getBill) {
                    Log.d("GET BILL", "The bills extracted bill " + String.valueOf(getBill.size()));
                    Log.d("GET BILL", "Amount" + String.valueOf(b.getAmount()) + " /Description " + b.getDescription()
                            + " /Date" + b.getDateString() + " /Company name" + b.getCompany_name() + " /Category" + b.getCategory() + b.getId());
                    bill_ID = listBills.get(position).getId();
                    //textInputEditTextAmount.addTextChangedListener(new NumberTextController(textInputEditTextAmount, "#,###.##"));

                    textInputEditTextAmount.setText(String.valueOf(b.getAmount()));
                    textInputEditTextDescription.setText(b.getDescription());
                    textInputEditTextDate.setText(b.getDateString());
                    textInputEditTextCompany.setText(b.getCompany_name());

                    ArrayAdapter<Category> adapter1 = new ArrayAdapter<>(getApplicationContext(), R.layout.contact_spinner_row_nothing_selected, categoryList);
                    //set the spinners adapters to the previously created one.'
                    textInputEditTextCategory.setAdapter(
                            new NothingSelectedSpinnerAdapter(
                                    adapter1,
                                    R.layout.contact_spinner_row_nothing_selected,
                                    BillsListActivity.this));

                    appCompatButtonModify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            modifyDataInSQLite();
                        }
                    });
                }

                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };

                textInputEditTextDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        new DatePickerDialog(BillsListActivity.this, R.style.DialogTheme, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                //to make layout behind routed corners transparent
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // if button is clicked, close the custom dialog

                // if button is clicked, close the custom dialog
                appCompatTextViewCancelLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerViewBills);

        recyclerViewBills.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDrawBothButton(c);
            }
        });

        getBillsFromSQLite();
        getCategoryFromSQLite();
    }

    @Override
    public void onBackPressed() {
        Intent intentUserInter = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intentUserInter);
    }

    /**
     * 입력한 텍스트 필드의 유효성을 검사하고 그 데이터를 SQLite에 보냄.
     */

    private void postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextAmount, textInputLayoutAmount, getString(R.string.error_empty_amount))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextDate, textInputLayoutDate, getString(R.string.error_empty_date))) {
            return;
        }
        if (textInputEditTextCategory.getSelectedItem() == null) {

            Toast toast = Toast.makeText(BillsListActivity.this, getString(R.string.error_empty_category), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(20);
            toast.show();
            return;
        }

        bills.setUserID(user_id);
        bills.setDescription(textInputEditTextDescription.getText().toString().trim());
        getTextAmount = textInputEditTextAmount.getText().toString();

        refactorAmount = getTextAmount.replaceAll("[$,.\\s+]", "");

        float convertStringToInt = Float.parseFloat(refactorAmount);
        float convert = convertStringToInt / 100;
        String refactor_Amount = Float.toString(convert);

        bills.setAmount(Float.parseFloat(refactor_Amount));
        bills.setDateString(textInputEditTextDate.getText().toString().trim());
        bills.setCompany_name(textInputEditTextCompany.getText().toString().trim());
        bills.setCategory(textInputEditTextCategory.getSelectedItem().toString().trim());

        dbCatch.addBill(bills);

        Intent intentUserInter = new Intent(getApplicationContext(), BillsListActivity.class);
        startActivity(intentUserInter);
    }

    private void modifyDataInSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextAmount, textInputLayoutAmount, getString(R.string.error_empty_amount))) {
            return;
        }if (!inputValidation.isInputEditTextFilled(textInputEditTextDate, textInputLayoutDate, getString(R.string.error_empty_date))) {
            return;
        }if (textInputEditTextCategory.getSelectedItem() == null){

            Toast toast = Toast.makeText(BillsListActivity.this, getString(R.string.error_empty_category), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(20);
            toast.show();
            return;
        }
        bills.setId(bill_ID);
        bills.setUserID(user_id);
        bills.setDescription(textInputEditTextDescription.getText().toString().trim());
        getTextAmount = textInputEditTextAmount.getText().toString();

        refactorAmount = getTextAmount.replaceAll("[$,.\\s+]", "");

        float convertStringToInt = Float.parseFloat(refactorAmount);
        float convert = convertStringToInt / 10;
        String refactor_Amount = Float.toString(convert);

        bills.setAmount(Float.parseFloat(refactor_Amount));
        bills.setDateString(textInputEditTextDate.getText().toString().trim());
        bills.setCompany_name(textInputEditTextCompany.getText().toString().trim());
        bills.setCategory(textInputEditTextCategory.getSelectedItem().toString().trim());

        dbCatch.updateBill(bills);
        Intent intentUserInter = new Intent(getApplicationContext(), BillsListActivity.class);
        startActivity(intentUserInter);
    }

    /**
     * SQLite에서 모든 사용자 기록을 가져오는 메소드.
     */
    private void getCategoryFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                categoryList.clear();
                categoryList.addAll(dbCatch.getCategoriesByUserID(user_id));

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void getBillsFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listBills.clear();
                listBills.addAll(dbCatch.getBillsByUserID(user_id));
                Collections.reverse(listBills);
                billAdapter.notifyItemInserted(0);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                billAdapter.notifyItemInserted(0);
                billAdapter.notifyDataSetChanged();
                recyclerViewBills.scrollToPosition(0);
            }
        }.execute();
    }
}
