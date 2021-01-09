
package com.example.barcodescanner_zxing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppComponentFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.integration.android.IntentIntegrator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity implements View.OnClickListener {

    AutoCompleteTextView actv;
    DatabaseHelper myDb;
    private LinearLayout layout_groceryList;
    createGroceryListThread thread;
    private Context context;
    List<String> groceries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        context = this;
        myDb = new DatabaseHelper(this);
        groceries = Arrays.asList(myDb.getGroceries());

        thread = new createGroceryListThread(this);
        thread.start();

        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, groceries);

        Button buyButton = (Button)findViewById(R.id.buyButton);
        buyButton.setOnClickListener(this);

        Button clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(this);

        //Getting the instance of AutoCompleteTextView
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                String textFromSearch = actv.getEditableText().toString();
                if (groceries.contains(textFromSearch)) {
                    boolean result = myDb.insertToGroceryList(textFromSearch);
                    if (result) {
                        thread.interrupt();
                        thread = new createGroceryListThread(context);
                        thread.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "Product already exists", Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "No such product exists", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

    }


    @Override
    public void onClick(View v) {
       switch (v.getId()) {
            case R.id.buyButton:
                myDb = new DatabaseHelper(this);
                Cursor result = myDb.getShoppingList();
                if(result.getCount() > 0)
                    myDb.checkOutList();
                    thread.interrupt();
                    thread = new createGroceryListThread(context);
                    thread.start();
                break;

           case R.id.clearButton:
               myDb = new DatabaseHelper(this);
               myDb.removeCurrentList();
               thread.interrupt();
               thread = new createGroceryListThread(context);
               thread.start();
               break;
        }
    }


    /* createGroceryList, this function will create all the items in our list, with buttons and textviews */
    @SuppressLint("ResourceType")
    //public void createGroceryList() {
    class createGroceryListThread extends Thread {

        Context context;
        int totalprice = 0;
        public createGroceryListThread(Context context) {
            this.context = context;
        }
        @Override
        public void run() {
            System.out.println("Restarted");
            //updateAmount("clear");
            Cursor groceryList;
            groceryList = myDb.getShoppingList();

            /* whole layout for our grocerylist, remove old views if any */
            layout_groceryList = (LinearLayout) findViewById(R.id.layout_groceryList);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    TextView itemsTextView = (TextView)findViewById(R.id.items);
                    itemsTextView.setText("0/0 groceries");
                    layout_groceryList.removeAllViews();
                }
            });
            int counterId = 1;
            if(groceryList.getCount() > 0) {
                int listLength = groceryList.getCount();
                groceryList.moveToFirst();
                /* create realtivelayouts for grocery and button */
                for(int i=0; i<listLength; i++){

                    String productId = groceryList.getString(1);
                    int taken = Integer.parseInt(groceryList.getString(5));
                    int buttonId = counterId;
                    counterId++;


                    RelativeLayout groceryLayout = new RelativeLayout(context);
                    groceryLayout.setId(counterId);
                    counterId++;


                    TextView grocery = new TextView(context);
                    grocery.setText(groceryList.getString(0));
                    grocery.setId(counterId);
                    counterId++;



                    TextView textView = new TextView(context);
                    textView.setId(buttonId);
                    textView.setVisibility(View.INVISIBLE);
                    int priceProduct = Integer.parseInt(groceryList.getString(2));
                    totalprice += priceProduct;
                    String displayText = "Amount: " + groceryList.getString(4) +"\nPrice: " + groceryList.getString(2) + "\nStore: " + groceryList.getString(3);

                    textView.setText(displayText);



                    ImageButton removeButton = new ImageButton(context);
                    removeButton.setImageResource(R.drawable.ic_baseline_delete_24);
                    removeButton.setBackgroundColor(Color.TRANSPARENT);
                    removeButton.setId(counterId);
                    counterId++;
                    removeButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {

                            if(myDb.getStatus(productId)) {
                                updateAmount("removeFromListAndTaken");
                            }

                            else {
                                updateAmount("removeFromList");
                            }
                            myDb.removeItem(Integer.parseInt(productId));
                            run();
                        }
                    });



                    ImageButton increaseButton = new ImageButton(context);
                    increaseButton.setImageResource(R.drawable.ic_baseline_add_24);
                    increaseButton.setBackgroundColor(Color.TRANSPARENT);
                    increaseButton.setId(counterId);
                    counterId++;
                    increaseButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {

                            boolean status = myDb.changeAmount(productId, 1);
                            int count = textView.getLineCount();
                            if(count > 0 && status) {
                                int start = textView.getLayout().getLineStart(0);
                                int end = textView.getLayout().getLineStart(1);
                                CharSequence substring = textView.getText().subSequence(start, end);
                                String[] amount = substring.toString().split(" ");
                                int productAmount = Integer.parseInt(amount[1].trim()) + 1;

                                start = textView.getLayout().getLineStart(1);
                                end = textView.getLayout().getLineStart(count);
                                substring = textView.getText().subSequence(start, end);
                                textView.setText("Amount: " + productAmount +"\n"+substring);
                                updateAmount("updatePrice");
                            }

                        }
                    });




                    ImageButton decreaseButton = new ImageButton(context);
                    decreaseButton.setImageResource(R.drawable.ic_baseline_remove_24);
                    decreaseButton.setBackgroundColor(Color.TRANSPARENT);
                    decreaseButton.setId(counterId);
                    counterId++;
                    decreaseButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {

                            boolean status = myDb.changeAmount(productId, -1);
                            int count = textView.getLineCount();
                            if(count > 0 && status) {

                                int start = textView.getLayout().getLineStart(0);
                                int end = textView.getLayout().getLineStart(1);
                                CharSequence substring = textView.getText().subSequence(start, end);
                                String[] amount = substring.toString().split(" ");
                                int productAmount = Integer.parseInt(amount[1].trim());

                                if(productAmount > 1) {
                                    productAmount--;
                                    start = textView.getLayout().getLineStart(1);
                                    end = textView.getLayout().getLineStart(count);
                                    substring = textView.getText().subSequence(start, end);
                                    textView.setText("Amount: " + productAmount +"\n"+substring);
                                    updateAmount("updatePrice");
                                }
                            }
                        }
                    });




                    ImageButton infoButton = new ImageButton(context);
                    infoButton.setImageResource(R.drawable.ic_baseline_info_24);
                    infoButton.setBackgroundColor(Color.TRANSPARENT);
                    infoButton.setId(counterId);
                    counterId++;
                    View.OnClickListener toggleTextView = new View.OnClickListener(){
                        public void onClick(View v) {
                            TextView txtView = (TextView)findViewById(buttonId);
                            //Toggle
                            if (txtView.getVisibility() == View.VISIBLE) {
                                txtView.setVisibility(View.INVISIBLE);
                            }

                            else {
                                RelativeLayout.LayoutParams paramsText2 = new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                paramsText2.addRule(RelativeLayout.LEFT_OF, infoButton.getId());
                                txtView.setLayoutParams(paramsText2);
                                txtView.setVisibility(View.VISIBLE);

                                int productAmount = myDb.getAmount(productId);
                                int count = txtView.getLineCount();
                                int start = txtView.getLayout().getLineStart(1);

                                int end = txtView.getLayout().getLineStart(count);
                                CharSequence substring = txtView.getText().subSequence(start, end);
                                txtView.setText("Amount: " + productAmount +"\n"+substring);

                            }
                        }
                    };
                    infoButton.setOnClickListener(toggleTextView);



                    // groceryLayout.addView(grocery);
                    groceryLayout.addView(removeButton);
                    groceryLayout.addView(infoButton);
                    groceryLayout.addView(increaseButton);
                    groceryLayout.addView(decreaseButton);
                    groceryLayout.addView(textView);
                    groceryLayout.setBackgroundResource(R.drawable.border_bottom);


                    grocery.setOnClickListener(new View.OnClickListener()
                    {

                        public void onClick(View view)
                        {

                            if(groceryLayout.getAlpha() > 0.3) {
                                groceryLayout.setAlpha((float) 0.2);
                                myDb.takeGrocery(productId, 1);
                                updateAmount("addTaken");
                            }
                            else {
                                groceryLayout.setAlpha((float) 1.0);
                                myDb.takeGrocery(productId, 0);
                                updateAmount("removeTaken");

                            }
                        }
                    });


                    if(myDb.getStatus(productId)) {
                        groceryLayout.setAlpha((float) 0.2);
                        updateAmount("addToList");
                        updateAmount("addTaken");

                    }
                    else {
                        updateAmount("addToList");
                    }
                    /* LAYOUT RULES FOR THE BUTTONS AND TEXTVIEWS */

                    RelativeLayout.LayoutParams paramsGrocery = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsGrocery.setMargins(convertDpToPx(context, 5), 15, convertDpToPx(context, 5), 0);
                    grocery.setLayoutParams(paramsGrocery);
                    groceryLayout.addView(grocery);


                    RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //paramsText.addRule(RelativeLayout.RIGHT_OF, grocery.getId());
                    paramsText.addRule(RelativeLayout.LEFT_OF, infoButton.getId());
                    textView.setLayoutParams(paramsText);

                    RelativeLayout.LayoutParams paramsInfo = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsInfo.addRule(RelativeLayout.LEFT_OF, increaseButton.getId());
                    paramsInfo.setMargins(convertDpToPx(context, 5), 0, convertDpToPx(context, 5), 0);
                    infoButton.setLayoutParams(paramsInfo);


                    RelativeLayout.LayoutParams paramsAdd = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsAdd.addRule(RelativeLayout.LEFT_OF, decreaseButton.getId());
                    paramsAdd.setMargins(convertDpToPx(context, 5), 0, convertDpToPx(context, 5), 0);
                    increaseButton.setLayoutParams(paramsAdd);

                    RelativeLayout.LayoutParams paramsDecrease = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    paramsDecrease.addRule(RelativeLayout.LEFT_OF, removeButton.getId());
                    paramsDecrease.setMargins(convertDpToPx(context, 5), 0, convertDpToPx(context, 5), 0);
                    decreaseButton.setLayoutParams(paramsDecrease);



                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) removeButton.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    removeButton.setLayoutParams(params);

                    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    int dp = convertDpToPx(context, 20);
                    relativeParams.setMargins(dp, dp, dp, 0);
                    groceryLayout.setLayoutParams(relativeParams);


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //priceTextView.setText("Total price: " + (totalPrice + priceProduct) +" kr");
                            layout_groceryList.addView(groceryLayout);
                        }
                    });
                groceryList.moveToNext();
                }
                updateAmount("updatePrice");
            }
            else {
                updateAmount("clear");
            }

        }

    }

    public void updateAmount(String command) {

        System.out.println(command);
        switch (command) {
            case "clear":
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView itemsTextView = (TextView)findViewById(R.id.items);
                        itemsTextView.setText("0/0 groceries");
                        TextView priceTextView = (TextView)findViewById(R.id.price);
                        priceTextView.setText("Total price: 0 kr");
                    }
                });
                break;

            case "addToList":

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView itemsTextView = (TextView)findViewById(R.id.items);
                        CharSequence items = itemsTextView.getText();
                        String[] amount = items.toString().split("/");
                        final int productAmount = Integer.parseInt(amount[1].split(" ")[0]);
                        final int takenAmount = Integer.parseInt(amount[0]);
                        itemsTextView.setText((takenAmount) + "/" + (productAmount + 1) +" groceries");
                    }
                });
                break;

            case "removeFromList":
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView itemsTextView = (TextView)findViewById(R.id.items);
                        CharSequence items = itemsTextView.getText();
                        String[] amount = items.toString().split("/");
                        final int productAmount = Integer.parseInt(amount[1].split(" ")[0]);
                        final int takenAmount = Integer.parseInt(amount[0]);
                        itemsTextView.setText((takenAmount) + "/" + (productAmount - 1) +" groceries");
                    }
                });
                break;

            case "removeFromListAndTaken":
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView itemsTextView = (TextView)findViewById(R.id.items);
                        CharSequence items = itemsTextView.getText();
                        String[] amount = items.toString().split("/");
                        final int productAmount = Integer.parseInt(amount[1].split(" ")[0]);
                        final int takenAmount = Integer.parseInt(amount[0]);
                        itemsTextView.setText((takenAmount - 1) + "/" + (productAmount - 1) +" groceries");
                    }
                });
                break;

            case("addTaken"):
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView itemsTextView = (TextView)findViewById(R.id.items);
                        CharSequence items = itemsTextView.getText();
                        String[] amount = items.toString().split("/");
                        final int productAmount = Integer.parseInt(amount[1].split(" ")[0]);
                        final int takenAmount = Integer.parseInt(amount[0]);
                        itemsTextView.setText((takenAmount + 1) + "/" + productAmount +" groceries");
                    }
                });
                break;

            case("removeTaken"):
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            TextView itemsTextView = (TextView)findViewById(R.id.items);
                            CharSequence items = itemsTextView.getText();
                            String[] amount = items.toString().split("/");
                            final int productAmount = Integer.parseInt(amount[1].split(" ")[0]);
                            final int takenAmount = Integer.parseInt(amount[0]);
                            if(takenAmount > 0)
                                itemsTextView.setText((takenAmount - 1) + "/" + productAmount +" groceries");
                        }
                    });
                break;
            case("updatePrice"):
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TextView priceTextView = (TextView)findViewById(R.id.price);
                        CharSequence price = priceTextView.getText();
                        String[] amount = price.toString().split(": ");
                        final int totalPrice = myDb.getTotalPrice();

                        priceTextView.setText("Total price: " + (totalPrice) +" kr");
                    }
                });
        }

    }
    public int convertDpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float convertedDp = dp * density;
        return (int)convertedDp;
    }

}