package io.github.realtryz.journal.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screens {
    @Serializable data object Home : NavKey
    @Serializable data object Settings : NavKey
    @Serializable data object Contributions : NavKey
    @Serializable data class Detail(val id: String) : NavKey
    @Serializable data class Overview(val id: String) : NavKey
}