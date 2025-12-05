package com.Rotary.Meeting.services.util;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Durum {
    LocalDateTime sonHareketZamani;
    String sonYon; // "GİRİŞ" veya "ÇIKIŞ"

    public Durum(LocalDateTime sonHareketZamani, String sonYon) {
        this.sonHareketZamani = sonHareketZamani;
        this.sonYon = sonYon;
    }

    public LocalDateTime getSonHareketZamani() { return sonHareketZamani; }
    public String getSonYon() { return sonYon; }
}