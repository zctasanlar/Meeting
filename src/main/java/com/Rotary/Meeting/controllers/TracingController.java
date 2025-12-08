package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.requestDtos.GetMeetingByIdRequest;
import com.Rotary.Meeting.models.requestDtos.LogDurationPeriodRequest;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.models.requestDtos.LogUserDurationRequest;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
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
    public boolean logTransaction(@RequestBody LogTransactionRequest request){
        return this.tracingService.logTransaction(request);
    }

    @PostMapping("/logMissingParticipants")
    public List<String> logMissingParticipants(@RequestBody LogDurationPeriodRequest request){
        return this.tracingService.logMissingParticipants(request);
    }

    @PostMapping("/hesaplaToplamIcerideKalmaSuresi")
    public Map<String, Long>  hesaplaToplamIcerideKalmaSuresi(@RequestBody LogUserDurationRequest request){
        return this.tracingService.hesaplaToplamIcerideKalmaSuresi(request);
    }

    @PostMapping("/calculateTotalInsideDuration")
    public List<KullaniciSure>  calculateTotalInsideDuration(@RequestBody LogUserDurationRequest request){
        return this.tracingService.calculateTotalInsideDuration(request);
    }


    @PostMapping("/closeActiveSessions")
    public int  closeActiveSessions(@RequestBody GetMeetingByIdRequest request){
        return this.tracingService.closeActiveSessions(request);
    }

}
