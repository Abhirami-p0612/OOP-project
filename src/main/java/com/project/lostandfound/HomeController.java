package com.project.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/index")
    public String indexPage() {
        return "index";          // templates/index.html
    }
    @PostMapping("/signup")
    public String handleSignUp(
            @RequestParam String email,
            @RequestParam String password ){
        int rowsAffected = userRepository.save(email, password);

        if (rowsAffected > 0) {
            return "selection";
        } else {
            return "index";
        }


    }

}

