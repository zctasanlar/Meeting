package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.dto.Transaction;
import com.Rotary.Meeting.models.requestDtos.*;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
import com.Rotary.Meeting.models.responseDtos.GeneralResponse;
import com.Rotary.Meeting.repositories.TracingRepository;
import com.Rotary.Meeting.services.util.Durum;
import com.Rotary.Meeting.services.util.KullaniciSure;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cglib.core.Local;
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
    //private MolaAnalizServisi molaAnalizServisi ;

    private Map<String, Durum> kullaniciDurumlari = new HashMap<>();

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

    public GeneralResponse logTransaction(LogTransactionRequest request){
        GeneralResponse response = new GeneralResponse();
        try{
                TracingEntity entity = new TracingEntity();
                entity.setId(randomUUID());
                entity.setParticipantId(request.getParticipant_id());
                entity.setDirection(request.getDirection());
                entity.setMeetingId(request.getMeeting_id());
                this.tracingRepository.save(entity);
                response.setResponse(true);
        }catch (Exception ex) {
            response.setResponse(false);
        }
        return response;

    }

    public String getNameSurname(UUID participantId){
        return participantService.getParticipantById(participantId).getParticipant().getName() + " " +
                participantService.getParticipantById(participantId).getParticipant().getSurname();
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


    public Long countCurrentUsers(CountCurrentUserRequest request) {
        return tracingRepository.countCurrentUsers(request.getMeetingId());
    }


    public List<String> logMissingParticipants(LogDurationPeriodRequest request) {





        islemHareketleri(this.tracingRepository.findAllByMeetingId(request.getMeetingId()));

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

    public List<KullaniciSure> calculateTotalInsideDuration(LogUserDurationRequest request) {

        // ... (Tanımlamalar ve Gruplama/Sıralama Adımları aynı kalır)

        LocalDateTime baslangicZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getStartTime();
        LocalDateTime bitisZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getEndTime();

        // NOT: Gerçek projede veritabanı sorgusunu filtreleyerek yapmanız (yorum satırındaki gibi) daha performanslı olacaktır.
        List<TracingEntity> hareketler = this.tracingRepository.findAllByMeetingIdForClient(request.getMeetingId());

        // 1. Gruplama Adımı (Aynı kalır)
        Map<UUID, List<TracingEntity>> gruplanmisHareketler = hareketler.stream()
                .collect(Collectors.groupingBy(TracingEntity::getParticipantId));

        // 2. Sıralama Adımı (Aynı kalır)
        gruplanmisHareketler.values().forEach(list ->
                list.sort(Comparator.comparing(TracingEntity::getCreated_at))
        );

        // Sonuç Listesi artık Map yerine KullaniciSure listesi olacak
        List<KullaniciSure> toplamSurelerListesi = new ArrayList<>();


        // 2. Her kullanıcı için döngü (Döngü içi mantık aynı kalır)
        for (Map.Entry<UUID, List<TracingEntity>> entry : gruplanmisHareketler.entrySet()) {
            String kullaniciId = getNameSurname(entry.getKey());

            List<TracingEntity> kullaniciHareketleri = entry.getValue();
            Duration icerideKalmaSuresi = Duration.ZERO;
            LocalDateTime girisZamani = null;

            // 3. Kullanıcının hareketlerini işle (Mantık aynı kalır)
            for (TracingEntity hareket : kullaniciHareketleri) {
                LocalDateTime hareketZamani = hareket.getCreated_at();

                String yon = (hareket.getDirection() == 1) ? "GİRİŞ" : "ÇIKIŞ";

                if (hareketZamani.isBefore(baslangicZamani) || hareketZamani.isAfter(bitisZamani)) {
                    continue;
                }

                if (yon.equals("GİRİŞ")) {
                    if (girisZamani == null) {
                        girisZamani = hareketZamani.isAfter(baslangicZamani) ? hareketZamani : baslangicZamani;
                    }
                } else if (yon.equals("ÇIKIŞ")) {
                    if (girisZamani != null) {
                        LocalDateTime cikisZamani = hareketZamani.isBefore(bitisZamani) ? hareketZamani : bitisZamani;

                        Duration icerideKalinanAnlikSure = Duration.between(girisZamani, cikisZamani);
                        icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);

                        girisZamani = null;
                    }
                }
            }

            // 4. Bitiş kontrolü (Mantık aynı kalır)
            if (girisZamani != null) {
                bitisZamani = LocalDateTime.now().isBefore(bitisZamani) ? LocalDateTime.now() : bitisZamani;

                Duration icerideKalinanAnlikSure = Duration.between(girisZamani, bitisZamani);
                icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
            }

            // Sonucu Listeye Ekleme
            // KullaniciSure nesnesi oluşturulur ve Listeye eklenir
            toplamSurelerListesi.add(new KullaniciSure(kullaniciId, icerideKalmaSuresi.toMinutes()));
        }

        toplamSurelerListesi.sort(Comparator.comparing(KullaniciSure::getDuration));
        return toplamSurelerListesi;
    }



    /**
     * Belirtilen toplantı ID'sine ait, son durumu GİRİŞ olan tüm katılımcılar için
     * ÇIKIŞ kaydı oluşturur ve kaydeder.
     */
    @Transactional // Tüm işlemlerin tek bir DB işlemi (transaction) içinde yapılmasını sağlar
    public GeneralResponse closeActiveSessions(GetMeetingByIdRequest request) {
        GeneralResponse response = new GeneralResponse();
        UUID meetingId = request.getId();
        // 1. Durumu GİRİŞ olan katılımcıları veritabanından bul
        // Bu sorgu, her participant_id için en son created_at'a sahip kaydın direction'ını kontrol etmelidir.
        // tracingRepository.findParticipantsWithLatestDirection(meetingId, DIRECTION_ENTRY) metodu aşağıda açıklanmıştır.
        List<UUID> activeParticipantIds = tracingRepository.findParticipantsWithLatestDirection(meetingId, 1);

        if (activeParticipantIds.isEmpty()) {
            System.out.println("Bilgi: Kapatılacak aktif oturum bulunamadı.");
            response.setResponse(false);
            return response;
        }

        // 2. Çıkış hareketleri listesini oluştur
        LocalDateTime now = LocalDateTime.now();

        List<TracingEntity> exitRecordsToSave = activeParticipantIds.stream()
                .map(participantId -> {
                    // Her aktif katılımcı için yeni bir ÇIKIŞ kaydı oluştur
                    TracingEntity exitRecord = new TracingEntity();

                    // UUID'yi otomatik ataması için id'yi null bırakıyoruz
                    // Eğer veritabanınız ID'yi otomatik atamıyorsa (auto-increment/sequence),
                    // buradan UUID.randomUUID() ile atamanız gerekir.
                    exitRecord.setId(randomUUID());
                    exitRecord.setParticipantId(participantId);
                    exitRecord.setMeetingId(meetingId);

                    // direction = 0 (ÇIKIŞ)
                    exitRecord.setDirection(0);

                    // created_at = Şimdiki zaman
                    exitRecord.setCreated_at(now);

                    return exitRecord;
                })
                .collect(Collectors.toList());

        // 3. Tüm ÇIKIŞ kayıtlarını toplu olarak kaydet (Batch Save)
        tracingRepository.saveAll(exitRecordsToSave);

        response.setResponse(true);
        return response;
    }



}
