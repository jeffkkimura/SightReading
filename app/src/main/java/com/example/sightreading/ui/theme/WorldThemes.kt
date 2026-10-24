package com.example.sightreading.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.KeyType

enum class SpriteType {
    CRYSTAL_MEADOW,
    SUNLIT_FOREST,
    DESERT_DUNES,
    AURORA_PEAKS,
    ELECTRIC_CITADEL,
    SAPPHIRE_PALACE,
    CORAL_REEF,
    TWILIGHT_HAVEN,
    SUNSET_VALLEY,
    COSMIC_NEBULA,
    EMERALD_RAINFOREST,
    SPRING_BLOSSOM,
    MIDNIGHT_NEBULA,
    OBSIDIAN_CAVERN,
    STORMY_CITADEL,
    FROZEN_GLACIER,
    CRIMSON_MOON,
    NETHER_TWILIGHT,
    VOID_ABYSS,
    DEEP_OCEAN,
    VOLCANIC_INFERNO,
    GOTHIC_CATHEDRAL,
    HAUNTED_FOREST,
    DRAGON_PEAK
}

data class WorldTheme(
    val worldName: String,
    val subtitle: String,
    val spriteType: SpriteType,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val terrainColor: Color,
    val pathColor: Color,
    val nodeActiveColor: Color,
    val nodeCompletedColor: Color,
    val accentColor: Color
)

object WorldThemeRepository {

    fun getThemeForKey(key: KeySignature): WorldTheme {
        return when (key) {
            // --- 12 MAJOR KEY WORLDS ---
            KeySignature.C_MAJOR -> WorldTheme(
                worldName = "Crystal Meadow Realm",
                subtitle = "Pure Tones & Shimmering Springs",
                spriteType = SpriteType.CRYSTAL_MEADOW,
                skyTopColor = Color(0xFFE0F7FA),
                skyBottomColor = Color(0xFFB2EBF2),
                terrainColor = Color(0xFF81C784),
                pathColor = Color(0xFFFFF59D),
                nodeActiveColor = Color(0xFF00E676),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00B0FF)
            )
            KeySignature.G_MAJOR -> WorldTheme(
                worldName = "Sunlit Redwood Forest",
                subtitle = "1 Sharp - Golden Sunbeams",
                spriteType = SpriteType.SUNLIT_FOREST,
                skyTopColor = Color(0xFFFFF8E1),
                skyBottomColor = Color(0xFFFFECB3),
                terrainColor = Color(0xFF4CAF50),
                pathColor = Color(0xFFD7CCC8),
                nodeActiveColor = Color(0xFFFF9800),
                nodeCompletedColor = Color(0xFFFFC107),
                accentColor = Color(0xFF2E7D32)
            )
            KeySignature.D_MAJOR -> WorldTheme(
                worldName = "Golden Desert Oasis",
                subtitle = "2 Sharps - Sunburst Sand Dunes",
                spriteType = SpriteType.DESERT_DUNES,
                skyTopColor = Color(0xFFFFF3E0),
                skyBottomColor = Color(0xFFFFE0B2),
                terrainColor = Color(0xFFFB8C00),
                pathColor = Color(0xFFFFE082),
                nodeActiveColor = Color(0xFFFF6D00),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFE65100)
            )
            KeySignature.A_MAJOR -> WorldTheme(
                worldName = "Aurora Celestial Peaks",
                subtitle = "3 Sharps - Starlit Glacier Summits",
                spriteType = SpriteType.AURORA_PEAKS,
                skyTopColor = Color(0xFF1A237E),
                skyBottomColor = Color(0xFF3949AB),
                terrainColor = Color(0xFF7986CB),
                pathColor = Color(0xFFE8EAF6),
                nodeActiveColor = Color(0xFF00E5FF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF7C4DFF)
            )
            KeySignature.E_MAJOR -> WorldTheme(
                worldName = "Electric Sky Citadel",
                subtitle = "4 Sharps - Lightning Crystal Spires",
                spriteType = SpriteType.ELECTRIC_CITADEL,
                skyTopColor = Color(0xFF311B92),
                skyBottomColor = Color(0xFF512DA8),
                terrainColor = Color(0xFF9575CD),
                pathColor = Color(0xFFEDE7F6),
                nodeActiveColor = Color(0xFFFFEA00),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFAA00FF)
            )
            KeySignature.B_MAJOR -> WorldTheme(
                worldName = "Royal Sapphire Palace",
                subtitle = "5 Sharps - Marble Terraces & Fountains",
                spriteType = SpriteType.SAPPHIRE_PALACE,
                skyTopColor = Color(0xFF0D47A1),
                skyBottomColor = Color(0xFF1976D2),
                terrainColor = Color(0xFF64B5F6),
                pathColor = Color(0xFFE3F2FD),
                nodeActiveColor = Color(0xFF00B0FF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF2979FF)
            )
            KeySignature.F_SHARP_MAJOR -> WorldTheme(
                worldName = "Bioluminescent Coral Reef",
                subtitle = "6 Sharps - Crystal Tides & Sea Stars",
                spriteType = SpriteType.CORAL_REEF,
                skyTopColor = Color(0xFF004D40),
                skyBottomColor = Color(0xFF00796B),
                terrainColor = Color(0xFF4DB6AC),
                pathColor = Color(0xFFE0F2F1),
                nodeActiveColor = Color(0xFF18FFFF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00BFA5)
            )
            KeySignature.D_FLAT_MAJOR -> WorldTheme(
                worldName = "Twilight Lavender Haven",
                subtitle = "5 Flats - Sunset Cloud Meadows",
                spriteType = SpriteType.TWILIGHT_HAVEN,
                skyTopColor = Color(0xFF4A148C),
                skyBottomColor = Color(0xFF7B1FA2),
                terrainColor = Color(0xFFBA68C8),
                pathColor = Color(0xFFF3E5F5),
                nodeActiveColor = Color(0xFFFF4081),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFE040FB)
            )
            KeySignature.A_FLAT_MAJOR -> WorldTheme(
                worldName = "Coral Sunset Valley",
                subtitle = "4 Flats - Warm Horizons",
                spriteType = SpriteType.SUNSET_VALLEY,
                skyTopColor = Color(0xFF880E4F),
                skyBottomColor = Color(0xFFC2185B),
                terrainColor = Color(0xFFE57373),
                pathColor = Color(0xFFFFEBEE),
                nodeActiveColor = Color(0xFFFF5252),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFFF1744)
            )
            KeySignature.E_FLAT_MAJOR -> WorldTheme(
                worldName = "Cosmic Nebula Observatory",
                subtitle = "3 Flats - Stardust Orbits",
                spriteType = SpriteType.COSMIC_NEBULA,
                skyTopColor = Color(0xFF18103C),
                skyBottomColor = Color(0xFF331E6D),
                terrainColor = Color(0xFF673AB7),
                pathColor = Color(0xFFD1C4E9),
                nodeActiveColor = Color(0xFFE040FB),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF651FFF)
            )
            KeySignature.B_FLAT_MAJOR -> WorldTheme(
                worldName = "Emerald Rainforest Canopy",
                subtitle = "2 Flats - Glowing Waterfalls",
                spriteType = SpriteType.EMERALD_RAINFOREST,
                skyTopColor = Color(0xFF1B5E20),
                skyBottomColor = Color(0xFF388E3C),
                terrainColor = Color(0xFF81C784),
                pathColor = Color(0xFFE8F5E9),
                nodeActiveColor = Color(0xFF00E676),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00C853)
            )
            KeySignature.F_MAJOR -> WorldTheme(
                worldName = "Spring Blossom Village",
                subtitle = "1 Flat - Cherry Blossoms & Petals",
                spriteType = SpriteType.SPRING_BLOSSOM,
                skyTopColor = Color(0xFFFCE4EC),
                skyBottomColor = Color(0xFFF8BBD0),
                terrainColor = Color(0xFFA5D6A7),
                pathColor = Color(0xFFFFF8E1),
                nodeActiveColor = Color(0xFFFF4081),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFF50057)
            )

            // --- 12 MINOR KEY WORLDS ---
            KeySignature.A_MINOR -> WorldTheme(
                worldName = "Midnight Shadow Nebula",
                subtitle = "0 Accidentals - Dark Violet Stardust",
                spriteType = SpriteType.MIDNIGHT_NEBULA,
                skyTopColor = Color(0xFF000000),
                skyBottomColor = Color(0xFF12005E),
                terrainColor = Color(0xFF3F51B5),
                pathColor = Color(0xFFC5CAE9),
                nodeActiveColor = Color(0xFF7C4DFF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF536DFE)
            )
            KeySignature.E_MINOR -> WorldTheme(
                worldName = "Mystic Obsidian Cavern",
                subtitle = "1 Sharp - Amethyst Crystal Arches",
                spriteType = SpriteType.OBSIDIAN_CAVERN,
                skyTopColor = Color(0xFF212121),
                skyBottomColor = Color(0xFF424242),
                terrainColor = Color(0xFF7E57C2),
                pathColor = Color(0xFFEDE7F6),
                nodeActiveColor = Color(0xFFB388FF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF7C4DFF)
            )
            KeySignature.B_MINOR -> WorldTheme(
                worldName = "Stormy Mountain Citadel",
                subtitle = "2 Sharps - Thunder & Fortress Towers",
                spriteType = SpriteType.STORMY_CITADEL,
                skyTopColor = Color(0xFF263238),
                skyBottomColor = Color(0xFF37474F),
                terrainColor = Color(0xFF78909C),
                pathColor = Color(0xFFECEFF1),
                nodeActiveColor = Color(0xFF80D8FF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00B0FF)
            )
            KeySignature.F_SHARP_MINOR -> WorldTheme(
                worldName = "Frozen Glacier Realm",
                subtitle = "3 Sharps - Ice Spires & Frozen Falls",
                spriteType = SpriteType.FROZEN_GLACIER,
                skyTopColor = Color(0xFF006064),
                skyBottomColor = Color(0xFF00838F),
                terrainColor = Color(0xFF80DEEA),
                pathColor = Color(0xFFE0F7FA),
                nodeActiveColor = Color(0xFF84FFFF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00E5FF)
            )
            KeySignature.C_SHARP_MINOR -> WorldTheme(
                worldName = "Crimson Moon Temple",
                subtitle = "4 Sharps - Blood Moon & Lanterns",
                spriteType = SpriteType.CRIMSON_MOON,
                skyTopColor = Color(0xFF3E2723),
                skyBottomColor = Color(0xFF4E342E),
                terrainColor = Color(0xFF8D6E63),
                pathColor = Color(0xFFEFEBE9),
                nodeActiveColor = Color(0xFFFF5252),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFFF1744)
            )
            KeySignature.G_SHARP_MINOR -> WorldTheme(
                worldName = "Nether Twilight Realm",
                subtitle = "5 Sharps - Deep Indigo Dusk",
                spriteType = SpriteType.NETHER_TWILIGHT,
                skyTopColor = Color(0xFF1A237E),
                skyBottomColor = Color(0xFF283593),
                terrainColor = Color(0xFF5C6BC0),
                pathColor = Color(0xFFE8EAF6),
                nodeActiveColor = Color(0xFF8C9EFF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF3D5AFE)
            )
            KeySignature.D_SHARP_MINOR -> WorldTheme(
                worldName = "Void Crystal Abyss",
                subtitle = "6 Sharps - Floating Void Shards",
                spriteType = SpriteType.VOID_ABYSS,
                skyTopColor = Color(0xFF000000),
                skyBottomColor = Color(0xFF212121),
                terrainColor = Color(0xFF616161),
                pathColor = Color(0xFFE0E0E0),
                nodeActiveColor = Color(0xFFFF4081),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFF50057)
            )
            KeySignature.B_FLAT_MINOR -> WorldTheme(
                worldName = "Abyssal Deep Ocean",
                subtitle = "5 Flats - Bioluminescent Abyss",
                spriteType = SpriteType.DEEP_OCEAN,
                skyTopColor = Color(0xFF001122),
                skyBottomColor = Color(0xFF002244),
                terrainColor = Color(0xFF0088AA),
                pathColor = Color(0xFFCCEEFF),
                nodeActiveColor = Color(0xFF00FFFF),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00CCFF)
            )
            KeySignature.F_MINOR -> WorldTheme(
                worldName = "Volcanic Inferno Citadel",
                subtitle = "4 Flats - Magma Rivers & Obsidian",
                spriteType = SpriteType.VOLCANIC_INFERNO,
                skyTopColor = Color(0xFFBF360C),
                skyBottomColor = Color(0xFFD84315),
                terrainColor = Color(0xFFFF7043),
                pathColor = Color(0xFFFBE9E7),
                nodeActiveColor = Color(0xFFFF6E40),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFFF3D00)
            )
            KeySignature.C_MINOR -> WorldTheme(
                worldName = "Gothic Cathedral Spire",
                subtitle = "3 Flats - Moonlight Stained Glass",
                spriteType = SpriteType.GOTHIC_CATHEDRAL,
                skyTopColor = Color(0xFF1B003A),
                skyBottomColor = Color(0xFF2C0055),
                terrainColor = Color(0xFF7B1FA2),
                pathColor = Color(0xFFE1BEE7),
                nodeActiveColor = Color(0xFFE040FB),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFD500F9)
            )
            KeySignature.G_MINOR -> WorldTheme(
                worldName = "Haunted Phantom Forest",
                subtitle = "2 Flats - Ethereal Mist & Lanterns",
                spriteType = SpriteType.HAUNTED_FOREST,
                skyTopColor = Color(0xFF002700),
                skyBottomColor = Color(0xFF004D00),
                terrainColor = Color(0xFF338833),
                pathColor = Color(0xFFDDFFDD),
                nodeActiveColor = Color(0xFF66FF66),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFF00FF00)
            )
            KeySignature.D_MINOR -> WorldTheme(
                worldName = "Dragon Peak Fortress",
                subtitle = "1 Flat - Misty Crags & Crimson Banners",
                spriteType = SpriteType.DRAGON_PEAK,
                skyTopColor = Color(0xFF22262E),
                skyBottomColor = Color(0xFF424A58),
                terrainColor = Color(0xFF181B20),
                pathColor = Color(0xFFB71C1C),
                nodeActiveColor = Color(0xFFFF3D00),
                nodeCompletedColor = Color(0xFFFFD54F),
                accentColor = Color(0xFFD50000)
            )
        }
    }
}
