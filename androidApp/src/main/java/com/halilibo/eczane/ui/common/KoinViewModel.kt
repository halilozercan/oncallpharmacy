package com.halilibo.eczane.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.ext.scope
import java.util.concurrent.Executors

@Composable inline fun <reified T : ViewModel> koinViewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    return lazy(LazyThreadSafetyMode.NONE) {
        KoinContextHandler.get().getViewModel(
            viewModelStoreOwner,
            T::class,
            qualifier,
            parameters
        )
    }
}

@Composable inline fun <reified T, reified S> statefulViewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Pair<T, S> where T: StatefulViewModel<S> {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val viewModel = KoinContextHandler.get().getViewModel(
        viewModelStoreOwner,
        T::class,
        qualifier,
        parameters
    )
    val state = viewModel.stateFlow.collectAsState().value
    return viewModel to state
}

/**
 * Taken from https://github.com/airbnb/MvRx/blob/release/2.0.0/mvrx/src/main/kotlin/com/airbnb/mvrx/CoroutinesStateStore.kt
 * Simplified for this project.
 */
abstract class StatefulViewModel<S>(
    initialState: S
): ViewModel() {

    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)

    private val _stateFlow = MutableStateFlow(initialState)

    val stateFlow: StateFlow<S> = _stateFlow.asStateFlow()

    init {
        setupTriggerFlushQueues(viewModelScope)
    }

    /**
     * Poll [withStateChannel] and [setStateChannel] to respond to set/get state requests.
     */
    private fun setupTriggerFlushQueues(scope: CoroutineScope) {
        scope.launch(flushDispatcher) {
            while (isActive) {
                flushQueuesOnce()
            }
        }
    }

    private suspend fun flushQueuesOnce() {
        select<Unit> {
            setStateChannel.onReceive { reducer ->
                val newState = state.reducer()
                if (newState != state) {
                    _stateFlow.emit(newState)
                }
            }
            withStateChannel.onReceive { block ->
                block(state)
            }
        }
    }

    fun withState(block: (S) -> Unit) {
        withStateChannel.offer(block)
    }

    protected fun setState(stateReducer: (S) -> S) {
        setStateChannel.offer(stateReducer)
    }

    private val state: S
        get() = _stateFlow.value

    companion object {
        private val flushDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
    }
}
