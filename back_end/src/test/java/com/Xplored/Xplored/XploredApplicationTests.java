package com.Xplored.Xplored;

import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserDao;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceDao;
import com.Xplored.Xplored.Model.Category.Category;
import com.Xplored.Xplored.Model.Category.CategoryDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XploredApplicationTests {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PlaceDao placeDao;

    @Autowired
    private CategoryDao categoryDao;

    // =========================
    // USER TESTS
    // =========================

    @Test
    void addUserWithAllFieldsTest() {
        String email = "fulluser_test@xplored.pt";
        String password = "FullUserPassword123";

        userDao.findByEmailAndPasswordHash(email, password)
                .ifPresent(userDao::delete);

        User user = new User();
        user.setName("Full Test User");
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole("BUSINESS");
        user.setCountry("Portugal");
        user.setPoints(500);
        user.setProfilePhotoUrl("https://example.com/profile/fulluser.png");

        userDao.save(user);
    }

    @Test
    void addUserWithMinimumFieldsTest() {
        String email = "minimaluser_test@xplored.pt";
        String password = "MinimalUserPassword123";

        userDao.findByEmailAndPasswordHash(email, password)
                .ifPresent(userDao::delete);

        User user = new User();
        user.setName("Minimal Test User");
        user.setEmail(email);
        user.setPasswordHash(password);

        user.setCountry(null);
        user.setProfilePhotoUrl(null);
        // role = "USER" and points = 0 come from defaults

        userDao.save(user);
    }

    // =========================
    // HELPER – create a category
    // =========================

    /**
     * Creates a fresh category row and returns its ID.
     * This avoids relying on "category_id = 1" existing.
     */
    private Long createTestCategoryAndReturnId() {
        Category category = new Category();
        category.setName("Test Category " + System.currentTimeMillis());
        // V5 schema: CHAR(7), e.g. "#A1B2C3"
        category.setColorHex("#3D6E44");
        category.setIconName("Icons.Outlined.Landscape");

        Category saved = categoryDao.save(category);
        // Assuming your Category entity has getCategoryId() or similar:
        return saved.getCategoryId();
    }

    // =========================
    // PLACE TESTS
    // =========================

    /**
     * Full place: fills all reasonable columns.
     * Uses your campus coordinates: 38.779639, -9.102597.
     */
    @Test
    void addPlaceWithAllFieldsTest() {
        Long categoryId = createTestCategoryAndReturnId();

        Place place = new Place();
        place.setName("Campus Xplored Spot Full");
        place.setDescription("Full test place near campus, with all fields filled.");
        place.setLat(38.779639);
        place.setLng(-9.102597);
        place.setAddressFull("Campus area, Lisbon");
        place.setPostalCode("1499-003");
        place.setAvgRating(4.5);
        place.setCategoryId(categoryId);
        place.setStatus("APPROVED");
        place.setCoverImageUrl("https://example.com/places/campus-full.jpg");

        placeDao.save(place);
    }

    /**
     * Minimal place: only required columns.
     */
    @Test
    void addPlaceWithMinimumFieldsTest() {
        Long categoryId = createTestCategoryAndReturnId();

        Place place = new Place();
        place.setName("Campus Xplored Spot Minimal");
        place.setLat(38.779700);
        place.setLng(-9.102500);
        place.setCategoryId(categoryId);
        place.setStatus("PENDING");

        // description, addressFull, postalCode, avgRating, coverImageUrl left null

        placeDao.save(place);
    }
}
