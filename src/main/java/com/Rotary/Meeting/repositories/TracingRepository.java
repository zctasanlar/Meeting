package com.Rotary.Meeting.repositories;

import com.Rotary.Meeting.models.dto.TracingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Repository
public interface TracingRepository extends JpaRepository<TracingEntity, Integer> {

    /**
     * Verilen meetingId'ye sahip ve en son hareketi belirtilen yönde olan (GİRİŞ=1)
     * katılımcıların ID'lerini döndürür.
*/
    @Query(value = "SELECT t.participant_id " +
            "FROM tracing t " +
            "WHERE t.meeting_id = :meetingId AND t.direction = :direction " +
            "AND t.created_at = (" +
            "    SELECT MAX(t2.created_at) " +
            "    FROM tracing t2 " +
            "    WHERE t2.participant_id = t.participant_id AND t2.meeting_id = :meetingId" +
            ")", nativeQuery = true)
    List<UUID> findParticipantsWithLatestDirection(@Param("meetingId") UUID meetingId,
                                                   @Param("direction") int direction);


    @Query(value = "SELECT * FROM tracing t WHERE t.meeting_id = :meetingId", nativeQuery = true)
    List<TracingEntity> findAllByMeetingId(@Param("meetingId") UUID meetingId);


    @Query(value = "SELECT * FROM tracing t WHERE t.meeting_id = :meetingId order by created_at desc limit 10", nativeQuery = true)
    List<TracingEntity> findAllByMeetingIdForClient(@Param("meetingId") UUID meetingId);


    @Query(value = "SELECT COUNT(*) FROM (" +
            "  SELECT DISTINCT ON (participant_id) direction " +
            "  FROM public.tracing " +
            "  WHERE meeting_id = :meetingId " +
            "  ORDER BY participant_id, created_at DESC" +
            ") as son_durum WHERE direction = 1", nativeQuery = true)
    long countCurrentUsers(@Param("meetingId") UUID meetingId);

}
