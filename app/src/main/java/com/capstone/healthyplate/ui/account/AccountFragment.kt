package com.capstone.healthyplate.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accountViewModel =
            ViewModelProvider(this)[AccountViewModel::class.java]

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        accountViewModel.userData.observe(viewLifecycleOwner) {
            Glide.with(this)
                .load(it.imageUri)
                .circleCrop()
                .into(binding.imgProfileCr)
            binding.apply {
                txtNameAccount.text = resources.getString(R.string.name_account, it.name)
                txtEmailAccount.text = resources.getString(R.string.email_account, it.email)
                txtAgeAccount.text = resources.getString(R.string.age_account, it.age)
                txtGenderAccount.text = resources.getString(R.string.gender_account, it.gender)
                txtJobAccount.text = resources.getString(R.string.job_account, it.job)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}