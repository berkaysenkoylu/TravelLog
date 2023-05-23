package com.berkaysenkoylu.travellog.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.berkaysenkoylu.travellog.R;
import com.berkaysenkoylu.travellog.database.PlaceDao;
import com.berkaysenkoylu.travellog.database.PlaceDatabase;
import com.berkaysenkoylu.travellog.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.berkaysenkoylu.travellog.databinding.ActivityLocationDetailBinding;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LocationDetailActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityLocationDetailBinding binding;
    private boolean isEditMode = false;
    private double latitude;
    private double longitude;
    private Place selectedLocation;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    PlaceDatabase placeDatabase;
    PlaceDao placeDao;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        registerLauncher();
        latitude = 0.0;
        longitude = 0.0;

        placeDatabase = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = placeDatabase.placeDao();

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("editMode", false);

        binding.locationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Enable the buttons if certain conditions are met
                if (s.toString().trim().length() != 0) {
                    if (latitude != 0.0 && longitude != 0.0) {
                        binding.saveButton.setEnabled(true);
                        binding.editButton.setEnabled(true);
                    }
                } else {
                    binding.saveButton.setEnabled(false);
                    binding.editButton.setEnabled(false);
                }
            }
        });

        if (isEditMode) {
            Place selectedPlace = (Place) intent.getSerializableExtra("selectedLocation");
            if (selectedPlace != null) {
                selectedLocation = selectedPlace;
                latitude = selectedPlace.latitude;
                longitude = selectedPlace.longitude;
                binding.locationText.setText(selectedLocation.name);
            }
            binding.saveButton.setVisibility(View.GONE);
            binding.editButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.editButton.setEnabled(true);
        } else {
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.editButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.saveButton.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        if (isEditMode) {
            // Show the selected location
            LatLng selectedPlacePosition = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(selectedPlacePosition).title(selectedLocation.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlacePosition, 15));
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {}
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.getRoot(), "You need to enable location services!", Snackbar.LENGTH_INDEFINITE).setAction("Grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }).show();
            } else {
                // request permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // We have the permission. We can do location-related operations now.
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

            if (selectedLocation == null) {
                // If we don't have a selectedLocation (we are not in edit mode), then show the lastKnownLocation.
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15));
                }
            }
        }
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    // Permission granted
                    if (ContextCompat.checkSelfPermission(LocationDetailActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()) , 15));
                        }
                    }
                } else {
                    // Permission denied
                    Toast.makeText(LocationDetailActivity.this, "Permission is needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        if (binding.locationText.getText().toString().trim().length() != 0 && (latitude != 0.0 && longitude != 0.0)) {
            binding.saveButton.setEnabled(true);
            binding.editButton.setEnabled(true);
        }
    }

    private void ctaPressedHandler() {
        Intent intent = new Intent(LocationDetailActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onSaveButtonPressedHandler(View view) {
        Place newPlace = new Place(binding.locationText.getText().toString(), latitude, longitude);
        compositeDisposable.add(placeDao.insert(newPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LocationDetailActivity.this::ctaPressedHandler)
        );
    }

    public void onEditButtonPressedHandler(View view) {
        compositeDisposable.add(placeDao.update(selectedLocation.id, binding.locationText.getText().toString(), latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LocationDetailActivity.this::ctaPressedHandler)
        );
    }

    public void onDeleteButtonPressedHandler(View view) {
        compositeDisposable.add(placeDao.delete(selectedLocation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(LocationDetailActivity.this::ctaPressedHandler)
        );
    }
}