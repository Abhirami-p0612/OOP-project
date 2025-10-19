package com.project.lostandfound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.Optional; // Added for Optional return type in findById

@Repository
public class FoundItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(FoundItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:src/main/resources/static/images/found/}")
    private String uploadDir;

    // --- UPDATED save method to accept new contact fields ---
    public int save(String itemName, String description, String location, String contactName, String contactPhone, String contactEmail, String dateFound, MultipartFile image) {

        String imagePathForDb = null;
        // ... (Image saving logic remains the same) ...
        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String imageName = UUID.randomUUID().toString() + "_" + originalFilename;

            Path uploadPath;
            try {
                uploadPath = Paths.get(resourceLoader.getResource("classpath:static/images/found/").getURI());
            } catch (IOException e) {
                logger.error("Failed to get upload directory path", e);
                return 0;
            }

            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(imageName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                imagePathForDb = "/images/found/" + imageName;
            } catch (IOException e) {
                logger.error("Failed to save uploaded file", e);
                return 0;
            }
        }

        String sql = "INSERT INTO found_items (item_name, description, location, contact_name, contact_phone, contact_email, date_found, image_path, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')"; // Set initial status
        try {
            // UPDATED Parameters
            return jdbcTemplate.update(sql, itemName, description, location, contactName, contactPhone, contactEmail, dateFound, imagePathForDb);
        } catch (Exception e) {
            logger.error("Failed to insert found item into DB", e);
            return 0;
        }
    }

    public List<FoundItem> findAll() {
        // Note: The fields in SELECT must match the case/style of the fields in FoundItem.java (e.g., item_name AS itemName)
        String sql = "SELECT id, item_name, description, location, contact_name, contact_phone, contact_email, status, date_found as dateFound, image_path as imagePath FROM found_items WHERE status != 'DELETED'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FoundItem.class));
    }

    public Optional<FoundItem> findById(int id) { // Changed return type to Optional
        String sql = "SELECT id, item_name, description, location, contact_name, contact_phone, contact_email, status, date_found as dateFound, image_path as imagePath FROM found_items WHERE id = ?";
        List<FoundItem> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FoundItem.class), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public FoundItem findSimilar(String itemName, String description) {
        // Updated SELECT query to include new contact fields and status
        String baseSql = "SELECT id, item_name, description, location, contact_name, contact_phone, contact_email, status, date_found as dateFound, image_path as imagePath FROM found_items WHERE status != 'DELETED' AND ";

        // First try exact name (case-insensitive), then partial description match
        String sqlName = baseSql + "LOWER(item_name) = LOWER(?) LIMIT 1";
        List<FoundItem> byName = jdbcTemplate.query(sqlName, new BeanPropertyRowMapper<>(FoundItem.class), itemName == null ? "" : itemName);
        if (!byName.isEmpty()) return byName.get(0);

        String sqlDesc = baseSql + "LOWER(description) LIKE LOWER(?) LIMIT 1";
        List<FoundItem> byDesc = jdbcTemplate.query(sqlDesc, new BeanPropertyRowMapper<>(FoundItem.class), "%" + (description == null ? "" : description) + "%");
        return byDesc.isEmpty() ? null : byDesc.get(0);
    }
    public List<FoundItem> findByReporterAndStatus(String email, String status) {
        String sql = "SELECT * FROM found_items WHERE contact_email = ? AND status = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FoundItem.class), email, status);
    }



    // --- NEW: Method to update item status ---
    // 1. Method to update item status (for step 1: Lost Person confirms receipt)
    public int updateStatus(int id, String newStatus) {
        String sql = "UPDATE found_items SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newStatus, id);
    }

    // 2. Method to delete item (for step 2: Reporter confirms deletion)
    public int deleteById(int id) {
        String sql = "DELETE FROM found_items WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
