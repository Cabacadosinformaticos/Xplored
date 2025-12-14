package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Category.Category;
import com.Xplored.Xplored.Model.Category.CategoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryDao categoryDao;

    @GetMapping("/get-all")
    public List<Category> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    @PostMapping("/save")
    public Category saveCategory(@RequestBody Category category) {
        return categoryDao.save(category);
    }
}
