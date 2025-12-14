// src/main/java/com/Xplored/Xplored/Model/Category/CategoryRepository.java
package com.Xplored.Xplored.Model.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Handy helper if you ever need it
    Category findByName(String name);
}
