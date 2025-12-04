package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.responseDtos.GetParticipantByIdResponse;
import com.Rotary.Meeting.repositories.ParticipantRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public GetParticipantByIdResponse getParticipantById(UUID id){
        GetParticipantByIdResponse response = new GetParticipantByIdResponse();
        this.participantRepository.findById(id).ifPresent(response::setParticipant);
        return response;
    }

    @CacheEvict(value = "allParticipants", allEntries = true)
    public void saveParticipant(ParticipantEntity meeting){
        this.participantRepository.save(meeting);
    }

    @Cacheable(value = "allParticipants")
    public List<ParticipantEntity> getAllParticipants(){
        return this.participantRepository.findAll();
    }

    public List<ParticipantEntity> getParticipantByRRoleId(UUID id){
        GetParticipantByIdResponse response = new GetParticipantByIdResponse();
        List<ParticipantEntity> list= this.participantRepository.findAll().stream().filter(participant -> participant.getRRoleId().equals(id)).toList();
        return list;

    }

    @CacheEvict(value = "allParticipants", key = "#id")
    public void deleteParticipant(UUID id){
        this.participantRepository.deleteById(id);
    }
}
