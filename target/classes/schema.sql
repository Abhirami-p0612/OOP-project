CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE lost_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    contact_info VARCHAR(255),
    date_lost DATE,
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE found_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(255),          -- NEW: item_name added for found items
    description TEXT,
    location VARCHAR(255),
    contact_info VARCHAR(255),
    date_found TIMESTAMP,
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
