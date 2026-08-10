# PremoLux SMS Relay

Bank tasdiqlash kodlarini avtomatik ilib, PremoLux serveriga
yetkazadigan Android ilova. Faqat Android (iOS da SMS o'qishga
ruxsat yo'q).

## Ishlash tamoyili

```
Bank SMS keladi
    │
    ▼
SmsReceiver — tizim signalini tutib oladi
    │
    ▼
SmsFilter — ikki shart tekshiriladi:
    · yuboruvchi nomi bank ro'yxatida bormi
    · matnda kalit so'z bormi (kod, code, tasdiqlash...)
    │  ikkalasi ham to'g'ri bo'lsagina davom etadi
    ▼
RelayApi — serverga HTTPS orqali yuboradi:
    { token, sender, body, code, received_at }
    │
    ▼
Server — buyurtma bilan moslashtirib,
    Playwright orqali bank sahifasiga kiritadi
```

## Loyihani ochish

1. Android Studio (Koala yoki undan yangi versiya)
2. `File → Open` → shu papkani tanlang
3. Gradle sinxronlanishini kuting
4. `app` modulini qurilma yoki emulyatorda ishga tushiring

## Sozlash (birinchi ishga tushirishda)

1. **Server** maydoniga backend manzilini kiriting
   (`Prefs.kt` dagi `DEFAULT_URL` ni ham shu manzilga o'zgartiring)
2. **Qurilma tokeni** — har mijoz uchun serverda yaratiladigan
   noyob kalit (backend qismida generatsiya qilinadi)
3. **Bank nomlari** va **Kalit so'zlar** — kerak bo'lsa
   qo'shimcha davlat/bank uchun moslang
4. **Saqlash** tugmasini bosing
5. Yuqoridagi kalitni yoqing — SMS ruxsati so'raladi

## Xavfsizlik

- Ilova faqat filtrga to'g'ri kelgan SMS larni o'qiydi va yuboradi
- Hech qanday SMS qurilmada saqlanmaydi (faqat yuborilib, unutiladi)
- Zaxira nusxaga sozlamalar kiritilmaydi (`backup_rules.xml`)
- Barcha tarmoq so'rovlari HTTPS orqali, token bilan autentifikatsiya qilinadi

## Google Play haqida eslatma

Bu ilova `RECEIVE_SMS` ruxsatini so'raganligi sabab Google Play
Store siyosatiga zid keladi (faqat "standart SMS ilovalari" bunday
ruxsat oladi). Shuning uchun APK fayl to'g'ridan-to'g'ri, PremoLux
ilovasi ichidan yuklab olinadi va qo'lda o'rnatiladi.

Mijozga o'rnatishdan oldin nima uchun bu ruxsat kerakligini
tushuntirish tavsiya etiladi.

## Backend bilan kelishiladigan API

**So'rov:** `POST {server}/relay/sms`

```json
{
  "token": "qurilma-tokeni",
  "device": "Samsung SM-A125F",
  "sender": "NBU",
  "body": "Your verification code is 390529...",
  "code": "390529",
  "received_at": 1723130400000
}
```

**Tekshirish:** `GET {server}/relay/ping` — `Authorization: Bearer {token}`
bilan, ulanish bor-yo'qligini bilish uchun.

---

## APK ni GitHub orqali build qilish (kompyutersiz)

Bu loyihada tayyor GitHub Actions sozlangan — kod yuklansa,
GitHub o'zi Android SDK bilan APK yig'ib beradi. Kompyuterda
hech narsa o'rnatish shart emas.

**Qadamlar:**

1. [github.com](https://github.com) da hisob oching (bepul)
2. Yangi repository yarating (masalan `premolux-relay`), **Private** qilib qo'yish tavsiya etiladi
3. Ushbu papkadagi barcha fayllarni shu repoga yuklang:
   - Sayt orqali: repo sahifasida "Add file → Upload files" tugmasi, papkani sudrab tashlang
   - Yoki: `git init && git add . && git commit -m "init" && git push`
4. Repo sahifasida yuqoridagi **Actions** bo'limiga o'ting
5. "Build APK" workflow avtomatik ishga tushadi (yashil ✓ belgisini kuting, 3-5 daqiqa)
6. Tugagach, workflow ichida **Artifacts** bo'limidan `premolux-sms-relay` ni yuklab oling — bu ZIP ichida `app-debug.apk` bor

Shu APK ni telefonga o'tkazib o'rnatishingiz mumkin.
