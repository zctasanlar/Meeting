package com.Rotary.Meeting.models.requestDtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LogUserDurationRequest {

    private UUID meetingId;

}
