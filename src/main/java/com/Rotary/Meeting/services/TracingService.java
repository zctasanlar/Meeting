package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.dto.Transaction;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
import com.Rotary.Meeting.repositories.TracingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.UUID.randomUUID;

@Service
@AllArgsConstructor
public class TracingService {

    private final TracingRepository tracingRepository;
    private MeetingService meetingService;
    private ParticipantService participantService;

    /*public TracingEntity getReferenceById(int id){
        return this.tracingRepository.getReferenceById(id);
    }*/

    public AllTransactionsListResponse getAllTransactions(){
        AllTransactionsListResponse response = new AllTransactionsListResponse();
        List<TracingEntity> list = this.tracingRepository.findAll();

        list.sort(Comparator.comparing(TracingEntity::getCreated_at).reversed());

        List<Transaction> respList = new ArrayList<>();
        for(TracingEntity ent : list){
            respList.add(mapTracingEntitytoTransaction(ent));
        }

        response.setTransactionList(respList);
        return response;
    }

    private Transaction mapTracingEntitytoTransaction(TracingEntity tracingEntity){
        Transaction transaction = new Transaction();
        transaction.setMeetingName(meetingService.getMeetingById(tracingEntity.getMeetingId()).getMeeting().getName());
        String nameSurname = participantService.getParticipantById(tracingEntity.getParticipantId()).getParticipant().getName() + " " +
                participantService.getParticipantById(tracingEntity.getParticipantId()).getParticipant().getSurname();
        transaction.setParticipantNameSurname(nameSurname);
        transaction.setDirection(tracingEntity.getDirection()==1 ? "IN":"OUT");
        transaction.setTime(convertToUTCPlus3(tracingEntity.getCreated_at()));
        return transaction;
    }

    public static String convertToUTCPlus3(Date date) {
        // 1. Date'i Instant'a dönüştürün
        Instant instant = date.toInstant();

        // 2. Hedef saat dilimini tanımlayın (UTC+3)
        // Türkiye'nin (TRT) saat dilimi genellikle UTC+3'tür ve
        // DST (Yaz Saati Uygulaması) kullanmaz.
        ZoneId targetZone = ZoneId.of("Europe/Istanbul");
        // Alternatif olarak, doğrudan of("+03:00") kullanabilirsiniz,
        // ancak ZoneId.of("Europe/Istanbul") tercih edilir.

        // 3. Instant'ı hedef saat dilimine sahip ZonedDateTime'a dönüştürün
        ZonedDateTime utcPlus3DateTime = instant.atZone(targetZone);

        return utcPlus3DateTime.toString();
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
