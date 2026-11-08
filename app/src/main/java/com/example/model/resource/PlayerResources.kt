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
 * Validates that [turnsRemaining] is strictly positive and [magnitude] is non-negative.
 */
data class ActiveStatusEffect(
    val type: StatusEffectType,
    val turnsRemaining: Int,
    val magnitude: Int = 1
) {
    init {
        require(turnsRemaining > 0) { "Status effect turnsRemaining must be strictly positive, was: $turnsRemaining" }
        require(magnitude >= 0) { "Status effect magnitude must be non-negative, was: $magnitude" }
    }
}

/**
 * Configuration options for player resource maximums and regeneration rates.
 * Validates that maximums are positive and regeneration rates are non-negative.
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
) {
    init {
        require(maxHp > 0) { "maxHp must be positive, was: $maxHp" }
        require(maxMana > 0) { "maxMana must be positive, was: $maxMana" }
        require(maxShield > 0) { "maxShield must be positive, was: $maxShield" }
        require(initialHp in 0..maxHp) { "initialHp must be between 0 and $maxHp, was: $initialHp" }
        require(initialMana in 0..maxMana) { "initialMana must be between 0 and $maxMana, was: $initialMana" }
        require(initialShield in 0..maxShield) { "initialShield must be between 0 and $maxShield, was: $initialShield" }
        require(manaRegenPerTurn >= 0) { "manaRegenPerTurn must be non-negative, was: $manaRegenPerTurn" }
        require(shieldRegenPerTurn >= 0) { "shieldRegenPerTurn must be non-negative, was: $shieldRegenPerTurn" }
    }
}

/**
 * Robust, immutable player resources representation covering HP, Mana, Shield,
 * harvestable commodities (Gold/Crystals), and status effects.
 *
 * All operations enforce strict boundary clamping (e.g. non-negative HP/Mana/Shield,
 * Shield bounded by maxShield) and atomic state transitions.
 *
 * Note: [Player.abilityUsesCount] is the single source of truth for ability usage counters.
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
    val statusEffects: List<ActiveStatusEffect> = emptyList()
) {
    init {
        require(maxHp > 0) { "maxHp must be strictly positive, was: $maxHp" }
        require(maxMana > 0) { "maxMana must be strictly positive, was: $maxMana" }
        require(maxShield > 0) { "maxShield must be strictly positive, was: $maxShield" }
        require(hp in 0..maxHp) { "hp must be between 0 and $maxHp, was: $hp" }
        require(mana in 0..maxMana) { "mana must be between 0 and $maxMana, was: $mana" }
        require(shield in 0..maxShield) { "shield must be between 0 and $maxShield, was: $shield" }
        require(gold >= 0) { "gold must be non-negative, was: $gold" }
        require(crystals >= 0) { "crystals must be non-negative, was: $crystals" }
    }

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
        val newShield = (shield - shieldAbsorbed).coerceIn(0, maxShield)
        val newHp = (hp - remainingDamage).coerceIn(0, maxHp)
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
     * Restores HP up to [maxHp]. Non-positive healing is ignored.
     */
    fun heal(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newHp = (hp + amount).coerceIn(0, maxHp)
        return copy(hp = newHp)
    }

    /**
     * Adds shield up to [maxShield]. Non-positive additions are ignored.
     */
    fun addShield(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newShield = (shield + amount).coerceIn(0, maxShield)
        return copy(shield = newShield)
    }

    /**
     * Restores Mana up to [maxMana]. Non-positive restorations are ignored.
     */
    fun restoreMana(amount: Int): PlayerResources {
        if (amount <= 0) return this
        val newMana = (mana + amount).coerceIn(0, maxMana)
        return copy(mana = newMana)
    }

    /**
     * Atomically spends Mana.
     * Negative cost is rejected. If the player does not possess enough Mana,
     * this returns `null` WITHOUT modifying state, ensuring that no partial or broken
     * ability activation occurs.
     */
    fun spendMana(cost: Int): PlayerResources? {
        if (cost < 0) return null
        if (cost == 0) return this
        if (mana < cost) return null
        return copy(mana = mana - cost)
    }

    /**
     * Atomically spends harvestable resources (gold and crystals).
     * Negative costs are strictly rejected and return null.
     * Returns null if either required amount is insufficient, without partial deduction.
     */
    fun spendHarvestables(goldCost: Int = 0, crystalCost: Int = 0): PlayerResources? {
        if (goldCost < 0 || crystalCost < 0) return null
        if (gold < goldCost || crystals < crystalCost) return null
        return copy(
            gold = gold - goldCost,
            crystals = crystals - crystalCost
        )
    }

    /**
     * Adds harvestable commodities collected on the board.
     * Negative deltas are strictly clamped to zero so they cannot cause invalid reductions.
     */
    fun addHarvestables(goldDelta: Int = 0, crystalsDelta: Int = 0): PlayerResources {
        val safeGoldDelta = goldDelta.coerceAtLeast(0)
        val safeCrystalsDelta = crystalsDelta.coerceAtLeast(0)
        return copy(
            gold = gold + safeGoldDelta,
            crystals = crystals + safeCrystalsDelta
        )
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
     * Rates are validated/coerced to non-negative, and resulting values never exceed maximums.
     */
    fun regenerateTurnResources(config: ResourceConfig): PlayerResources {
        val safeManaRegen = config.manaRegenPerTurn.coerceAtLeast(0)
        val safeShieldRegen = config.shieldRegenPerTurn.coerceAtLeast(0)
        val newMana = (mana + safeManaRegen).coerceIn(0, maxMana)
        val newShield = (shield + safeShieldRegen).coerceIn(0, maxShield)
        return copy(
            mana = newMana,
            shield = newShield
        )
    }
}
