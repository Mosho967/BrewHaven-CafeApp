package com.brewhaven.app.ui.feedback

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment(R.layout.fragment_feedback) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(true)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val editFeedback = view.findViewById<EditText>(R.id.editFeedback)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        toolbar.title = "Feedback"
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSubmit.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "Please sign in to send feedback.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rating = ratingBar.rating.toInt()
            if (rating == 0) {
                Toast.makeText(requireContext(), "Please choose a rating.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comment = editFeedback.text?.toString()?.trim().orEmpty()

            btnSubmit.isEnabled = false

            val payload = hashMapOf(
                "rating" to rating,
                "comment" to comment,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("customers")
                .document(user.uid)
                .collection("feedback")
                .add(payload)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        e.localizedMessage ?: "Failed to send feedback.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnCompleteListener {
                    btnSubmit.isEnabled = true
                }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }
}
