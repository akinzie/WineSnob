package com.britt.winesnob.winesnob;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar;
import com.sap.cloud.mobile.foundation.authentication.BasicAuthDialogAuthenticator;
import com.sap.cloud.mobile.foundation.logging.Logging;
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.foundation.securestore.CreateDatabaseCallback;
import com.sap.cloud.mobile.foundation.securestore.OpenFailureException;
import com.sap.cloud.mobile.foundation.securestore.SecureDatabaseResultSet;
import com.sap.cloud.mobile.foundation.securestore.SecureDatabaseStore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import ch.qos.logback.classic.Level;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    static Logger log = LoggerFactory.getLogger("com.my.package");

    //TextView currStatus;
    TextView timeInterval;
    FioriProgressBar progressBar;
    RecyclerView storeData;
    FloatingActionButton fab;

    private static String appID;
    private static String connID;
    private static String serviceURL;
    private static String appVersion;
    private static String deviceID;
    private static String myTag = "WINESNOB";
    private OkHttpClient myBasicAuthOkHttpClient;

    private long startTime;
    private long endTime;
    int totalRecords = 0;

    private static SecureDatabaseStore localDB;

    // database stuff
    private static final String DATABASE_NAME = "myDatabase";
    private static final int DATABASE_VERSION = 1; // Increment the version number when there are schema changes
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS stores (id INTEGER UNIQUE PRIMARY KEY, name TEXT, add1 TEXT, add2 TEXT, city TEXT, postal_code TEXT, telephone TEXT)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logging.initialize(this, new Logging.ConfigurationBuilder().initialLevel(Level.DEBUG).logToConsole(true).build());
        setContentView(R.layout.activity_main);

        // Handle for recycler view
        storeData = findViewById(R.id.storeList);

        // Need textview for time interval
        timeInterval = findViewById(R.id.timeInterval);

        // Get Progress bar
        progressBar = findViewById(R.id.progressBar);

        // Get the Floating Action Button
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Refresh from the backend if the fab is tapped
                GetStores();
            }
        });

        //Create or open the local db
        CreateOrOpenStore();

        // Basic Connection info
        serviceURL = getResources().getString(R.string.SERVICE_URL);
        appID = getResources().getString(R.string.APPLICATION_ID);
        connID = getResources().getString(R.string.CONNECTION_ID);
        appVersion = getResources().getString(R.string.APPLICATION_VERSION);

        deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        log.debug("In onRegister");

        try {
            myBasicAuthOkHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AppHeadersInterceptor(appID, deviceID, appVersion))
                    .authenticator(new BasicAuthDialogAuthenticator())
                    .cookieJar(new WebkitCookieJar())
                    .build();

            if (totalRecords == 0) {
                GetStores();
            }

        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage(), e);
        }

    }

    public void CreateOrOpenStore() {
        try {

            localDB = new SecureDatabaseStore(this,
                DATABASE_NAME,
                DATABASE_VERSION,
                new CreateDatabaseCallback() {

                    @Override
                    public void onCreate(SecureDatabaseStore store) {
                        store.executeUpdate(CREATE_TABLE);
                        log.debug("Database Succesfully Created");
                    }

                    @Override
                    public void onUpgrade(SecureDatabaseStore store, int oldVersion, int newVersion) {

                    }
                });


            if (!localDB.isOpen()) {
                // Use the auto-generated encryption key by passing null.
                localDB.open(null);
            }

            SecureDatabaseResultSet rs = localDB.executeQuery("select count(*) from stores");

            if (rs.next()) {
                totalRecords = rs.getInt(0);

                rs.close();

                if (totalRecords > 0) {
                    // We have local records already, load locally
                    // Populate the recycler view
                    ReadOfflineStores();
                }

                log.debug("There are " + totalRecords + " records in the stores table");
            }


            log.debug("Database opened");
        } catch (OpenFailureException e) {
            log.error(e.getMessage(), e);
            // Some recovery here. For example, re-get the encryption key from the end user via UI...
        }
    }

    public void InitializeStore(View view) {

        // Create Store
        CreateOrOpenStore();

    }

    public void ReadOfflineStores() {

        String storesQuery = "select id, name, add1, add2, city, postal_code, telephone from stores";

        try {

            startTime = System.currentTimeMillis();

            SecureDatabaseResultSet rs = localDB.executeQuery(storesQuery);

            ArrayList<Store> stores = new ArrayList<>();

            while (rs.next()) {

                Store currStore = new Store(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("add1"),
                        rs.getString("add2"),
                        rs.getString("city"),
                        rs.getString("postal_code"),
                        rs.getString("telephone")
                );

                stores.add(currStore);

                String name = rs.getString("name");
            }

            rs.close();

            endTime = System.currentTimeMillis();

            long ellapsedTime = endTime - startTime;

            // Show the amount of time it takes.
            timeInterval.setText("Time taken: " + ellapsedTime + " milliseconds");

            showStores(stores);


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    private class getAsyncInfo extends AsyncTask<String, Integer, ArrayList<Store>> {

        @Override
        protected ArrayList<Store> doInBackground(String... Strings) {

            // Start time for action
            startTime = System.currentTimeMillis();

            int progress = 0;

            ArrayList<Store> sReturn = new ArrayList<>();

            boolean lastPage = false;
            int pageNum = 1;

            try {

                while (!lastPage) {

                    Request request = new Request.Builder()
                            .get()
                            .url(serviceURL + connID + Strings[0] + "&page=" + pageNum)
                            .build();

                    Response response = myBasicAuthOkHttpClient.newCall(request).execute();

                    JSONObject jsonresponse = new JSONObject(response.body().string());

                    // Is this the last page?
                    JSONObject pager = jsonresponse.getJSONObject("pager");
                    lastPage = pager.getBoolean("is_final_page");
                    totalRecords = pager.getInt("total_record_count");

                    // Let's get the data!
                    JSONArray jsonDataArray = jsonresponse.getJSONArray("result");

                    int arrayLength =  jsonDataArray.length();

                    // for the progress bar
                    progress += arrayLength;

                    publishProgress(progress);

                    // Iterate through the result set, 100 at a time....
                    for (int x = 0; x < arrayLength; x++) {

                        JSONObject jsonData = jsonDataArray.getJSONObject(x);

                        Store currStore = new Store(
                                jsonData.getInt("id"),
                                jsonData.getString("name"),
                                jsonData.getString("address_line_1"),
                                jsonData.getString("address_line_2"),
                                jsonData.getString("city"),
                                jsonData.getString("postal_code"),
                                jsonData.getString("telephone")
                        );
                        sReturn.add(currStore);
                    }

                    if (!lastPage) {
                        pageNum++;
                    } else {
                        log.debug("FINALLY REACHED LAST PAGE");
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return sReturn;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setIndeterminate(false);

            double myProgress = (values[0].intValue()/(double)totalRecords) * 100;

            progressBar.setProgress((int)myProgress);

            log.debug("Fetched: " + values[0] + " Records");

        }

        @Override
        protected void onPostExecute(ArrayList<Store> Stores) {
            super.onPostExecute(Stores);

            localDB.beginExclusiveTransaction();

            try {
                for (Store entry : Stores) {

                    ContentValues content = new ContentValues();

                    content.put("id", entry.id);
                    content.put("name", entry.name);
                    content.put("add1", entry.add1);
                    content.put("add2", entry.add2);
                    content.put("city", entry.city);
                    content.put("postal_code", entry.postal_code);
                    content.put("telephone", entry.telephone);

                    localDB.executeInsert("stores", content);
                }

                localDB.commit();

                endTime = System.currentTimeMillis();

                long ellapsedTime = endTime - startTime;

                log.debug("Successfully inserted " + Stores.size() + " records");

                // Show the amount of time it takes.
                timeInterval.setText("Time taken: " + ellapsedTime + " milliseconds");

                // Populate the recyclerView
                showStores(Stores);

            } catch (Exception e) {
                localDB.rollback();
                log.error(e.getMessage(), e);

            }
            // show the store list, hide the progress bar
            storeData.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

        }
    }


    public void showStores (ArrayList<Store> stores) {
        ObjectCellStoreAdapter storeAdapter = new ObjectCellStoreAdapter(this, stores);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        storeData.setAdapter(storeAdapter);

        storeData.setLayoutManager(mLayoutManager);
    }

    public void GetStores() {
        // hide the store list, show the progress bar
        storeData.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        String sQuery = "/stores?access_key=MDo4MTgwMjhjZS05OTk3LTExZTgtODg0ZS04NzVhNTU1MmYyNjA6ZHg3UWpKUUxwTTVWamRxZ0E4bmFIWEVGZjVrT0ozMktLU0Qw&per_page=100";

        try {
            new getAsyncInfo().execute(sQuery);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
