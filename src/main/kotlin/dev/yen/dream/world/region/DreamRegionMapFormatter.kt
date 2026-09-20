package dev.yen.dream.world.region

object DreamRegionMapFormatter {
    /*
     * Radius 4 produces a 9 x 9 map:
     *
     * four cells west/east of the player
     * four cells north/south of the player
     * the player's region in the center
     */
    private const val DEFAULT_RADIUS = 4

    /**
     * Creates a local ASCII view of allocated Dream regions.
     *
     * "." = unallocated region
     * "O" = another allocated region
     * "X" = the current player's permanent region
     *
     * Smaller Z coordinates are printed first, so north appears
     * at the top of the map.
     */
    fun createMap(
        worldData: DreamWorldSavedData,
        playerRegion: DreamRegion,
        radius: Int = DEFAULT_RADIUS,
    ): List<String> {
        require(radius >= 0) {
            "Dream region map radius cannot be negative"
        }

        val occupiedCoordinates =
            worldData
                .allRegions()
                .values
                .map { region ->
                    region.gridX to region.gridZ
                }.toSet()

        val rows =
            mutableListOf<String>()

        for (
        gridZ in
        playerRegion.gridZ - radius..playerRegion.gridZ + radius
        ) {
            val row =
                buildString {
                    for (
                    gridX in
                    playerRegion.gridX - radius..playerRegion.gridX + radius
                    ) {
                        val symbol =
                            when {
                                gridX == playerRegion.gridX &&
                                    gridZ == playerRegion.gridZ -> {
                                    'X'
                                }

                                (gridX to gridZ) in occupiedCoordinates -> {
                                    'O'
                                }

                                else -> {
                                    '.'
                                }
                            }

                        append(symbol)
                    }
                }

            rows.add(row)
        }

        return rows
    }
}
