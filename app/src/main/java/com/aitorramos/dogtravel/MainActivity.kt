package com.aitorramos.dogtravel

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.aitorramos.dogtravel.Activities.LoginActivity
import com.aitorramos.dogtravel.Fragments.ChatFragment
import com.aitorramos.dogtravel.Fragments.SearchFragment
import com.aitorramos.dogtravel.Fragments.MyTravelsFragment
import com.aitorramos.dogtravel.Utils.CircleTransform
import com.aitorramos.dogtravel.Utils.goToActivity
import com.aitorramos.mylibrary.ToolbarActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ToolbarActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var currentuser: FirebaseUser
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("Mis Viajes")
        setContentView(R.layout.activity_main)
        toolbarToLoad(toolbar as Toolbar)

        MobileAds.initialize(this, "ca-app-pub-1792861677014604~6627921361")

        setNavDrawer()
        setUpCurrentUser()
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + currentuser.uid)

        currentuser.photoUrl?.let {
            val imageProfile = navView.getHeaderView(0).findViewById<ImageView>(R.id.ivProfile)
            val name = navView.getHeaderView(0).findViewById<TextView>(R.id.tvName)

            name.text = currentuser.displayName
            Picasso.get().load(currentuser.photoUrl).resize(70, 70).centerCrop().transform(CircleTransform()).into(imageProfile)
        }

        fragmentTransaction(MyTravelsFragment())
        navView.menu.getItem(0).isChecked = true
    }

    private fun setUpCurrentUser(){
        currentuser = mAuth.currentUser!!
        //Log.e("URL", currentuser.photoUrl.toString())
    }

    private fun setNavDrawer(){
        val toggle = ActionBarDrawerToggle(this, drawerLayout, _toolbar, R.string.open_drawer, R.string.close_drawer)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    private fun fragmentTransaction(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.travels -> {
                setTitle("Mis viajes")
                fragmentTransaction(MyTravelsFragment())
            }
            R.id.search -> {
                setTitle("Buscar")
                fragmentTransaction(SearchFragment())
            }
            R.id.chat -> {
                setTitle("Chats")
                fragmentTransaction(ChatFragment())
            }
            R.id.logOut ->{
                FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/" + currentuser.uid)
                mAuth.signOut()
                goToActivity<LoginActivity>{
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}