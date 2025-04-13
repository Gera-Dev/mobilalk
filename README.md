# Áramlejelentő Alkalmazás

Ez az Android alkalmazás lehetővé teszi a felhasználók számára az áramóra állásának leolvasását és lejelentését online módon.


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