package com.Rotary.Meeting.models.requestDtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LogTransactionWithTimestampRequest {
    private UUID participant_id;
    private int direction;
    private UUID meeting_id;
    private String created_at;
}
