package game

object Physics {
    fun executeIfPlayerIsCollidingWithTile(mapEntity: MapEntity, block: (pWorldX: Float, pWorldY: Float, playerEntity: MapEntity) -> Unit) {
        val playerEntity = GameState.map.find { it.entity == Entity.Player }
        if (playerEntity == null) return

        val pWorldX = playerEntity.gridPositionX * GameState.TILE_SIZE + GameState.playerPositionX
        val pWorldY = playerEntity.gridPositionY * GameState.TILE_SIZE - GameState.playerPositionY

        val playerRight = pWorldX + GameState.TILE_SIZE
        val playerTop = pWorldY + GameState.TILE_SIZE

        val blockLeft = mapEntity.gridPositionX * GameState.TILE_SIZE
        val blockRight = blockLeft + GameState.TILE_SIZE
        val blockBottom = mapEntity.gridPositionY * GameState.TILE_SIZE
        val blockTop = blockBottom + GameState.TILE_SIZE

        if (playerRight > blockLeft && pWorldX < blockRight &&
            playerTop > blockBottom && pWorldY < blockTop
        ) {
            block(pWorldX, pWorldY, playerEntity)
        }
    }
}