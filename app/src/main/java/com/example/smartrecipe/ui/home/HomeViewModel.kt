package com.example.smartrecipe.ui.home

<<<<<<< HEAD
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _navigateToCategories = MutableLiveData<Boolean>()
    val navigateToCategories: LiveData<Boolean>
        get() = _navigateToCategories

    fun onFabClicked() {
        _navigateToCategories.value = true
=======
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartrecipe.ui.subscription.SubscriptionActivity

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _navigateToCategories = MutableLiveData<Boolean>()
    val navigateToCategories: LiveData<Boolean>
        get() = _navigateToCategories
    private val _navigateToSubscription = MutableLiveData<Boolean>()
    val navigateToSubscription: LiveData<Boolean>
        get() = _navigateToSubscription

    fun onFabClicked() {
        if (notifyList()){
            _navigateToCategories.value = true
            print("Navigating to Categories")

        }else{
            _navigateToSubscription.value = true
            print("Navigating to subscription")
        }

>>>>>>> 16715773b7da518edfb3d99bc1cc586dfe7e17c7
    }

    fun onNavigatedToCategories() {
        _navigateToCategories.value = false
    }
<<<<<<< HEAD
=======
    private fun notifyList(): Boolean {
        var subscribed  = false;
        for (p in SubscriptionActivity.subcribeItemIDs) {
            if (getSubscribeItemValueFromPref(p)) {
                subscribed = getSubscribeItemValueFromPref(p)
                break
            }
        }
        return subscribed
    }
    private val application = application.applicationContext
    private val preferenceObject: SharedPreferences
        get() = application.getSharedPreferences(SubscriptionActivity.PREF_FILE, 0)

    private fun getSubscribeItemValueFromPref(PURCHASE_KEY: String): Boolean {
        return preferenceObject.getBoolean(PURCHASE_KEY, false)
    }
>>>>>>> 16715773b7da518edfb3d99bc1cc586dfe7e17c7
}