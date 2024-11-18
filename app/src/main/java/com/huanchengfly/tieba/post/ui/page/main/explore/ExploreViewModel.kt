package com.huanchengfly.tieba.post.ui.page.main.explore

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.HistoryUtil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import org.litepal.LitePal

@Stable
class ExploreViewModel : BaseViewModel<ExploreUiIntent, ExplorePartialChange, ExploreUiState, HomeUiEvent>() {
    override fun createInitialState(): ExploreUiState = ExploreUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ExploreUiIntent, ExplorePartialChange, ExploreUiState> =
        HomePartialChangeProducer

    override fun dispatchEvent(partialChange: ExplorePartialChange): UiEvent? =
        when (partialChange) {
            is ExplorePartialChange.TopForums.Delete.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            is ExplorePartialChange.TopForums.Add.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            else -> null
        }

    object HomePartialChangeProducer :
        PartialChangeProducer<ExploreUiIntent, ExplorePartialChange, ExploreUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ExploreUiIntent>): Flow<ExplorePartialChange> {
            return merge(
                intentFlow.filterIsInstance<ExploreUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.RefreshHistory>()
                    .flatMapConcat { produceRefreshHistoryPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.TopForums.Delete>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.TopForums.Add>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.Unfollow>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.ToggleHistory>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<ExploreUiIntent.ChangeFollowedType>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )
        }

        @Suppress("USELESS_CAST")
        private fun produceRefreshPartialChangeFlow(): Flow<ExplorePartialChange.Refresh> {
            return HistoryUtil.getFlow(HistoryUtil.TYPE_FORUM, 0)
                .zip(
                    TiebaApi.getInstance().forumHomeFlow(1)
                ) { historyForums, forumRecommend ->
                    val forums = forumRecommend.data?.likeForum?.list?.map {
                        ExploreUiState.Forum(
                            it.avatar,
                            "${it.forumId}",
                            it.forumName,
                            it.hotNum,
                            "${it.levelId}"
                        )
                    } ?: emptyList()
                    val topForums = mutableListOf<ExploreUiState.Forum>()
                    val topForumsDB = LitePal.findAll(TopForum::class.java).map { it.forumId }
                    topForums.addAll(forums.filter { topForumsDB.contains(it.forumId) })
                    ExplorePartialChange.Refresh.Success(
                        forums,
                        topForums,
                        historyForums
                    ) as ExplorePartialChange.Refresh
                }
                .onStart { emit(ExplorePartialChange.Refresh.Start) }
                .catch {
                    emit(ExplorePartialChange.Refresh.Failure(it))
                }
        }
        @Suppress("USELESS_CAST")
        private fun produceRefreshHistoryPartialChangeFlow(): Flow<ExplorePartialChange.RefreshHistory> =
            HistoryUtil.getFlow(HistoryUtil.TYPE_FORUM, 0)
                .map { ExplorePartialChange.RefreshHistory.Success(it) as ExplorePartialChange.RefreshHistory }
                .catch { emit(ExplorePartialChange.RefreshHistory.Failure(it)) }

        private fun ExploreUiIntent.TopForums.Delete.toPartialChangeFlow() =
            flow {
                val deletedRows = LitePal.deleteAll(TopForum::class.java, "forumId = ?", forumId)
                if (deletedRows > 0) {
                    emit(ExplorePartialChange.TopForums.Delete.Success(forumId))
                } else {
                    emit(ExplorePartialChange.TopForums.Delete.Failure("forum $forumId is not top!"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(ExplorePartialChange.TopForums.Delete.Failure(it.getErrorMessage())) }

        private fun ExploreUiIntent.TopForums.Add.toPartialChangeFlow() =
            flow {
                val success = TopForum(forum.forumId).saveOrUpdate("forumId = ?", forum.forumId)
                if (success) {
                    emit(ExplorePartialChange.TopForums.Add.Success(forum))
                } else {
                    emit(ExplorePartialChange.TopForums.Add.Failure("未知错误"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(ExplorePartialChange.TopForums.Add.Failure(it.getErrorMessage())) }

        private fun ExploreUiIntent.Unfollow.toPartialChangeFlow() =
            TiebaApi.getInstance()
                .unlikeForumFlow(forumId, forumName, AccountUtil.getLoginInfo()!!.tbs)
                .map<CommonResponse, ExplorePartialChange.Unfollow> {
                    ExplorePartialChange.Unfollow.Success(forumId)
                }
                .catch { emit(ExplorePartialChange.Unfollow.Failure(it.getErrorMessage())) }

        private fun ExploreUiIntent.ToggleHistory.toPartialChangeFlow() =
            flowOf(ExplorePartialChange.ToggleHistory(!currentExpand))

        private fun ExploreUiIntent.ChangeFollowedType.toPartialChangeFlow() =
            flowOf(ExplorePartialChange.ChangeFollowedType(if (currType == 0) 1 else 0))
    }
}

sealed interface ExploreUiIntent : UiIntent {
    data object Refresh : ExploreUiIntent

    data object RefreshHistory : ExploreUiIntent

    data class Unfollow(val forumId: String, val forumName: String) : ExploreUiIntent

    sealed interface TopForums : ExploreUiIntent {
        data class Delete(val forumId: String) : TopForums

        data class Add(val forum: ExploreUiState.Forum) : TopForums
    }

    data class ToggleHistory(val currentExpand: Boolean) : ExploreUiIntent

    data class ChangeFollowedType(val currType: Int) : ExploreUiIntent
}

sealed interface ExplorePartialChange : PartialChange<ExploreUiState> {
    sealed class Unfollow : ExplorePartialChange {
        override fun reduce(oldState: ExploreUiState): ExploreUiState =
            when (this) {
                is Success -> {
                    oldState.copy(
                        forums = oldState.forums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                        topForums = oldState.topForums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                    )
                }

                is Failure -> oldState
            }

        data class Success(val forumId: String) : Unfollow()

        data class Failure(val errorMessage: String) : Unfollow()
    }

    sealed class Refresh : ExplorePartialChange {
        override fun reduce(oldState: ExploreUiState): ExploreUiState =
            when (this) {
                is Success -> oldState.copy(
                    isLoading = false,
                    forums = forums.toImmutableList(),
                    topForums = topForums.toImmutableList(),
                    historyForums = historyForums.toImmutableList(),
                    error = null
                )

                is Failure -> oldState.copy(isLoading = false, error = error)
                Start -> oldState.copy(isLoading = true)
            }

        data object Start : Refresh()

        data class Success(
            val forums: List<ExploreUiState.Forum>,
            val topForums: List<ExploreUiState.Forum>,
            val historyForums: List<History>,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class RefreshHistory : ExplorePartialChange {
        override fun reduce(oldState: ExploreUiState): ExploreUiState =
            when (this) {
                is Success -> oldState.copy(
                    historyForums = historyForums.toImmutableList(),
                )

                else -> oldState
            }

        data class Success(
            val historyForums: List<History>,
        ) : RefreshHistory()

        data class Failure(
            val error: Throwable,
        ) : RefreshHistory()
    }

    sealed interface TopForums : ExplorePartialChange {
        sealed interface Delete : ExplorePartialChange {
            override fun reduce(oldState: ExploreUiState): ExploreUiState =
                when (this) {
                    is Success -> oldState.copy(topForums = oldState.topForums.filterNot { it.forumId == forumId }
                        .toImmutableList())

                    is Failure -> oldState
                }

            data class Success(val forumId: String) : Delete

            data class Failure(val errorMessage: String) : Delete
        }

        sealed interface Add : ExplorePartialChange {
            override fun reduce(oldState: ExploreUiState): ExploreUiState =
                when (this) {
                    is Success -> {
                        val topForumsId = oldState.topForums.map { it.forumId }.toMutableList()
                        topForumsId.add(forum.forumId)
                        oldState.copy(
                            topForums = oldState.forums.filter { topForumsId.contains(it.forumId) }
                                .toImmutableList()
                        )
                    }

                    is Failure -> oldState
                }

            data class Success(val forum: ExploreUiState.Forum) : Add

            data class Failure(val errorMessage: String) : Add
        }
    }

    data class ToggleHistory(val expand: Boolean) : ExplorePartialChange {
        override fun reduce(oldState: ExploreUiState): ExploreUiState =
            oldState.copy(expandHistoryForum = expand)
    }

    data class ChangeFollowedType(val currType: Int) :ExplorePartialChange{
        override fun reduce(oldState: ExploreUiState): ExploreUiState =
            oldState.copy(followedType = currType)
    }
}

@Immutable
data class ExploreUiState(
    val isLoading: Boolean = true,
    val forums: ImmutableList<Forum> = persistentListOf(),
    val topForums: ImmutableList<Forum> = persistentListOf(),
    val historyForums: ImmutableList<History> = persistentListOf(),
    val expandHistoryForum: Boolean = true,
    val followedType: Int = 0,
    val error: Throwable? = null,
) : UiState {
    @Immutable
    data class Forum(
        val avatar: String,
        val forumId: String,
        val forumName: String,
        val hotNum: Long,
        val levelId: String,
    )
}

sealed interface HomeUiEvent : UiEvent