package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.requestDtos.*;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
import com.Rotary.Meeting.models.responseDtos.GeneralResponse;
import com.Rotary.Meeting.services.RotaryRoleService;
import com.Rotary.Meeting.services.TracingService;
import com.Rotary.Meeting.services.util.KullaniciSure;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/tracing",produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class TracingController {

    private final TracingService tracingService;

    @GetMapping("/getAllTransactions")
    public AllTransactionsListResponse getAllTransactions(){
        return this.tracingService.getAllTransactions();
    }

    @PostMapping("/logTransaction")
    public GeneralResponse logTransaction(@RequestBody LogTransactionRequest request){
        return this.tracingService.logTransaction(request);
    }

    @PostMapping("/logMissingParticipants")
    public List<String> logMissingParticipants(@RequestBody LogDurationPeriodRequest request){
        return this.tracingService.logMissingParticipants(request);
    }

    @PostMapping("/countCurrentUsers")
    public Long countCurrentUsers(@RequestBody CountCurrentUserRequest request){
        return this.tracingService.countCurrentUsers(request);
    }

    //bu metodu client kullanıyor.
    @PostMapping("/calculateTotalInsideDuration")
    public List<KullaniciSure>  calculateTotalInsideDuration(@RequestBody LogUserDurationRequest request){
        return this.tracingService.calculateTotalInsideDuration(request);
    }

    //bu metodu client kullanıyor.
    @PostMapping("/calculateTotalInsideDurationForAdmin")
    public List<KullaniciSure>  calculateTotalInsideDurationForAdmin(@RequestBody LogUserDurationRequest request){
        return this.tracingService.calculateTotalInsideDurationForAdmin(request);
    }


    @PostMapping("/closeActiveSessions")
    public GeneralResponse  closeActiveSessions(@RequestBody GetMeetingByIdRequest request){
        return this.tracingService.closeActiveSessions(request);
    }

}
