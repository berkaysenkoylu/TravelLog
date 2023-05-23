package com.berkaysenkoylu.travellog.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.berkaysenkoylu.travellog.R;
import com.berkaysenkoylu.travellog.adapter.LocationListAdapter;
import com.berkaysenkoylu.travellog.database.PlaceDao;
import com.berkaysenkoylu.travellog.database.PlaceDatabase;
import com.berkaysenkoylu.travellog.databinding.ActivityMainBinding;
import com.berkaysenkoylu.travellog.model.Place;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    PlaceDatabase placeDatabase;
    PlaceDao placeDao;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.getRoot();
        setSupportActionBar(toolbar);

        placeDatabase = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = placeDatabase.placeDao();

        mDisposable.add(placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MainActivity.this::handleResponse)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_location) {
            Intent intent = new Intent(MainActivity.this, LocationDetailActivity.class);
            intent.putExtra("editMode", false);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleResponse(List<Place> placeList) {
        binding.locationList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        LocationListAdapter locationListAdapter = new LocationListAdapter(placeList);
        binding.locationList.setAdapter(locationListAdapter);
    }
}