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

@Repository
public class LostItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(LostItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:src/main/resources/static/images/lost/}")
    private String uploadDir;

    public int save(String itemName, String description, String location, String contact, String dateLost, MultipartFile image) {

        String imagePathForDb = null;

        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String imageName = UUID.randomUUID().toString() + "_" + originalFilename;

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

        String sql = "INSERT INTO lost_items (item_name, description, location, contact_info, date_lost, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return jdbcTemplate.update(sql, itemName, description, location, contact, dateLost, imagePathForDb);
        } catch (Exception e) {
            logger.error("Failed to insert lost item into DB", e);
            return 0;
        }
    }

    public List<LostItem> findAll() {
        String sql = "SELECT id, item_name as itemName, description, location, contact_info as contactInfo, date_lost as dateLost, image_path as imagePath FROM lost_items";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class));
    }

    public LostItem findById(int id) {
        String sql = "SELECT id, item_name as itemName, description, location, contact_info as contactInfo, date_lost as dateLost, image_path as imagePath FROM lost_items WHERE id = ?";
        List<LostItem> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    // Find a similar lost item (used when user reports a found item)
    public LostItem findSimilar(String itemName, String description) {
        String sqlName = "SELECT id, item_name as itemName, description, location, contact_info as contactInfo, date_lost as dateLost, image_path as imagePath FROM lost_items WHERE LOWER(item_name) = LOWER(?) LIMIT 1";
        List<LostItem> byName = jdbcTemplate.query(sqlName, new BeanPropertyRowMapper<>(LostItem.class), itemName == null ? "" : itemName);
        if (!byName.isEmpty()) return byName.get(0);

        String sqlDesc = "SELECT id, item_name as itemName, description, location, contact_info as contactInfo, date_lost as dateLost, image_path as imagePath FROM lost_items WHERE LOWER(description) LIKE LOWER(?) LIMIT 1";
        List<LostItem> byDesc = jdbcTemplate.query(sqlDesc, new BeanPropertyRowMapper<>(LostItem.class), "%" + (description == null ? "" : description) + "%");
        return byDesc.isEmpty() ? null : byDesc.get(0);
    }
}
