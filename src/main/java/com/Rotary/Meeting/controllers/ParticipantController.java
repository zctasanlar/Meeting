package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.requestDtos.GetParticipantByIdRequest;
import com.Rotary.Meeting.models.requestDtos.QrCodeByRoleIdRequest;
import com.Rotary.Meeting.models.responseDtos.GetParticipantByIdResponse;
import com.Rotary.Meeting.services.ParticipantService;
import com.Rotary.Meeting.services.util.QRGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/participant",produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final QRGeneratorService qrGeneratorService;

    @GetMapping("/getAllParticipants")
    public List<ParticipantEntity> getAllParticipants(){
        return this.participantService.getAllParticipants();
    }

    @PostMapping("/getParticipantById")
    public GetParticipantByIdResponse getParticipantById(@RequestBody GetParticipantByIdRequest meeting){
        return this.participantService.getParticipantById(meeting.getId());
    }

    @PostMapping("/addParticipant")
    public String addParticipant(@RequestBody ParticipantEntity participant){
        participantService.saveParticipant(participant);
        return "Meeting saved succesfully";
    }

    @PostMapping("/generateQrCodes")
    public boolean generateQrCodesToFolder(@RequestBody QrCodeByRoleIdRequest request){
        this.qrGeneratorService.generateQrCodesToFolder(request);
        return true;
    }


}
