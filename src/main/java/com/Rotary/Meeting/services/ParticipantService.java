package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.repositories.ParticipantRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantEntity getParticipantById(UUID id){
        return this.participantRepository.getReferenceById(id);
    }

    @CacheEvict(value = "allParticipants", allEntries = true)
    public void saveParticipant(ParticipantEntity meeting){
        this.participantRepository.save(meeting);
    }

    @Cacheable(value = "allParticipants")
    public List<ParticipantEntity> getAllParticipants(){
        return this.participantRepository.findAll();
    }

    @CacheEvict(value = "allParticipants", key = "#id")
    public void deleteParticipant(UUID id){
        this.participantRepository.deleteById(id);
    }
}
