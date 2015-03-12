package com.jiraiyeah.androidplacesearch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class PlaceSearchActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  private GoogleMap mGoogleMap;
  private EditText mSearchPlate;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_place_search);

    SupportMapFragment fragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mGoogleMap = fragment.getMap();

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent){
    if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
      doSearch(intent.getStringExtra(SearchManager.QUERY));
    } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
      getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
    }
  }

  private void doSearch(String query){
    Bundle data = new Bundle();
    data.putString("query", query);
    getSupportLoaderManager().restartLoader(0, data, this);
  }

  private void getPlace(String query){
    Bundle data = new Bundle();
    data.putString("query", query);
    getSupportLoaderManager().restartLoader(1, data, this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_place_search, menu);

    // Get the SearchView and set the searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

    // Hack to get rid of the search hint icon
    mSearchPlate =
        (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
    mSearchPlate.setHint("");

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
    CursorLoader cLoader = null;
    if(arg0==0)
      cLoader = new CursorLoader(getBaseContext(), PlaceSearchProvider.SEARCH_URI, null, null,
          new String[]{ query.getString("query") }, null);
    else if(arg0==1)
      cLoader = new CursorLoader(getBaseContext(), PlaceSearchProvider.DETAILS_URI, null, null,
          new String[]{ query.getString("query") }, null);
    return cLoader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
    showLocations(c);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
  }

  private void showLocations(Cursor c){
    MarkerOptions markerOptions = null;
    LatLng position = null;
    mGoogleMap.clear();
    while(c.moveToNext()){
      markerOptions = new MarkerOptions();
      position = new LatLng(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
      markerOptions.position(position);
      markerOptions.title(c.getString(0));
      mGoogleMap.addMarker(markerOptions);
      if (mSearchPlate != null) {
        mSearchPlate.setText(c.getString(0));
      }
    }
    if(position!=null){
      mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }
  }
}

