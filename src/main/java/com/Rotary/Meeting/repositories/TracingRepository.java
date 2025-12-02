package com.Rotary.Meeting.repositories;

import com.Rotary.Meeting.models.dto.TracingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TracingRepository extends JpaRepository<TracingEntity, Integer> {
}
