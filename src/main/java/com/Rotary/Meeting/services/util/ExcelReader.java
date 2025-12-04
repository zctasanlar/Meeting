package com.Rotary.Meeting.services.util;

import com.Rotary.Meeting.models.dto.ParticipantEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExcelReader {

    // Excel'deki sütun indekslerinin sabitleri (Sıfırdan başlar)
    private static final int COL_ID = 0;      // 1. Sütun: ID
    private static final int COL_AD_SOYAD = 1; // 2. Sütun: Ad Soyad
    private static final int COL_SICIL_NO = 2; // 3. Sütun: Sicil No

    /**
     * Belirtilen yoldaki Excel dosyasını okur ve Personel listesi döndürür.
     * @param filePath Okunacak Excel dosyasının yolu (örn: "personel.xlsx")
     * @return Excel'deki verileri içeren List<Personel>
     */
    public static List<ParticipantEntity> readExcelFile(String filePath) throws IOException {
        List<ParticipantEntity> personelList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             // xlsx dosyaları için XSSFWorkbook, xls dosyaları için HSSFWorkbook kullanılır
             Workbook workbook = new XSSFWorkbook(fis)) {

            // İlk sayfayı al (genellikle 0. indeks)
            Sheet sheet = workbook.getSheetAt(0);

            // Satırları dolaşmaya başla. Başlık satırını atlamak için 1'den başla (varsayarsak)
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // Boş satırları atla
                }

                try {
                    // Verileri oku
                    int id = (int) row.getCell(COL_ID).getNumericCellValue();
                    String adSoyad = row.getCell(COL_AD_SOYAD).getStringCellValue();
                    String sicilNo = row.getCell(COL_SICIL_NO).getStringCellValue();

                    UUID baskan = UUID.fromString("1fe9b7f5-c0eb-432c-9e78-8d517d861933");

                    // Yeni Personel nesnesi oluştur ve listeye ekle
                    ParticipantEntity personel = new ParticipantEntity(UUID.randomUUID(),adSoyad,adSoyad, sicilNo,baskan);
                    personelList.add(personel);

                } catch (NullPointerException | IllegalStateException e) {
                    System.err.println("❗ Hata: Satır " + (i + 1) + " içindeki hücre verileri eksik veya yanlış formatta. Atlanıyor.");
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Excel Dosyası Okuma Hatası: " + e.getMessage());
            throw e;
        }

        return personelList;
    }
}




