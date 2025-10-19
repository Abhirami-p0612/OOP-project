package com.project.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int save(String email, String password) {
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
        return jdbcTemplate.update(sql, email, password);
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public String getPasswordByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject("SELECT password FROM users WHERE email = ?", String.class, email);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }
    public void saveNotification(String email, String message) {
        String sql = "UPDATE users SET notification_message = ? WHERE email = ?";
        jdbcTemplate.update(sql, message, email);
    }
    // Get pending notification for a user
    public String getNotification(String email) {
        String sql = "SELECT notification_message FROM users WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, String.class, email);
    }

    // Clear notification after showing it once
    public void clearNotification(String email) {
        String sql = "UPDATE users SET notification_message = NULL WHERE email = ?";
        jdbcTemplate.update(sql, email);
    }


}
