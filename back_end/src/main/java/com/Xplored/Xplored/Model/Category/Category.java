// src/main/java/com/Xplored/Xplored/Model/Category/Category.java
package com.Xplored.Xplored.Model.Category;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // CHAR(11) in DB – we just keep it as String
    @Column(name = "color_hex", nullable = false, length = 11)
    private String colorHex;

    @Column(name = "icon_name", length = 64)
    private String iconName;

    public Category() {
    }

    public Category(String name, String colorHex, String iconName) {
        this.name = name;
        this.colorHex = colorHex;
        this.iconName = iconName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
