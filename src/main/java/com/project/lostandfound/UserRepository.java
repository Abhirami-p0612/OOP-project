package com.project.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
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
}
