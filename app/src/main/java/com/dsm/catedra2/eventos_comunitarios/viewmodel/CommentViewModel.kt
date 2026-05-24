package com.dsm.catedra2.eventos_comunitarios.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.catedra2.eventos_comunitarios.model.Comment
import com.dsm.catedra2.eventos_comunitarios.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    fun loadCommentsForEvent(eventId: String) {
        viewModelScope.launch {
            repository.getCommentsForEvent(eventId).collect { eventComments ->
                _comments.update { currentMap ->
                    currentMap + (eventId to eventComments)
                }
            }
        }
    }

    private fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
        while (true) {
            val prevValue = value
            val nextValue = function(prevValue)
            if (compareAndSet(prevValue, nextValue)) {
                return
            }
        }
    }

    fun addComment(eventId: String, userId: String, userEmail: String, rating: Int, content: String) {
        viewModelScope.launch {
            val newComment = Comment(
                eventId = eventId,
                userId = userId,
                userEmail = userEmail,
                rating = rating,
                content = content
            )
            repository.addComment(newComment)
        }
    }
}