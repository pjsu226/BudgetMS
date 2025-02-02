package com.example.budgetreceipt.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetreceipt.R;
import com.example.budgetreceipt.adapters.CategoryAdapter;
import com.example.budgetreceipt.controllers.SwipeController;
import com.example.budgetreceipt.controllers.SwipeControllerActions;
import com.example.budgetreceipt.database.DBcatch;
import com.example.budgetreceipt.global.GlobalProperties;
import com.example.budgetreceipt.models.Category;
import com.example.budgetreceipt.models.User;
import com.example.budgetreceipt.validation.InputValidation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    final Context context = this;
    private final AppCompatActivity activity = CategoriesActivity.this;

    private LinearLayout nestedScrollView;

    private TextInputLayout textInputLayoutCategory;
    private TextInputEditText textInputEditTextCategory;

    private AppCompatButton appCompatButtonAdd;
    private AppCompatTextView appCompatTextViewCancelLink;
    private AppCompatTextView textViewAdd_Category;
    private RecyclerView recyclerViewCategory;
    private List<Category> categoryList;
    private InputValidation inputValidation;
    private CategoryAdapter categoryAdapter;
    SwipeController swipeController = null;
    private FloatingActionButton floatingActionButton;
    private String asd;
    private int user_id;


    private DBcatch dbCatch;
    private Category category;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        getSupportActionBar().setTitle("Add Category");

        floatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab);

        callActionButton();
        initViews();
        initObjects();
    }

    private void callActionButton(){
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.activity_popup_category);
                dialog.setTitle("Add Bill");

                // set the custom dialog components - text, image and button
                textViewAdd_Category = (AppCompatTextView) dialog.findViewById(R.id.textViewAdd_Category);

                textInputLayoutCategory = (TextInputLayout) dialog.findViewById(R.id.textInputLayoutCategory);

                textInputEditTextCategory = (TextInputEditText) dialog.findViewById(R.id.textInputEditTextCategory);

                appCompatButtonAdd = (AppCompatButton) dialog.findViewById(R.id.appCompatButtonAdd);
                appCompatTextViewCancelLink = (AppCompatTextView) dialog.findViewById(R.id.appCompatTextViewCancelLink);

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

    /**
     * 뷰를 초기화하는 메소드.
     */
    private void initViews() {
        inputValidation = new InputValidation(activity);
        nestedScrollView = (LinearLayout) findViewById(R.id.nestedScrollView);
        recyclerViewCategory = (RecyclerView) findViewById(R.id.recyclerViewCategory);
    }

    /**
     * 사용할 객체를 초기화하는 메소드.
     */
    private void initObjects() {
        dbCatch = new DBcatch(activity);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList);
        category = new Category();
        user = new User();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewCategory.setLayoutManager(mLayoutManager);
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setHasFixedSize(true);
        recyclerViewCategory.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        recyclerViewCategory.setAdapter(categoryAdapter);
        user_id  = ((GlobalProperties) this.getApplication()).getUserTokenVariable();
        asd = Integer.toString(user_id);
        setDefault();
        //attach Controller to recyclerViewCategory
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                DBcatch dbCatch = new DBcatch(CategoriesActivity.this);
                dbCatch.deleteCategory(categoryList.get(position).getId());
                categoryList.remove(position);
                categoryAdapter.notifyItemRemoved(position);
                categoryAdapter.notifyItemRangeChanged(position, categoryAdapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerViewCategory);

        recyclerViewCategory.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDrawR(c);
            }
        });


        getDataFromSQLite();
    }

    @Override
    public void onBackPressed() {
        Intent intentUserInter = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intentUserInter);
    }

    private void postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextCategory, textInputLayoutCategory, getString(R.string.error_category))) {
           return;
        }
        if (!dbCatch.checkCategory(textInputEditTextCategory.getText().toString().trim(),asd)) {
            category.setUserID(user_id);
            category.setName(textInputEditTextCategory.getText().toString().trim());
            dbCatch.addCategory(category);
            emptyInputEditText();
            Intent intentUserInter = new Intent(getApplicationContext(), CategoriesActivity.class);
            startActivity(intentUserInter);
        }else {
            inputValidation.isInputAlreadyExists(textInputEditTextCategory, textInputLayoutCategory, getString(R.string.success_already_exists));
        }
    }
    private void emptyInputEditText() {
        textInputEditTextCategory.setText(null);
    }

    /**
     * SQLite에서 모든 사용자 기록을 가져오는 메소드.
     */
    public void setDefault(){
        if (!dbCatch.checkCategory("Food & Beverages",asd)) {
            category.setUserID(user_id);
            category.setName("Food & Beverages");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Transportation",asd)) {
            category.setUserID(user_id);
            category.setName("Transportation");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Accommodation",asd)) {
            category.setUserID(user_id);
            category.setName("Accommodation");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Education",asd)) {
            category.setUserID(user_id);
            category.setName("Education");
            dbCatch.addCategory(category);
        }  if (!dbCatch.checkCategory("Entertainment",asd)) {
            category.setUserID(user_id);
            category.setName("Entertainment");
            dbCatch.addCategory(category);
        }  if (!dbCatch.checkCategory("Financial Services",asd)) {
            category.setUserID(user_id);
            category.setName("Financial Services");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Insurance",asd)) {
            category.setUserID(user_id);
            category.setName("Insurance");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Sport",asd)) {
            category.setUserID(user_id);
            category.setName("Sport");
            dbCatch.addCategory(category);
        } if (!dbCatch.checkCategory("Telecommunication",asd)) {
            category.setUserID(user_id);
            category.setName("Telecommunication");
            dbCatch.addCategory(category);
        } if(!dbCatch.checkCategory("Others",asd)) {
            category.setUserID(user_id);
            category.setName("Others");
            dbCatch.addCategory(category);
        }
    }
    private void getDataFromSQLite() {
        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                categoryList.clear();
                categoryList.addAll(dbCatch.getCategoriesByUserID(user_id));
                Collections.reverse(categoryList);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                categoryAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
