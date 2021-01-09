package com.example.barcodescanner_zxing;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseHelper myDb;
    private TextView textView;
    private Button scanButton;
    private Button addButton;
    private Button updateButton;
    private Button shoppingListButton;
    private Button historyButton;
    private LinearLayout linearLayout;
    private String barcode;
    private String priceString;
    private String storeString;
    private String descriptionString;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton =  (Button)findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);

        addButton = (Button)findViewById(R.id.addButton);
        addButton.setOnClickListener(this);

        updateButton = (Button)findViewById(R.id.updateButton);
        updateButton.setOnClickListener(this);

        shoppingListButton = (Button)findViewById(R.id.shoppingListButton);
        shoppingListButton.setOnClickListener(this);

        historyButton = (Button)findViewById(R.id.historyButton);
        historyButton.setOnClickListener(this);

        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);

        textView = findViewById(R.id.textView);
        myDb = new DatabaseHelper(this);


        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(intentResult != null) {
            if (intentResult.getContents() == null) {
                textView.setText("Cancelled");
            }
            else {
                barcode = intentResult.getContents();
                Cursor result = myDb.getBarcodeInfo(barcode);
                linearLayout.setVisibility(View.VISIBLE);

                if(result.getCount() == 0) {
                    addButton.setVisibility(View.VISIBLE);
                }
                else {
                    result.moveToFirst();
                    priceString = result.getString(2);
                    storeString = result.getString(3);
                    descriptionString = result.getString(4);

                    String displayText = "Barcode: " + barcode + "\nPrice: " + priceString + "\nStore: " + storeString + "\nDescription: "+ descriptionString;
                    textView.setText(displayText);
                    updateButton.setVisibility(View.VISIBLE);
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanButton:
                IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                intentIntegrator.setPrompt("Scan a barcode");
                intentIntegrator.initiateScan();
                break;
            case R.id.addButton:
                //create a new intent with an add screen
                intent = new Intent(this, AddActivity.class);
                intent.putExtra("barcode", barcode);
                this.startActivity(intent);
                //myDb.insertData()
                break;
            case R.id.updateButton:
                intent = new Intent(this, UpdateActivity.class);
                intent.putExtra("barcode", barcode);
                intent.putExtra("price", priceString);
                intent.putExtra("store", storeString);
                intent.putExtra("description", descriptionString);
                this.startActivity(intent);
                break;
            case R.id.shoppingListButton:
                intent = new Intent(this, ShoppingListActivity.class);
                this.startActivity(intent);
                break;
            case R.id.historyButton:
                intent = new Intent(this, HistoryActivity.class);
                this.startActivity(intent);
        }
    }
}