package com.Xplored.Xplored.Model.Category;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryDao {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Category category) {categoryRepository.delete(category);}

    public List<Category> getAllCategories(){
        List<Category> categories = new ArrayList<>();
        Streamable.of(categoryRepository.findAll())
                .forEach(categories::add);
        return categories;
    }


}
