package com.example.sanjeev.newsapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsItem>>, AdapterView.OnItemClickListener {

    private ListView listView;
    private NewsItemAdapter adapter;
    private ArrayList<NewsItem> mNewsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);
        // create an empty list of news items
        mNewsItems = new ArrayList<>();
        // start a new thread in background to fetch data
        if(!isConnected()){
            // Device is not connected
            // Display the error information
            //Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Device Not Connected to Internet")
                    .setTitle("Network Error");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            // else start to fetch the data in background
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(1, null, this);
            // To display details of a news
            // implement onclick for each item in list
            listView.setOnItemClickListener(this);

        }
    }

    // On creation of loader
    // create a http request on guardian server
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {

        Uri baseUri = Uri.parse(Utility.getmURL());
        Uri.Builder uriBuilder = baseUri.buildUpon();
        return new NewsLoader(this, uriBuilder.toString());
    }

    // on receiving the response, Update the UI
    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> data) {
        mNewsItems = new ArrayList<>(data);
        if(mNewsItems.isEmpty()){
            // No news fetched
            // Display Message to user
            // possible reasons are server is down
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No News updates available, please reload the app or check after some time..")
                    .setTitle("Unknown Error");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        UpdateView(mNewsItems);
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        adapter.clear();
    }

    private void UpdateView(ArrayList<NewsItem> newsItems) {
        adapter = new NewsItemAdapter(getApplicationContext(), newsItems);
        listView.setAdapter(adapter);
    }

    // On click over any item in list
    // a webview will be opened with url specific to that item
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent in = new Intent(getApplicationContext(), NewsDetailView.class);
        in.putExtra("URL", mNewsItems.get(position).getmUrl());
        startActivity(in);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.settings){
            Intent in = new Intent(this, SettingsActivity.class);
            startActivity(in);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Checks if device is connected to internet
    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
