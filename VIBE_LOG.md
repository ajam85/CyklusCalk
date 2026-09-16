# CyklusCalk - Vibe Coding Log

## 🛠 Aktuální Architektura
Projekt je postaven na moderním Android stacku:
- **UI:** Jetpack Compose (Declarative UI)
- **DI:** Dagger Hilt (Dependency Injection)
- **DB:** Room + SQLite (Local Persistence)
- **Logic:** MVVM (ViewModel + Flow)
- **JSON:** Gson (pro serializaci tagů v DB)

## 📋 Implementované vlastnosti
1. **Kalendář:** Možnost vybrat jakýkoliv den pro zápis.
2. **Poznámky:** Textové pole s automatickým ukládáním do databáze.
3. **Příznaky (Tagy):** Možnost přepínat (toggle) vybrané štítky dne. 
4. **Reaktivita:** Data plynou z databáze skrze Flow až do UI. Změna v DB okamžitě aktualizuje obrazovku.

## 🗄 Datový model
`DayRecord`
- `date` (String, PK): Formát ISO_LOCAL_DATE (např. 2023-10-27)
- `note` (String): Libovolný text
- `tags` (List<String>): Uloženo jako JSON string díky `Converters.kt`

## 🚀 Jak pokračovat?
Aplikace je nyní "živá". Další nápady:
- [ ] Analýza (grafy/statistiky z uložených tagů)
- [ ] Export dat (např. do PDF nebo CSV)
- [ ] Notifikace (připomenutí zápisu)
- [ ] Biometrické zamykání (soukromí)
