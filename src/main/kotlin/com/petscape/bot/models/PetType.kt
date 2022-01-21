package com.petscape.bot.models

enum class PetType {
    AbyssalOrphan,
    BabyMole,
    CallistoCub,
    Hellpuppy,
    IkkleHydra,
    JalNibRek,
    KalphitePrincess,
    LilZik,
    LittleNightmare,
    Nexling,
    Noon,
    Olmlet,
    PetChaosElemental,
    PetDagannothPrime,
    PetDagannothRex,
    PetDagannothSupreme,
    PetDarkCore,
    PetGeneralGraardor,
    PetKrilTsutsaroth,
    PetKraken,
    PetKreeArra,
    PetSmokeDevil,
    PetSnakeling,
    PetZilyana,
    PrinceBlackDragon,
    ScorpiasOffspring,
    Skotos,
    Sraracha,
    TzrekJad,
    VenenatisSpiderling,
    VetionJr,
    Vorki,

    BabyChinchompa,
    Beaver,
    GiantSquirrel,
    Heron,
    RiftGuardian,
    RockGolem,
    Rocky,
    Tangleroot,

    Bloodhound,
    ChompyChick,
    Herbi,
    LilCreator,
    PetPenanceQueen,
    Phoenix,
    TinyTempor,
    Youngllef,
    Smolcano,

    Unknown;

    companion object {
        fun validPets(): List<PetType> = values().toMutableList().apply { remove(Unknown) }
    }
}
