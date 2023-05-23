package com.berkaysenkoylu.travellog.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

import com.berkaysenkoylu.travellog.model.Place;

import java.util.List;

@Dao
public interface PlaceDao {
    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getAll();

    @Query("SELECT * FROM Place WHERE id IN (:placeId)")
    Flowable<Place> getPlaceWithId(int placeId);

    @Query("UPDATE Place SET name = (:placeName), latitude = (:lat), longitude = (:log) WHERE id = (:id)")
    Completable update(int id, String placeName, double lat, double log);

    @Insert
    Completable insert(Place place);

    @Delete
    Completable delete(Place place);
}
