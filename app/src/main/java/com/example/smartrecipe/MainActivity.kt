/*
 * Copyright (c) 2012 Google Inc.
 *@author #offensive TDBSoft
 */
 
package com.example.smartrecipe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartrecipe.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

//app billing imports
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType
import java.io.IOException
import java.util.*

public open class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {
    
    //app billing variables
       var arrayAdapter: ArrayAdapter<String>? = null
    var listView: ListView? = null
    private var billingClient: BillingClient? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //app billing listview 
        listView = findViewById<View>(R.id.listview) as ListView

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /////////////////////////app billing functions//////////////////////////////////

  // Establish connection to billing client
        //check purchase status from google play store cache on every app start
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build()

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryPurchase = billingClient!!.queryPurchases(SkuType.SUBS)
                    val queryPurchases = queryPurchase.purchasesList
                    if (queryPurchases != null && queryPurchases.size > 0) {
                        handlePurchases(queryPurchases)
                    }

                    //check which items are in purchase list and which are not in purchase list
                    //if items that are found add them to purchaseFound
                    //check status of found items and save values to preference
                    //item which are not found simply save false values to their preference
                    //indexOf return index of item in purchase list from 0-2 (because we have 3 items) else returns -1 if not found
                    val purchaseFound = ArrayList<Int>()
                    if (queryPurchases != null && queryPurchases.size > 0) {
                        //check item in purchase list
                        for (p in queryPurchases) {
                            val index = subcribeItemIDs.indexOf(p.sku)
                            //if purchase found
                            if (index > -1) {
                                purchaseFound.add(index)
                                if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    saveSubscribeItemValueToPref(subcribeItemIDs[index], true)
                                }
                                else {
                                    saveSubscribeItemValueToPref(subcribeItemIDs[index], false)
                                }
                            }
                        }
                        //items that are not found in purchase list mark false
                        //indexOf returns -1 when item is not in foundlist
                        for (i in subcribeItemIDs.indices) {
                            if (purchaseFound.indexOf(i) == -1) {
                                saveSubscribeItemValueToPref(subcribeItemIDs[i], false)
                            }
                        }
                    }
                    //if purchase list is empty that means no item is not purchased/Subscribed
                    //Or purchase is refunded or canceled
                    //so mark them all false
                    else {
                        for (purchaseItem in subcribeItemIDs) {
                            saveSubscribeItemValueToPref(purchaseItem, false)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {}
        })

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, subscribeItemDisplay)
        listView!!.adapter = arrayAdapter
        notifyList()

        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            if (getSubscribeItemValueFromPref(subcribeItemIDs[position])) {
                Toast.makeText(applicationContext, subcribeItemIDs[position] + " is Already Subscribed", Toast.LENGTH_SHORT).show()
                //selected item is already purchased/subscribed
                return@OnItemClickListener
            }
            //initiate purchase on selected product/subscribe item click
            //check if service is already connected
            if (billingClient!!.isReady) {
                initiatePurchase(subcribeItemIDs[position])
            }
            else {
                billingClient = BillingClient.newBuilder(this@MainActivity).enablePendingPurchases().setListener(this@MainActivity).build()
                billingClient!!.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            initiatePurchase(subcribeItemIDs[position])
                        } else {
                            Toast.makeText(applicationContext, "Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onBillingServiceDisconnected() {}
                })
            }
        }
    }

    private fun notifyList() {
        subscribeItemDisplay.clear()
        for (p in subcribeItemIDs) {
            subscribeItemDisplay.add("Subscribe Status of " + p + " = " + getSubscribeItemValueFromPref(p))
        }
        arrayAdapter!!.notifyDataSetChanged()
    }

    private val preferenceObject: SharedPreferences
        private get() = applicationContext.getSharedPreferences(PREF_FILE, 0)
    private val preferenceEditObject: Editor
        private get() {
            val pref = applicationContext.getSharedPreferences(PREF_FILE, 0)
            return pref.edit()
        }

    private fun getSubscribeItemValueFromPref(PURCHASE_KEY: String): Boolean {
        return preferenceObject.getBoolean(PURCHASE_KEY, false)
    }

    private fun saveSubscribeItemValueToPref(PURCHASE_KEY: String, value: Boolean) {
        preferenceEditObject.putBoolean(PURCHASE_KEY, value).commit()
    }

    private fun initiatePurchase(PRODUCT_ID: String) {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(SkuType.SUBS)
        val billingResult = billingClient!!.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            billingClient!!.querySkuDetailsAsync(params.build()
            ) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (skuDetailsList != null && skuDetailsList.size > 0) {
                        val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetailsList[0])
                                .build()
                        billingClient!!.launchBillingFlow(this@MainActivity, flowParams)
                    }
                    else {
                        ///////////try to add item/product id "weekly" "monthly" "yearly" inside subscription in google play console////////////////////////
                        Toast.makeText(applicationContext, "Subscribe Item $PRODUCT_ID not Found", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(applicationContext,
                            " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            Toast.makeText(applicationContext,
                    "Sorry Subscription not Supported. Please Update Play Store", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        //if item newly purchased
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(SkuType.SUBS)
            val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let { handlePurchases(it) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            val index = subcribeItemIDs.indexOf(purchase.sku)
            //purchase found
            if (index > -1) {

                //if item is purchased
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                        // Invalid purchase
                        // show error to user
                        Toast.makeText(applicationContext, "Error : Invalid Purchase", Toast.LENGTH_SHORT).show()
                        continue  //skip current iteration only because other items in purchase list must be checked if present
                    }
                    // else purchase is valid
                    //if item is purchased/subscribed and not Acknowledged
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                        billingClient!!.acknowledgePurchase(acknowledgePurchaseParams
                        ) { billingResult ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                //if purchase is acknowledged
                                //then saved value in preference
                                saveSubscribeItemValueToPref(subcribeItemIDs[index], true)
                                Toast.makeText(applicationContext, subcribeItemIDs[index] + " Item Subscribed", Toast.LENGTH_SHORT).show()
                                notifyList()
                            }
                        }
                    }
                    else {
                        // Grant entitlement to the user on item purchase
                        if (!getSubscribeItemValueFromPref(subcribeItemIDs[index])) {
                            saveSubscribeItemValueToPref(subcribeItemIDs[index], true)
                            Toast.makeText(applicationContext, subcribeItemIDs[index] + " Item Subscribed.", Toast.LENGTH_SHORT).show()
                            notifyList()
                        }
                    }
                } 
                else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    Toast.makeText(applicationContext, subcribeItemIDs[index] + " Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show()
                }
                else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                    //mark purchase false in case of UNSPECIFIED_STATE
                    saveSubscribeItemValueToPref(subcribeItemIDs[index], false)
                    Toast.makeText(applicationContext, subcribeItemIDs[index] + " Purchase Status Unknown", Toast.LENGTH_SHORT).show()
                    notifyList()
                }
            }
        }
    }

    
///////////////////////client side verification function///////////////////////

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     *
     * Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     *
     */
    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            //for old playconsole
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            //for new play console
            //To get key go to Developer Console > Select your app > Monetize > Monetization setup
            val base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnoQ/+O/X3mVyIhgSDm3iKW31xi6nPnatIDTCiO6KMpHVfGGzIN+UmTgwJDn67Wjhe9w2VVAvdJDx0G+tL96gCs+8CBK/npiSWIpsHX8rWcFE6M37MFV7hgycLzIrP9ixCLDruTJPsD7KUhP+l5up8Dj+0vA40itGaPZfHGk3ImMhykcRdRdKOzrh6n5Hf6ng+1Urv8bZnAnZFX6ipG6kEv/D1R1HO8dhbFuWjqCxWyXPHXks/BlBiFaKPrqWRuAubSd1Q/cYszhk8JaAFAYgS7icqABmRXSvo8CfZIyimcPXIhtlF4ACVS9Ngj3FVn1iThmhmsRi6HesFIdNZbGd8QIDAQAB"
			Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

    companion object {
        const val PREF_FILE = "MyPref"

        //note add unique product ids
        //use same id for preference key
        private val subcribeItemIDs: ArrayList<String> = object : ArrayList<String>() {
            init {
                add("w1")
                add("m2")
                add("y3")
            }
        }
        private val subscribeItemDisplay = ArrayList<String>()
    }


    ////////////////////////////end of app billing functions//////////////////////////////////////////

        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_categories,  R.id.nav_subscription,
                R.id.nav_how_it_works, R.id.nav_about,listView
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


}