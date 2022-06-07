package com.capstone.healthyplate.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.FragmentAccountBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

    private val user = Firebase.auth.currentUser
    private val userID = user?.uid
    private val db = Firebase.firestore
    private var storage = Firebase.storage
    private val storageRef = storage.reference

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accountViewModel =
            ViewModelProvider(this).get(AccountViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        getData()

        return root
    }

    private fun getData() {
        val docRef = db.collection("users").document(userID.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val age = document.getString("age")
                    val gender = document.getString("gender")
                    val job = document.getString("job")
                    binding.apply {
                        txtNameAccount.text = resources.getString(R.string.name_account, name)
                        txtEmailAccount.text = resources.getString(R.string.email_account, email)
                        txtAgeAccount.text = resources.getString(R.string.age_account, age)
                        txtGenderAccount.text = resources.getString(R.string.gender_account, gender)
                        txtJobAccount.text = resources.getString(R.string.job_account, job)
                    }
                    getProfilePic()
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getProfilePic() {
        storageRef.child("profile_picture/${userID.toString()}.png").downloadUrl.addOnSuccessListener { url ->
            Glide.with(this)
                .load(url)
                .into(binding.imgProfileCr)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "AccountFragment"
    }
}