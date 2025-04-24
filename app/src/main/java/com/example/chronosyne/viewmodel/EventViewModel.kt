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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.jvm.java


@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val dateFormatter = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())


    val userEvents = repository.getEventsForUser(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val upcomingEvents: StateFlow<List<Event>> = userEvents
        .map { events ->
            val now = Date()
            val calendar = Calendar.getInstance().apply {
                time = now
                add(Calendar.MONTH, 1)
            }
            val oneMonthFromNow = calendar.time

            events.filter {
                it.startTime.after(now) && it.startTime.before(oneMonthFromNow)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val futureEvents: StateFlow<List<Event>> = userEvents
        .map { events ->
            val calendar = Calendar.getInstance().apply {
                time = Date()
                add(Calendar.MONTH, 1)
            }
            val oneMonthFromNow = calendar.time

            events.filter {
                it.startTime.after(oneMonthFromNow)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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