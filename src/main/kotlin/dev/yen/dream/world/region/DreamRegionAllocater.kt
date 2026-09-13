package dev.yen.dream.world.region

object DreamRegionAllocator {
    /**
     * Finds the next permanent Dream region for a real player.
     *
     * This reads the currently occupied regions from DreamWorldSavedData,
     * then finds the first free coordinate in the outward-growing square spiral.
     */
    fun nextAvailableRegion(worldData: DreamWorldSavedData): DreamRegion {
        val occupied =
            worldData
                .allRegions()
                .values
                .map { region ->
                    region.gridX to region.gridZ
                }.toSet()

        val coordinates =
            nextAvailableCoordinates(
                occupied,
            )

        return DreamRegion(
            gridX = coordinates.first,
            gridZ = coordinates.second,
        )
    }

    /**
     * Debug-only preview.
     *
     * Simulates assigning several future regions without modifying
     * DreamWorldSavedData or writing anything to disk.
     *
     * This is useful for verifying the spiral allocation order without
     * launching several extra Minecraft clients.
     */
    fun previewNextRegions(
        worldData: DreamWorldSavedData,
        count: Int,
    ): List<DreamRegion> {
        /*
         * Start with the same cells that are actually occupied
         * by real players.
         */
        val occupied =
            worldData
                .allRegions()
                .values
                .map { region ->
                    region.gridX to region.gridZ
                }.toMutableSet()

        val result =
            mutableListOf<DreamRegion>()

        /*
         * Repeatedly ask the allocator for the next available cell.
         *
         * After each simulated allocation, add that coordinate to the
         * temporary occupied set so the next simulated player receives
         * the following cell in the spiral.
         *
         * Nothing here modifies the real SavedData.
         */
        repeat(count) {
            val coordinates =
                nextAvailableCoordinates(
                    occupied,
                )

            val region =
                DreamRegion(
                    gridX = coordinates.first,
                    gridZ = coordinates.second,
                )

            result.add(region)

            occupied.add(
                region.gridX to region.gridZ,
            )
        }

        return result
    }

    /**
     * Core square-spiral search.
     *
     * The method accepts a set of occupied grid coordinates and returns
     * the first unoccupied coordinate when scanning outward from (0, 0).
     *
     * Expected beginning of the sequence:
     *
     * (0, 0)
     * (1, 0)
     * (1, 1)
     * (0, 1)
     * (-1, 1)
     * (-1, 0)
     * (-1, -1)
     * (0, -1)
     * (1, -1)
     * (2, -1)
     * ...
     */
    private fun nextAvailableCoordinates(occupied: Set<Pair<Int, Int>>): Pair<Int, Int> {
        fun isFree(
            gridX: Int,
            gridZ: Int,
        ): Boolean = (gridX to gridZ) !in occupied

        /*
         * The very first Dream region is the center of the grid.
         */
        if (isFree(0, 0)) {
            return 0 to 0
        }

        /*
         * Radius describes the square "ring" around the origin.
         *
         * radius = 1:
         *
         * (-1, 1)   (0, 1)   (1, 1)
         * (-1, 0)   (0, 0)   (1, 0)
         * (-1,-1)   (0,-1)   (1,-1)
         *
         * Once radius 1 is full, radius 2 surrounds it, and so on.
         */
        var radius = 1

        while (true) {
            /*
             * RIGHT EDGE
             *
             * Walk upward along X = +radius.
             *
             * For radius 1:
             * (1, 0)
             * (1, 1)
             *
             * For larger rings we begin one block below the horizontal
             * center so the spiral continues naturally from the previous ring.
             */
            for (
            gridZ in
            -(radius - 1)..radius
            ) {
                if (isFree(radius, gridZ)) {
                    return radius to gridZ
                }
            }

            /*
             * TOP EDGE
             *
             * Walk left along Z = +radius.
             *
             * Example for radius 1:
             * (0, 1)
             * (-1, 1)
             */
            for (
            gridX in
            radius - 1 downTo -radius
            ) {
                if (isFree(gridX, radius)) {
                    return gridX to radius
                }
            }

            /*
             * LEFT EDGE
             *
             * Walk downward along X = -radius.
             *
             * Example for radius 1:
             * (-1, 0)
             * (-1, -1)
             */
            for (
            gridZ in
            radius - 1 downTo -radius
            ) {
                if (isFree(-radius, gridZ)) {
                    return -radius to gridZ
                }
            }

            /*
             * BOTTOM EDGE
             *
             * Walk right along Z = -radius.
             *
             * Example for radius 1:
             * (0, -1)
             * (1, -1)
             */
            for (
            gridX in
            -radius + 1..radius
            ) {
                if (isFree(gridX, -radius)) {
                    return gridX to -radius
                }
            }

            /*
             * Every coordinate in this ring was occupied,
             * so move outward to the next square ring.
             */
            radius++
        }
    }
}
