package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

public record DescriptionDto(
        @JsonProperty("upload_image_key") String imageKey,
        String title,
        @JsonProperty("product_name") String name,
        @JsonProperty("total_price") int totalPrice,
        @JsonProperty("count") int unitAmount,
        @JsonProperty("summary") String description,
        @JsonProperty("due_date") LocalDateTime dueDate,
        @JsonProperty("pickup_date") LocalDateTime pickupDate
) {}
