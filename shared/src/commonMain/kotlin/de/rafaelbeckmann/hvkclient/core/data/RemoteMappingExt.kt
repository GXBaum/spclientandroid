package de.rafaelbeckmann.hvkclient.core.data

import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.Result

// helper to catch mapping errors. not sure if it should stay like that or if the .toDomain() functions should already handle that, but that would be a bigger change
fun <T, R> Result<T, DataError>.mapRemoteData(
    convert: (T) -> R
): Result<R, DataError> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            try {
                Result.Success(convert(this.data))
            } catch (e: IllegalArgumentException) {
                Result.Error(DataError.Remote.SERIALIZATION)
            }
        }
    }
}