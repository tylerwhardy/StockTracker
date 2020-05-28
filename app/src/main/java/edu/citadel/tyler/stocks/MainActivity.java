package edu.citadel.tyler.stocks;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import java.net.*;
import java.io.*;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText editStock;
        editStock = (EditText) findViewById(R.id.editStock);
        editStock.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    DownloadStockTask stockTask = new DownloadStockTask();
                    String pass = editStock.getText().toString();
                    stockTask.execute(pass);
                    return true;
                }
                return false;
            }
        });
    }
    private class DownloadStockTask extends AsyncTask<String, Void, Stock>{
        protected Stock doInBackground(String... symbols) {
            String symbol = symbols[0];
            Stock StockApp = new Stock(symbol);
            try {
                StockApp.load();
                StockApp.getSymbol();
                StockApp.getName();
                StockApp.getLastTradePrice();
                StockApp.getLastTradeTime();
                StockApp.getChange();
                StockApp.getRange();
            } catch (IOException e) {}
            return StockApp;
        }
        protected void onPostExecute(Stock stock){
            final TextView textSymbol = (TextView) findViewById(R.id.textSymbol2);
            final TextView textName = (TextView) findViewById(R.id.textName2);
            final TextView textLastPrice = (TextView) findViewById(R.id.textLastPrice2);
            final TextView textLastTime = (TextView) findViewById(R.id.textLastTime2);
            final TextView textChange = (TextView) findViewById(R.id.textChange2);
            final TextView textRange = (TextView) findViewById(R.id.textRange2);
            try{if (stock.getName().equalsIgnoreCase("/")){
                textSymbol.setText(" ");
                textName.setText(" ");
                textLastPrice.setText(" ");
                textLastTime.setText(" ");
                textChange.setText(" ");
                textRange.setText(" ");
            } else {
                textSymbol.setText(" " + stock.getSymbol());
                textName.setText(" " + stock.getName());
                textLastPrice.setText(" " + stock.getLastTradePrice());
                textLastTime.setText(" " + stock.getLastTradeTime());
                textChange.setText(" " + stock.getChange());
                textRange.setText(" " + stock.getRange());
            }} catch(NullPointerException e){}
        }
    }
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        final EditText editStock;
        editStock = (EditText) findViewById(R.id.editStock);
        String pass = editStock.getText().toString();
        outState.putString("save", pass);       // call appropriate outState "put" methods
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
        String pass = inState.getString("save");
        DownloadStockTask stockTask = new DownloadStockTask();
        stockTask.execute(pass);
    }
    public class Stock implements Serializable{
        private static final long serialVersionUID = 46827221313596640L;
        private static final boolean DEBUG = true;
        private static final String QUOTE_FORMAT = "&f=lcwn";
        // format for symbols: last trade (with time), change & percent change,
        // 52-week range, name
        private static final String TAG_PREFIX = "Stock";
        private String symbol;
        private String lastTradeTime;
        private String lastTradePrice;
        private String change;
        private String range;
        private String name;
        public Stock(String symbol){
            this.symbol = symbol.toUpperCase();
            if (DEBUG)
                Log.i(TAG_PREFIX + "Stock()", "symbol = " + symbol);
        }
        public void load() throws MalformedURLException, IOException {
            URL url = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol
                    + QUOTE_FORMAT);
            try {
                if (DEBUG)
                    Log.i(TAG_PREFIX + "Stock.load()", "url = " + url);
                URLConnection connection = url.openConnection();
                BufferedReader in = new
                        BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = in.readLine();
                if (DEBUG)
                    Log.i(TAG_PREFIX + "Stock.load()", "line = " + line);
                // consume any data remaining in the input stream
                while (in.readLine() != null)
                    ;
                in.close();
                if (line != null && line.length() > 0) {
                    // parse the line and remove quotes where necessary
                    String[] values = line.split(",");
                    change = values[1].substring(1, values[1].length() - 1);
                    range = values[2].substring(1, values[2].length() - 1);
                    name = values[3].substring(1, values[3].length() - 1);
                    // Since real names can have commas, handle possible rest of name.
                    for (int i = 4; i < values.length; ++i)
                        name = name + ", " + values[i].substring(1, values[i].length() - 1);
                    if (DEBUG)
                        Log.i(TAG_PREFIX + "Stock.load()", "name = " + name);
                    String lastTrade = values[0];
                    // parse last trade time
                    int start = 1; // skip opening quote
                    int end = lastTrade.indexOf(" - ");
                    lastTradeTime = lastTrade.substring(start, end);
                    // parse last trade price
                    start = lastTrade.indexOf(">") + 1;
                    end = lastTrade.indexOf("<", start);
                    lastTradePrice = lastTrade.substring(start, end);
                }
            } catch (IOException e) {

            } catch (StringIndexOutOfBoundsException e){
            }
        }
        /**
         * Returns the stock's last trade time.
         */
        public String getLastTradeTime()
        {
            return lastTradeTime;
        }


        /**
         * Returns the stock's last trade price.
         */
        public String getLastTradePrice()
        {
            return lastTradePrice;
        }
        /**
         * Returns the stock's .
         */
        public String getChange()
        {
            return change;
        }
        /**
         * Returns the stock's 52-week range.
         */
        public String getRange()
        {
            return range;
        }
        /**
         * Returns the stock's name; e.g., Google, Inc.
         */
        public String getName()
        {
            return name;
        }
        /**
         * Returns the stock's symbol; e.g., GOOG.
         */
        public String getSymbol()
        {
            return symbol;
        }
    }
}

