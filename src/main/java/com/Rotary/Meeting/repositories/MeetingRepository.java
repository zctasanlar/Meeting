package com.Rotary.Meeting.repositories;

import com.Rotary.Meeting.models.dto.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {
}
