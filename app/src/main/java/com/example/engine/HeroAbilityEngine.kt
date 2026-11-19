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
     */
    fun canCastAbility(player: Player): Boolean {
        if (!player.resources.isAlive) {
            return false
        }

        val ability = player.character.abilityType

        // 1. Passive abilities cannot be actively triggered
        if (ability.activationMode == AbilityActivationMode.PASSIVE) {
            return false
        }

        // 2. Already armed for current turn
        if (player.isActiveAbilityArmedForTurn) {
            return false
        }

        // 3. Max uses quota exceeded
        if (ability.maxUsesPerMatch > 0 && player.abilityUsesCount >= ability.maxUsesPerMatch) {
            return false
        }

        // 4. Mana check
        val manaCost = getAbilityManaCost(ability)
        if (player.resources.mana < manaCost) {
            return false
        }

        // 5. Special rule for Raven's Shadow Walker 10-personal-turn window
        if (ability == CharacterAbilityType.SHADOW_WALKER) {
            return player.leaderSwapWindowTurnsRemaining > 0
        }

        // 6. Standard Cooldown / Readiness checks
        return when (ability.activationMode) {
            AbilityActivationMode.PASSIVE -> false
            AbilityActivationMode.AUTO_CHARGE -> player.isShieldCharged
            AbilityActivationMode.ACTIVE_COOLDOWN -> player.abilityChargeProgress >= ability.cooldownTurns
            AbilityActivationMode.ACTIVE_ONCE -> !player.hasUsedOnceAbility
        }
    }

    /**
     * Atomically activates the ability for the player.
     * Enforces that if Mana or charges are insufficient, no state change occurs.
     */
    fun activateAbility(player: Player): AbilityActivationResult {
        val ability = player.character.abilityType

        if (!canCastAbility(player)) {
            val failureReason = when {
                !player.resources.isAlive ->
                    "قهرمان حذف شده و نمی‌تواند از قابلیت استفاده کند."
                ability.activationMode == AbilityActivationMode.PASSIVE ->
                    "این قابلیت دائمی (Passive) است و به صورت خودکار عمل می‌کند."
                player.isActiveAbilityArmedForTurn ->
                    "قابلیت برای این پرتاب مسلح شده است."
                ability.maxUsesPerMatch > 0 && player.abilityUsesCount >= ability.maxUsesPerMatch ->
                    "سهمیه استفاده از این قابلیت در این بازی به پایان رسیده است."
                player.resources.mana < getAbilityManaCost(ability) ->
                    "مانا کافی نیست! مانا مورد نیاز: ${getAbilityManaCost(ability)}، مانا فعلی: ${player.resources.mana}"
                ability == CharacterAbilityType.SHADOW_WALKER && player.leaderSwapWindowTurnsRemaining <= 0 ->
                    "فرصت جابه‌جایی با نفر اول هنوز شارژ نشده یا منقضی شده است."
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
            ?.incrementAbilityUses()
            ?: return AbilityActivationResult.Failure("کسر مانا با شکست مواجه شد.")

        val updatedPlayer = when {
            ability == CharacterAbilityType.SHADOW_WALKER -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                leaderSwapWindowTurnsRemaining = 0,
                leaderSwapNextChargeTurn = player.personalTurnsTaken + 10,
                abilityChargeProgress = 0,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            ability.activationMode == AbilityActivationMode.ACTIVE_COOLDOWN -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                abilityChargeProgress = 0,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            ability.activationMode == AbilityActivationMode.ACTIVE_ONCE -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                hasUsedOnceAbility = true,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            ability.activationMode == AbilityActivationMode.AUTO_CHARGE -> player.copy(
                resources = updatedResources,
                isActiveAbilityArmedForTurn = true,
                abilityUsesCount = player.abilityUsesCount + 1
            )
            else -> player
        }

        return AbilityActivationResult.Success(
            updatedPlayer = updatedPlayer,
            abilityName = ability.persianTitle,
            message = "قابلیت «${ability.persianTitle}» فعال شد! $manaCost مانا مصرف شد."
        )
    }

    /**
     * Executes beginning-of-turn ability and resource progression:
     * - Advances cooldown progress and Guardian Shield recharge
     * - Regenerates turn Mana and Shield
     * - Ticks down status effects
     */
    fun onTurnStart(player: Player): Player {
        if (!player.resources.isAlive) return player

        val ability = player.character.abilityType
        val maxCooldown = ability.cooldownTurns.coerceAtLeast(3)
        val newCharge = (player.abilityChargeProgress + 1).coerceAtMost(maxCooldown)
        val rechargedShield = if (ability == CharacterAbilityType.GUARDIAN_SHIELD && !player.isShieldCharged) {
            newCharge >= 3
        } else {
            player.isShieldCharged
        }
        val updatedResources = player.resources
            .regenerateTurnResources(resourceConfig)
            .tickStatusEffects()

        return player.copy(
            resources = updatedResources,
            abilityChargeProgress = newCharge,
            isShieldCharged = rechargedShield,
            isActiveAbilityArmedForTurn = false
        )
    }

    /**
     * Standard mana cost for active abilities.
     */
    fun getAbilityManaCost(ability: CharacterAbilityType): Int {
        return when (ability.activationMode) {
            AbilityActivationMode.PASSIVE -> 0
            AbilityActivationMode.ACTIVE_COOLDOWN -> 25
            AbilityActivationMode.ACTIVE_ONCE -> 40
            AbilityActivationMode.AUTO_CHARGE -> 15
        }
    }
}
