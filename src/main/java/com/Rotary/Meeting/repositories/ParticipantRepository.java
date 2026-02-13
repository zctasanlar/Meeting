package com.Rotary.Meeting.repositories;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

    /*
    @Query(value = "SELECT DISTINCT p.* FROM participant p " +
                "WHERE ( " +
                "  -- 1. DURUM: Toplantının rolü 'ALL' ise herkes zorunludur " +
                "  EXISTS ( " +
                "    SELECT 1 FROM meeting m " +
                "    JOIN rotary_role rr ON m.rrole_id = rr.id " +
                "    WHERE m.id = :meetingId AND rr.title = 'ALL' " +
                "  ) " +
                "  OR " +
                "  -- 2. DURUM: Toplantının rolü 'ALL' değilse, sadece o role sahip olanlar zorunludur " +
                "  p.rrole_id = (SELECT rrole_id FROM meeting WHERE id = :meetingId) " +
                ") " +
                "AND NOT EXISTS ( " +
                "  -- ORTAK KOŞUL: Bu kişi bu toplantıda hiç iz bırakmamış olmalı " +
                "  SELECT 1 FROM tracing t " +
                "  WHERE t.participant_id = p.id AND t.meeting_id = :meetingId " +
                ") " +
                "ORDER BY p.name ASC, p.surname ASC", nativeQuery = true)
        List<ParticipantEntity> findAbsentParticipants(@Param("meetingId") UUID meetingId);


    @Query(value = "SELECT DISTINCT p.* FROM participant p " +
            "WHERE ( " +
            "  -- KOŞUL 1: Katılımı zorunlu kitleyi belirle (Zaman farkından bağımsız) " +
            "  EXISTS ( " +
            "    SELECT 1 FROM meeting m " +
            "    JOIN rotary_role rr ON m.rrole_id = rr.id " +
            "    WHERE m.id = :meetingId AND rr.title = 'ALL' " +
            "  ) " +
            "  OR " +
            "  p.rrole_id = (SELECT rrole_id FROM meeting WHERE id = :meetingId) " +
            ") " +
            "AND ( " +
            "  -- KOŞUL 2: Bu kitleden şu an içeride OLMAYANLARI filtrele " +
            "  -- Durum A: Toplantıya hiç gelmemiş olanlar " +
            "  NOT EXISTS ( " +
            "    SELECT 1 FROM tracing t " +
            "    WHERE t.participant_id = p.id AND t.meeting_id = :meetingId " +
            "  ) " +
            "  OR " +
            "  -- Durum B: Gelmiş ama son hareketi ÇIKIŞ olan veya uzun süredir sessiz olanlar " +
            "  p.id IN ( " +
            "    SELECT sub.participant_id FROM ( " +
            "      SELECT DISTINCT ON (participant_id) participant_id, direction, created_at " +
            "      FROM tracing " +
            "      WHERE meeting_id = :meetingId " +
            "      ORDER BY participant_id, created_at DESC " +
            "    ) sub " +
            "    WHERE sub.direction = 0 " + // En son hareketi ÇIKIŞ
            "    OR sub.created_at < :checkTime " + // En son hareketi (Giriş olsa bile) belirlenen süreden eski
            "  ) " +
            ") " +
            "ORDER BY p.name ASC, p.surname ASC", nativeQuery = true)
    List<ParticipantEntity> findUsersNotPresentInLastTenMinutes(
            @Param("meetingId") UUID meetingId,
            @Param("checkTime") LocalDateTime checkTime
    ); */

    @Query(value = "SELECT DISTINCT p.* FROM participant p " +
            "WHERE p.identity_number <> '22' AND ( " +
            "  EXISTS ( " +
            "    SELECT 1 FROM meeting m " +
            "    JOIN rotary_role rr ON m.rrole_id = rr.id " +
            "    WHERE m.id = :meetingId AND 1=1 " +
            "  ) " +
            "  OR " +
            "  p.rrole_id = (SELECT m2.rrole_id FROM meeting m2 WHERE m2.id = :meetingId) " +
            ") " +
            "AND NOT EXISTS ( " +
            "  SELECT 1 FROM tracing t " +
            "  WHERE t.participant_id = p.id AND t.meeting_id = :meetingId " +
            ") " +
            "ORDER BY p.name ASC, p.surname ASC", nativeQuery = true)
    List<ParticipantEntity> findAbsentParticipants(@Param("meetingId") UUID meetingId);

    @Query(value = "SELECT DISTINCT p.* FROM participant p " +
            "WHERE p.id IN ( " +
            "    SELECT sub.participant_id FROM ( " +
            "        SELECT DISTINCT ON (participant_id) participant_id, direction, created_at " +
            "        FROM tracing " +
            "        WHERE meeting_id = :meetingId " +
            "        ORDER BY participant_id, created_at DESC " +
            "    ) sub " +
            "    WHERE sub.direction = 0 " +
            "    AND sub.created_at < :checkTime " +
            ") " +
            "ORDER BY p.name ASC, p.surname ASC", nativeQuery = true)
    List<ParticipantEntity> findUsersNotPresentInLastTenMinutes(
            @Param("meetingId") UUID meetingId,
            @Param("checkTime") LocalDateTime checkTime
    );

}

