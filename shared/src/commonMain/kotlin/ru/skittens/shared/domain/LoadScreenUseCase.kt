package ru.skittens.shared.domain

import kotlinx.coroutines.flow.Flow
import ru.skittens.shared.data.ScreenRepository
import ru.skittens.shared.data.ScreenResult

class LoadScreenUseCase(private val repository: ScreenRepository) {
    operator fun invoke(screenId: String, refresh: Boolean = true): Flow<ScreenResult> =
        repository.streamScreen(screenId, refresh)
}
