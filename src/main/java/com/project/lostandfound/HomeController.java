package com.project.lostandfound;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private FoundItemRepository foundItemRepository;

    @GetMapping({"/", "/index"})
    public String indexPage() {
        return "index";          // templates/index.html
    }

    @PostMapping("/signup")
    public String handleSignUp(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // If email already exists, redirect to login with message
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("message", "This email has an account already. Please login");
            return "redirect:/login";
        }

        int rowsAffected = userRepository.save(email, password);

        if (rowsAffected > 0) {
            // Save email in session and go to dashboard
            session.setAttribute("username", email);
            model.addAttribute("username", email); // Pass username to next page
            return "selection";
        } else {
            model.addAttribute("error", "Failed to create account. Please try again.");
            return "index";
        }
    }

    // --- Login GET (shows login page) ---
    @GetMapping("/login")
    public String loginPage() {
        return "login";  // templates/login.html
    }

    // --- Handle Login POST ---
    @PostMapping("/handle-login")
    public String handleLogin(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // If email doesn't exist, redirect to signup with message
        if (!userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Account doesn't exist. Please sign up first.");
            return "redirect:/index";
        }

        String stored = userRepository.getPasswordByEmail(email);
        if (stored == null || !stored.equals(password)) {
            model.addAttribute("error", "Incorrect password. Please try again.");
            return "login";
        }

        // Successful login -> set session and go to dashboard
        session.setAttribute("username", email);
        model.addAttribute("username", email);
        return "selection";
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
    public String viewFoundPage(@RequestParam(value = "highlightId", required = false) Integer highlightId, Model model) {
        model.addAttribute("foundItems", foundItemRepository.findAll());
        model.addAttribute("highlightId", highlightId);
        return "view-found";  // templates/view-found.html
    }

    @GetMapping("/view-lost")
    public String viewLostPage(@RequestParam(value = "highlightId", required = false) Integer highlightId, Model model) {
        model.addAttribute("items", lostItemRepository.findAll());
        model.addAttribute("highlightId", highlightId);
        return "view-lost";  // templates/view-lost.html
    }

    @GetMapping("/selection")
    public String selectionPage(HttpSession session, Model model) {
        // Pull username from session if present
        Object username = session.getAttribute("username");
        if (username != null) {
            model.addAttribute("username", username.toString());
        }
        return "selection"; // templates/selection.html
    }

    // --- Handle Lost Report ---
    @PostMapping("/report-lost")
    public String handleReportLost(
            @RequestParam("itemName") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("contact") String contact,
            @RequestParam("dateLost") String dateLost,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model) {

        try {
            int rowsAffected = lostItemRepository.save(itemName, description, location, contact, dateLost, image);

            // After saving, check for a similar found item and redirect if found
            FoundItem match = foundItemRepository.findSimilar(itemName, description);
            if (match != null) {
                model.addAttribute("message", "We found a similar item reported as FOUND. Redirecting you to the item...");
                return "redirect:/view-found?highlightId=" + match.getId();
            }

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
    public String handleReportFound(@RequestParam(value = "itemName", required = false) String itemName,
                                    @RequestParam("description") String description,
                                    @RequestParam("location") String location,
                                    @RequestParam("contact") String contact,
                                    @RequestParam("dateFound") String dateFound,
                                    @RequestParam(value = "image", required = false) MultipartFile image,
                                    Model model) {
        try {
            int rowsAffected = foundItemRepository.save(itemName, description, location, contact, dateFound, image);

            // After saving, check for a similar lost item and redirect if found
            LostItem match = lostItemRepository.findSimilar(itemName, description);
            if (match != null) {
                model.addAttribute("message", "We found a similar item reported as LOST. Redirecting you to the item...");
                return "redirect:/view-lost?highlightId=" + match.getId();
            }

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
