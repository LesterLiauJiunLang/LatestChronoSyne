package com.example.chronosyne.viewmodel

import android.R.attr.category
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.relations.EventWithCategories
import com.example.chronosyne.database.model.User
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.utils.addDays
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

open class EventViewModel(private val repository: EventRepository) : ViewModel() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val dateFormatter = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val today: Date by lazy {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }


    val userEvents = repository.getEventsForUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // In EventViewModel.kt
    val upcomingEvents: Flow<List<EventWithCategories>> =
        repository.getUpcomingEvents(userId, today, today.addDays(31))

    val futureEvents: Flow<List<EventWithCategories>> =
        repository.getFutureEvents(userId, today.addDays(31))

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
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to load user", e)
            }
    }

    open suspend fun insertEventWithCategory(event: Event, category: String): Long{
        return repository.insertEventWithCategory(event, category)
    }

    suspend fun getEventById(eventId: Int): Event? {
        return repository.getEventWithCategories(eventId).event
    }

}