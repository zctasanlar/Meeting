package com.Rotary.Meeting.services;

import com.Rotary.Meeting.models.dto.TracingEntity;
import com.Rotary.Meeting.models.dto.Transaction;
import com.Rotary.Meeting.models.requestDtos.GetMeetingByIdRequest;
import com.Rotary.Meeting.models.requestDtos.LogDurationPeriodRequest;
import com.Rotary.Meeting.models.requestDtos.LogTransactionRequest;
import com.Rotary.Meeting.models.requestDtos.LogUserDurationRequest;
import com.Rotary.Meeting.models.responseDtos.AllTransactionsListResponse;
import com.Rotary.Meeting.models.responseDtos.GeneralResponse;
import com.Rotary.Meeting.repositories.TracingRepository;
import com.Rotary.Meeting.services.util.Durum;
import com.Rotary.Meeting.services.util.KullaniciSure;
import com.Rotary.Meeting.services.util.MolaAnalizServisi;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    private MolaAnalizServisi molaAnalizServisi ;

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
        //List<TracingEntity> hareketler = this.tracingRepository.findByMeetingIdAndCreated_atBetweenOrderByCreated_at(request.getMeetingId(), baslangicZamani, bitisZamani);
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

    public List<KullaniciSure> calculateTotalInsideDuration(LogUserDurationRequest request) {

        // ... (Tanımlamalar ve Gruplama/Sıralama Adımları aynı kalır)

        LocalDateTime baslangicZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getStartTime();
        LocalDateTime bitisZamani = meetingService.getMeetingById(request.getMeetingId()).getMeeting().getEndTime();

        // NOT: Gerçek projede veritabanı sorgusunu filtreleyerek yapmanız (yorum satırındaki gibi) daha performanslı olacaktır.
        List<TracingEntity> hareketler = this.tracingRepository.findAll();

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
                Duration icerideKalinanAnlikSure = Duration.between(girisZamani, bitisZamani);
                icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
            }

            // Sonucu Listeye Ekleme
            // KullaniciSure nesnesi oluşturulur ve Listeye eklenir
            toplamSurelerListesi.add(new KullaniciSure(kullaniciId, icerideKalmaSuresi.toMinutes()));
        }

        return toplamSurelerListesi;
    }


/*
    public Map<UUID, Duration> hesaplaToplamIcerideKalmaSuresiHibrit(
            LogUserDurationRequest request,
            LocalDateTime molaBaslangici,
            LocalDateTime molaBitisZamani)
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


        // 1. Molaya hiç çıkmamış kişileri tespit et
        List<UUID> molayaCikmayanlar = findParticipantsWhoDidNotExitForBreak(...);
        Duration molaSuresi = Duration.between(molaBaslangici, molaBitisZamani);

        // 2. Her kullanıcı için toplam süreyi standart yöntemle hesapla
        for (Map.Entry<UUID, List<TracingEntity>> entry : gruplanmisHareketler.entrySet()) {
            UUID kullaniciId = entry.getKey();
            // ... (Standart süre hesaplama mantığı ile icerideKalmaSuresi bulunur) ...

            // 3. Molada kalanlar için Mola süresini ekle (ÖZEL KURAL ENTEGRASYONU)
            if (molayaCikmayanlar.contains(kullaniciId)) {
                icerideKalmaSuresi = icerideKalmaSuresi.plus(molaSuresi);
            }

            toplamSureler.put(kullaniciId, icerideKalmaSuresi);
        }

        return toplamSureler;
    }



    public List<UUID> findParticipantsWhoDidNotReturnAfterBreak(
            UUID meetingId,
            LocalDateTime molaBitisZamani,
            LocalDateTime kontrolZamani)
    {
        // 1. Mola başlangıcından KONTROL ZAMANINA kadar HİÇ GİRİŞ yapmamış kişileri bul.
        // Bu sorgu, meetingId'ye ait TARTIŞMALI tüm participant ID'lerini döndürmelidir.

        // Basit bir yaklaşımla: Mola Bitişi ve Kontrol Anı arasında GİRİŞ yapmamış herkesi filtrele.

        // Adım 1: Toplantıdaki tüm katılımcıları al (Daha önce GİRİŞ yapmış olanlar)
        List<UUID> allParticipants = tracingRepository.findAllParticipantsByMeetingId(meetingId);

        // Adım 2: Mola bitimi ile kontrol anı arasında GİRİŞ yapmış olanları bul (Geri Dönenler)
        List<UUID> returnedParticipants = tracingRepository.findParticipantsWithEntryBetween(
                meetingId,
                molaBitisZamani,
                kontrolZamani,
                1
        );

        // Adım 3: Geri dönenleri tüm katılımcılardan çıkar
        java.util.Set<UUID> returnedSet = new java.util.HashSet<>(returnedParticipants);

        List<UUID> didNotReturn = allParticipants.stream()
                .filter(participantId -> !returnedSet.contains(participantId))
                .collect(Collectors.toList());

        return didNotReturn; // Bu listedeki kişilerin moladan sonra dönmediği varsayılır.
    }
*/
    /**
     * Belirtilen toplantı ID'sine ait, son durumu GİRİŞ olan tüm katılımcılar için
     * ÇIKIŞ kaydı oluşturur ve kaydeder.
     *

     * @return Kaydı atılan ÇIKIŞ hareketlerinin sayısı.
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

    /**
     * Toplantı molasına hiç çıkmamış (otomatik ÇIKIŞ kuralından etkilenmemiş) kişileri bulur.
     *
     * @param meetingId Toplantı ID'si.
     * @param molaBaslangici Mola başlangıç zamanı.
     * @param molaBitisZamani Mola bitiş zamanı.
     * @return Molaya hiç çıkmamış (toplantı salonunda kalan) katılımcıların ID listesi.

    public List<UUID> findParticipantsWhoDidNotExitForBreak(
            UUID meetingId,
            LocalDateTime molaBaslangici,
            LocalDateTime molaBitisZamani)
    {
        // 1. Mola başlangıcından önceki son hareketleri GİRİŞ olanları bul (Koşul 1)
        // Bu, molaya girerken hala içeride görünen herkesin listesidir.
        List<UUID> activeBeforeBreak = tracingRepository.findLastActiveParticipantsBefore(
                meetingId,
                molaBaslangici,
                1
        );

        if (activeBeforeBreak.isEmpty()) {
            return List.of(); // Hiç kimse içeride değildi, boş listeyi dön.
        }

        // 2. Mola bitişinden sonra GİRİŞ kaydı atan tüm kişileri bul (Moladan dönenler)
        // Bu sorgu, mola bittikten sonra (molaBitisZamani sonrası) herhangi bir GİRİŞ yapan herkesi listeler.
        List<UUID> enteredAfterBreak = tracingRepository.findParticipantsWithEntryBetween(
                meetingId,
                molaBitisZamani,
                LocalDateTime.now(), // Şu ana kadar olan tüm girişleri kontrol et
                1
        );

        // 3. İki listeyi karşılaştır (Koşul 2'yi kontrol et)
        // Molaya çıkmayanlar = (Mola öncesinde içeride olanlar) - (Moladan sonra GİRİŞ yapanlar)
        // Set'leri kullanarak farkı bulmak en hızlı yoldur.

        // Moladan dönenleri hızlı arama için Set'e çevir
        java.util.Set<UUID> enteredSet = new java.util.HashSet<>(enteredAfterBreak);

        // activeBeforeBreak listesini filtrele:
        // Eğer katılımcı activeBeforeBreak'te varsa VE enteredSet'te yoksa, molaya çıkmamıştır.
        List<UUID> didNotExit = activeBeforeBreak.stream()
                .filter(participantId -> !enteredSet.contains(participantId))
                .collect(Collectors.toList());

        return didNotExit;
    }
     */
/*

    public Map<UUID, Duration> hesaplaToplamIcerideKalmaSuresiHibrit(
            Map<UUID, List<TracingEntity>> gruplanmisHareketler,
            UUID meetingId, // Mola analizi için toplantı ID'sine ihtiyacımız var
            LocalDateTime baslangicZamani,
            LocalDateTime molaBaslangici,
            LocalDateTime molaBitisZamani,
            LocalDateTime bitisZamani)
    {

        // 1. Mola analizi (Entegrasyon noktası)
        // Molaya hiç çıkmayıp içeride kalanların listesini bul.
        List<UUID> molayaCikmayanList = molaAnalizServisi.findParticipantsWhoDidNotExitForBreak(
                meetingId,
                molaBaslangici,
                molaBitisZamani
        );
        // Hızlı arama için Set kullanıyoruz
        Set<UUID> molayaCikmayanlar = new java.util.HashSet<>(molayaCikmayanList);

        Duration molaSuresi = Duration.between(molaBaslangici, molaBitisZamani);
        Map<UUID, Duration> toplamSureler = new HashMap<>();

        // 2. Her kullanıcı için döngü ve Standart Süre Hesaplaması
        for (Map.Entry<UUID, List<TracingEntity>> entry : gruplanmisHareketler.entrySet()) {
            UUID kullaniciId = entry.getKey();
            List<TracingEntity> kullaniciHareketleri = entry.getValue();

            Duration icerideKalmaSuresi = Duration.ZERO;
            LocalDateTime girisZamani = null;

            for (TracingEntity hareket : kullaniciHareketleri) {
                LocalDateTime hareketZamani = hareket.getCreated_at();
                int direction = hareket.getDirection();

                // Yalnızca toplantı sınırları içindeki hareketleri hesaba kat
                if (hareketZamani.isBefore(baslangicZamani) || hareketZamani.isAfter(bitisZamani)) {
                    continue;
                }

                // NOT: Otomatik ÇIKIŞ logları bu döngüde normal bir ÇIKIŞ gibi işlenir.

                if (direction == DIRECTION_ENTRY) { // GİRİŞ
                    if (girisZamani == null) {
                        girisZamani = hareketZamani.isAfter(baslangicZamani) ? hareketZamani : baslangicZamani;
                    }
                } else if (direction == DIRECTION_EXIT) { // ÇIKIŞ (Manuel veya Otomatik)
                    if (girisZamani != null) {
                        LocalDateTime cikisZamani = hareketZamani.isBefore(bitisZamani) ? hareketZamani : bitisZamani;

                        Duration icerideKalinanAnlikSure = Duration.between(girisZamani, cikisZamani);
                        icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);

                        girisZamani = null;
                    }
                }
            }

            // 3. Kapanış Kontrolü (Toplantı sonunda hala içerideyse)
            if (girisZamani != null) {
                Duration icerideKalinanAnlikSure = Duration.between(girisZamani, bitisZamani);
                icerideKalmaSuresi = icerideKalmaSuresi.plus(icerideKalinanAnlikSure);
            }

            // 4. KURAL UYGULAMASI (Molaya Çıkmayanlara Eksik Süreyi Ekle)
            if (molayaCikmayanlar.contains(kullaniciId)) {
                System.out.println("Ekleme: Kullanıcı " + kullaniciId + " molada kalmıştır. " + molaSuresi.toMinutes() + " dk ekleniyor.");
                icerideKalmaSuresi = icerideKalmaSuresi.plus(molaSuresi);
            }

            toplamSureler.put(kullaniciId, icerideKalmaSuresi);
        }

        return toplamSureler;
    }
 */
}
