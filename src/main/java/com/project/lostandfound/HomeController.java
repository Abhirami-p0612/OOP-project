package com.project.lostandfound;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional; // Added for Optional return from Repository

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

        // Get stored password
        String stored = userRepository.getPasswordByEmail(email);
        if (stored == null || !stored.equals(password)) {
            model.addAttribute("error", "Incorrect password. Please try again.");
            return "login";
        }

        // Successful login -> set session
        session.setAttribute("username", email);
        model.addAttribute("username", email);

        // --- Notification logic start ---
        // Fetch pending notification for this user (if any)
        String notification = userRepository.getNotification(email);
        if (notification != null && !notification.isEmpty()) {
            // Use FlashAttribute to show popup in selection.html
            redirectAttributes.addFlashAttribute("message", notification);

            // Clear the notification so it shows only once
            userRepository.clearNotification(email);
        }
        // --- Notification logic end ---

        // Redirect to selection page so that flash attributes are available
        return "redirect:/selection";
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
        // Only fetch items that are not marked as DELETED
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
        String username = (String) session.getAttribute("username");

        if (username != null) {
            model.addAttribute("username", username);

            // Fetch items reported by this user that are awaiting deletion
            List<FoundItem> awaitingItems = foundItemRepository.findByReporterAndStatus(username, "AWAITING_DELETION");

            if (!awaitingItems.isEmpty()) {
                model.addAttribute("awaitingItems", awaitingItems);
            }
        }

        return "selection";
    }



    // --- Handle Lost Report ---
    @PostMapping("/report-found")
    public String handleReportFound(
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("contactName") String contactName,     // NEW
            @RequestParam("contactPhone") String contactPhone,   // NEW
            @RequestParam("contactEmail") String contactEmail,   // NEW
            @RequestParam("dateFound") String dateFound,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model) {

        try {
            // Validate 10-digit phone number (redundant if HTML pattern is used, but good server-side practice)
            if (!contactPhone.matches("\\d{10}")) {
                model.addAttribute("error", "Phone number must be exactly 10 digits.");
                return "report-found";
            }

            // Call updated save method with new contact fields
            int rowsAffected = foundItemRepository.save(itemName, description, location, contactName, contactPhone, contactEmail, dateFound, image);

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

    // --- NEW: Endpoint to Display Contact Details ---
    @GetMapping("/get-contact/{id}")
    public String getContactDetails(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FoundItem> itemOptional = foundItemRepository.findById(id);

        if (itemOptional.isPresent()) {
            model.addAttribute("item", itemOptional.get());
            return "contact-details"; // templates/contact-details.html
        } else {
            redirectAttributes.addFlashAttribute("error", "Item not found.");
            return "redirect:/view-found";
        }
    }

    // HomeController.java - UPDATED confirmItemReceived
    @PostMapping("/confirm-item-received/{id}")
    public String confirmItemReceived(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem item = itemOpt.get();

            // Update the item status to awaiting deletion
            foundItemRepository.updateStatus(id, "AWAITING_DELETION");

            // Store notification for the reporter (Person A)
            userRepository.saveNotification(item.getContactEmail(),
                    "Your reported item '" + item.getItemName() + "' has been confirmed by the receiver.");

            redirectAttributes.addFlashAttribute("message",
                    "The item will be deleted after confirmation by the reporter.");
        }
        return "redirect:/view-found";
    }


    // --- NEW: Reported Person Confirms Deletion (Status: AWAITING_DELETION -> DELETED / DELETE) ---
    @PostMapping("/confirm-item-deleted/{id}")
    public String confirmItemDeleted(@PathVariable int id, RedirectAttributes redirectAttributes) {
        foundItemRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Item successfully removed from the list!");
        return "redirect:/selection";
    }
}
