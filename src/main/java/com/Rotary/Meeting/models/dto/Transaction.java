package com.Rotary.Meeting.models.dto;

import lombok.*;

import java.util.Date;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String participantNameSurname ;
    private String meetingName;
    private String direction ;
    private String time;
}
