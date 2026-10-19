-- Reference schema for a NEW MySQL database; review existing columns before applying.
CREATE TABLE IF NOT EXISTS hospital (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 hosp_code VARCHAR(20) NOT NULL UNIQUE, name VARCHAR(200) NOT NULL,
 org_type VARCHAR(40), phone VARCHAR(100), address VARCHAR(500),
 is_active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS medical_categories (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, category_codes VARCHAR(100), name VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS hospital_category_mapping (
 hospital_id BIGINT NOT NULL, category_id INT NOT NULL,
 PRIMARY KEY (hospital_id, category_id)
);
