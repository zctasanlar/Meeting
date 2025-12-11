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

    // NOT: Yukarıdaki sorgu verimli çalışmayabilir (MAX subquery'si nedeniyle).
    // Daha verimli bir sorgu, veritabanına özel (örn: PostgreSQL için DISTINCT ON)
    // veya CTE (Common Table Expression) kullanılarak yazılabilir.
    // Ancak bu, en basit ve DB agnostic (DB'den bağımsız) SQL çözümüdür.

    @Query(value = "SELECT * FROM tracing t WHERE t.meeting_id = :meetingId ", nativeQuery = true)
    List<TracingEntity> findAllByMeetingId(@Param("meetingId") UUID meetingId);

    /**
     * Belirtilen MeetingId'ye sahip tüm farklı katılımcıların (ParticipantId) listesini döndürür.
     * JPQL (Java Persistence Query Language) kullanır.
     * * @param meetingId Aranacak toplantının UUID'si.
     * @return Toplantıya katılmış olan tüm farklı katılımcı ID'lerinin listesi.

    @Query("SELECT DISTINCT t.ParticipantId FROM TracingEntity t WHERE t.MeetingId = :meetingId")
    List<UUID> findAllParticipantsByMeetingId(@Param("meetingId") UUID meetingId);


    List<TracingEntity> findByMeetingIdAndCreated_atBetweenOrderByCreated_at(
            UUID meetingId,
            LocalDateTime start,
            LocalDateTime end
    );


    // YÖN SABİTLERİ
    int DIRECTION_ENTRY = 1;

    /**
     * 1. SORGUNUN AMACI (Mola Öncesi İçeride Olanları Bulma - Koşul 1):
     * Mola başlangıcından önceki en son hareketi GİRİŞ (1) olan katılımcıların ID'lerini döndürür.
     * Bu kişiler, molaya girerken salonda GÖRÜNEN kişilerdir (otomatik ÇIKIŞ loguna maruz kalanlar).

    @Query(value = "SELECT t.participant_id " +
            "FROM tracing t " +
            "WHERE t.meeting_id = :meetingId " +
            "  AND t.created_at < :molaBaslangici " +
            "  AND t.direction = :directionEntry " +
            "  AND t.created_at = (" +
            "      SELECT MAX(t2.created_at) " +
            "      FROM tracing t2 " +
            "      WHERE t2.participant_id = t.participant_id " +
            "        AND t2.meeting_id = :meetingId " +
            "        AND t2.created_at < :molaBaslangici" +
            "  )",
            nativeQuery = true)
    List<UUID> findLastActiveParticipantsBefore(
            @Param("meetingId") UUID meetingId,
            @Param("molaBaslangici") LocalDateTime molaBaslangici,
            @Param("directionEntry") int directionEntry
    );

    /**
     * 2. SORGUNUN AMACI (Moladan Sonra Geri Dönenleri Bulma - Koşul 2):
     * Mola bitişi ile kontrol anı arasında GİRİŞ (1) kaydı atan (yani moladan manuel dönen)
     * tüm farklı katılımcıların ID'lerini döndürür.

    @Query(value = "SELECT DISTINCT t.participant_id " +
            "FROM tracing t " +
            "WHERE t.meeting_id = :meetingId " +
            "  AND t.direction = :directionEntry " +
            "  AND t.created_at >= :molaBitisZamani " +
            "  AND t.created_at <= :currentTime",
            nativeQuery = true)
    List<UUID> findParticipantsWithEntryBetween(
            @Param("meetingId") UUID meetingId,
            @Param("molaBitisZamani") LocalDateTime molaBitisZamani,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("directionEntry") int directionEntry
    );

    */
}
