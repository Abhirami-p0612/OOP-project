package com.project.lostandfound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam String password,
            Model model){
        int rowsAffected = userRepository.save(email, password);

        if (rowsAffected > 0) {
            model.addAttribute("username", email); // Pass username to next page
            return "selection";
        } else {
            return "index";
        }
    }

    // --- New Mappings for Second Page Buttons ---
    @GetMapping("/report-found")
    public String reportFoundPage() {
        return "report-found";  // templates/report-found.html
    }

    @GetMapping("/report-lost")
    public String reportLostPage() {
        return "report-lost";  // templates/report-lost.html
    }

    @GetMapping("/view-found")
    public String viewFoundPage() {
        return "view-found";  // templates/view-found.html
    }

    @GetMapping("/view-lost")
    public String viewLostPage() {
        return "view-lost";  // templates/view-lost.html
    }
}
