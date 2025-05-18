# Kilowatt Kapitány - (Áram lejelentő alkalmazás)

Ez az Android alkalmazás lehetővé teszi a felhasználók számára az áramóra állásának leolvasását és lejelentését online módon.

# 2. mérföldkő

## Fordítási és futtatási hibák
- **Fordítási hiba:** Nincs - A projekt sikeresen fordul 
- **Futtatási hiba:** Nincs - Az alkalmazás stabilan fut a megadott követelményekkel

## Firebase autentikáció
- Az alkalmazás Firebase autentikációt használ a felhasználók kezelésére:
  - Bejelentkezés: `app/src/main/java/com/geradev/mobilalk/LoginActivity.java`
  - Regisztráció: `app/src/main/java/com/geradev/mobilalk/RegisterActivity.java`
  - Felhasználói profil kezelése: `app/src/main/java/com/geradev/mobilalk/ProfileActivity.java`

## Adatmodell definiálása
- `MeterReading` osztály: `app/src/main/java/com/geradev/mobilalk/MeterReading.java`
- Ez az osztály tárolja a villanyóra-leolvasási adatokat:
  - `id`: A dokumentum egyedi azonosítója Firestore-ban
  - `reading`: A mérőállás értéke (kWh)
  - `date`: A leolvasás dátuma és időpontja
  - `userId`: A felhasználó azonosítója, akihez az adat tartozik
- Az adatok a Firebase Firestore adatbázisban tárolódnak és szinkronizálódnak

## Legalább 4 különböző activity használata
Az alkalmazás a következő 7 különböző activity-t használja:
1. `LoginActivity`: Bejelentkezés kezelése
2. `RegisterActivity`: Regisztráció kezelése
3. `HomeActivity`: Főképernyő, mérőóra-leolvasások listája és új leolvasás rögzítése
4. `DetailActivity`: Egy leolvasás részletes adatai, törlés funkció
5. `EditActivity`: Meglévő leolvasás szerkesztése
6. `ProfileActivity`: Felhasználói profil kezelése, kamera és helymeghatározás funkcióval
7. `SettingsActivity`: Alkalmazás beállítások, értesítések konfigurálása

## Beviteli mezők típusa
- Jelszó mező kicsillagozva: 
  - `app/src/main/res/layout/activity_login.xml` és `activity_register.xml` - `android:inputType="textPassword"`
- Email mezőnél email billentyűzet: 
  - `app/src/main/res/layout/activity_login.xml` és `activity_register.xml` - `android:inputType="textEmailAddress"`
- Mérőóra leolvasásnál numerikus billentyűzet: 
  - `app/src/main/res/layout/activity_home.xml` - `android:inputType="numberDecimal"`

## Layout típusok
1. **ConstraintLayout**:
   - Minden fő képernyőn: `activity_home.xml`, `activity_login.xml`, `activity_register.xml`, stb.

2. **CardView**:
   - A mérőóra-leolvasás űrlaphoz: `app/src/main/res/layout/activity_home.xml`
   - Az egyes leolvasások megjelenítéséhez: `app/src/main/res/layout/item_reading.xml`
   - A statisztika részhez: `app/src/main/res/layout/activity_home.xml` - statisztikai kártya

3. **RecyclerView**:
   - A leolvasások listájához: `app/src/main/res/layout/activity_home.xml`
   - Adapter megvalósítása: `app/src/main/java/com/geradev/mobilalk/ReadingsAdapter.java`

4. **LinearLayout**:
   - A kártyák belső elrendezéséhez: `app/src/main/res/layout/activity_home.xml` és egyéb layout fájlok

## Reszponzivitás
- **Különböző kijelző méreteken**:
  - A ConstraintLayout megfelelően skálázódik különböző méretű eszközökön
  - A kártyák adaptív szélessége: `layout_width="0dp"` és `app:layout_constraintStart_toStartOf="parent"` illetve `app:layout_constraintEnd_toEndOf="parent"` segítségével
  - A RecyclerView automatikusan kezeli a listaelemek megjelenítését különböző kijelző méreteken
  
- **Elforgatás esetén**:
  - A constraintlayout alapú elrendezés megtartja a megfelelő arányokat elforgatás esetén is
  - A RecyclerView megtartja az állapotát és pozícióját képernyő elforgatáskor
  - Az összes layout megfelelően skálázódik mindkét orientációban

## Legalább 2 különböző animáció használata
1. **Slide animáció**: 
   - Definíció: `app/src/main/res/anim/slide_in_animation.xml`
   - Használat: `app/src/main/java/com/geradev/mobilalk/DetailActivity.java` (94-95. sor)
   - Az elemek oldalról csúsznak be a képernyőre

2. **Fade animáció**:
   - Definíció: `app/src/main/res/anim/fade_in_animation.xml`
   - Használat: `app/src/main/java/com/geradev/mobilalk/EditActivity.java` (53-54. sor)
   - Elemek fokozatosan jelennek meg a képernyőn

3. **Card animáció**:
   - Definíció: `app/src/main/res/anim/card_animation.xml`
   - Használat: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (78-79. sor)
   - Kártyák animált megjelenése a főképernyőn

4. **Pulse animáció**:
   - Definíció: `app/src/main/res/anim/pulse_animation.xml`
   - Használat: `app/src/main/java/com/geradev/mobilalk/ProfileActivity.java` (77-78. sor)
   - Lüktető animáció a profil képernyő kártyáján

## Intentek használata: navigáció
- Navigáció a LoginActivity-ből a HomeActivity-be:
  - `app/src/main/java/com/geradev/mobilalk/LoginActivity.java` (navigateToHome metódus)

- Navigáció a HomeActivity-ből a DetailActivity-be:
  - `app/src/main/java/com/geradev/mobilalk/ReadingsAdapter.java` (onBindViewHolder metódus)

- Navigáció a ProfileActivity-be:
  - `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (profileFab gomb eseménykezelője)

- Navigáció a SettingsActivity-be:
  - `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (settingsButton gomb eseménykezelője)

- Navigáció az EditActivity-be:
  - `app/src/main/java/com/geradev/mobilalk/DetailActivity.java` (editButton gomb eseménykezelője)

## Lifecycle Hook használata
1. **onResume** a HomeActivity-ben:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (106-111. sor)
   - Funkció: Újra beállítja a valós idejű Firestore figyelőt, amikor a felhasználó visszatér az activity-re
   - Értelmes felhasználás: Biztosítja, hogy a leolvasások listája mindig naprakész legyen

2. **onPause** és **onResume** az EditActivity-ben:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/EditActivity.java` (120-148. sor)
   - Funkció: SharedPreferences használatával elmenti és visszaállítja a szerkesztési állapotot
   - Értelmes felhasználás: Megőrzi az űrlap tartalmát, ha a felhasználó elhagyja a képernyőt, majd visszatér

3. **onStart** a ProfileActivity-ben:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/ProfileActivity.java` (81-88. sor)
   - Funkció: Ellenőrzi, hogy a felhasználó továbbra is be van-e jelentkezve
   - Értelmes felhasználás: Automatikus átirányítás a bejelentkezési képernyőre, ha a felhasználói munkamenet lejárt

## Android erőforrások, amelyekhez permission kell
1. **Kamera használata**:
   - Permission: `<uses-permission android:name="android.permission.CAMERA" />` (AndroidManifest.xml)
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/ProfileActivity.java` (checkCameraPermissions és openCamera metódusok)
   - Funkció: Profilkép készítése a kamerával

2. **Helymeghatározás**:
   - Permission: `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />` és `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />` (AndroidManifest.xml)
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/ProfileActivity.java` (checkLocationPermissions és getLocation metódusok)
   - Funkció: A felhasználó jelenlegi helyzetének meghatározása, leolvasási hely rögzítése

3. **Értesítések**:
   - Permission: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` (AndroidManifest.xml)
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/NotificationReceiver.java` (showNotification metódus)
   - Funkció: Emlékeztető értesítések küldése a leolvasási határidőkről

## Rendszerszolgáltatások (háttérszolgáltatások)
1. **Értesítések (Notification)**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/NotificationReceiver.java`
   - Funkció: Értesítések megjelenítése a felhasználónak a leolvasási határidőkről
   - Használat: NotificationManager és NotificationCompat.Builder a modern értesítések létrehozásához

2. **AlarmManager**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/SettingsActivity.java` (scheduleAlarm és cancelAlarm metódusok)
   - Funkció: Időzített emlékeztetők beállítása a mérőóra-leolvasáshoz
   - Használat: AlarmManager API a rendszerszintű időzítők kezeléséhez

3. **JobScheduler**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/MeterReadingJobService.java` és `app/src/main/java/com/geradev/mobilalk/SettingsActivity.java` (scheduleJob metódus)
   - Funkció: Háttérben futó feladat, amely rendszeresen ellenőrzi a leolvasásokat és szükség esetén értesítéseket küld
   - Használat: JobScheduler API az energiahatékony háttérfeladatok ütemezéséhez

## CRUD műveletek
1. **Create (Létrehozás)**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (submitReading metódus, 201-232. sor)
   - Funkció: Új mérőóra-leolvasás létrehozása és tárolása a Firestore adatbázisban

2. **Read (Olvasás)**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (setupRealtimeReadingsListener metódus, 113-144. sor)
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/DetailActivity.java` (loadReadingDetails metódus, 98-126. sor)
   - Funkció: Mérőóra-leolvasások listázása és egy adott leolvasás részleteinek lekérése

3. **Update (Frissítés)**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/EditActivity.java` (updateReading metódus, 78-119. sor)
   - Funkció: Meglévő mérőóra-leolvasás adatainak módosítása

4. **Delete (Törlés)**:
   - Implementáció: `app/src/main/java/com/geradev/mobilalk/DetailActivity.java` (deleteReading metódus, 137-154. sor)
   - Funkció: Mérőóra-leolvasás törlése az adatbázisból

## Komplex Firestore lekérdezések
1. **Felhasználó szerinti szűrés, dátum szerinti rendezés és limitálás**:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (setupRealtimeReadingsListener metódus, 113-144. sor)
   - Lekérdezés: 
     ```java
     db.collection("readings")
        .whereEqualTo("userId", currentUserId)
        .orderBy("date", Query.Direction.DESCENDING)
        .limit(20)
     ```
   - Funkció: A felhasználó leolvasásainak lekérése, időrendi sorrendben, maximum 20 elem

2. **Nagy fogyasztású mérőállások lekérdezése**:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (queryHighConsumptionReadings metódus, 146-156. sor)
   - Lekérdezés:
     ```java
     db.collection("readings")
        .whereEqualTo("userId", currentUserId)
        .whereGreaterThan("reading", 10000)
        .orderBy("reading", Query.Direction.DESCENDING)
     ```
   - Funkció: A felhasználó magas fogyasztású (10000 kWh fölötti) leolvasásainak lekérése, fogyasztási mennyiség szerinti csökkenő sorrendben

3. **Adott időszak szerinti mérőállások lekérdezése**:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (queryReadingsByDateRange metódus, 158-168. sor)
   - Lekérdezés:
     ```java
     db.collection("readings")
        .whereEqualTo("userId", currentUserId)
        .whereGreaterThanOrEqualTo("date", startDate)
        .whereLessThanOrEqualTo("date", endDate)
        .orderBy("date", Query.Direction.ASCENDING)
     ```
   - Funkció: A felhasználó egy adott időszakban (kezdő- és végdátum között) rögzített leolvasásainak lekérése, időrendi sorrendben

4. **Átlagos fogyasztás számítása**:
   - Fájl: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (calculateAverageConsumption metódus, új implementáció)
   - Lekérdezés:
     ```java
     db.collection("readings")
        .whereEqualTo("userId", currentUserId)
        .orderBy("date", Query.Direction.ASCENDING)
     ```
   - Funkció: A felhasználó leolvasásainak idő szerinti sorba rendezése, majd az első és utolsó leolvasás közti fogyasztás és időtartam alapján átlagos napi fogyasztás számítása

## Továbbfejlesztett komponensek a 2. mérföldkőben
1. **Kibővített naplózási rendszer**:
   - Minden komponensben részletes Log.d, Log.e és Log.w hívások segítik a hibakeresést
   - TAG konstansok az aktivitásokban a naplóbejegyzések egyszerű szűréséhez
   - Kivételek naplózása stacktrace-szel együtt a hibaelhárítás megkönnyítésére

2. **Statisztikai funkció**:
   - Az átlagos fogyasztást számító funkció vizuálisan megjeleníti az eredményt
   - A beépített animációk dinamikusabbá teszik a felhasználói élményt
   - A számításokhoz komplex Firestore lekérdezések szükségesek

3. **Továbbfejlesztett MeterReadingJobService**:
   - Statisztikai adatok gyűjtése és elemzése a háttérben
   - Intelligens értesítési rendszer, amely csak szükség esetén küld emlékeztetőt
   - Az alkalmazáshoz egy energiahatékony háttérszolgáltatás tartozik

# 1. mérföldkő

## Demó felhasználói adatok
Az alkalmazásba beléphetsz az alábbi tesztfiókkal:
- Email: `test@test.com`
- Jelszó: `test123`

Vagy regisztrálhatsz egy új fiókot a regisztrációs képernyőn.

## Program futtatása

### Rendszerkövetelmények
- Android Studio Arctic Fox vagy újabb
- JDK 19 vagy újabb
- Gradle 8.11.1 vagy újabb
- Firebase projekt

### Futtatás lépései
1. Klónozd a projektet
2. Nyisd meg Android Studioban
3. Szinkronizáld a Gradle-t
4. Futtasd az alkalmazást emulátoron vagy valós eszközön

## Technikai követelmények és megvalósítások

### Fordítási és futtatási hibák
- **Fordítási hiba:** Nincs - A projekt sikeresen fordul a Gradle 8.11.1 verzióval és JDK 19-cel
- **Futtatási hiba:** Nincs - Az alkalmazás megfelelően fut Android eszközökön

### Firebase autentikáció
- A Firebase autentikáció megvalósítása az alábbi fájlokban található:
  - Bejelentkezés: `app/src/main/java/com/geradev/mobilalk/LoginActivity.java` (teljes fájl)
  - Regisztráció: `app/src/main/java/com/geradev/mobilalk/RegisterActivity.java` (teljes fájl)
  - Firebase inicializáció: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (47-55. sor)

### Beviteli mezők típusa
- Jelszó mező kicsillagozva: `app/src/main/res/layout/activity_login.xml` (android:inputType="textPassword")
- Email mezőnél email billentyűzet: `app/src/main/res/layout/activity_login.xml` (android:inputType="textEmailAddress")

### Layout típusok
1. **ConstraintLayout**:
   - A főoldalon: `app/src/main/res/layout/activity_home.xml`
   - A bejelentkezési képernyőn: `app/src/main/res/layout/activity_login.xml`

2. **RecyclerView**:
   - A leolvasások listájához: `app/src/main/res/layout/activity_home.xml`
   - Adapter megvalósítása: `app/src/main/java/com/geradev/mobilalk/ReadingsAdapter.java`

3. **CardView**:
   - A mérőóra-leolvasás űrlaphoz: `app/src/main/res/layout/activity_home.xml`
   - Az egyes leolvasások megjelenítéséhez: `app/src/main/res/layout/item_reading.xml`

### Reszponzivitás
- **Különböző kijelzőméreteken**:
  - A ConstraintLayout megfelelően skálázódik különböző méretű eszközökön
  - A mérőóra-leolvasási képernyő dinamikusan alkalmazkodik a képernyő méretéhez
  
- **Elforgatás esetén**:
  - Az elforgatás kezelése: Az összes layout megfelelően kezeli a képernyő elforgatását
  - A RecyclerView megtartja az állapotát elforgatás közben

### Animáció
- A mérőóra-leolvasási kártya animációja: 
  - Animáció definíció: `app/src/main/res/anim/card_animation.xml`
  - Animáció alkalmazása: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (79-80. sor)
  - Az animáció egy enyhe felúszást és elhalványulást valósít meg

### Intentek és navigáció
- Activityk közötti navigáció:
  - Bejelentkezésből a főoldalra: `app/src/main/java/com/geradev/mobilalk/LoginActivity.java` 
  - Regisztrációból bejelentkezéshez: `app/src/main/java/com/geradev/mobilalk/RegisterActivity.java`
  - Főoldalról kijelentkezés: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (136-141. sor és a profil gomb esetén: 86-91. sor)

### Valós idejű adatfrissítés
- Firebase Firestore valós idejű adatfrissítés: 
  - Implementáció: `app/src/main/java/com/geradev/mobilalk/HomeActivity.java` (setupRealtimeReadingsListener metódus, 98-135. sor)
  - A leolvasások automatikusan frissülnek, amikor új adatot adunk hozzá

## Alkalmazás funkciói
- Felhasználói regisztráció és bejelentkezés
- Áramóra-leolvasás rögzítése
- Korábbi leolvasások megtekintése időrendi sorrendben
- Kijelentkezés a profil gombbal