package com.Rotary.Meeting.models.responseDtos;

import com.Rotary.Meeting.models.dto.MeetingEntity;
import com.Rotary.Meeting.models.dto.ParticipantEntity;
import lombok.*;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMeetingByIdResponse {
    private MeetingEntity meeting;
}
