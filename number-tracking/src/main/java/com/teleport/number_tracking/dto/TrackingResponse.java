package com.teleport.number_tracking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class TrackingResponse {
    @JsonProperty("tracking_number")
    private final String trackingNumber;

    @JsonProperty("created_at")
    private final OffsetDateTime createdAt;

    public TrackingResponse(String trackingNumber, OffsetDateTime createdAt) {
        this.trackingNumber = trackingNumber;
        this.createdAt = createdAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
