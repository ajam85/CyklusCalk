package com.example.cykluscalk.ui

data class FetalDevelopmentMonth(
    val month: Int,
    val weeks: IntRange,
    val size: String,
    val developedOrgans: String,
    val willForm: String,
    val reactions: String
)

data class Trimester(
    val name: String,
    val range: IntRange,
    val months: List<FetalDevelopmentMonth>
)

val fetalDevelopmentData = listOf(
    Trimester(
        name = "1. trimestr",
        range = 1..13,
        months = listOf(
            FetalDevelopmentMonth(
                month = 1,
                weeks = 1..4,
                size = "Jako makové semínko (~1–2 mm).",
                developedOrgans = "Dochází k oplodnění a vzniku blastocysty, vytváří se neuralní trubice (základ mozku a míchy) a primitivní oběhový systém.",
                willForm = "Základy srdečních oddílů, obličejové struktury, pupeny končetin.",
                reactions = "Plod nereaguje na vnější podněty; srdeční tkáň začíná rytmicky pulsovat kolem 4. až 5. týdne."
            ),
            FetalDevelopmentMonth(
                month = 2,
                weeks = 5..8,
                size = "Jako kulička vína nebo borůvka (~1,5–2 cm).",
                developedOrgans = "Vytváří se zřetelná srdeční akce (4 komory), tvoří se základy trávicího traktu, jater, ledvin a očí. Končetiny mají prstové prstence.",
                willForm = "Kosterní konstrukce (osifikace chrupavek), vnější přirození, oční víčka.",
                reactions = "Začínají reflexní, nekontrolované mikroskopické pohyby plodu, které matka zatím necítí."
            ),
            FetalDevelopmentMonth(
                month = 3,
                weeks = 9..13,
                size = "Jako švestka nebo citrón (~6–7 cm, ~14–25 g).",
                developedOrgans = "Plně založeny všechny hlavní orgánové systémy. Játra tvoří červené krvinky, ledviny produkují moč. Vyvinuty jsou nehty a základy zubů.",
                willForm = "Plíce maturují k budoucí funkci, dochází ke zdokonalování nervových spojů.",
                reactions = "Otvírá a zavírá pěst, polyká plodovou vodu, reaguje na dotek v oblasti obličeje reflexním pohybem."
            )
        )
    ),
    Trimester(
        name = "2. trimestr",
        range = 14..27,
        months = listOf(
            FetalDevelopmentMonth(
                month = 4,
                weeks = 14..17,
                size = "Jako avokádo (~11–13 cm, ~100–140 g).",
                developedOrgans = "Funkční ledviny, vyvinutý sací reflex, zřetelné pohlaví na ultrazvuku, kůže pokrytá jemným chmýřím (lanugo).",
                willForm = "Tuková vrstva pod kůží, nervové obaly (myelinizace).",
                reactions = "Plod vnímá tlumené zvuky z těla matky (tlukot srdce, proudění krve). Začíná pohybovat očima pod zavřenými víčky."
            ),
            FetalDevelopmentMonth(
                month = 5,
                weeks = 18..22,
                size = "Jako banán nebo mango (~25–28 cm s nožkami, ~300–450 g).",
                developedOrgans = "Sluchové kůstky jsou plně zkostnatělé, kůže je chráněna mázkem (vernix caseosa), vyvinuté otisky prstů.",
                willForm = "Vzduchové sklípky v plicích, chuťové pohárky.",
                reactions = "Matka začíná cítit prvotní pohyby (kopání). Plod reaguje na hlasité zvuky z okolí (trhnutím nebo zrychlením tepu) a na sladkou chuť v plodové vodě."
            ),
            FetalDevelopmentMonth(
                month = 6,
                weeks = 23..27,
                size = "Jako lilek nebo velký pomelo (~35–37 cm, ~650–1000 g).",
                developedOrgans = "Vyvinutá plicní tkáň (začíná tvorba surfaktantu), plně funkční vnitřní ucho, vytvořené řasy a obočí.",
                willForm = "Další větvení plicních cest, mozkové brázdy a závity.",
                reactions = "Otevírá oči, má pravidelné cykly spánku a bdění, reaguje na světlo namířené na břicho matky a na dotek přes břišní stěnu."
            )
        )
    ),
    Trimester(
        name = "3. trimestr",
        range = 28..40,
        months = listOf(
            FetalDevelopmentMonth(
                month = 7,
                weeks = 28..31,
                size = "Jako ananas (~40–42 cm, ~1100–1500 g).",
                developedOrgans = "CNS zvládá regulaci tělesné teploty a rytmu dýchání. Zrak je funkční (zornice reagují na světlo).",
                willForm = "Ukládání podkožního tuku, zrání plic.",
                reactions = "Aktivně vnímá hlasy (zvláště matčin), dokáže se otočit za světlem a zvukem, reaguje na škytavku (pravidelné rytmické záškuby)."
            ),
            FetalDevelopmentMonth(
                month = 8,
                weeks = 32..35,
                size = "Jako meloun cantaloupe nebo dýně (~45–47 cm, ~1800–2500 g).",
                developedOrgans = "Většina orgánů je plně vyvinutá a zralá. Plíce dokončují tvorbu surfaktantu pro zabránění slepení sklípků. Mizí lanugo.",
                willForm = "Ukládání protilátek z těla matky, finální přibývání na váze.",
                reactions = "Reaguje silnými pohyby na omezený prostor v děloze, rozpoznává známé melodie a hlasy, reaguje na úroveň stresu či zklidnění matky."
            ),
            FetalDevelopmentMonth(
                month = 9,
                weeks = 36..40,
                size = "Jako velký vodní meloun (~50–53 cm, ~3000–4000 g).",
                developedOrgans = "Všechny orgánové systémy jsou plně připraveny na mimoděložní život (plíce, trávicí trakt, thermoregulace).",
                willForm = "Prochází závěrečným zráním imunitního systému a zpevňováním kostí (kromě lebky, která zůstává poddajná pro průchod porodními cestami).",
                reactions = "Zaujímá finální porodní polohu (zpravidla hlavičkou dolů), má vyvinutý silný úchopový a sací reflex, reaguje na doteky, hlasy i změny světla v bezprostředním okolí."
            )
        )
    )
)
