package game

object Physics {
    fun executeIfPlayerIsCollidingWithTile(mapEntity: MapEntity, block: (pWorldX: Float, pWorldY: Float, playerEntity: MapEntity) -> Unit) {
        val playerEntityType = GameState.map.find { it.entity == EntityType.Player }
        if (playerEntityType == null) return

        val pWorldX = playerEntityType.gridPositionX * GameState.tileSize + GameState.playerPositionX
        val pWorldY = playerEntityType.gridPositionY * GameState.tileSize - GameState.playerPositionY

        val playerRight = pWorldX + GameState.tileSize
        val playerTop = pWorldY + GameState.tileSize

        val blockLeft = mapEntity.gridPositionX * GameState.tileSize
        val blockRight = blockLeft + GameState.tileSize
        val blockBottom = mapEntity.gridPositionY * GameState.tileSize
        val blockTop = blockBottom + GameState.tileSize

        if (playerRight > blockLeft && pWorldX < blockRight &&
            playerTop > blockBottom && pWorldY < blockTop
        ) {
            block(pWorldX, pWorldY, playerEntityType)
        }
    }
}