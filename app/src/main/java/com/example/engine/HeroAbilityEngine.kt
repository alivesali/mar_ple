package com.example.engine

import com.example.model.AbilityActivationMode
import com.example.model.CharacterAbilityType
import com.example.model.Player
import com.example.model.resource.ResourceConfig

/**
 * Result of attempting to activate a hero ability.
 */
sealed class AbilityActivationResult {
    data class Success(
        val updatedPlayer: Player,
        val abilityName: String,
        val message: String
    ) : AbilityActivationResult()

    data class Failure(
        val reason: String
    ) : AbilityActivationResult()
}

/**
 * Pure domain engine responsible for hero ability lifecycles, validation,
 * atomic mana and charge consumption, and active/passive distinction.
 */
class HeroAbilityEngine(
    private val resourceConfig: ResourceConfig = ResourceConfig()
) {
    /**
     * Determines whether a player's ability can be cast right now.
     * Enforces:
     * 1. Player must be alive.
     * 2. Passive abilities cannot be actively triggered.
     * 3. Cannot cast if already armed/cast in the current turn.
     * 4. Match quota of uses not exceeded.
     * 5. Sufficient mana.
     * 6. Cooldown / charge conditions met.
     */
    fun canCastAbility(player: Player): Boolean {
        // 1. Dead players cannot cast abilities
        if (!player.resources.isAlive) {
            return false
        }

        val ability = player.character.abilityType

        // 2. Passive abilities cannot be actively triggered
        if (ability.activationMode == AbilityActivationMode.PASSIVE) {
            return false
        }

        // 3. Already armed for current turn
        if (player.isActiveAbilityArmedForTurn) {
            return false
        }

        // 4. Max uses quota exceeded (single source of truth: player.abilityUsesCount)
        if (ability.maxUsesPerMatch > 0 && player.abilityUsesCount >= ability.maxUsesPerMatch) {
            return false
        }

        // 5. Mana check
        val manaCost = getAbilityManaCost(ability)
        if (player.resources.mana < manaCost) {
            return false
        }

        // 6. Cooldown / Readiness checks
        return when (ability.activationMode) {
            AbilityActivationMode.PASSIVE -> false
            AbilityActivationMode.AUTO_CHARGE -> player.isShieldCharged
            AbilityActivationMode.ACTIVE_COOLDOWN -> player.abilityChargeProgress >= ability.cooldownTurns
            AbilityActivationMode.ACTIVE_ONCE -> !player.hasUsedOnceAbility
        }
    }

    /**
     * Atomically activates the ability for the player:
     * 1. Checks player alive status and conditions.
     * 2. Deducts mana atomically once.
     * 3. Consumes charges and increments abilityUsesCount on Player once.
     * 4. Arms ability for current turn.
     */
    fun activateAbility(player: Player): AbilityActivationResult {
        if (!player.resources.isAlive) {
            return AbilityActivationResult.Failure("بازیکن مرده نمی‌تواند از قابلیت استفاده کند.")
        }

        val ability = player.character.abilityType

        if (!canCastAbility(player)) {
            val failureReason = when {
                ability.activationMode == AbilityActivationMode.PASSIVE ->
                    "این قابلیت دائمی (Passive) است و به صورت خودکار عمل می‌کند."
                player.isActiveAbilityArmedForTurn ->
                    "قابلیت قبلاً در این نوبت فعال شده است."
                ability.maxUsesPerMatch > 0 && player.abilityUsesCount >= ability.maxUsesPerMatch ->
                    "سهمیه استفاده از این قابلیت در این بازی به پایان رسیده است."
                player.resources.mana < getAbilityManaCost(ability) ->
                    "مانا کافی نیست! مانا مورد نیاز: ${getAbilityManaCost(ability)}، مانا فعلی: ${player.resources.mana}"
                ability.activationMode == AbilityActivationMode.ACTIVE_COOLDOWN && player.abilityChargeProgress < ability.cooldownTurns ->
                    "قابلیت هنوز شارژ نشده است (${player.abilityChargeProgress}/${ability.cooldownTurns} نوبت)."
                ability.activationMode == AbilityActivationMode.ACTIVE_ONCE && player.hasUsedOnceAbility ->
                    "این قابلیت تک‌بار مصرف قبلاً استفاده شده است."
                else -> "شرایط فعال‌سازی مهیا نیست."
            }
            return AbilityActivationResult.Failure(failureReason)
        }

        val manaCost = getAbilityManaCost(ability)
        val updatedResources = player.resources.spendMana(manaCost)
            ?: return AbilityActivationResult.Failure("کسر مانا با شکست مواجه شد.")

        val updatedPlayer = when (ability.activationMode) {
            AbilityActivationMode.ACTIVE_COOLDOWN -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                abilityChargeProgress = 0,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            AbilityActivationMode.ACTIVE_ONCE -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                hasUsedOnceAbility = true,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            AbilityActivationMode.AUTO_CHARGE -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            AbilityActivationMode.PASSIVE -> player
        }

        return AbilityActivationResult.Success(
            updatedPlayer = updatedPlayer,
            abilityName = ability.persianTitle,
            message = "قابلیت «${ability.persianTitle}» فعال شد! $manaCost مانا مصرف شد."
        )
    }

    /**
     * Executes beginning-of-turn ability and resource progression:
     * - Advances cooldown progress
     * - Regenerates turn Mana and Shield
     * - Ticks down status effects
     * - Disarms ability until explicitly cast in the new turn
     */
    fun onTurnStart(player: Player): Player {
        if (!player.resources.isAlive) return player

        val ability = player.character.abilityType
        val newCharge = (player.abilityChargeProgress + 1).coerceAtMost(ability.cooldownTurns)
        val updatedResources = player.resources
            .regenerateTurnResources(resourceConfig)
            .tickStatusEffects()

        return player.copy(
            resources = updatedResources,
            abilityChargeProgress = newCharge,
            isActiveAbilityArmedForTurn = false // Disarmed until manually cast
        )
    }

    /**
     * Proportional, tailored mana cost for abilities based on power and mode.
     */
    fun getAbilityManaCost(ability: CharacterAbilityType): Int {
        if (ability.activationMode == AbilityActivationMode.PASSIVE) return 0
        return when (ability) {
            CharacterAbilityType.GUARDIAN_SHIELD -> 20
            CharacterAbilityType.SPEEDSTER -> 25
            CharacterAbilityType.HIGH_PRECISION -> 20
            CharacterAbilityType.EARTH_TITAN -> 15
            CharacterAbilityType.HALF_REVERSE -> 30
            CharacterAbilityType.PHOENIX_WINGS -> 35
            CharacterAbilityType.SNAKE_HUNTER -> 35
            CharacterAbilityType.LADDER_MAGE -> 25
            CharacterAbilityType.SHADOW_WALKER -> 40
            CharacterAbilityType.STORM_LORD -> 30
            CharacterAbilityType.FORTUNE_STAR -> 25
            CharacterAbilityType.WIND_RIDER -> 30
            CharacterAbilityType.ALCHEMIST -> 25
            CharacterAbilityType.FROST_KNIGHT -> 30
            CharacterAbilityType.TIME_WEAVER -> 35
            CharacterAbilityType.WORM_IMMUNE -> 20
            CharacterAbilityType.CUSTOM_HERO -> 25
        }
    }
}
