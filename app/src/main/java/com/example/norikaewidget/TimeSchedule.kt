package com.example.norikaewidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import java.util.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.AttributeSet
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext


class TimeSchedule : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.backgroundofschedule)

        val buttonToMain = findViewById<Button>(R.id.buttonToMain)
        buttonToMain.setOnClickListener(listener)


        val fragment = TimeListFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
        }
    }
    val listener = object:View.OnClickListener {
        override fun onClick(v: View?) {
            val prefs: SharedPreferences = applicationContext.getSharedPreferences("savedata", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putInt("FromPage", 0)
            editor.apply()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }
}


