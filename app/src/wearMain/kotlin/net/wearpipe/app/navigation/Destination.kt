package net.wearpipe.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable data object Home : Destination
    @Serializable data class Search(val query: String = "") : Destination
    @Serializable data class Details(val serviceId: Int, val url: String) : Destination
    @Serializable data object Player : Destination
    @Serializable data object Queue : Destination
    @Serializable data object History : Destination
    @Serializable data object Settings : Destination
}
