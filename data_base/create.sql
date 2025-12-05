DROP DATABASE IF EXISTS xplored_db;

CREATE DATABASE xplored_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE xplored_db;

-- 1. USERS
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL, 
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    about TEXT,
    country VARCHAR(80),
    role VARCHAR(20) DEFAULT 'USER',
    profile_photo VARCHAR(255),
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CATEGORIES
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    color_hex VARCHAR(20),
    icon_name VARCHAR(50)
);

-- 3. PLACES
CREATE TABLE places (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    lat DECIMAL(9,6) NOT NULL,
    lng DECIMAL(9,6) NOT NULL,
    address_full VARCHAR(255),
    category_id BIGINT UNSIGNED NOT NULL,
    author_id BIGINT UNSIGNED,
    avg_rating DECIMAL(3,2) DEFAULT 0.0,
    is_verified BOOLEAN DEFAULT FALSE,
    -- Moderation: PENDING (new place by a user), APPROVED, REJECTED
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_places_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_places_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. REVIEWS
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(100),
    comment TEXT,
    user_id BIGINT UNSIGNED NOT NULL,
    place_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);

-- 5. PHOTOS
CREATE TABLE photos (
    id SERIAL PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    place_id BIGINT UNSIGNED,
    user_id BIGINT UNSIGNED,
    review_id BIGINT UNSIGNED,
    kind VARCHAR(20) DEFAULT 'GALLERY',
    status VARCHAR(20) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_photos_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE,
    CONSTRAINT fk_photos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_photos_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- 6. REACTIONS
CREATE TABLE reactions (
    id SERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    review_id BIGINT UNSIGNED NOT NULL,
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    UNIQUE (user_id, review_id)
);

-- 7. PEDIPAPERS (Gamification)
CREATE TABLE pedipapers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    total_points INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE route_stops (
    id SERIAL PRIMARY KEY,
    pedipaper_id BIGINT UNSIGNED NOT NULL,
    place_id BIGINT UNSIGNED NOT NULL,
    stop_order INT NOT NULL,
    task_description TEXT,
    requires_photo BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_stops_pedi FOREIGN KEY (pedipaper_id) REFERENCES pedipapers(id) ON DELETE CASCADE,
    CONSTRAINT fk_stops_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);


CREATE TABLE route_participations (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    pedipaper_id BIGINT UNSIGNED NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    points_awarded INT DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    UNIQUE (user_id, pedipaper_id),
    CONSTRAINT fk_part_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_part_pedi FOREIGN KEY (pedipaper_id) REFERENCES pedipapers(id) ON DELETE CASCADE
);

-- 8. COUPONS & BUSINESS
CREATE TABLE business_accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coupons (
    id SERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    cost_points INT NOT NULL,
    business_account_id BIGINT UNSIGNED NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    code VARCHAR(50),
    CONSTRAINT fk_coupons_biz FOREIGN KEY (business_account_id) REFERENCES business_accounts(id) ON DELETE CASCADE
);

CREATE TABLE redemptions (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    coupon_id BIGINT UNSIGNED NOT NULL,
    redeemed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_redem_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_redem_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);

CREATE TABLE points_ledger (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    amount INT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. PASSWORD RESET
CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 10. PEDIPAPER STOP CHECK-INS
CREATE TABLE route_stop_checkins (
    id SERIAL PRIMARY KEY,
    participation_id BIGINT UNSIGNED NOT NULL,
    route_stop_id BIGINT UNSIGNED NOT NULL,
    lat DECIMAL(9,6) NOT NULL,
    lng DECIMAL(9,6) NOT NULL,
    photo_id BIGINT UNSIGNED NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (participation_id, route_stop_id),
    CONSTRAINT fk_checkin_part FOREIGN KEY (participation_id) REFERENCES route_participations(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_stop FOREIGN KEY (route_stop_id) REFERENCES route_stops(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_photo FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE SET NULL
);

-- 11. FAVORITES
CREATE TABLE favorites (
    user_id BIGINT UNSIGNED NOT NULL,
    place_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, place_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);

-- 12. VISITS (GPS check-in at a place, once per user and place)
CREATE TABLE visits (
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    place_id BIGINT UNSIGNED NOT NULL,
    lat DECIMAL(9,6) NOT NULL,
    lng DECIMAL(9,6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, place_id),
    CONSTRAINT fk_visit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);

INSERT INTO categories (id, name, color_hex, icon_name) VALUES 
(1, 'Atividades', '#4192FF', 'hiking'),
(2, 'Lojas', '#FFEB3B', 'shopping_cart'),
(3, 'Restauração', '#E65100', 'restaurant'),
(4, 'Históricos', '#880E4F', 'account_balance'),
(5, 'Paisagens', '#3D6E44', 'landscape');