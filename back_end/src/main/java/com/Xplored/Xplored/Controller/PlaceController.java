package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceDao;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places") // ✅ Base path now matches the Android API call
@CrossOrigin(origins = "*") // ✅ Allow Android app access (important)
public class PlaceController {

    @Autowired
    private PlaceRepository placeRepository;

    @PostMapping
    public Place createPlace(@RequestBody Place place) {
        return placeRepository.save(place);
    }

    @GetMapping
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }
}
