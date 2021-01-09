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

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {
    DatabaseHelper myDb;
    private Context context;
    HistoryActivity.createHistoryListsThread thread;
    private LinearLayout layout_historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myDb = new DatabaseHelper(this);
        context = this;

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
            historyList = myDb.getHistory();

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

                    String date = historyList.getString(0);

                    String list_id = historyList.getString(1);

                    int price = myDb.getTotalPriceOnHistoryList(date);

                    RelativeLayout dateLayout = new RelativeLayout(context);
                    dateLayout.setId(counterId);
                    counterId++;

                    TextView dateView = new TextView(context);
                    dateView.setText(date);
                    dateView.setId(counterId);
                    counterId++;

                    ImageButton removeButton = new ImageButton(context);
                    removeButton.setImageResource(R.drawable.ic_baseline_delete_24);
                    removeButton.setBackgroundColor(Color.TRANSPARENT);
                    removeButton.setId(counterId);
                    counterId++;

                    removeButton.setOnClickListener(new View.OnClickListener()
                    {

                        public void onClick(View view)
                        {
                            myDb.removeHistoryList(list_id);
                            run();
                        }
                    });

                    ImageButton showButton = new ImageButton(context);
                    showButton.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
                    showButton.setBackgroundColor(Color.TRANSPARENT);
                    showButton.setId(counterId);
                    counterId++;

                    showButton.setOnClickListener(new View.OnClickListener()
                    {

                        public void onClick(View view)
                        {
                            System.out.println("intent");
                            Intent intent = new Intent(context, SpecificHistoryActivity.class);
                            intent.putExtra("list_id", list_id);

                            context.startActivity(intent);
                        }
                    });


                    TextView textView = new TextView(context);
                    System.out.println(price);
                    textView.setText(String.valueOf(price) + "kr");
                    showButton.setId(counterId);
                    counterId++;


                    dateLayout.addView(dateView);
                    dateLayout.addView(removeButton);
                    dateLayout.addView(showButton);
                    dateLayout.addView(textView);
                    dateLayout.setBackgroundResource(R.drawable.border_bottom);

                    dateView.setOnClickListener(new View.OnClickListener()
                    {

                        public void onClick(View view)
                        {

                        }
                    });



                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) removeButton.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.setMargins(convertDpToPx(context, 5), 0, convertDpToPx(context, 5), 0);
                    removeButton.setLayoutParams(params);

                    RelativeLayout.LayoutParams paramsEye = (RelativeLayout.LayoutParams) showButton.getLayoutParams();
                    paramsEye.addRule(RelativeLayout.LEFT_OF, removeButton.getId());
                    paramsEye.setMargins(convertDpToPx(context, 5), 0, convertDpToPx(context, 5), 0);
                    showButton.setLayoutParams(paramsEye);

                    RelativeLayout.LayoutParams paramsTextview = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                    paramsTextview.addRule(RelativeLayout.LEFT_OF, showButton.getId());
                    paramsTextview.setMargins(convertDpToPx(context, 5), 15, convertDpToPx(context, 5), 0);
                    textView.setLayoutParams(paramsTextview);

                    RelativeLayout.LayoutParams paramsDateview = (RelativeLayout.LayoutParams) dateView.getLayoutParams();
                    paramsDateview.setMargins(convertDpToPx(context, 5), 15, convertDpToPx(context, 5), 0);
                    dateView.setLayoutParams(paramsDateview);

                    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    int dp = convertDpToPx(context, 20);
                    relativeParams.setMargins(dp, dp, dp, 0);
                    dateLayout.setLayoutParams(relativeParams);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //priceTextView.setText("Total price: " + (totalPrice + priceProduct) +" kr");
                            layout_historyList.addView(dateLayout);
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
