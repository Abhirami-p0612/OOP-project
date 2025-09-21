package com.project.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class LostItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(LostItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // configure this in application.properties: file.upload-dir=/absolute/or/relative/uploads/lost
    @Value("${file.upload-dir:src/main/resources/static/images/lost/}")
    private String uploadDir;

    /**
     * Saves a lost item to the database, including optional image upload and date lost.
     *
     * @param itemName   Name of the lost item
     * @param description Description/details of the item
     * @param location   Where it was lost
     * @param contact    Contact information
     * @param dateLost   Date the item was lost (yyyy-MM-dd)
     * @param image      Optional image of the item
     * @return 1 if inserted successfully, 0 if failed
     */
    public int save(String itemName, String description, String location, String contact, String dateLost, MultipartFile image) {

        String imagePathForDb = null;

        // 1) Handle image upload
        if (image != null && !image.isEmpty()) {
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String imageName = UUID.randomUUID().toString() + "_" + originalFilename;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
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

        // 2) Insert into database
        String sql = "INSERT INTO lost_items (item_name, description, location, contact_info, date_lost, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return jdbcTemplate.update(sql, itemName, description, location, contact, dateLost, imagePathForDb);
        } catch (Exception e) {
            logger.error("Failed to insert lost item into DB", e);
            return 0;
        }
    }
}
