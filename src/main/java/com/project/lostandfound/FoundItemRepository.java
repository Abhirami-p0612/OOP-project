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
public class FoundItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(FoundItemRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${file.upload-dir:src/main/resources/static/images/found/}")
    private String uploadDir;

    public int save(String description, String location, String contact, String dateFound, MultipartFile image) {

        String imagePathForDb = null;

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

        String sql = "INSERT INTO found_items (description, location, contact_info, date_found, image_path) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            return jdbcTemplate.update(sql, description, location, contact, dateFound, imagePathForDb);
        } catch (Exception e) {
            logger.error("Failed to insert found item into DB", e);
            return 0;
        }
    }

    public List<FoundItem> findAll() {
        String sql = "SELECT id, description, location, contact_info, date_found, image_path FROM found_items";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FoundItem.class));
    }

    public int deleteById(int id) {
        String sql = "DELETE FROM found_items WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
