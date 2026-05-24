package com.dsm.catedra2.eventos_comunitarios.repository

import com.dsm.catedra2.eventos_comunitarios.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface CommentRepository {
    suspend fun addComment(comment: Comment): Result<Unit>
    fun getCommentsForEvent(eventId: String): Flow<List<Comment>>
}

class MockCommentRepository : CommentRepository {
    private val _commentsFlow = MutableStateFlow<List<Comment>>(emptyList())

    override suspend fun addComment(comment: Comment): Result<Unit> {
        _commentsFlow.update { currentList -> currentList + comment }
        return Result.success(Unit)
    }

    override fun getCommentsForEvent(eventId: String): Flow<List<Comment>> {
        return _commentsFlow.asStateFlow().map { allComments ->
            allComments.filter { it.eventId == eventId }
        }
    }
}