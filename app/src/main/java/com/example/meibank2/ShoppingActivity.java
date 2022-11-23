package com.example.meibank2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.meibank2.Adapters.ItemsAdapter;
import com.example.meibank2.Database.DatabaseHelper;
import com.example.meibank2.Dialogs.SelectItemDialog;
import com.example.meibank2.Models.Item;
import com.example.meibank2.Models.Shopping;
import com.example.meibank2.Models.Transaction;
import com.example.meibank2.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ShoppingActivity extends AppCompatActivity implements ItemsAdapter.GetItem {
    private static final String TAG = "ShoppingActivity";

    @Override
    public void OnGettingItemResult(Item item) {
        if (null != item) {
            invisibleItemRelLayout.setVisibility(View.VISIBLE);
            txtItemName.setText(item.getName());
            if (null != item.getImage_url() && null != itemImg) {
                Glide.with(this).asBitmap().load(item.getImage_url()).into(itemImg);
            }

            shopping.setItem_id(item.get_id());
        }
    }

    private Button btnAdd, btnPickDate, btnPickItem;
    private TextView txtItemName, txtWarning;
    private EditText edtTxtDate, edtTxtStore, edtTxtDesc, edtTxtPrice;
    private ImageView itemImg;
    private RelativeLayout invisibleItemRelLayout;

    private Transaction transaction;
    private Shopping shopping;

    private Utils utils;

    AdTransactionToDB addTransactionToDB;
    AddShoppingToShoppingTable addShoppingToShoppingTable;
    UpdateRemainedAmountOfUser updateRemainedAmountOfUser;

    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        
        initViews();
        setBtnPick();
        setSelectDate();

        transaction = new Transaction();
        shopping = new Shopping();

        utils = new Utils(this);

        initAddShoppingBtn();

        databaseHelper = new DatabaseHelper(this);
    }

    private void initAddShoppingBtn() {
        Log.d(TAG, "initAddShoppingBtn: started");
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtItemName.getText().toString().equals("") ||
                        edtTxtPrice.getText().toString().equals("")) {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please pick an item and set it's price");
                } else if (edtTxtDate.getText().toString().equals("") ||
                        edtTxtStore.getText().toString().equals("")) {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please set the date and enter store name");

                } else {
                    //do pay for shopping
                    addShoppingTransactions();
                }
            }
        });
    }

    /**
     * Set payment for shopping
     */
    private void addShoppingTransactions() {
        Log.d(TAG, "addShoppingTransaction: started");

        // set properties for trans instance
        transaction.setAmount(-Double.parseDouble(String.valueOf(edtTxtPrice.getText())));
        transaction.setDate(String.valueOf(edtTxtDate.getText()));
        transaction.setType("spend");
        transaction.setRecipient(edtTxtStore.getText().toString());
        transaction.setDescription(edtTxtDesc.getText().toString());

        //set user_id for trans instance
        User user = utils.isUserLoggedIn();
        if (null != user) {
            transaction.setUser_id(user.get_id());
        }

        //add the transaction to transactions table in data base
        addTransactionToDB = new AdTransactionToDB();
        addTransactionToDB.execute(transaction);
    }

    private class AdTransactionToDB extends AsyncTask<Transaction, Void, Shopping> {

        @Override
        protected Shopping doInBackground(Transaction... transactions) {
            try {
                Transaction transaction = transactions[0];
                SQLiteDatabase db = databaseHelper.getWritableDatabase();

                ContentValues values = new ContentValues();

                values.put("amount", transaction.getAmount());
                values.put("user_id", transaction.getUser_id());
                values.put("date", transaction.getDate());
                values.put("type", transaction.getType());
                values.put("recipient", transaction.getRecipient());
                values.put("description", transaction.getDescription());

                Log.d(TAG, "AdTransactionToDB: doInBackground: before inserting trans:");
                long transactionId = db.insert("transactions", null, values);
                db.close();
                Log.d(TAG, "AdTransactionToDB: doInBackground: " + transactionId);

                if (transactionId != -1) {
                    shopping.setTransaction_id((int) transactionId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return shopping;
        }

        @Override
        protected void onPostExecute(Shopping shopping) {
            super.onPostExecute(shopping);
            Toast.makeText(ShoppingActivity.this, "successfully paid for shopping", Toast.LENGTH_SHORT).show();

            //update the remained amount in "users" table
            updateRemainedAmountOfUser = new UpdateRemainedAmountOfUser();
            updateRemainedAmountOfUser.execute(transaction);


            // add shopping to the "shopping" table in the database
            addShoppingToShoppingTable = new AddShoppingToShoppingTable();
            addShoppingToShoppingTable.execute(shopping);
        }
    }

    private class UpdateRemainedAmountOfUser extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            Log.d(TAG, "UpdateRemainedAmountofUser: doInBackground: started");
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                Cursor cursor = db.query("users", null, "_id=?",
                        new String[] {String.valueOf(transactions[0].getUser_id())},
                        null, null, null);
                Log.d(TAG, "UpdateRemainedAmountofUser: doInBackground: gotten cursor");

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                       ContentValues values = new ContentValues();
                       values.put("_id", cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                       values.put("email", cursor.getString(cursor.getColumnIndexOrThrow("email")));
                       values.put("password", cursor.getString(cursor.getColumnIndexOrThrow("password")));
                       values.put("first_name", cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                       values.put("last_name", cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                       values.put("address", cursor.getString(cursor.getColumnIndexOrThrow("address")));
                       values.put("image_url", cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                       Double newRemainedAmout = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"))
                               + transaction.getAmount();
                       values.put("remained_amount", newRemainedAmout);

                       Long userId = db.replace("users", null, values);
                       cursor.close();
                       db.close();
                    } else {
                        cursor.close();
                        db.close();
                    }

                } else {
                    db.close();

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "UpdateRemainedAmountofUser: doInBackground: ended");
            return null;

        }
    }

    private class AddShoppingToShoppingTable extends AsyncTask<Shopping, Void, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // set user_id for the "Shopping" instance
            User user = utils.isUserLoggedIn();
            if (null != user) {
                shopping.setUser_id(user.get_id());
            }

            shopping.setPrice(Double.parseDouble(edtTxtPrice.getText().toString()));
            shopping.setDescription(edtTxtDesc.getText().toString());
            shopping.setDate(edtTxtDate.getText().toString());
        }

        /**
         * @param shoppings
         * @return
         */
        @Override
        protected Long doInBackground(Shopping... shoppings) {
            Log.d(TAG, "AddShoppingToShoppingTable: doInBackground: started");
            try {
                Shopping shopping = shoppings[0];
                SQLiteDatabase db = databaseHelper.getWritableDatabase();

                ContentValues values = new ContentValues();

                values.put("item_id", shopping.getItem_id());
                values.put("user_id", shopping.getUser_id());
                values.put("transaction_id", shopping.getTransaction_id());
                values.put("price", shopping.getPrice());
                values.put("date", shopping.getDate());
                values.put("description", shopping.getDescription());

                Long shoppingId = db.insert("shopping", null, values);
                db.close();
                return shoppingId;

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

            if (aLong != null && aLong != -1) {
                Toast.makeText(ShoppingActivity.this, "Successfully add shopping", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ShoppingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != addTransactionToDB) {
            if (!addTransactionToDB.isCancelled()) {
                addTransactionToDB.cancel(true);
            }
        }

        if (null != addShoppingToShoppingTable) {
            if (!addShoppingToShoppingTable.isCancelled()) {
                addShoppingToShoppingTable.cancel(true);
            }
        }
    }

    private void setSelectDate() {
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                String sDate = calendar.get(Calendar.YEAR) + "-"
                        + (calendar.get(Calendar.MONTH) + 1) + "-"
                        + calendar.get(Calendar.DATE);
                edtTxtDate.setText(sDate);
            }
        });

    }

    private void setBtnPick() {
        btnPickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onCreate: btnPick.setOnClickListener: started");
                SelectItemDialog selectItemDialog = new SelectItemDialog();
                selectItemDialog.show(getSupportFragmentManager(), "select item dialog");
            }
        });
    }

    private void initViews() {
        btnAdd = findViewById(R.id.btnAdd);
        txtWarning = findViewById(R.id.txtWarning);

        btnPickDate = findViewById(R.id.btnPickDate);
        edtTxtDate = findViewById(R.id.edtTxtDate);

        edtTxtStore = findViewById(R.id.edtTxtStore);

        edtTxtDesc = findViewById(R.id.edtTxtDesc);

        btnPickItem = findViewById(R.id.btnPick);
        invisibleItemRelLayout = findViewById(R.id.invisibleItemRelLayout);
        edtTxtPrice = findViewById(R.id.edtTxtPrice);
        itemImg = findViewById(R.id.itemImage);
        txtItemName = findViewById(R.id.txtItemName);


    }
}