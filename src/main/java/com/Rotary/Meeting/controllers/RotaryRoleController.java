package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import com.Rotary.Meeting.models.requestDtos.GetMeetingByIdRequest;
import com.Rotary.Meeting.services.ParticipantService;
import com.Rotary.Meeting.services.RotaryRoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/rotaryrole",produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class RotaryRoleController {

    private final RotaryRoleService rotaryRoleService;

    @GetMapping("/getAllRotaryRoles")
    public List<RotaryRoleEntity> getAllRotaryRoles(){
        return this.rotaryRoleService.getAllRotaryRoles();
    }

    @PostMapping("/getRotaryRoleById")
    public RotaryRoleEntity getMeetingById(@RequestBody GetMeetingByIdRequest meeting){
        return this.rotaryRoleService.getParticipantById(meeting.getId());
    }
}
