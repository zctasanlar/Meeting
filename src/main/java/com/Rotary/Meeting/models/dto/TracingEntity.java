package com.Rotary.Meeting.models.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "public" , name="tracing")
public class TracingEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "participant_id")
    private UUID ParticipantId;

    @Column(name = "meeting_id")
    private UUID MeetingId;

    @Column(name = "direction")
    private int Direction;
}
