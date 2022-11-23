package com.example.meibank2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddInvestmentActivity extends AppCompatActivity {
    private static final String TAG = "AddInvestmentActivity";

    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate;
    private Button btnPickInitDate, btnPickFinishDate, btnAddInvestment;
    private TextView txtWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Log.d(TAG, "initDataSetListener: onDataSet");
            initCalendar.set(Calendar.YEAR, year);
            initCalendar.set(Calendar.MONTH, month);
            initCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            finishCalendar.set(Calendar.YEAR, year);
            finishCalendar.set(Calendar.MONTH, month);
            finishCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            edtTxtFinishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.getTime()));
        }
    };

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);
        
        initViews();
        setOnClockListeners();
    }

    private void setOnClockListeners() {
        Log.d(TAG, "setOnClockListeners: started");
        btnPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, initDateSetListener,
                        initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH),
                        initCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, finishDateSetListener,
                        finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH),
                        finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");

        edtTxtName = findViewById(R.id.edtTxtName);
        edtTxtInitAmount = findViewById(R.id.edtTxtInitAmount);
        edtTxtROI = findViewById(R.id.edtTxtMonthlyROI);
        edtTxtInitDate = findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = findViewById(R.id.edtTxtFinishDate);

        btnPickInitDate = findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = findViewById(R.id.btnPickFinishDate);
        btnAddInvestment = findViewById(R.id.btnAddInvestment);

        txtWarning = findViewById(R.id.txtWarning);

    }
}