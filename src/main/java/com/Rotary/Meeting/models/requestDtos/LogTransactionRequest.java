package com.Rotary.Meeting.models.requestDtos;

import lombok.Data;

import java.util.UUID;

@Data
public class LogTransactionRequest {
    private UUID participant_id;
    private int direction;
    private UUID meeting_id;
}
