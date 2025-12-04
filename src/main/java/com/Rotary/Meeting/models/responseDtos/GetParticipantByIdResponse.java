package com.Rotary.Meeting.models.responseDtos;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetParticipantByIdResponse {
    private ParticipantEntity participant;
}
