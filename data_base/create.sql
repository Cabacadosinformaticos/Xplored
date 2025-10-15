-- Criação da base de dados
CREATE DATABASE IF NOT EXISTS xplored_simplefied
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_general_ci;

USE xplored_simplefied;

-- Desativa FKs temporariamente para recriar as tabelas
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS photos;
DROP TABLE IF EXISTS places;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- USERS
CREATE TABLE users (
  user_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('USER','BUSINESS','ADMIN') NOT NULL DEFAULT 'USER',
  country VARCHAR(56),
  points INT NOT NULL DEFAULT 0,
  profile_photo_url VARCHAR(255),  -- novo campo: foto de perfil
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- CATEGORIES
CREATE TABLE categories (
  category_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  color_hex CHAR(7) NOT NULL, -- Exemplo: #A1B2C3
  icon_name VARCHAR(64),
  PRIMARY KEY (category_id),
  UNIQUE KEY uq_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- PLACES
CREATE TABLE places (
  place_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  address_full VARCHAR(255),
  postal_code VARCHAR(15),
  avg_rating DECIMAL(2,1),
  category_id INT UNSIGNED NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  cover_image_url VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (place_id),
  KEY idx_places_category (category_id),
  KEY idx_places_lat_lng (lat, lng),
  CONSTRAINT fk_places_category
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- PHOTOS
CREATE TABLE photos (
  photo_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  place_id INT UNSIGNED NULL,
  user_id INT UNSIGNED NOT NULL,
  url VARCHAR(255) NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (photo_id),
  KEY idx_photos_place (place_id),
  KEY idx_photos_user (user_id),
  CONSTRAINT fk_photos_place
    FOREIGN KEY (place_id) REFERENCES places(place_id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_photos_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
