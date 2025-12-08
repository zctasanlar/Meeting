package com.Rotary.Meeting.services.util;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.repositories.TracingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MolaAnalizServisi {
//
//    private final TracingRepository tracingRepository;
//    private static final int DIRECTION_ENTRY = 1;
//
//    public MolaAnalizServisi(TracingRepository tracingRepository) {
//        this.tracingRepository = tracingRepository;
//    }
//
//    /**
//     * Toplantı molasına hiç çıkmamış (otomatik ÇIKIŞ kuralından etkilenmemiş) kişileri bulur.
//     * Bu, molada salonda kaldığı için toplam süresine mola süresi eklenmesi gereken gruptur.
//     *
//     * Mantık: (Mola Öncesi İçeride Olanlar) - (Moladan Sonra GİRİŞ Yapanlar)
//     *
//     * @param meetingId Toplantı ID'si.
//     * @param molaBaslangici Mola başlangıç zamanı.
//     * @param molaBitisZamani Mola bitiş zamanı.
//     * @return Molaya hiç çıkmamış (toplantı salonunda kalan) katılımcıların ID listesi.
//     */
//    public List<UUID> findParticipantsWhoDidNotExitForBreak(
//            UUID meetingId,
//            LocalDateTime molaBaslangici,
//            LocalDateTime molaBitisZamani)
//    {
//        // 1. Koşul: Mola başlangıcından önceki son hareketleri GİRİŞ olanları bul.
//        // Bu sorgu, Mola başlangıcında içeride olan herkesi getirir.
//        List<UUID> activeBeforeBreak = tracingRepository.findLastActiveParticipantsBefore(
//                meetingId,
//                molaBaslangici,
//                DIRECTION_ENTRY
//        );
//
//        if (activeBeforeBreak.isEmpty()) {
//            return List.of(); // Hiç kimse içeride değildi.
//        }
//
//        // 2. Koşul: Mola bitişinden sonra GİRİŞ kaydı atan tüm kişileri bul (Moladan dönenler).
//        // Bu sorgu, mola bittikten sonra (manuel) okutma yaparak geri dönen herkesi listeler.
//        List<UUID> enteredAfterBreak = tracingRepository.findParticipantsWithEntryBetween(
//                meetingId,
//                molaBitisZamani,
//                LocalDateTime.now(), // Şu ana kadar olan tüm girişleri kontrol et
//                DIRECTION_ENTRY
//        );
//
//        // 3. İki listeyi karşılaştır (Farkı bulma)
//        // Set'leri kullanarak farkı bulmak en verimli yoldur.
//        java.util.Set<UUID> enteredSet = new java.util.HashSet<>(enteredAfterBreak);
//
//        // Molaya çıkmayanlar = activeBeforeBreak listesini, enteredSet'te olmayanlarla filtrele
//        List<UUID> didNotExit = activeBeforeBreak.stream()
//                .filter(participantId -> !enteredSet.contains(participantId))
//                .collect(Collectors.toList());
//
//        return didNotExit;
//    }
}