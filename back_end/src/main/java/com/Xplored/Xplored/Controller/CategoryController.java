package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Category.Category;
import com.Xplored.Xplored.Model.Category.CategoryDao;
import com.Xplored.Xplored.Model.Category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/get-all")
    public List<Category> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (categoryRepository.findByName(category.getName()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Category name already exists"));
        }
        // Never overwrite an existing row through a supplied id.
        if (category.getCategoryId() != null && categoryRepository.existsById(category.getCategoryId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Category id already exists"));
        }
        category.setCategoryId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryDao.save(category));
    }
}
