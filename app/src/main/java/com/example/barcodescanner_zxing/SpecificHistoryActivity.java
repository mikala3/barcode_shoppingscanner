package com.example.barcodescanner_zxing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpecificHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    DatabaseHelper myDb;
    private Context context;
    SpecificHistoryActivity.createHistoryListsThread thread;
    private LinearLayout layout_historyList;
    private String list_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myDb = new DatabaseHelper(this);
        context = this;

        Intent intent = getIntent();
        list_id = intent.getStringExtra("list_id");

        thread = new createHistoryListsThread(this);
        thread.start();
    }

    @Override
    public void onClick(View v) {

    }

    class createHistoryListsThread extends Thread {
        Context context;

        public createHistoryListsThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Cursor historyList;
            historyList = myDb.getSpecificHistory(list_id);

            /* whole layout for our grocerylist, remove old views if any */
            layout_historyList = (LinearLayout) findViewById(R.id.layout_groceryList);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    layout_historyList.removeAllViews();
                }
            });

            int counterId = 1;
            if(historyList.getCount() > 0) {
                int listLength = historyList.getCount();
                historyList.moveToFirst();
                /* create realtivelayouts for grocery and button */
                for(int i=0; i<listLength; i++){

                    String product_price = historyList.getString(0);

                    String amount = historyList.getString(1);

                    String product_name = historyList.getString(2);

                    int price = Integer.parseInt(product_price)*Integer.parseInt(amount);

                    RelativeLayout nameLayout = new RelativeLayout(context);
                    nameLayout.setId(counterId);
                    counterId++;

                    TextView nameView = new TextView(context);
                    nameView .setText(product_name);
                    nameView .setId(counterId);
                    counterId++;

                    TextView textView = new TextView(context);
                    textView.setText(String.valueOf(price) + "kr");


                    nameLayout.addView(nameView);
                    nameLayout.addView(textView);
                    nameLayout.setBackgroundResource(R.drawable.border_bottom);


                    RelativeLayout.LayoutParams paramsTextview = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                    paramsTextview.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    paramsTextview.setMargins(convertDpToPx(context, 5), 15, convertDpToPx(context, 5), 0);
                    textView.setLayoutParams(paramsTextview);

                    RelativeLayout.LayoutParams paramsDateview = (RelativeLayout.LayoutParams) nameView.getLayoutParams();
                    paramsDateview.setMargins(convertDpToPx(context, 5), 15, convertDpToPx(context, 5), 0);
                    nameView.setLayoutParams(paramsDateview);

                    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    int dp = convertDpToPx(context, 20);
                    relativeParams.setMargins(dp, dp, dp, 0);
                    nameLayout.setLayoutParams(relativeParams);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //priceTextView.setText("Total price: " + (totalPrice + priceProduct) +" kr");
                            layout_historyList.addView(nameLayout);
                        }
                    });
                    historyList.moveToNext();
                }
            }
        }

        public int convertDpToPx(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            float convertedDp = dp * density;
            return (int)convertedDp;
        }

    }
}
