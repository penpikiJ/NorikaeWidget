package com.aaa.norikaewidget

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
import com.example.norikaewidget.R


class TimeSchedule : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blanklayout)

        //Fragmentの適用
        val timeListFragment = TimeListFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            //複数ならadd
            transaction.replace(R.id.container, timeListFragment)
            transaction.commit()
        }
    }
}


