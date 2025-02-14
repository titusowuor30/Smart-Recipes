package com.example.smartrecipe.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.smartrecipe.R
import com.example.smartrecipe.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.viewModel = viewModel

<<<<<<< HEAD
=======

>>>>>>> 16715773b7da518edfb3d99bc1cc586dfe7e17c7
        viewModel.navigateToCategories.observe(viewLifecycleOwner,
            Observer<Boolean> { shouldNavigate ->
                if (shouldNavigate == true) {
                    val navController = binding.root.findNavController()
<<<<<<< HEAD
                    navController.navigate(R.id.action_nav_home_to_recipeListActivity)
=======
                    navController.navigate(R.id.action_nav_home_to_nav_categories)
                    viewModel.onNavigatedToCategories()
                }
            })

        viewModel.navigateToSubscription.observe(viewLifecycleOwner,
            Observer<Boolean> { shouldNavigate ->
                if (shouldNavigate == true) {
                    val navController = binding.root.findNavController()
                    navController.navigate(R.id.action_nav_home_to_subscriptionActivity)
>>>>>>> 16715773b7da518edfb3d99bc1cc586dfe7e17c7
                    viewModel.onNavigatedToCategories()
                }
            })
        return binding.root

    }

}