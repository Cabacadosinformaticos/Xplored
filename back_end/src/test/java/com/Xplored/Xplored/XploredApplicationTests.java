package com.Xplored.Xplored;

import com.Xplored.Xplored.Model.Category.Category;
import com.Xplored.Xplored.Model.Category.CategoryDao;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceDao;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class XploredApplicationTests {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PlaceDao placeDao;
    @Autowired
    private CategoryDao categoryDao;


    //@Test
	void addUserTest() {
        User user = new User();
        user.setName("Muhammad");
        user.setEmail("muhammad@iade.pt");
        user.setPasswordHash("airbusA380");
        userDao.save(user);
	}

    //@Test
    void getAllUsersAndDeleteThem(){
        List<User> users = userDao.getAllusers();
        for (User user : users) {
            userDao.delete(user);
        }
    }

    //@Test
    void addPlaceTest(){
        Place place = new Place();
        place.setName("Iade");
        place.setDescription("QueroMorrer");
        place.setLat(69.69);
        place.setLng(69.69);
        place.setAddressFull("nocaralho");
        place.setCategoryId(1L);
        place.setPostalCode("12345");
        place.setCoverImageUrl("chupame");
        placeDao.save(place);


    }

    //@Test
    void getAllPlacesAndDeleteThem(){
        List<Place> places = placeDao.getAllPlaces();
        for (Place place : places) {
            placeDao.delete(place);
        }
    }

    @Test
    void addCategoryTest(){
        Category category = new Category();
        category.setName("Paisagens");
        category.setColorHex("3D6E44FF");
        category.setIconName("Icons.Outlined.Landscape");
        categoryDao.save(category);
    }
}
