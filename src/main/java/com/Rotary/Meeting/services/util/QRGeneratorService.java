package com.Rotary.Meeting.services.util;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import com.Rotary.Meeting.models.requestDtos.QrCodeByRoleIdRequest;
import com.Rotary.Meeting.services.ParticipantService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class QRGeneratorService {

    private ParticipantService participantService;

    private static final int QR_WIDTH = 350;
    private static final int QR_HEIGHT = 350;
    private static final String IMAGE_FORMAT = "png";
    private static final String OUTPUT_DIR = "qr_kodlar";


    public void generateQrCodesToFolder(QrCodeByRoleIdRequest request) {
        System.out.println("⏳ QR Kod Oluşturma İşlemi Başlatılıyor...");

        List<ParticipantEntity> participantEntityList = participantService.getParticipantByRRoleId(request.getId());

        for (ParticipantEntity participant : participantEntityList) {
            try {
                String qrData = getQrData(participant);
                String fileName = getFileName(participant);

                generateAndSaveQrCode(qrData, fileName);

            } catch (WriterException e) {
                System.err.println("❌ QR Kod Oluşturma Hatası: " + participant.getName() + ". Hata: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("❌ Dosya Kaydetme Hatası: " + participant.getName() + ". Hata: " + e.getMessage());
            }
        }
        System.out.println("\n🎉 İşlem Tamamlandı.");
    }

    /**
     * Verilen metin için QR kodu oluşturur ve belirtilen yola kaydeder.
     */
    public static void generateAndSaveQrCode(String data, String fileName) throws WriterException, IOException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // BitMatrix Oluşturma
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                data,
                BarcodeFormat.QR_CODE,
                QR_WIDTH,
                QR_HEIGHT,
                hints
        );

        // Dosya Yolu Oluşturma ve Klasörü Kontrol Etme
        Path dirPath = Paths.get(OUTPUT_DIR);
        if (!dirPath.toFile().exists()) {
            dirPath.toFile().mkdirs();
        }

        // Tam dosya yolu: qr_kodlar/DosyaAdi.png
        Path filePath = Paths.get(OUTPUT_DIR, fileName + "." + IMAGE_FORMAT);

        // BitMatrix'i Görüntü Olarak Kaydetme
        MatrixToImageWriter.writeToPath(bitMatrix, IMAGE_FORMAT, filePath);

        System.out.println("✅ QR Kod Oluşturuldu ve Kaydedildi: " + filePath.getFileName());
    }

    /**
     * QR koda yerleştirilecek veriyi string olarak döndürür.
     */
    public String getQrData(ParticipantEntity participant) {
        String nameSurname = participant.getName() + " " + participant.getSurname() ;

        // JSON formatında string oluşturma
        String jsonTemplate = "{\"id\": %s, \"nameSurname\": \"%s\"}";

        // Ad Soyad içindeki çift tırnak (") gibi karakterlerin JSON yapısını bozmaması için
        // escape edilmesi gerekebilir. Basitlik için sadece tırnak işaretlerini siliyoruz.
        String safeNameSurname = nameSurname.replace("\"", "").trim();

        return String.format(jsonTemplate, participant.getId().toString(), safeNameSurname);
    }

    /**
     * Dosya adı için güvenli bir isim döndürür.
     */
    public String getFileName(ParticipantEntity participant) {
        String adSoyad = participant.getName() + " " + participant.getSurname();
        return adSoyad
                .replaceAll("[^a-zA-Z0-9\\sçÇğĞıİöÖşŞüÜ]", "")
                .replace(' ', '_');
    }




}
