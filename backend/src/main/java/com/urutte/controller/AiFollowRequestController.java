package com.urutte.controller;

import com.urutte.service.AiFollowRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-follow-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://frontend", "https://urutte.com", "https://www.urutte.com"})
public class AiFollowRequestController {

    @Autowired
    private AiFollowRequestService aiFollowRequestService;

    /**
     * Manually trigger auto-approval of follow requests to AI user
     */
    @PostMapping("/auto-approve")
    public ResponseEntity<String> triggerAutoApproval() {
        try {
            aiFollowRequestService.triggerAutoApproval();
            return ResponseEntity.ok("Auto-approval triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error triggering auto-approval: " + e.getMessage());
        }
    }

    /**
     * Get count of pending follow requests to AI user
     */
    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        try {
            long count = aiFollowRequestService.getPendingFollowRequestsToAiUserCount();
            return ResponseEntity.ok(Map.of("pendingCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", -1L));
        }
    }
}
