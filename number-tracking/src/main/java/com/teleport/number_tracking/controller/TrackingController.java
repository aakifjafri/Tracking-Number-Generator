package com.teleport.number_tracking.controller;


import com.teleport.number_tracking.dto.TrackingResponse;
import com.teleport.number_tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class TrackingController {

    @Autowired
    private TrackingService service;

    @GetMapping("/next-tracking-number")
    public ResponseEntity<TrackingResponse> next(
            @RequestParam("origin_country_id")
            @Pattern(regexp = "^[A-Z]{2}$", message = "origin_country_id must be ISO3166-1 alpha-2 uppercase")
            String originCountryId,

            @RequestParam("destination_country_id")
            @Pattern(regexp = "^[A-Z]{2}$", message = "destination_country_id must be ISO3166-1 alpha-2 uppercase")
            String destinationCountryId,

            @RequestParam("weight")
            @Pattern(regexp = "^\\d{1,4}(\\.\\d{1,3})?$", message = "weight must be decimal up to 3 decimals")
            String weight,

            @RequestParam("created_at")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?([ ]?[+-]\\d{2}:\\d{2}|Z)$", 
                     message = "created_at must be in RFC3339 format (e.g., 2023-12-01T10:30:00Z or 2023-12-01T10:30:00+05:30)")
            String createdAt,

            @RequestParam("customer_id")
            @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
                     message = "customer_id must be a valid UUID")
            String customerId,

            @RequestParam("customer_name")
            @NotBlank
            @Size(max = 100)
            String customerName,

            @RequestParam("customer_slug")
            @NotBlank
            @Size(max = 100)
            String customerSlug
    ) {
        try {
            // parse created_at (RFC3339) - handle space before timezone offset and missing + sign
            String normalizedCreatedAt = createdAt;
            // Handle space before timezone offset
            if (normalizedCreatedAt.contains(" ")) {
                // If there's a space, assume positive timezone offset if no sign is present
                normalizedCreatedAt = normalizedCreatedAt.replaceAll(" (\\d{2}:\\d{2})$", "+$1");
                // handle cases where there's already a + or - sign
                normalizedCreatedAt = normalizedCreatedAt.replaceAll(" ([+-])", "$1");
            }
            OffsetDateTime createdAtDt = OffsetDateTime.parse(normalizedCreatedAt);
            UUID cid = UUID.fromString(customerId);

            TrackingResponse resp = service.generate(originCountryId, destinationCountryId, weight, createdAtDt, cid, customerName, customerSlug);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date or UUID format: " + e.getMessage());
        }
    }

}
