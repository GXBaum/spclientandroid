package de.rafaelbeckmann.hvkclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatform.getKoin
import kotlin.reflect.KClass
import org.koin.viewmodel.resolveViewModel as koinResolveViewModel

// adapted for koin from https://developer.android.com/kotlin/multiplatform/viewmodel

/**
 *   This function allows retrieving any ViewModel from Swift Code with generics. We only get
 *   [ObjCClass] type for the [modelClass], because the interop between Kotlin and Swift code
 *   doesn't preserve the generic class, but we can retrieve the original KClass in Kotlin.
 */
@OptIn(BetaInteropApi::class, KoinInternalApi::class)
@Throws(IllegalArgumentException::class)
fun ViewModelStore.resolveViewModel(
    modelClass: ObjCClass,
    //factory: ViewModelProvider.Factory,
    key: String?,
    extras: CreationExtras? = null,
): ViewModel {
    @Suppress("UNCHECKED_CAST")
    val vmClass = getOriginalKotlinClass(modelClass) as? KClass<ViewModel>
    require(vmClass != null) { "The modelClass parameter must be a ViewModel type." }

    //val provider = ViewModelProvider.Companion.create(this, factory, extras ?: CreationExtras.Empty)
    //return key?.let { provider[key, vmClass] } ?: provider[vmClass]

    return koinResolveViewModel(
        vmClass = vmClass,
        viewModelStore = this,
        key = key,
        extras = extras ?: CreationExtras.Empty,
        scope = getKoin().scopeRegistry.rootScope
    )
}
