package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.MeetingEntity;
import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.repositories.TracingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Service
@AllArgsConstructor
public class TracingService {

    private final TracingRepository tracingRepository;

    /*public TracingEntity getReferenceById(int id){
        return this.tracingRepository.getReferenceById(id);
    }*/

    public List<TracingEntity> getAllTransactions(){
        return this.tracingRepository.findAll();
    }

    public boolean logTransaction(LogTransactionRequest request){
        try{
                TracingEntity entity = new TracingEntity();
                entity.setId(randomUUID());
                entity.setParticipantId(request.getParticipant_id());
                entity.setDirection(request.getDirection());
                entity.setMeetingId(request.getMeeting_id());
                this.tracingRepository.save(entity);
                return true;
        }catch (Exception ex) {
                return false;
        }

    }

}
