package com.Rotary.Meeting.services.util;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.repositories.TracingRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ToplantiSuresiEntegreServisi {

//    // YÖN SABİTLERİ
//    private static final int DIRECTION_ENTRY = 1; // GİRİŞ
//    private static final int DIRECTION_EXIT = 0;  // ÇIKIŞ
//
//    private final TracingRepository tracingRepository;
//    private final MolaAnalizServisi molaAnalizServisi; // Molada kalanları bulmak için
//
//    public ToplantiSuresiEntegreServisi(TracingRepository tracingRepository, MolaAnalizServisi molaAnalizServisi) {
//        this.tracingRepository = tracingRepository;
//        this.molaAnalizServisi = molaAnalizServisi;
//    }
//
//    /**
//     * Toplantı süresini (moladaki otomasyon kuralını dikkate alarak) hesaplayan ana metot.
//     * * @param meetingId Hesaplama yapılacak toplantının ID'si.
//     * @param baslangicZamani Toplantı başlangıcı.
//     * @param molaBaslangici Mola başlangıcı (Otomatik ÇIKIŞ'ın atıldığı an).
//     * @param molaBitisZamani Mola bitişi (Yeni GİRİŞ'lerin beklendiği an).
//     * @param bitisZamani Toplantı bitişi.
//     * @return Kullanıcı ID'si ve toplam katılım süresini içeren Map<UUID, Duration>.
//     */
//    public Map<UUID, Duration> hesaplaToplantiSuresiHibrit(
//            UUID meetingId,
//            LocalDateTime baslangicZamani,
//            LocalDateTime molaBaslangici,
//            LocalDateTime molaBitisZamani,
//            LocalDateTime bitisZamani)
//    {
//        // 1. ADIM: Molaya hiç çıkmayıp içeride kalanları tespit et (Özel Kural İçin Ön Hazırlık)
//        // Bu kişiler, moladaki otomatik ÇIKIŞ logundan etkilenip geri dönmeyenlerdir.
//        List<UUID> molayaCikmayanList = molaAnalizServisi.findParticipantsWhoDidNotExitForBreak(
//                meetingId,
//                molaBaslangici,
//                molaBitisZamani
//        );
//        Set<UUID> molayaCikmayanlar = new java.util.HashSet<>(molayaCikmayanList);
//        Duration molaSuresi = Duration.between(molaBaslangici, molaBitisZamani);
//
//        // 2. ADIM: Tüm Logları Çekme ve Gruplama
//        // Tüm logları veritabanından çek. (Bu metot TracingRepository'de olmalıdır)
//        List<TracingEntity> hareketler = tracingRepository.findByMeetingIdAndCreated_atBetweenOrderByCreated_at(
//                meetingId, baslangicZamani.minusMinutes(5), bitisZamani.plusMinutes(5) // Sınırların biraz dışını da al
//        );
//
//        // Kullanıcıya göre grupla ve her listeyi zamana göre sırala (Veri temizliği ve hazırlığı)
//        Map<UUID, List<TracingEntity>> gruplanmisHareketler = hareketler.stream()
//                .collect(Collectors.groupingBy(TracingEntity::getParticipantId,
//                        Collectors.collectingAndThen(
//                                Collectors.toList(),
//                                list -> {
//                                    list.sort(Comparator.comparing(TracingEntity::getCreated_at));
//                                    return list;
//                                }
//                        )
//                ));
//
//        Map<UUID, Duration> toplamSureler = new HashMap<>();
//
//        // 3. ADIM: Standart Süre Hesaplaması ve Kural Uygulama
//        for (Map.Entry<UUID, List<TracingEntity>> entry : gruplanmisHareketler.entrySet()) {
//            UUID kullaniciId = entry.getKey();
//            List<TracingEntity> kullaniciHareketleri = entry.getValue();
//
//            Duration icerideKalmaSuresi = Duration.ZERO;
//            LocalDateTime girisZamani = null; // En son kaydedilen giriş zamanı
//
//            // Standart Hesaplama Döngüsü
//            for (TracingEntity hareket : kullaniciHareketleri) {
//                LocalDateTime hareketZamani = hareket.getCreated_at();
//                int direction = hareket.getDirection();
//
//                // Yalnızca toplantı sınırları içindeki hareketleri ele al
//                if (hareketZamani.isBefore(baslangicZamani) || hareketZamani.isAfter(bitisZamani)) {
//                    continue;
//                }
//
//                if (direction == DIRECTION_ENTRY) {
//                    if (girisZamani == null) {
//                        // Giriş zamanı: Hareket anı veya Başlangıç anından hangisi daha yeniyse
//                        girisZamani = hareketZamani.isAfter(baslangicZamani) ? hareketZamani : baslangicZamani;
//                    }
//                } else if (direction == DIRECTION_EXIT) {
//                    if (girisZamani != null) {
//                        // Çıkış zamanı: Hareket anı veya Bitiş anından hangisi daha eskiyse
//                        LocalDateTime cikisZamani = hareketZamani.isBefore(bitisZamani) ? hareketZamani : bitisZamani;
//
//                        // Süreyi hesapla ve toplama ekle
//                        Duration icerideKalinanAnlikSure = Duration.between(girisZamani, cikisZamani);
//                        icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
//
//                        girisZamani = null; // Çıkış yapıldı
//                    }
//                }
//            }
//
//            // Kapanış Kontrolü (Toplantı bittiğinde hala içerideyse)
//            if (girisZamani != null) {
//                Duration icerideKalinanAnlikSure = Duration.between(girisZamani, bitisZamani);
//                icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
//            }
//
//            // 4. ADIM: Mola Kuralını Uygula
//            if (molayaCikmayanlar.contains(kullaniciId)) {
//                // Molaya çıkmayıp içeride kalanlar için, Otomatik ÇIKIŞ logunun neden olduğu eksik mola süresini ekle.
//                icerideKalmaSuresi = icerideKalmaSuresi.plus(molaSuresi);
//            }
//
//            toplamSureler.put(kullaniciId, icerideKalmaSuresi);
//        }
//
//        return toplamSureler;
//    }
}