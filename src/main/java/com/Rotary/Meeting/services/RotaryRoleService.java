package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import com.Rotary.Meeting.repositories.ParticipantRepository;
import com.Rotary.Meeting.repositories.RotaryRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RotaryRoleService {

    private final RotaryRoleRepository rotaryRoleRepository;

    public RotaryRoleEntity getParticipantById(UUID id){
        return this.rotaryRoleRepository.getReferenceById(id);
    }

    @Cacheable(value = "allRotaryRoles")
    public List<RotaryRoleEntity> getAllRotaryRoles(){
        return this.rotaryRoleRepository.findAll();
    }

}
