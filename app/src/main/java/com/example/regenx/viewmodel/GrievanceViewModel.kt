package com.example.regenx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.regenx.models.Grievance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ListenerRegistration

class GrievanceViewModel : ViewModel() {

    private val _grievances = MutableLiveData<List<Grievance>>()
    val grievances: LiveData<List<Grievance>> get() = _grievances

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenToGrievances()
    }

    private fun listenToGrievances() {
        val db = Firebase.firestore
        listenerRegistration = db.collection("grievances")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Grievance::class.java)?.copy(id = doc.id)
                    }
                    _grievances.value = list
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun submitResidentGrievance(description: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val grievance = Grievance(
            type = "resident",
            userId = userId,
            description = description
        )
        Firebase.firestore.collection("grievances")
            .add(grievance)
    }
}
