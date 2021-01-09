package com.example.barcodescanner_zxing;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    private String barcode;
    TextView textView;
    DatabaseHelper myDb;
    Button postButton;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        postButton = (Button)findViewById(R.id.postButton);
        postButton.setOnClickListener(this);

        textView = findViewById(R.id.textView);
        Intent intent = getIntent();
        barcode= intent.getStringExtra("barcode");

        textView.setText(barcode);

        myDb = new DatabaseHelper(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postButton:
                final EditText price =  (EditText) findViewById(R.id.price);
                String priceString = (String) price.getText().toString();
                final EditText store =  (EditText) findViewById(R.id.store);
                String storeString = (String) store.getText().toString();
                final EditText description =  (EditText) findViewById(R.id.description);
                String descriptionString = (String) description.getText().toString();
                boolean state = myDb.insertData(barcode, Integer.parseInt(priceString), storeString, descriptionString);
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
