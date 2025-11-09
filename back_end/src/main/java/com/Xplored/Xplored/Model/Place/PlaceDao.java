package com.Xplored.Xplored.Model.Place;


import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaceDao {

    @Autowired
    private PlaceRepository placeRepository;

    public Place save(Place place) {
        return placeRepository.save(place);
    }

    public void delete(Place place) {placeRepository.delete(place);}

    public List<Place> getAllPlaces() {
        List<Place> places = new ArrayList<>();
        Streamable.of(placeRepository.findAll())
                .forEach(places::add);
        return places;
    }
}
