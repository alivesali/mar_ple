package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.model.AiDifficulty
import com.example.model.BoardTheme
import com.example.model.CardCategory
import com.example.model.CharacterAbilityType
import com.example.model.CharacterGender
import com.example.model.CustomAbilityEffectType
import com.example.model.CustomAbilityTarget
import com.example.model.CustomCharacterSpec
import com.example.model.DifficultyLevel
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.GameCharacter
import com.example.model.HeldItemVisual
import com.example.model.MusicGenre
import com.example.model.ObstacleType
import com.example.model.SettingsProfile
import com.example.model.TruthOrDareCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "truth_or_dare_cards")
data class TruthOrDareCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val body: String,
    val categoryName: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): TruthOrDareCard {
        return TruthOrDareCard(
            id = id,
            title = title,
            body = body,
            category = CardCategory.fromName(categoryName),
            createdAtMillis = createdAtMillis
        )
    }
}

@Entity(tableName = "custom_characters")
data class CustomCharacterEntity(
    @PrimaryKey val id: String,
    val englishName: String,
    val persianTitle: String,
    val genderName: String,
    val primaryColorArgb: Long,
    val secondaryColorArgb: Long,
    val hairColorArgb: Long,
    val skinToneArgb: Long,
    val heldItemName: String,
    val targetName: String,
    val effectTypeName: String,
    val percentageOrValue: Int,
    val customDescription: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): GameCharacter {
        val spec = CustomCharacterSpec(
            target = CustomAbilityTarget.fromName(targetName),
            effectType = CustomAbilityEffectType.fromName(effectTypeName),
            percentageOrValue = percentageOrValue.coerceIn(10, 100),
            customDescription = customDescription
        )
        return GameCharacter(
            id = id,
            defaultEnglishName = englishName,
            persianTitle = persianTitle.ifBlank { spec.buildPersianAbilityTitle() },
            gender = if (genderName == CharacterGender.FEMALE.name) CharacterGender.FEMALE else CharacterGender.MALE,
            primaryColor = Color(primaryColorArgb),
            secondaryColor = Color(secondaryColorArgb),
            hairColor = Color(hairColorArgb),
            skinTone = Color(skinToneArgb),
            abilityType = CharacterAbilityType.CUSTOM_HERO,
            heldItem = HeldItemVisual.fromName(heldItemName),
            isCustom = true,
            customSpec = spec
        )
    }
}

@Entity(tableName = "settings_profiles")
data class SettingsProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileName: String,
    val maxTiles: Int,
    val difficultyName: String,
    val boardThemeName: String,
    val aiDifficultyName: String,
    val playerCount: Int,
    val enabledObstaclesCsv: String,
    val enabledCategoriesCsv: String,
    val environmentEffectName: String = EnvironmentEffectType.GOLDEN_DUST.name,
    val effectIntensityName: String = EffectIntensity.MEDIUM.name,
    val isEnvironmentEffectsEnabled: Boolean = true,
    val musicGenreName: String = MusicGenre.ROYAL.name,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): SettingsProfile {
        val diff = DifficultyLevel.entries.find { it.name == difficultyName } ?: DifficultyLevel.MEDIUM
        val theme = BoardTheme.fromName(boardThemeName)
        val aiDiff = AiDifficulty.fromName(aiDifficultyName)
        val obstacleNames = enabledObstaclesCsv.split(",").filter { it.isNotBlank() }.toSet()
        val obstacles = ObstacleType.entries.filter { it.name in obstacleNames }.toSet()
        val catNames = enabledCategoriesCsv.split(",").filter { it.isNotBlank() }.toSet()
        val categories = CardCategory.entries.filter { it.name in catNames }.toSet()
            .ifEmpty { CardCategory.entries.toSet() }

        return SettingsProfile(
            id = id,
            profileName = profileName,
            maxTiles = maxTiles.coerceIn(50, 200),
            difficulty = diff,
            boardTheme = theme,
            aiDifficulty = aiDiff,
            playerCount = playerCount.coerceIn(1, 8),
            enabledObstacles = obstacles,
            enabledCardCategories = categories,
            environmentEffect = EnvironmentEffectType.fromName(environmentEffectName),
            effectIntensity = EffectIntensity.fromName(effectIntensityName),
            isEnvironmentEffectsEnabled = isEnvironmentEffectsEnabled,
            musicGenre = MusicGenre.fromName(musicGenreName),
            createdAtMillis = createdAtMillis
        )
    }
}

@Dao
interface TruthOrDareCardDao {
    @Query("SELECT * FROM truth_or_dare_cards ORDER BY createdAtMillis DESC")
    fun observeAllCards(): Flow<List<TruthOrDareCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: TruthOrDareCardEntity): Long

    @Update
    suspend fun updateCard(card: TruthOrDareCardEntity)

    @Query("DELETE FROM truth_or_dare_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)
}

@Dao
interface CustomCharacterDao {
    @Query("SELECT * FROM custom_characters ORDER BY createdAtMillis DESC")
    fun observeCustomCharacters(): Flow<List<CustomCharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCharacter(character: CustomCharacterEntity)

    @Query("DELETE FROM custom_characters WHERE id = :id")
    suspend fun deleteCustomCharacterById(id: String)
}

@Dao
interface SettingsProfileDao {
    @Query("SELECT * FROM settings_profiles ORDER BY createdAtMillis DESC")
    fun observeSettingsProfiles(): Flow<List<SettingsProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettingsProfile(profile: SettingsProfileEntity): Long

    @Query("DELETE FROM settings_profiles WHERE id = :id")
    suspend fun deleteSettingsProfileById(id: Long)
}

@Database(
    entities = [
        TruthOrDareCardEntity::class,
        CustomCharacterEntity::class,
        SettingsProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MysticGameDatabase : RoomDatabase() {
    abstract fun truthOrDareCardDao(): TruthOrDareCardDao
    abstract fun customCharacterDao(): CustomCharacterDao
    abstract fun settingsProfileDao(): SettingsProfileDao

    companion object {
        @Volatile
        private var INSTANCE: MysticGameDatabase? = null

        fun getInstance(context: Context): MysticGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MysticGameDatabase::class.java,
                    "mystic_snakes_local_storage.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Permanent Local Storage Repository for:
 * 1. User-created Truth or Dare cards (Room SQLite)
 * 2. User-created Custom Characters (Room SQLite)
 * 3. Saved Settings Profiles (Room SQLite)
 * 4. Active session preferences (SharedPreferences)
 */
class TruthOrDareRepository(context: Context) {

    private val database = MysticGameDatabase.getInstance(context)
    private val cardDao = database.truthOrDareCardDao()
    private val customCharacterDao = database.customCharacterDao()
    private val settingsProfileDao = database.settingsProfileDao()
    private val prefs = context.applicationContext.getSharedPreferences(
        "mystic_snakes_session_prefs",
        Context.MODE_PRIVATE
    )

    val allCards: Flow<List<TruthOrDareCard>> = cardDao.observeAllCards().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val customCharacters: Flow<List<GameCharacter>> = customCharacterDao.observeCustomCharacters().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val settingsProfiles: Flow<List<SettingsProfile>> = settingsProfileDao.observeSettingsProfiles().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun addCard(
        title: String,
        body: String,
        category: CardCategory
    ): Long {
        val entity = TruthOrDareCardEntity(
            title = title.trim(),
            body = body.trim(),
            categoryName = category.name,
            createdAtMillis = System.currentTimeMillis()
        )
        return cardDao.insertCard(entity)
    }

    suspend fun updateCard(
        id: Long,
        title: String,
        body: String,
        category: CardCategory,
        createdAtMillis: Long
    ) {
        val entity = TruthOrDareCardEntity(
            id = id,
            title = title.trim(),
            body = body.trim(),
            categoryName = category.name,
            createdAtMillis = createdAtMillis
        )
        cardDao.updateCard(entity)
    }

    suspend fun deleteCard(id: Long) {
        cardDao.deleteCardById(id)
    }

    suspend fun saveCustomCharacter(
        englishName: String,
        persianTitle: String,
        gender: CharacterGender,
        primaryColorArgb: Long,
        secondaryColorArgb: Long,
        hairColorArgb: Long,
        skinToneArgb: Long,
        heldItem: HeldItemVisual,
        target: CustomAbilityTarget,
        effectType: CustomAbilityEffectType,
        percentageOrValue: Int,
        customDescription: String
    ): String {
        val customId = "custom_${System.currentTimeMillis()}"
        val entity = CustomCharacterEntity(
            id = customId,
            englishName = englishName.trim(),
            persianTitle = persianTitle.trim(),
            genderName = gender.name,
            primaryColorArgb = primaryColorArgb,
            secondaryColorArgb = secondaryColorArgb,
            hairColorArgb = hairColorArgb,
            skinToneArgb = skinToneArgb,
            heldItemName = heldItem.name,
            targetName = target.name,
            effectTypeName = effectType.name,
            percentageOrValue = percentageOrValue.coerceIn(10, 100),
            customDescription = customDescription.trim(),
            createdAtMillis = System.currentTimeMillis()
        )
        customCharacterDao.insertCustomCharacter(entity)
        return customId
    }

    suspend fun deleteCustomCharacter(characterId: String) {
        customCharacterDao.deleteCustomCharacterById(characterId)
    }

    suspend fun saveSettingsProfile(
        profileName: String,
        maxTiles: Int,
        difficulty: DifficultyLevel,
        boardTheme: BoardTheme,
        aiDifficulty: AiDifficulty,
        playerCount: Int,
        enabledObstacles: Set<ObstacleType>,
        enabledCardCategories: Set<CardCategory>,
        environmentEffect: EnvironmentEffectType = EnvironmentEffectType.GOLDEN_DUST,
        effectIntensity: EffectIntensity = EffectIntensity.MEDIUM,
        isEnvironmentEffectsEnabled: Boolean = true,
        musicGenre: MusicGenre = MusicGenre.ROYAL
    ): Long {
        val entity = SettingsProfileEntity(
            profileName = profileName.trim(),
            maxTiles = maxTiles,
            difficultyName = difficulty.name,
            boardThemeName = boardTheme.name,
            aiDifficultyName = aiDifficulty.name,
            playerCount = playerCount,
            enabledObstaclesCsv = enabledObstacles.joinToString(",") { it.name },
            enabledCategoriesCsv = enabledCardCategories.joinToString(",") { it.name },
            environmentEffectName = environmentEffect.name,
            effectIntensityName = effectIntensity.name,
            isEnvironmentEffectsEnabled = isEnvironmentEffectsEnabled,
            musicGenreName = musicGenre.name,
            createdAtMillis = System.currentTimeMillis()
        )
        return settingsProfileDao.insertSettingsProfile(entity)
    }

    suspend fun deleteSettingsProfile(profileId: Long) {
        settingsProfileDao.deleteSettingsProfileById(profileId)
    }

    fun getEnabledCardCategories(): Set<CardCategory> {
        val savedRaw = prefs.getStringSet("enabled_card_categories", null)
        if (savedRaw == null) {
            return CardCategory.entries.toSet()
        }
        val parsed = CardCategory.entries.filter { it.name in savedRaw }.toSet()
        return parsed.ifEmpty { CardCategory.entries.toSet() }
    }

    fun saveEnabledCardCategories(categories: Set<CardCategory>) {
        prefs.edit()
            .putStringSet("enabled_card_categories", categories.map { it.name }.toSet())
            .apply()
    }
}
