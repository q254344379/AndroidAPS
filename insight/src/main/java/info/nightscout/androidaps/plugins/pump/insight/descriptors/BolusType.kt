package info.nightscout.androidaps.plugins.pump.insight.descriptors

enum class BolusType (val id: Int,val activeId: Int)  {
    STANDARD (31, 227),
    EXTENDED (227, 252),
    MULTIWAVE (252, 805);

    companion object {
        fun fromActiveId(activeId: Int) = values().firstOrNull { it.activeId == activeId } ?:MULTIWAVE
        fun fromId(id: Int) = values().firstOrNull { it.id == id } ?:MULTIWAVE

    }
}