package com.tasbih.app.data

/** One step inside a combined dhikr, e.g. the words change every 33 beads. */
data class Phase(val start: Int, val dhikr: Dhikr)

data class Dhikr(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val meaning: String,
    val target: Int,
    /** Group shown in the "Change dhikr" list. */
    val category: String = "More",
    val phases: List<Phase>? = null,
) {
    /** The words to show at this count (changes for combined dhikr). */
    fun at(count: Int): Dhikr {
        val p = phases ?: return this
        return p.lastOrNull { count >= it.start }?.dhikr ?: this
    }
}

private const val COMBINED = "After prayer and before sleep"
private const val GLORIFY = "Glorify Allah"
private const val TAWHID = "Oneness of Allah"
private const val FORGIVE = "Seeking forgiveness"
private const val SALAWAT = "Blessings on the Prophet ﷺ"
private const val DUAS = "Trust and duas"

private val subhanAllah = Dhikr("subhanallah", "سُبْحَانَ ٱللَّٰهِ", "SubhanAllah", "Glory be to Allah", 33, GLORIFY)
private val alhamdulillah = Dhikr("alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ", "Alhamdulillah", "All praise is due to Allah", 33, GLORIFY)
private val allahuAkbar = Dhikr("allahuakbar", "ٱللَّٰهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest", 33, GLORIFY)
private val tahlil = Dhikr(
    "tahlil",
    "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ ٱلْمُلْكُ وَلَهُ ٱلْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ",
    "La ilaha illallah wahdahu la sharika lah",
    "There is no god but Allah alone, with no partner. His is the dominion, His is the praise, and He has power over all things",
    100,
    TAWHID,
)

val Dhikrs: List<Dhikr> = listOf(
    // ---- combined ----
    Dhikr(
        id = "after_prayer",
        arabic = "تَسْبِيحُ بَعْدَ ٱلصَّلَاةِ",
        transliteration = "After every prayer",
        meaning = "SubhanAllah ×33 · Alhamdulillah ×33 · Allahu Akbar ×33 · then La ilaha illallah wahdahu… once",
        target = 100,
        category = COMBINED,
        phases = listOf(Phase(0, subhanAllah), Phase(33, alhamdulillah), Phase(66, allahuAkbar), Phase(99, tahlil)),
    ),
    Dhikr(
        id = "before_sleep",
        arabic = "تَسْبِيحُ فَاطِمَةَ",
        transliteration = "Tasbih of Fatima · before sleep",
        meaning = "SubhanAllah ×33 · Alhamdulillah ×33 · Allahu Akbar ×34",
        target = 100,
        category = COMBINED,
        phases = listOf(Phase(0, subhanAllah), Phase(33, alhamdulillah), Phase(66, allahuAkbar)),
    ),

    // ---- glorify Allah ----
    subhanAllah,
    alhamdulillah,
    allahuAkbar,
    Dhikr("subhan_hamd", "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ", "SubhanAllahi wa bihamdihi", "Glory and praise be to Allah", 100, GLORIFY),
    Dhikr("subhan_azim", "سُبْحَانَ ٱللَّٰهِ ٱلْعَظِيمِ", "SubhanAllahil Azim", "Glory be to Allah, the Almighty", 100, GLORIFY),
    Dhikr(
        "tasbih_hamd",
        "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ، سُبْحَانَ ٱللَّٰهِ ٱلْعَظِيمِ",
        "SubhanAllahi wa bihamdihi, SubhanAllahil Azim",
        "Glory and praise be to Allah; glory be to Allah, the Almighty",
        100,
        GLORIFY,
    ),
    Dhikr(
        "subbuh", "سُبُّوحٌ قُدُّوسٌ رَبُّ ٱلْمَلَائِكَةِ وَٱلرُّوحِ", "Subbuhun Quddusun",
        "Most Glorious, Most Holy, Lord of the angels and the Spirit", 33, GLORIFY,
    ),

    // ---- oneness ----
    tahlil,
    Dhikr("la_ilaha", "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", "La ilaha illallah", "There is no god but Allah", 100, TAWHID),
    Dhikr("kalima", "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ مُحَمَّدٌ رَسُولُ ٱللَّٰهِ", "La ilaha illallah Muhammadur Rasulullah", "There is no god but Allah; Muhammad is the Messenger of Allah", 100, TAWHID),

    // ---- forgiveness ----
    Dhikr("istighfar", "أَسْتَغْفِرُ ٱللَّٰهَ", "Astaghfirullah", "I seek forgiveness from Allah", 100, FORGIVE),
    Dhikr("istighfar_atubu", "أَسْتَغْفِرُ ٱللَّٰهَ وَأَتُوبُ إِلَيْهِ", "Astaghfirullah wa atubu ilayh", "I seek Allah’s forgiveness and turn to Him in repentance", 100, FORGIVE),
    Dhikr("istighfar_azim", "أَسْتَغْفِرُ ٱللَّٰهَ ٱلْعَظِيمَ وَأَتُوبُ إِلَيْهِ", "Astaghfirullahal Azim wa atubu ilayh", "I seek forgiveness from Allah the Almighty and turn to Him", 100, FORGIVE),
    Dhikr(
        "rabbighfir", "رَبِّ ٱغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنتَ ٱلتَّوَّابُ ٱلرَّحِيمُ", "Rabbighfir li wa tub alayya",
        "My Lord, forgive me and accept my repentance; You are the Accepting of repentance, the Merciful", 100, FORGIVE,
    ),
    Dhikr(
        "yunus", "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ ٱلظَّالِمِينَ", "La ilaha illa anta subhanaka",
        "There is no god but You, glory be to You; I was among the wrongdoers (Qur’an 21:87)", 100, FORGIVE,
    ),

    // ---- blessings on the Prophet ----
    Dhikr("durood", "ٱللَّٰهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ", "Allahumma salli ala Muhammad", "O Allah, send blessings upon Muhammad ﷺ", 100, SALAWAT),
    Dhikr("salli_sallim", "ٱللَّٰهُمَّ صَلِّ وَسَلِّمْ عَلَىٰ نَبِيِّنَا مُحَمَّدٍ", "Allahumma salli wa sallim ala nabiyyina Muhammad", "O Allah, send peace and blessings upon our Prophet Muhammad ﷺ", 100, SALAWAT),
    Dhikr("sallallahu", "صَلَّىٰ ٱللَّٰهُ عَلَيْهِ وَسَلَّمَ", "Sallallahu alayhi wa sallam", "May Allah’s peace and blessings be upon him ﷺ", 100, SALAWAT),

    // ---- trust and duas ----
    Dhikr("hawqala", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِٱللَّٰهِ", "La hawla wa la quwwata illa billah", "There is no power nor strength except with Allah", 100, DUAS),
    Dhikr("hasbunallah", "حَسْبُنَا ٱللَّٰهُ وَنِعْمَ ٱلْوَكِيلُ", "Hasbunallahu wa ni’mal wakil", "Allah is enough for us, and He is the best Disposer of affairs (Qur’an 3:173)", 100, DUAS),
    Dhikr("ya_hayyu", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ", "Ya Hayyu ya Qayyum", "O Ever-Living, O Sustainer, in Your mercy I seek relief", 100, DUAS),
    Dhikr(
        "rabbana", "رَبَّنَا آتِنَا فِي ٱلدُّنْيَا حَسَنَةً وَفِي ٱلْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ ٱلنَّارِ", "Rabbana atina fid-dunya hasanah",
        "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire (Qur’an 2:201)", 33, DUAS,
    ),
)

fun dhikrById(id: String): Dhikr = Dhikrs.firstOrNull { it.id == id } ?: Dhikrs[0]

/**
 * Glad tidings reported in authentic hadith, shown next to the day's progress.
 * They are encouragement to stay consistent — only Allah knows the true reward, and no number is invented here.
 * [goal] = 0 means the hadith gives no daily number.
 */
data class Virtue(
    val id: String,
    val title: String,
    val dhikrIds: List<String>,
    val arabic: String,
    val goal: Int,
    val text: String,
    val source: String,
)

val Virtues: List<Virtue> = listOf(
    Virtue(
        "foam", "Like the foam of the sea", listOf("tasbih_hamd", "subhan_hamd"),
        "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ", 100,
        "Whoever says “SubhanAllahi wa bihamdihi” one hundred times in a day, his sins are forgiven, even if they are like the foam of the sea.",
        "Sahih al-Bukhari · Sahih Muslim",
    ),
    Virtue(
        "tahlil100", "Worth freeing ten slaves", listOf("tahlil"),
        "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ…", 100,
        "Whoever says it one hundred times in a day has the reward of freeing ten slaves. One hundred good deeds are written for him, one hundred sins are erased, and it is a protection from Shaytan until evening.",
        "Sahih al-Bukhari · Sahih Muslim",
    ),
    Virtue(
        "after_prayer", "After every prayer", listOf("after_prayer"),
        "تَسْبِيحُ بَعْدَ ٱلصَّلَاةِ", 100,
        "Whoever glorifies Allah thirty-three times after each prayer, praises Him thirty-three and magnifies Him thirty-three, then completes the hundred with “La ilaha illallah wahdahu…”, his sins are forgiven, even if they are like the foam of the sea.",
        "Sahih Muslim",
    ),
    Virtue(
        "sleep", "Better than a servant", listOf("before_sleep"),
        "تَسْبِيحُ فَاطِمَةَ", 100,
        "The Prophet ﷺ taught his daughter Fatima, when going to bed, to say SubhanAllah thirty-three times, Alhamdulillah thirty-three times and Allahu Akbar thirty-four times, and told her it is better for her than a servant.",
        "Sahih al-Bukhari · Sahih Muslim",
    ),
    Virtue(
        "istighfar", "Seeking forgiveness", listOf("istighfar", "istighfar_atubu", "istighfar_azim", "rabbighfir"),
        "أَسْتَغْفِرُ ٱللَّٰهَ", 70,
        "The Prophet ﷺ, whose sins were already forgiven, said that he sought Allah’s forgiveness and repented to Him more than seventy times in a day.",
        "Sahih al-Bukhari",
    ),
    Virtue(
        "durood", "Returned tenfold", listOf("durood", "salli_sallim", "sallallahu"),
        "ٱللَّٰهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ", 0,
        "Whoever sends one blessing upon me, Allah sends ten blessings upon him.",
        "Sahih Muslim",
    ),
    Virtue(
        "hawqala", "A treasure of Paradise", listOf("hawqala"),
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِٱللَّٰهِ", 0,
        "The Prophet ﷺ told Abu Musa that “La hawla wa la quwwata illa billah” is a treasure from the treasures of Paradise.",
        "Sahih al-Bukhari · Sahih Muslim",
    ),
    Virtue(
        "alhamdulillah", "It fills the scale", listOf("alhamdulillah"),
        "ٱلْحَمْدُ لِلَّٰهِ", 0,
        "“Alhamdulillah” fills the scale, and “SubhanAllah” and “Alhamdulillah” fill what is between the heavens and the earth.",
        "Sahih Muslim",
    ),
)
