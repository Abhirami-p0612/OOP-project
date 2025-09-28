package com.project.lostandfound;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class LostItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(LostItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Add this Autowired annotation
    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:src/main/resources/static/images/lost/}")
    private String uploadDir;

    public int save(String itemName, String description, String location, String contact, String dateLost, MultipartFile image) {

        String imagePathForDb = null;

        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String imageName = UUID.randomUUID().toString() + "_" + originalFilename;

            // Use ResourceLoader to get a reliable path
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
        String sql = "SELECT id, item_name, description, location, contact_info, date_lost, image_path FROM lost_items";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LostItem.class));
    }
}
