package com.Rotary.Meeting.models.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "public" , name="meeting")
public class MeetingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "name")
    private String Name;

    @Column(name = "start_time")
    private LocalDateTime StartTime;

    @Column(name = "end_time")
    private LocalDateTime EndTime;

    @Column(name = "rrole_id")
    private UUID RRoleId;
}
