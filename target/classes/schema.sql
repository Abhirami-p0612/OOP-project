CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    notification_message VARCHAR(255)
);

CREATE TABLE lost_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),

    -- Contact details similar to found_items
    contact_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(10),
    contact_email VARCHAR(255) NOT NULL,

    -- Status for confirmation flow (PENDING, AWAITING_DELETION, DELETED)
    status VARCHAR(50) DEFAULT 'PENDING',

    date_lost DATE,
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE found_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(255),
    description TEXT,
    location VARCHAR(255),

    -- ADDED: New Contact Details
    contact_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(10),
    contact_email VARCHAR(255) NOT NULL,

    -- ADDED: Status for confirmation flow (PENDING, AWAITING_DELETION, DELETED)
    status VARCHAR(50) DEFAULT 'PENDING',

    date_found DATE,
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
