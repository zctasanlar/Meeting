package com.Rotary.Meeting.models.responseDtos;

import com.Rotary.Meeting.models.dto.Transaction;
import lombok.*;

import java.util.List;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralResponse {
    private boolean response;
}
