package com.example.chronosyne.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.model.User
import com.example.chronosyne.repository.EventRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.java

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val userEvents = repository.getEventsForUser(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEvent(event: Event) = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun deleteEvent(event: Event) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun loadUser(userId: String) {
        Firebase.firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                _user.value = doc.toObject(User::class.java)
            }
    }

}