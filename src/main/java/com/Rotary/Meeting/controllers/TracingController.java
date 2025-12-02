package com.Rotary.Meeting.controllers;


import com.Rotary.Meeting.models.dto.RotaryRoleEntity;
import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.requestDtos.GetMeetingByIdRequest;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.services.RotaryRoleService;
import com.Rotary.Meeting.services.TracingService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/tracing",produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class TracingController {

    private final TracingService tracingService;

    @GetMapping("/getAllTransactions")
    public List<TracingEntity> getAllRotaryRoles(){
        return this.tracingService.getAllTransactions();
    }

    @PostMapping("/logTransaction")
    public boolean logTransaction(@RequestBody LogTransactionRequest request){
        return this.tracingService.logTransaction(request);
    }
}
