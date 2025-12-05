package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.dto.Transaction;
import com.Rotary.Meeting.models.requestDtos.LogDurationPeriodRequest;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.models.requestDtos.LogUserDurationRequest;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
import com.Rotary.Meeting.repositories.TracingRepository;
import com.Rotary.Meeting.services.util.Durum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

@Service
@AllArgsConstructor
public class TracingService {

    private final TracingRepository tracingRepository;
    private MeetingService meetingService;
    private ParticipantService participantService;

    private Map<String, Durum> kullaniciDurumlari = new HashMap<>();

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
        transaction.setTime(formatToUtcPlus3String(tracingEntity.getCreated_at(),"yyyy-MM-dd HH:mm:ss"));
        return transaction;
    }

//    public static String convertToUTCPlus3(Date date) {
//        // 1. Date'i Instant'a dönüştürün
//        Instant instant = date.toInstant();
//
//        // 2. Hedef saat dilimini tanımlayın (UTC+3)
//        // Türkiye'nin (TRT) saat dilimi genellikle UTC+3'tür ve
//        // DST (Yaz Saati Uygulaması) kullanmaz.
//        ZoneId targetZone = ZoneId.of("Europe/Istanbul");
//        // Alternatif olarak, doğrudan of("+03:00") kullanabilirsiniz,
//        // ancak ZoneId.of("Europe/Istanbul") tercih edilir.
//
//        // 3. Instant'ı hedef saat dilimine sahip ZonedDateTime'a dönüştürün
//        ZonedDateTime utcPlus3DateTime = instant.atZone(targetZone);
//
//        return utcPlus3DateTime.toString();
//    }

    /**
     * LocalDateTime objesini alır, onu UTC+3 saat dilimine çevirir
     * ve belirlenen formatta String olarak döndürür.
     * * @param localDateTime Formatlanacak LocalDateTime objesi.
     * @param pattern Çıktı formatı (Örn: "yyyy-MM-dd HH:mm:ss").
     * @return UTC+3 saat diliminde formatlanmış tarih/saat stringi.
     */
    public static String formatToUtcPlus3String(LocalDateTime localDateTime, String pattern) {

        // 1. Hedef saat dilimini tanımla (UTC+3)
        // Türkiye saati (Europe/Istanbul) şu anda UTC+3'tür ve ofset ID'si daha güvenilir bir yoldur.
        // Eğer her zaman +03:00 olmasını istiyorsanız, ZoneId.of("+03:00") kullanabilirsiniz.
        ZoneId targetZone = ZoneId.of("Europe/Istanbul");

        // 2. LocalDateTime'ı, UTC+3 saat dilimini varsayarak ZonedDateTime'a çevir
        // Bu, objeye saat dilimi bilgisini ekler.
        ZonedDateTime zonedDateTime = localDateTime.atZone(targetZone);

        // 3. İstenen formatı tanımla
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        // 4. Formatlanmış stringi döndür
        return zonedDateTime.format(formatter);
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

    public String getNameSurname(UUID participantId){
        String nameSurname = participantService.getParticipantById(participantId).getParticipant().getName() + " " +
                participantService.getParticipantById(participantId).getParticipant().getSurname();
        return nameSurname;
    }

    /**
     * Tüm hareket verilerini işler ve her kullanıcının en son durumunu günceller.
     * @param hareketler Timestampli tüm giriş/çıkış kayıtları
     */
    private void islemHareketleri(List<TracingEntity> hareketler) {
        // 1. Verileri zamana göre sıralama (Önemli adım!)
        hareketler.sort(Comparator.comparing(TracingEntity::getCreated_at));

        // 2. Her hareketi işleyerek durum haritasını doldurma
        for (TracingEntity hareket : hareketler) {
            Durum mevcutDurum = new Durum(hareket.getCreated_at(), hareket.getDirection()==1?"Giriş":"Çıkış");
            // Haritayı günceller
            kullaniciDurumlari.put(getNameSurname(hareket.getParticipantId()), mevcutDurum);
        }
    }

    /**
     * Belirtilen kontrol anında, maksimum dışarıda kalma süresini aşan kişileri listeler.
     * @return Belirlenen süredir toplantıda olmayan kullanıcıların listesi
     */
    public List<String> logMissingParticipants(LogDurationPeriodRequest request) {
        islemHareketleri(this.tracingRepository.findAll());

        LocalDateTime kontrolZamani = LocalDateTime.now();
        List<String> kayipKullanicilar = new ArrayList<>();
        Duration maxSure = Duration.ofMinutes(request.getMaxDisaridaKalmaDakika());

        for (Map.Entry<String, Durum> entry : kullaniciDurumlari.entrySet()) {
            String kullaniciAdi = entry.getKey();
            Durum durum = entry.getValue();

            // 1. Dışarıda kalma süresini hesapla: Kontrol zamanı - Son hareket zamanı
            Duration gecenSure = Duration.between(durum.getSonHareketZamani(), kontrolZamani);

            // Kullanıcı sadece ÇIKIŞ durumundaysa kontrol yapılır
            if (durum.getSonYon().equalsIgnoreCase("ÇIKIŞ")) {

                // Eğer geçen süre, maksimum izin verilen süreyi aşmışsa
                if (gecenSure.compareTo(maxSure) >= 0) {
                    kayipKullanicilar.add(kullaniciAdi);
                }
            }
            // NOT: Kullanıcının son hareketi "GİRİŞ" ise, içeridedir, kontrol gerekmez.
        }
        return kayipKullanicilar;
    }

    public Map<String, Long> hesaplaToplamIcerideKalmaSuresi(LogUserDurationRequest request)
    {

        LocalDateTime baslangicZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getStartTime();
        LocalDateTime bitisZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getEndTime();
        List<TracingEntity> hareketler = this.tracingRepository.findAll();
        // 1. Gruplama Adımı (Bu kısım hatasız çalışır)
        Map<UUID, List<TracingEntity>> gruplanmisHareketler = hareketler.stream()
                .collect(Collectors.groupingBy(TracingEntity::getParticipantId));

        // 2. Sıralama Adımı (Gruplanmış Map üzerinde döngü)
        gruplanmisHareketler.values().forEach(list ->
                list.sort(Comparator.comparing(TracingEntity::getCreated_at))
        );

        Map<String, Long> toplamSureler = new HashMap<>();

        // 2. Her kullanıcı için döngü
        for (Map.Entry<UUID, List<TracingEntity>> entry : gruplanmisHareketler.entrySet()) {
            String kullaniciId = getNameSurname(entry.getKey());

            List<TracingEntity> kullaniciHareketleri = entry.getValue();
            Duration icerideKalmaSuresi = Duration.ZERO;
            LocalDateTime girisZamani = null; // En son kaydedilen giriş zamanı

            // 3. Kullanıcının hareketlerini işle
            for (TracingEntity hareket : kullaniciHareketleri) {
                LocalDateTime hareketZamani = hareket.getCreated_at();

                // Direction int olduğu için varsayımlı dönüşüm yapıyoruz:
                String yon = (hareket.getDirection() == 1) ? "GİRİŞ" : "ÇIKIŞ";

                // Toplantı başlangıcından önceki ve bitişinden sonraki hareketleri atla
                if (hareketZamani.isBefore(baslangicZamani) || hareketZamani.isAfter(bitisZamani)) {
                    continue;
                }

                if (yon.equals("GİRİŞ")) {
                    // Eğer kullanıcı zaten içeride görünüyorsa, bu yeni giriş bilgisini yoksay (hata durumu)
                    if (girisZamani == null) {
                        // Giriş zamanını, toplantı başlangıç zamanından daha erken olamaz (max fonksiyonu gibi)
                        girisZamani = hareketZamani.isAfter(baslangicZamani) ? hareketZamani : baslangicZamani;
                    }

                } else if (yon.equals("ÇIKIŞ")) {
                    // Eğer bir giriş kaydı varsa (yani kullanıcı içeride görünüyor)
                    if (girisZamani != null) {
                        // Çıkış zamanını, toplantı bitiş zamanından daha geç olamaz (min fonksiyonu gibi)
                        LocalDateTime cikisZamani = hareketZamani.isBefore(bitisZamani) ? hareketZamani : bitisZamani;

                        // İçeride kalınan süreyi hesapla ve toplama ekle (Duration kullanılır)
                        Duration icerideKalinanAnlikSure = Duration.between(girisZamani, cikisZamani);
                        icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);

                        girisZamani = null; // Çıktığı için giriş zamanını sıfırla
                    }
                }
            }

            // 4. Bitiş kontrolü (Toplantı bittiğinde kullanıcı hala içerideyse)
            if (girisZamani != null) {
                // Toplantı bitiş zamanına kadar içeride kaldığı süreyi hesapla
                Duration icerideKalinanAnlikSure = Duration.between(girisZamani, bitisZamani);
                icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
            }

            toplamSureler.put(kullaniciId, icerideKalmaSuresi.toMinutes());
        }

        return toplamSureler;
    }




}
