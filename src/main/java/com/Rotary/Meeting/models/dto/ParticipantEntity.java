package com.Rotary.Meeting.models.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "public" , name="participant")
public class ParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String Name;

    @Column(name = "surname")
    private String Surname;

    @Column(name = "identity_number")
    private String IdentityNumber;


    @Column(name = "rrole_id")
    private UUID RRoleId;
}
