# Vault — Bütçe Takip Uygulaması

Vault, Android platformu için geliştirilmiş kişisel finans yönetim uygulamasıdır. Gelir ve giderlerinizi kayıt altına alın, kategori bazlı bütçe limitleri belirleyin, tasarruf hedefleri oluşturun ve finansal durumunuzu tek ekranda takip edin.

---

## İçindekiler

- [Özellikler](#özellikler)
- [Ekran Görüntüleri](#ekran-görüntüleri)
- [Teknik Altyapı](#teknik-altyapı)
- [Proje Yapısı](#proje-yapısı)
- [Kurulum](#kurulum)
- [Veritabanı Şeması](#veritabanı-şeması)
- [Renk Paleti & Tasarım](#renk-paleti--tasarım)
- [Bağımlılıklar](#bağımlılıklar)
- [Geliştirme Notları](#geliştirme-notları)

---

## Özellikler

### Kullanıcı Girişi & Hesap Yönetimi
- **Kayıt / Giriş** — E-posta ve şifre ile yerel hesap oluşturma ve giriş yapma
- **SHA-256 şifre hash'leme** — Şifreler cihazda güvenli şekilde saklanır
- **Oturum yönetimi** — Uygulama yeniden açıldığında giriş durumu korunur
- **Şifre değiştirme** — Mevcut şifre doğrulanarak yeni şifre belirlenir
- **Profil düzenleme** — Ad, soyad ve e-posta güncellenebilir
- **Profil fotoğrafı** — Galeriden fotoğraf seçme; uzun basınca kaldırma/değiştirme seçeneği

### Ana Sayfa (Dashboard)
- **Animasyonlu bakiye sayacı** — Net bakiye, toplam gelir ve toplam gider değerleri açılışta animasyonla gösterilir
- **Pasta grafik** — Harcama kategorilerine göre renkli dağılım (MPAndroidChart)
- **Son işlemler listesi** — Kategori ikonu ve renkli tutar göstergesiyle her işlem kartı
- **Hızlı ekleme butonu** — FAB ile doğrudan işlem ekleme ekranına geçiş
- **Kategori filtreleme** — İşlem listesini kategori ve türe göre filtrele

### İşlem Yönetimi
- **Gelir / Gider ekleme** — Tutar, başlık, kategori, not ve tarih ile tam kayıt
- **8 kategori** — Maaş 💼, Kira 🏠, Fatura 📄, Market 🛒, Ulaşım 🚌, Eğlence 🎮, Sağlık 💊, Diğer 💰
- **İşlem detay ekranı** — Mevcut işlemi düzenleme ve silme
- **Renk kodlaması** — Gelirler yeşil (↑), giderler kırmızı (↓) olarak gösterilir

### Bütçe Limitleri
- **Kategori bazlı aylık limit** — Her kategori için ayrı limit belirleme
- **İlerleme çubuğu** — Harcanan / Limit oranı görsel olarak takip edilir
- **Renk uyarısı** — %0–60 yeşil, %60–90 turuncu, %90+ kırmızı

### Hedefler
- **Tasarruf hedefi oluşturma** — Hedef adı, hedef tutar ve mevcut birikim
- **Para ekleme** — Mevcut birikime para ekleme dialog'u
- **İlerleme çubuğu** — Hedefe ne kadar yaklaşıldığı görsel olarak gösterilir
- **Hedef silme** — Uzun basarak silme

### Profil & Ayarlar
- **İstatistikler** — Toplam işlem sayısı, kullanılan kategori sayısı, aktif hedef sayısı
- **Bildirim tercihi** — Açma/kapama toggle (SharedPreferences'a kaydedilir)
- **Veri dışa aktarma** — Tüm işlemleri CSV veya PDF formatında dışa aktar
- **Tüm verileri sil** — Onay dialoguyla tüm işlem, hedef ve limitleri temizle
- **Çıkış yapma** — Oturumu kapatma ve giriş ekranına yönlendirme

### Dışa Aktarma
- **CSV** — Başlık satırlı elektronik tablo formatı
- **PDF** — Özet ve tablo formatında rapor; paylaşma intent'i ile doğrudan iletme

---

## Teknik Altyapı

| Başlık | Detay |
|---|---|
| **Platform** | Android |
| **Dil** | Java |
| **Min SDK** | API 21 (Android 5.0 Lollipop) |
| **Target SDK** | API 34 (Android 14) |
| **Veritabanı** | SQLite (`SQLiteOpenHelper`) |
| **Mimari** | Fragment tabanlı tek Activity (MainActivity) |
| **UI** | XML Layout, Material Design 3 |
| **Grafik** | MPAndroidChart |
| **Kimlik Doğrulama** | Yerel / SharedPreferences + SHA-256 |

---

## Proje Yapısı

```
app/src/main/
├── java/com/example/proje/
│   ├── auth/
│   │   └── UserManager.java          # Kayıt, giriş, şifre, profil yönetimi
│   ├── adapter/
│   │   ├── TransactionAdapter.java   # İşlem listesi adapter (kategori ikonu, renk)
│   │   └── GoalAdapter.java          # Hedefler listesi adapter
│   ├── db/
│   │   └── DatabaseHelper.java       # SQLite CRUD, agregasyon, filtreleme sorguları
│   ├── fragment/
│   │   ├── HomeFragment.java         # Dashboard, grafik, filtre
│   │   ├── AddTransactionFragment.java # İşlem ekleme formu
│   │   ├── BudgetLimitFragment.java  # Bütçe limit listesi
│   │   ├── GoalFragment.java         # Hedefler listesi
│   │   └── ProfileFragment.java      # Profil, ayarlar, export
│   ├── helper/
│   │   └── ExportHelper.java         # CSV ve PDF dışa aktarma
│   ├── model/
│   │   ├── Transaction.java          # İşlem veri modeli
│   │   └── Goal.java                 # Hedef veri modeli
│   ├── LoginActivity.java            # Giriş / Kayıt ekranı
│   ├── MainActivity.java             # Fragment navigasyon merkezi
│   ├── SplashActivity.java           # Açılış animasyonu + oturum kontrolü
│   └── TransactionDetailActivity.java # İşlem düzenleme ekranı
│
└── res/
    ├── layout/                       # XML ekran ve bileşen layoutları
    ├── drawable/                     # Şekiller, gradient arka planlar, ikonlar
    ├── menu/                         # Bottom nav ve toolbar menüleri
    ├── values/
    │   ├── colors.xml                # Vault renk sistemi
    │   ├── strings.xml               # Türkçe metin kaynakları
    │   └── themes.xml                # Material 3 tema ve stiller
    └── anim/                         # Fragment geçiş animasyonları
```

---

## Kurulum

### Gereksinimler
- Android Studio Hedgehog (2023.1.1) veya üzeri
- JDK 8+
- Android SDK API 34

### Adımlar

1. Repoyu klonlayın:
   ```bash
   git clone https://github.com/KULLANICI_ADI/vault-butce-takip.git
   ```

2. Android Studio'da açın:
   ```
   File → Open → vault-butce-takip klasörünü seçin
   ```

3. Gradle sync bekleyin (bağımlılıklar otomatik indirilir)

4. Bir emülatör veya fiziksel cihaz seçip **Run** edin

> Uygulama ilk açılışta kayıt ekranına yönlendirir. Hesap oluşturduktan sonra doğrudan ana sayfaya geçilir.

---

## Veritabanı Şeması

### `transactions`
| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | INTEGER PK | Otomatik artan kimlik |
| `title` | TEXT | İşlem başlığı |
| `amount` | REAL | Tutar |
| `category` | TEXT | Kategori adı |
| `type` | TEXT | `gelir` veya `gider` |
| `note` | TEXT | İsteğe bağlı not |
| `date` | TEXT | `yyyy-MM-dd` formatı |

### `goals`
| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | INTEGER PK | Otomatik artan kimlik |
| `title` | TEXT | Hedef adı |
| `target_amount` | REAL | Hedef tutar |
| `current_amount` | REAL | Mevcut birikim |
| `note` | TEXT | İsteğe bağlı not |

### `budget_limits`
| Sütun | Tip | Açıklama |
|---|---|---|
| `id` | INTEGER PK | Otomatik artan kimlik |
| `category` | TEXT UNIQUE | Kategori adı |
| `monthly_limit` | REAL | Aylık harcama limiti |

---

## Renk Paleti & Tasarım

Uygulama koyu (dark) temaya sahiptir. Tüm renkler `res/values/colors.xml` dosyasında tanımlıdır.

| Değişken | Hex | Kullanım |
|---|---|---|
| `vault_bg` | `#0E111D` | Arka plan |
| `vault_card` | `#171B2F` | Kart arka planı |
| `vault_primary` | `#7F7CFD` | Accent, butonlar, seçili nav |
| `grad_blue_start` | `#3B82F6` | Gradient başlangıç |
| `grad_blue_end` | `#2DD4BF` | Gradient bitiş |
| `income_mint` | `#00E5A0` | Gelir göstergesi |
| `expense_pink` | `#FF6B8A` | Gider göstergesi |
| `warning_orange` | `#FFB347` | Bütçe uyarısı |
| `gray_text` | `#8A8D9F` | İkincil metin |
| `nav_inactive` | `#4E546A` | Pasif nav ikonu |

---

## Bağımlılıklar

```kotlin
// UI
implementation(libs.androidx.appcompat)
implementation(libs.material)                  // Material Design 3
implementation(libs.androidx.constraintlayout)
implementation(libs.androidx.recyclerview)
implementation(libs.androidx.cardview)
implementation(libs.androidx.fragment)

// Grafik
implementation(libs.mpandroidchart)            // com.github.PhilJay:MPAndroidChart:v3.1.0
```

MPAndroidChart için `settings.gradle`'da JitPack repository tanımlı olmalıdır:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## Geliştirme Notları

### Kimlik Doğrulama
Uygulama internet bağlantısı gerektirmez. Kullanıcı bilgileri ve oturum durumu `SharedPreferences`'ta saklanır. Şifreler SHA-256 ile hashlenerek kaydedilir, düz metin olarak hiçbir yerde tutulmaz.

### Veri Saklama
Tüm veriler cihaz üzerindeki SQLite veritabanında (`butce.db`) saklanır. Dışa aktarma özelliği ile veriler harici depolama alanına CSV/PDF olarak kaydedilebilir.

### Fragment Navigasyon
`MainActivity` bir `BottomNavigationView` ile 5 fragment'ı yönetir: Home, Budget, Add, Goals, Profile. Fragment geçişlerinde sağ/sol slide animasyonları uygulanır.

### Desteklenen Android Sürümleri
- **Minimum:** Android 5.0 (API 21)
- **Hedef:** Android 14 (API 34)
- Galeri erişimi için Android 13+ cihazlarda `READ_MEDIA_IMAGES` izni, eski cihazlarda `READ_EXTERNAL_STORAGE` izni kullanılır.

---

## Lisans

Bu proje MIT lisansı ile lisanslanmıştır.
