package com.Rotary.Meeting.repositories;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RotaryRoleRepository extends JpaRepository<RotaryRoleEntity, UUID> {
}
