package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.MeetingEntity;
import com.Rotary.Meeting.models.requestDtos.ChangeMeetingTimeByIdRequest;
import com.Rotary.Meeting.models.requestDtos.GetMeetingByIdRequest;
import com.Rotary.Meeting.models.responseDtos.GetMeetingByIdResponse;
import com.Rotary.Meeting.services.MeetingService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/meeting",produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping("/getAll")
    public List<MeetingEntity> getAllMeetings(){
        return this.meetingService.getAllMeeting().stream().filter(meetingEntity -> meetingEntity.getStatus()==1).toList();
    }

    @PostMapping("/getMeetingById")
    public GetMeetingByIdResponse getMeetingById(@RequestBody GetMeetingByIdRequest meeting){
        return this.meetingService.getMeetingById(meeting.getMeetingId());
    }

    @PostMapping("/changeMeetingTime")
    public boolean changeMeetingTime(@RequestBody ChangeMeetingTimeByIdRequest request){
        return this.meetingService.changeMeetingTime(request);
    }


    @PostMapping("/add")
    public String addMeeting(@RequestBody MeetingEntity meeting){
        meetingService.saveMeeting(meeting);
        return "Meeting saved succesfully";
    }
}
