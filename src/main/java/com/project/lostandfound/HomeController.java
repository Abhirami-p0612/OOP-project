package com.project.lostandfound;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    // --- Home & Index ---
    @GetMapping({"/", "/index"})
    public String indexPage() {
        return "index";
    }

    // --- Signup ---
    @PostMapping("/signup")
    public String handleSignUp(@RequestParam String email,
                               @RequestParam String password,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("message", "This email has an account already. Please login");
            return "redirect:/login";
        }

        int rowsAffected = userRepository.save(email, password);
        if (rowsAffected > 0) {
            session.setAttribute("username", email);
            model.addAttribute("username", email);
            return "selection";
        } else {
            model.addAttribute("error", "Failed to create account. Please try again.");
            return "index";
        }
    }

    // --- Login GET ---
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // --- Login POST ---
    @PostMapping("/handle-login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (!userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Account doesn't exist. Please sign up first.");
            return "redirect:/index";
        }

        String stored = userRepository.getPasswordByEmail(email);
        if (stored == null || !stored.equals(password)) {
            model.addAttribute("error", "Incorrect password. Please try again.");
            return "login";
        }

        session.setAttribute("username", email);
        model.addAttribute("username", email);

        // --- Notification logic ---
        String notification = userRepository.getNotification(email);
        if (notification != null && !notification.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", notification);
            userRepository.clearNotification(email);
        }

        return "redirect:/selection";
    }

    // --- Logout ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }

    // --- Navigation Pages ---
    @GetMapping("/report-found")
    public String reportFoundPage() {
        return "report-found";
    }

    @GetMapping("/report-lost")
    public String reportLostPage() {
        return "report-lost";
    }

    @GetMapping("/view-found")
    public String viewFoundPage(@RequestParam(value = "highlightId", required = false) Integer highlightId,
                                Model model,
                                HttpSession session) {
        model.addAttribute("foundItems", foundItemRepository.findAll());
        model.addAttribute("highlightId", highlightId);
        model.addAttribute("username", session.getAttribute("username"));
        return "view-found";
    }

    @GetMapping("/view-lost")
    public String viewLostPage(@RequestParam(value = "highlightId", required = false) Integer highlightId,
                               Model model,
                               HttpSession session) {
        model.addAttribute("items", lostItemRepository.findAll());
        model.addAttribute("highlightId", highlightId);
        model.addAttribute("username", session.getAttribute("username"));
        return "view-lost";
    }

    @GetMapping("/selection")
    public String selectionPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            model.addAttribute("username", username);

            List<FoundItem> awaitingFound = foundItemRepository.findByReporterAndStatus(username, "AWAITING_DELETION");
            List<LostItem> awaitingLost = lostItemRepository.findByReporterAndStatus(username, "AWAITING_DELETION");

            model.addAttribute("awaitingFound", awaitingFound);
            model.addAttribute("awaitingLost", awaitingLost);
        }
        return "selection";
    }




    // --- Handle Found Report POST ---
    @PostMapping("/report-found")
    public String handleReportFound(@RequestParam(value = "itemName", required = false) String itemName,
                                    @RequestParam("description") String description,
                                    @RequestParam("location") String location,
                                    @RequestParam("contactName") String contactName,
                                    @RequestParam("contactPhone") String contactPhone,
                                    @RequestParam("contactEmail") String contactEmail,
                                    @RequestParam("dateFound") String dateFound,
                                    @RequestParam(value = "image", required = false) MultipartFile image,
                                    Model model) {
        try {
            if (!contactPhone.matches("\\d{10}")) {
                model.addAttribute("error", "Phone number must be exactly 10 digits.");
                return "report-found";
            }

            int rowsAffected = foundItemRepository.save(itemName, description, location, contactName,
                    contactPhone, contactEmail, dateFound, image);

            LostItem match = lostItemRepository.findSimilar(itemName, description);
            if (match != null) {
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

    // --- Handle Lost Report POST ---
    @PostMapping("/report-lost")
    public String handleReportLost(
            @RequestParam("itemName") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("contactName") String contactName,
            @RequestParam("contactPhone") String contactPhone,
            @RequestParam("contactEmail") String contactEmail,
            @RequestParam("dateLost") String dateLost,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Model model) {

        try {
            if (!contactPhone.matches("\\d{10}")) {
                model.addAttribute("error", "Phone number must be exactly 10 digits.");
                return "report-lost";
            }

            int rowsAffected = lostItemRepository.save(itemName, description, location,
                    contactName, contactPhone, contactEmail, dateLost, image);

            FoundItem match = foundItemRepository.findSimilar(itemName, description);
            if (match != null) {
                return "redirect:/view-found?highlightId=" + match.getId();
            }

            if (rowsAffected > 0) {
                model.addAttribute("message", "Lost item reported successfully!");
                return "selection";
            } else {
                model.addAttribute("error", "Failed to save the lost item. Please try again.");
                return "report-lost";
            }

        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "report-lost";
        }
    }

    // --- Get Contact Details for Found Item ---
    @GetMapping("/get-contact/{id}")
    public String getContactDetails(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FoundItem> itemOptional = foundItemRepository.findById(id);
        if (itemOptional.isPresent()) {
            model.addAttribute("item", itemOptional.get());
            return "contact-found"; // renamed template
        } else {
            redirectAttributes.addFlashAttribute("error", "Item not found.");
            return "redirect:/view-found";
        }
    }

    // --- Confirm Found Item Received ---
    @PostMapping("/confirm-item-received/{id}")
    public String confirmItemReceived(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            FoundItem item = itemOpt.get();
            foundItemRepository.updateStatus(id, "AWAITING_DELETION");
            userRepository.saveNotification(item.getContactEmail(),
                    "Your reported item '" + item.getItemName() + "' has been confirmed by the receiver.");
            redirectAttributes.addFlashAttribute("message",
                    "The item will be deleted after confirmation by the reporter.");
        }
        return "redirect:/view-found";
    }


    // --- Confirm Lost Item Received ---
    @PostMapping("/confirm-lost-item-received/{id}")
    public String confirmLostItemReceived(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<LostItem> itemOpt = lostItemRepository.findById(id);
        if (itemOpt.isPresent()) {
            LostItem item = itemOpt.get();
            lostItemRepository.updateStatus(id, "AWAITING_DELETION");
            userRepository.saveNotification(item.getContactEmail(),
                    "Your reported lost item '" + item.getItemName() + "' has been confirmed as received by the owner.");
            redirectAttributes.addFlashAttribute("message",
                    "The item will be deleted after confirmation by the reporter.");
        }
        return "redirect:/view-lost";
    }

    // --- Confirm Found Item Deletion ---
    @PostMapping("/confirm-found-item-deletion/{id}")
    public String confirmFoundItemDeletion(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        Optional<FoundItem> itemOpt = foundItemRepository.findById(id);

        if (itemOpt.isPresent() && itemOpt.get().getContactEmail().equals(username)) { // reporter check
            foundItemRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Found item successfully removed!");
        } else {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this item.");
        }
        return "redirect:/selection";
    }

    // --- Get Contact Details for Lost Item ---
    @GetMapping("/get-lost-contact/{id}")
    public String getLostContact(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<LostItem> itemOptional = lostItemRepository.findById(id);

        if (itemOptional.isPresent()) {
            model.addAttribute("item", itemOptional.get());
            return "contact-lost";
        } else {
            redirectAttributes.addFlashAttribute("error", "Item not found.");
            return "redirect:/view-lost";
        }
    }
    // --- Confirm Lost Item Deletion ---
    @PostMapping("/confirm-lost-item-deletion/{id}")
    public String confirmLostItemDeletion(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        Optional<LostItem> itemOpt = lostItemRepository.findById(id);

        if (itemOpt.isPresent() && itemOpt.get().getContactEmail().equals(username)) { // reporter check
            lostItemRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Lost item successfully removed!");
        } else {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this item.");
        }
        return "redirect:/selection";
    }

}
