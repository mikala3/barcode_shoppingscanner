package com.example.barcodescanner_zxing;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    private String barcode;
    TextView textView;
    DatabaseHelper myDb;
    Button postButton;
    EditText editTextPrice;
    EditText editTextStore;
    EditText editTextDescription;
    private Handler mHandler = new Handler();
    private String priceString;
    private String storeString;
    private String descriptionString;
    private String price;
    private String store;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        postButton = (Button)findViewById(R.id.postButton);
        postButton.setOnClickListener(this);

        textView = findViewById(R.id.textView);
        editTextPrice =  (EditText) findViewById(R.id.price);
        editTextStore =  (EditText) findViewById(R.id.store);
        editTextDescription = (EditText) findViewById(R.id.description);
        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");
        price = intent.getStringExtra("price");
        store = intent.getStringExtra("store");
        description = intent.getStringExtra("description");

        textView.setText(barcode);
        editTextPrice.setText(price);
        editTextStore.setText(store);
        editTextDescription.setText(description);

        myDb = new DatabaseHelper(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postButton:
                editTextPrice =  (EditText) findViewById(R.id.price);
                String price = (String) editTextPrice.getText().toString();
                editTextStore =  (EditText) findViewById(R.id.store);
                String store = (String) editTextStore.getText().toString();
                editTextDescription =  (EditText) findViewById(R.id.description);
                String description= (String) editTextDescription.getText().toString();

                boolean state = myDb.updateData(barcode, Integer.parseInt(price), store, description);
                if(state == true) {
                    Toast.makeText(this, "post successful",
                            Toast.LENGTH_LONG).show();
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            changeIntent();
                        }
                    }, 3000);
                }
                else {
                    Toast.makeText(this, "post error",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /* changeIntent will change to the MainActivity after the information is posted to the database */
    private void changeIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
