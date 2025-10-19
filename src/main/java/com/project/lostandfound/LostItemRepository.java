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
import java.util.Optional;
import java.util.UUID;

@Repository
public class LostItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(LostItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:src/main/resources/static/images/lost/}")
    private String uploadDir;

    // --- Save lost item ---
    public int save(String itemName, String description, String location,
                    String contactName, String contactPhone, String contactEmail,
                    String dateLost, MultipartFile image) {

        String imagePathForDb = null;

        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String imageName = UUID.randomUUID() + "_" + originalFilename;

            Path uploadPath;
            try {
                uploadPath = Paths.get(resourceLoader.getResource("classpath:static/images/lost/").getURI());
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
                imagePathForDb = "/images/lost/" + imageName;
            } catch (IOException e) {
                logger.error("Failed to save uploaded file", e);
                return 0;
            }
        }

        String sql = "INSERT INTO lost_items " +
                "(item_name, description, location, contact_name, contact_phone, contact_email, status, date_lost, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)";
        return jdbcTemplate.update(sql, itemName, description, location, contactName, contactPhone, contactEmail, dateLost, imagePathForDb);
    }

    // --- Find all lost items ---
    public List<LostItem> findAll() {
        String sql = "SELECT id, item_name as itemName, description, location, " +
                "contact_name as contactName, contact_phone as contactPhone, contact_email as contactEmail, " +
                "status, date_lost as dateLost, image_path as imagePath FROM lost_items";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class));
    }

    // --- Find by ID (returns Optional to match FoundItemRepository) ---
    public Optional<LostItem> findById(int id) {
        String sql = "SELECT id, item_name as itemName, description, location, contact_name as contactName, " +
                "contact_phone as contactPhone, contact_email as contactEmail, status, date_lost as dateLost, image_path as imagePath " +
                "FROM lost_items WHERE id = ?";
        List<LostItem> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // --- Find similar lost item (used when reporting found) ---
    public LostItem findSimilar(String itemName, String description) {
        String sqlName = "SELECT id, item_name as itemName, description, location, " +
                "contact_name as contactName, contact_phone as contactPhone, contact_email as contactEmail, " +
                "status, date_lost as dateLost, image_path as imagePath " +
                "FROM lost_items WHERE LOWER(item_name) = LOWER(?) LIMIT 1";
        List<LostItem> byName = jdbcTemplate.query(sqlName, new BeanPropertyRowMapper<>(LostItem.class),
                itemName == null ? "" : itemName);
        if (!byName.isEmpty()) return byName.get(0);

        String sqlDesc = "SELECT id, item_name as itemName, description, location, " +
                "contact_name as contactName, contact_phone as contactPhone, contact_email as contactEmail, " +
                "status, date_lost as dateLost, image_path as imagePath " +
                "FROM lost_items WHERE LOWER(description) LIKE LOWER(?) LIMIT 1";
        List<LostItem> byDesc = jdbcTemplate.query(sqlDesc, new BeanPropertyRowMapper<>(LostItem.class),
                "%" + (description == null ? "" : description) + "%");
        return byDesc.isEmpty() ? null : byDesc.get(0);

    }
    public List<LostItem> findByReporterAndStatus(String email, String status) {
        String sql = "SELECT id, item_name as itemName, description, location, " +
                "contact_name as contactName, contact_phone as contactPhone, contact_email as contactEmail, " +
                "status, date_lost as dateLost, image_path as imagePath " +
                "FROM lost_items WHERE contact_email = ? AND status = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class), email, status);
    }



    // --- Update status ---
    public int updateStatus(int id, String status) {
        String sql = "UPDATE lost_items SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }
    public int deleteById(int id) {
        String sql = "DELETE FROM lost_items WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

}
