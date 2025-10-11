package exercise.Group.enums;

public enum MemberSort {
    DEFAULT, // mevcut davranış (ADMIN önde, geri kalanı isme göre vs.)
    LAST_LOGIN, // son giriş zamanı (en yeni/en eski)
    TOTAL_APP_MINUTES, // uygulamada geçirilen toplam dakika
    EXERCISE_COMPLETIONS, // egzersiz tamamlama sayısı
    BADGE_SCORE // hedef başarım/rozet skoru
}
