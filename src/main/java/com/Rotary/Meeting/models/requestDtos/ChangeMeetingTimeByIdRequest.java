package com.Rotary.Meeting.models.requestDtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChangeMeetingTimeByIdRequest {
    private UUID id;
    private int type; // 0-start 1-end
    private LocalDateTime time;
}
