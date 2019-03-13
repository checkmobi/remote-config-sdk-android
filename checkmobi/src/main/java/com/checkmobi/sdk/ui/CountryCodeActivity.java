package com.checkmobi.sdk.ui;

import com.checkmobi.sdk.model.CountryCode;
import com.checkmobi.sdk.R;
import com.checkmobi.sdk.network.RetrofitController;
import com.checkmobi.sdk.storage.StorageController;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CountryCodeActivity extends CheckmobiBaseActivity {
    
    public final static String SET_COUNTRY_CODE = "SET_COUNTRY_CODE";
    public final static String ADD_DEFAULT = "ADD_DEFAULT";
    public final static String COUNTRY_NAME = "COUNTRY_NAME";
    
    private SearchView searchView;
    private RecyclerView mCountryCodes;
    private CountryCodeAdapter mCountryCodeAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setViews();
    
        Call<List<CountryCode>> repos = RetrofitController.getInstance().getService().getCountries();
        repos.enqueue(new Callback<List<CountryCode>>() {
            @Override
            public void onResponse(Call<List<CountryCode>> call, Response<List<CountryCode>> response) {
                System.out.print(response);
                setData(response.body());
            }
        
            @Override
            public void onFailure(Call<List<CountryCode>> call, Throwable t) {
                System.out.print(t.toString());
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        
        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new CountryCodeOnQueryTextListener());
        searchView.requestFocus();
        
        return true;
    }
    
    private void setViews() {
        mCountryCodes = RecyclerView.class.cast(findViewById(R.id.rv_country_codes));
        mCountryCodes.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setData(List<CountryCode> countryCodes) {
        List<CountryCode> nonEmptyCountryCodes = removeEmptyCountryCodes(countryCodes);
        mCountryCodeAdapter = new CountryCodeAdapter(nonEmptyCountryCodes, new MyCountryCodeListener());
        mCountryCodes.setAdapter(mCountryCodeAdapter);
    }
    
    private List<CountryCode> removeEmptyCountryCodes(List<CountryCode> countryCodes) {
        List<CountryCode> nonEmptyCountryCodes = new ArrayList<>();
        for (CountryCode countryCode : countryCodes) {
            if (countryCode.getName() != null && countryCode.getPrefix() != null) {
                nonEmptyCountryCodes.add(countryCode);
            }
        }
        return nonEmptyCountryCodes;
    }
    
    private class CountryCodeOnQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
        
        @Override
        public boolean onQueryTextChange(String newText) {
            if (mCountryCodeAdapter != null) {
                mCountryCodeAdapter.getFilter().filter(newText);
                return true;
            }
            return false;
        }
    }
    
    private class MyCountryCodeListener implements CountryCodeAdapter.CountryCodeListener {
        @Override
        public void onItemSelected(CountryCode countryCode) {
            Intent intent = CountryCodeActivity.this.getIntent();
            if (intent != null && intent.getBooleanExtra(SET_COUNTRY_CODE, false)) {
                StorageController.getInstance().saveCountryCode(CountryCodeActivity.this, countryCode);
            }
            Intent countryIntent = new Intent();
            countryIntent.putExtra(COUNTRY_NAME, countryCode.getName());
            CountryCodeActivity.this.setResult(RESULT_OK, countryIntent);
            CountryCodeActivity.this.finish();
        }
    }
}
