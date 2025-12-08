package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.MeetingEntity;
import com.Rotary.Meeting.models.requestDtos.ChangeMeetingTimeByIdRequest;
import com.Rotary.Meeting.models.responseDtos.GetMeetingByIdResponse;
import com.Rotary.Meeting.repositories.MeetingRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public GetMeetingByIdResponse getMeetingById(UUID id){
        GetMeetingByIdResponse response = new GetMeetingByIdResponse();
        this.meetingRepository.findById(id).ifPresent(response::setMeeting);
        return response;
    }

    @CacheEvict(value = "allMeetings", allEntries = true)
    public void saveMeeting(MeetingEntity meeting){
        this.meetingRepository.save(meeting);
    }

    @CacheEvict(value = "allMeetings", allEntries = true)
    public boolean changeMeetingTime(ChangeMeetingTimeByIdRequest request){
        MeetingEntity meeting = this.meetingRepository.findById(request.getId()).get();
        if(request.getType() == 0){
           meeting.setStartTime(request.getTime());
        }else {
            meeting.setEndTime(request.getTime());
        }
        this.meetingRepository.save(meeting);
        return true;
    }


    @Cacheable(value = "allMeetings")
    public List<MeetingEntity> getAllMeeting(){
        return this.meetingRepository.findAll();
    }

    @CacheEvict(value = "allMeetings", key = "#id")
    public void deleteMeeting(UUID id){
        this.meetingRepository.deleteById(id);
    }
}
