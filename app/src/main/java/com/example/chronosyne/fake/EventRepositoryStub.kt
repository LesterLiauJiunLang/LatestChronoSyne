package com.example.chronosyne.fake

import com.example.chronosyne.database.Event
import com.example.chronosyne.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// EventRepositoryStub.kt
class EventRepositoryStub : EventRepository(
    eventDao = FakeEventDao(),
    crossRefDao = FakeCrossRefDao(),
    auth = FirebaseAuth.getInstance(),
    firestore = FirebaseFirestore.getInstance()
) {
    // Add 'suspend' modifier
    override suspend fun insertEventWithCategory(event: Event, category: String) {
        // No-op for preview
    }
}