package com.example.meibank2.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meibank2.Adapters.ItemsAdapter;
import com.example.meibank2.Database.DatabaseHelper;
import com.example.meibank2.Models.Item;
import com.example.meibank2.R;

import java.util.ArrayList;

public class SelectItemDialog extends DialogFragment implements ItemsAdapter.GetItem {
    private static final String TAG = "SelectItemDialog";

    private ItemsAdapter.GetItem getItem;

    @Override
    public void OnGettingItemResult(Item item) {
        Log.d(TAG, "OnGettingItemResult: item: " + item.toString());
        try {
            getItem = (ItemsAdapter.GetItem) getActivity();
            getItem.OnGettingItemResult(item);
            dismiss();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private EditText edtTxtItemName;
    private RecyclerView itemRecView;

    private ItemsAdapter adapter;

    private DatabaseHelper databaseHelper;

    private GetAllItems getAllItems;
    private SearchForItems searchForItems;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: started");
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_item, null);

        edtTxtItemName = (EditText) view.findViewById(R.id.edtTxtItemName);
        itemRecView = (RecyclerView) view.findViewById(R.id.itemsRecView);

        adapter = new ItemsAdapter(getActivity(), this);
        itemRecView.setAdapter(adapter);
        itemRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        databaseHelper = new DatabaseHelper(getActivity());

        edtTxtItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d(TAG, "addTextChangedListener: onTextChanged");
                //TODO: exec
                searchForItems = new SearchForItems();
                searchForItems.execute(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getAllItems = new GetAllItems();
        getAllItems.execute();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Select an item");

        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getAllItems) {
            if (!getAllItems.isCancelled()) {
                getAllItems.cancel(true);
            }
        }

        if (null != searchForItems) {
            if (!searchForItems.isCancelled()) {
                searchForItems.cancel(true);
            }
        }
    }

    private class GetAllItems extends AsyncTask<Void, Void, ArrayList<Item>> {
        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("items", null, null, null, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Item> items = new ArrayList<>();
                        for (int i=0; i<cursor.getCount(); i++) {
                            Item item = new Item();
                            item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            item.setImage_url(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                            item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            items.add(item);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return items;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    db.close();
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (null != items) {
                adapter.setItems(items);
            } else {
                adapter.setItems(new ArrayList<Item>());
            }
        }
    }


    private class SearchForItems extends AsyncTask<String, Void, ArrayList<Item>> {
        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            Log.d(TAG, "SearchForItems: doInBackground: started");
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("items", null, "name LIKE ?",
                        new String[] {strings[0]}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Item> items = new ArrayList<>();
                        for (int i=0; i<cursor.getCount(); i++) {
                            Item item = new Item();
                            item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            item.setImage_url(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                            item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            items.add(item);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return items;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    db.close();
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            Log.d(TAG, "SearchForItems: onPostExecute: started");
            super.onPostExecute(items);

            if (null != items) {
                adapter.setItems(items);
                // Toast.makeText(getActivity(), items.toString(), Toast.LENGTH_SHORT).show();
            } else {
                adapter.setItems(new ArrayList<Item>());
            }
        }
    }
}
