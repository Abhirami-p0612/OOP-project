package com.project.lostandfound;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private FoundItemRepository foundItemRepository;

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

    // --- Pages for navigation ---
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

    // --- Handle Lost Report ---
    @PostMapping("/report-lost")
    public String handleReportLost(
            @RequestParam("itemName") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("contact") String contact,
            @RequestParam("dateLost") String dateLost,
            @RequestParam("image") MultipartFile image,
            Model model) {

        try {
            int rowsAffected = lostItemRepository.save(itemName, description, location, contact, dateLost, image);

            if (rowsAffected > 0) {
                model.addAttribute("message", "Item reported successfully!");
                return "selection";
            } else {
                model.addAttribute("error", "Failed to save the item. Please try again.");
                return "report-lost";
            }
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "report-lost";
        }
    }


    // --- Handle Found Report ---
    @PostMapping("/report-found")
    public String handleReportFound(@RequestParam("description") String description,
                                    @RequestParam("location") String location,
                                    @RequestParam("contact") String contact,
                                    @RequestParam("dateFound") String dateFound,
                                    @RequestParam("image") MultipartFile image,
                                    Model model) {
        try {
            int rowsAffected = foundItemRepository.save(description, location, contact, dateFound, image);

            if (rowsAffected > 0) {
                model.addAttribute("message", "Found item reported successfully!");
                return "selection";
            } else {
                model.addAttribute("error", "Failed to save the found item.");
                return "report-found";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "report-found";
        }
    }
}
