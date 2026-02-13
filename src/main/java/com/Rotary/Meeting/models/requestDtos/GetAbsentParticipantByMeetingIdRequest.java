package com.Rotary.Meeting.models.requestDtos;

import lombok.Data;

import java.util.UUID;

@Data
public class GetAbsentParticipantByMeetingIdRequest {
    private UUID id;
}
