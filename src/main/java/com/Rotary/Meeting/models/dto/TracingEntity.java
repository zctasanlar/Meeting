package com.Rotary.Meeting.models.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @PrePersist
    protected void onCreate() {
        if (this.created_at == null) { // Eğer dışarıdan bir değer atanmadıysa
            this.created_at = LocalDateTime.now(ZoneId.of("Europe/Istanbul")); // 0 diliminde ata
        }
    }

    @Column(name = "participant_id")
    private UUID ParticipantId;

    @Column(name = "meeting_id")
    private UUID MeetingId;

    @Column(name = "direction")
    private int Direction;
}
