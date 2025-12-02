package com.Rotary.Meeting.models.requestDtos;

import lombok.Data;

import java.util.UUID;

@Data
public class GetParticipantByIdRequest {
    private UUID id;
}
