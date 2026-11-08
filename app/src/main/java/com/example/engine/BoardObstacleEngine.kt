package com.example.engine

import com.example.model.BannerType
import com.example.model.BoardConfiguration
import com.example.model.LinkType
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.resource.ActiveStatusEffect
import com.example.model.resource.StatusEffectType

/**
 * Result of resolving a tile landing against board links and obstacles.
 */
data class ObstacleResolution(
    val initialTile: Int,
    val resolvedTile: Int,
    val linkType: LinkType? = null,
    val obstacleType: ObstacleType? = null,
    val bannerMessage: String? = null,
    val bannerType: BannerType = BannerType.NORMAL,
    val addedStatusEffects: List<ActiveStatusEffect> = emptyList(),
    val halveNextRoll: Boolean = false,
    val skipNextTurn: Boolean = false,
    val isProtectedByShieldOrPassive: Boolean = false
)

/**
 * Pure domain engine for evaluating Snakes, Ladders, and Special Board Obstacles,
 * cleanly separated from ViewModel and UI rendering.
 */
class BoardObstacleEngine(
    private val randomProvider: RandomProvider = DefaultRandomProvider()
) {
    /**
     * Evaluates the landing tile of a player against board links (ladders & snakes)
     * and special obstacles (swamp, worm, well, reverse, wind, luck, storm).
     */
    fun evaluateLanding(
        tile: Int,
        boardConfig: BoardConfiguration,
        player: Player
    ): ObstacleResolution {
        // 1. Check Ladders and Snakes first
        val matchingLink = boardConfig.links.find { it.startTile == tile }
        if (matchingLink != null) {
            when (matchingLink.type) {
                LinkType.LADDER -> {
                    return ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = matchingLink.endTile,
                        linkType = LinkType.LADDER,
                        bannerMessage = "ایول! از پله بالا رفتی تا خانه ${matchingLink.endTile}! 🚀",
                        bannerType = BannerType.LADDER_CLIMB
                    )
                }
                LinkType.SNAKE -> {
                    // Check if player has active shield or snake protection passive
                    val hasSnakeProtection = player.guardianAngelProtected ||
                        (player.isShieldCharged && player.resources.shield > 0)

                    if (hasSnakeProtection) {
                        return ObstacleResolution(
                            initialTile = tile,
                            resolvedTile = tile,
                            linkType = LinkType.SNAKE,
                            bannerMessage = "🛡️ سپر محافظ نیش مار را کاملاً دفع کرد و سر جایت ماندی!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            isProtectedByShieldOrPassive = true
                        )
                    }

                    return ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = matchingLink.endTile,
                        linkType = LinkType.SNAKE,
                        bannerMessage = "آخ! نیش مار! سقوط به خانه ${matchingLink.endTile} 🐍",
                        bannerType = BannerType.SNAKE_BITE,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.POISON, turnsRemaining = 1, magnitude = 1)
                        )
                    )
                }
            }
        }

        // 2. Check Special Obstacles
        val matchingObstacle = boardConfig.obstacles.find { it.tile == tile }
        if (matchingObstacle != null) {
            return when (matchingObstacle.type) {
                ObstacleType.WORM -> {
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = tile,
                        obstacleType = ObstacleType.WORM,
                        bannerMessage = "🐛 کرم لزج! تا ۲ نوبت بدشانسی و تاس ۱ و ۲ همراهته!",
                        bannerType = BannerType.OBSTACLE_DANGER,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.WORM_CURSE, turnsRemaining = 2, magnitude = 1)
                        )
                    )
                }
                ObstacleType.REVERSE -> {
                    val fallbackTile = (tile - 3).coerceAtLeast(1)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = fallbackTile,
                        obstacleType = ObstacleType.REVERSE,
                        bannerMessage = "↩️ خانه معکوس! ۳ خانه به عقب پرتاب شدی!",
                        bannerType = BannerType.OBSTACLE_DANGER
                    )
                }
                ObstacleType.SWAMP -> {
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = tile,
                        obstacleType = ObstacleType.SWAMP,
                        bannerMessage = "🌿 باتلاق چسبناک! یک نوبت گیر می‌افتی و تاس بعدی‌ات کند می‌شود!",
                        bannerType = BannerType.OBSTACLE_DANGER,
                        skipNextTurn = true,
                        halveNextRoll = true,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.SWAMP_SLOWNESS, turnsRemaining = 1, magnitude = 1)
                        )
                    )
                }
                ObstacleType.WELL -> {
                    val wellDropTile = (tile - 5).coerceAtLeast(1)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = wellDropTile,
                        obstacleType = ObstacleType.WELL,
                        bannerMessage = "🕳️ چاه تاریک! سقوط ناگهانی به خانه $wellDropTile!",
                        bannerType = BannerType.OBSTACLE_DANGER
                    )
                }
                ObstacleType.WIND -> {
                    val isForward = randomProvider.nextBoolean()
                    val delta = if (isForward) 4 else -2
                    val newTile = (tile + delta).coerceIn(1, boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.WIND,
                        bannerMessage = if (isForward) "💨 باد موافق وزید! ۴ خانه جلو افتادی!" else "💨 باد مخالف! ۲ خانه به عقب رانده شدی!",
                        bannerType = BannerType.OBSTACLE_SURPRISE
                    )
                }
                ObstacleType.LUCK -> {
                    val bonus = randomProvider.nextInt(1, 5)
                    val newTile = (tile + bonus).coerceAtMost(boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.LUCK,
                        bannerMessage = "🎁 خانه شانس! $bonus خانه جایزه گرفتی!",
                        bannerType = BannerType.OBSTACLE_SURPRISE
                    )
                }
                ObstacleType.STORM -> {
                    val delta = if (randomProvider.nextBoolean()) 2 else -2
                    val newTile = (tile + delta).coerceIn(1, boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.STORM,
                        bannerMessage = "🌪️ طوفان سهمگین! باد تو را به خانه $newTile جابه‌جا کرد!",
                        bannerType = BannerType.STORM_CHAOS
                    )
                }
            }
        }

        // Standard tile with no link or obstacle
        return ObstacleResolution(
            initialTile = tile,
            resolvedTile = tile
        )
    }
}
