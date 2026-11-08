package com.example.model.resource

/**
 * Status effect types that can be applied to players during the game.
 */
enum class StatusEffectType(
    val persianName: String,
    val isDebuff: Boolean,
    val iconEmoji: String
) {
    FREEZE("انجماد", isDebuff = true, iconEmoji = "❄️"),
    STUN("سرگیجه", isDebuff = true, iconEmoji = "💫"),
    POISON("سم مار", isDebuff = true, iconEmoji = "🧪"),
    SWAMP_SLOWNESS("کندی باتلاق", isDebuff = true, iconEmoji = "🌿"),
    WORM_CURSE("نفرین کرم", isDebuff = true, iconEmoji = "🐛"),
    SHIELD_BOOST("تقویت سپر", isDebuff = false, iconEmoji = "🛡️"),
    SPEED_SURGE("جهش سرعت", isDebuff = false, iconEmoji = "⚡"),
    DRAGON_FURY("خشم اژدها", isDebuff = false, iconEmoji = "🐉")
}

/**
 * An active status effect on a player with remaining turn duration.
 */
data class ActiveStatusEffect(
    val type: StatusEffectType,
    val turnsRemaining: Int,
    val magnitude: Int = 1
)

/**
 * Configuration options for player resource maximums and regeneration rates.
 */
data class ResourceConfig(
    val initialHp: Int = 100,
    val maxHp: Int = 100,
    val initialMana: Int = 100,
    val maxMana: Int = 100,
    val initialShield: Int = 50,
    val maxShield: Int = 50,
    val manaRegenPerTurn: Int = 15,
    val shieldRegenPerTurn: Int = 5
)

/**
 * Robust, immutable player resources representation covering HP, Mana, Shield,
 * harvestable commodities (Gold/Crystals), and status effects.
 *
 * All operations enforce strict boundary clamping (e.g. non-negative HP/Mana,
 * Shield bounded by maxShield) and atomic state transitions.
 */
data class PlayerResources(
    val hp: Int = 100,
    val maxHp: Int = 100,
    val mana: Int = 100,
    val maxMana: Int = 100,
    val shield: Int = 50,
    val maxShield: Int = 50,
    val gold: Int = 0,
    val crystals: Int = 0,
    val abilityUsesCount: Int = 0,
    val statusEffects: List<ActiveStatusEffect> = emptyList()
) {
    val isAlive: Boolean
        get() = hp > 0

    val isShieldDepleted: Boolean
        get() = shield <= 0

    val hpPercentage: Float
        get() = if (maxHp > 0) (hp.toFloat() / maxHp).coerceIn(0f, 1f) else 0f

    val manaPercentage: Float
        get() = if (maxMana > 0) (mana.toFloat() / maxMana).coerceIn(0f, 1f) else 0f

    val shieldPercentage: Float
        get() = if (maxShield > 0) (shield.toFloat() / maxShield).coerceIn(0f, 1f) else 0f

    /**
     * Applies damage to the player.
     * Damage is first absorbed by the Shield. Any remaining penetration damage
     * is subtracted from HP. Neither Shield nor HP will drop below zero.
     *
     * @return Pair containing the updated [PlayerResources] and the actual damage dealt to HP.
     */
    fun takeDamage(amount: Int): Pair<PlayerResources, Int> {
        if (amount <= 0) return Pair(this, 0)

        val shieldAbsorbed = minOf(shield, amount)
        val remainingDamage = amount - shieldAbsorbed
        val newShield = shield - shieldAbsorbed
        val newHp = (hp - remainingDamage).coerceAtLeast(0)
        val hpDamageDealt = hp - newHp

        return Pair(
            copy(
                hp = newHp,
                shield = newShield
            ),
            hpDamageDealt
        )
    }

    /**
     * Restores HP up to [maxHp].
     */
    fun heal(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newHp = (hp + amount).coerceAtMost(maxHp)
        return copy(hp = newHp)
    }

    /**
     * Adds shield up to [maxShield].
     */
    fun addShield(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newShield = (shield + amount).coerceAtMost(maxShield)
        return copy(shield = newShield)
    }

    /**
     * Restores Mana up to [maxMana].
     */
    fun restoreMana(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newMana = (mana + amount).coerceAtMost(maxMana)
        return copy(mana = newMana)
    }

    /**
     * Atomically spends Mana.
     * If the player does not possess enough Mana, this returns `null` WITHOUT modifying state,
     * ensuring that no partial or broken ability activation occurs.
     */
    fun spendMana(cost: Int): PlayerResources? {
        if (cost <= 0) return this
        if (mana < cost) return null
        return copy(mana = mana - cost)
    }

    /**
     * Atomically spends harvestable resources (gold and crystals).
     * Returns null if either required amount is insufficient.
     */
    fun spendHarvestables(goldCost: Int = 0, crystalCost: Int = 0): PlayerResources? {
        if (gold < goldCost || crystals < crystalCost) return null
        return copy(
            gold = gold - goldCost,
            crystals = crystals - crystalCost
        )
    }

    /**
     * Adds harvestable commodities collected on the board.
     */
    fun addHarvestables(goldDelta: Int = 0, crystalsDelta: Int = 0): PlayerResources {
        return copy(
            gold = (gold + goldDelta).coerceAtLeast(0),
            crystals = (crystals + crystalsDelta).coerceAtLeast(0)
        )
    }

    /**
     * Increments the count of abilities cast during the match.
     */
    fun incrementAbilityUses(): PlayerResources {
        return copy(abilityUsesCount = abilityUsesCount + 1)
    }

    /**
     * Adds or refreshes an active status effect.
     */
    fun applyStatusEffect(effect: ActiveStatusEffect): PlayerResources {
        val filtered = statusEffects.filterNot { it.type == effect.type }
        return copy(statusEffects = filtered + effect)
    }

    /**
     * Removes an active status effect by type.
     */
    fun removeStatusEffect(type: StatusEffectType): PlayerResources {
        return copy(statusEffects = statusEffects.filterNot { it.type == type })
    }

    /**
     * Ticks down remaining duration of active status effects, purging expired ones.
     */
    fun tickStatusEffects(): PlayerResources {
        val updated = statusEffects.mapNotNull { effect ->
            if (effect.turnsRemaining > 1) {
                effect.copy(turnsRemaining = effect.turnsRemaining - 1)
            } else {
                null
            }
        }
        return copy(statusEffects = updated)
    }

    /**
     * Checks if a specific status effect is currently active.
     */
    fun hasStatus(type: StatusEffectType): Boolean {
        return statusEffects.any { it.type == type && it.turnsRemaining > 0 }
    }

    /**
     * Regenerates resources at the start or end of a turn based on configuration.
     */
    fun regenerateTurnResources(config: ResourceConfig): PlayerResources {
        val newMana = (mana + config.manaRegenPerTurn).coerceAtMost(maxMana)
        val newShield = (shield + config.shieldRegenPerTurn).coerceAtMost(maxShield)
        return copy(
            mana = newMana,
            shield = newShield
        )
    }
}
