package com.example.norikaewidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import java.util.*
import android.content.Context
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout


class TimeSchedule : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blanklayout)

        val fragment = TimeListFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
        }
    }
}

 class CardRecyclerAdapter(context: Context, private val list: Array<String>) :
     RecyclerView.Adapter<CardRecyclerAdapter.ViewHolder>() {
     private val context: Context
     override fun getItemCount(): Int {
         return list.size
     }

     override fun onBindViewHolder(vh: ViewHolder, position: Int) {
         // vh.textView_main.text = list[position]
         vh.layout.setOnClickListener {
             Toast.makeText(context, list[position], Toast.LENGTH_SHORT).show()
         }
     }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val layoutInflater = LayoutInflater.from(context)
            val v: View = layoutInflater.inflate(R.layout.timelistcard, parent, false)
            return ViewHolder(v)
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            //var textView_main: TextView
            var layout: ConstraintLayout
            init {
                //textView_main = v.findViewById<View>(R.id.textView_main) as TextView
                layout = v.findViewById<View>(R.id.cardlayout) as ConstraintLayout
            }
        }

        init {
            this.context = context
        }
    }

    class CardRecyclerView(context: Context, attrs: AttributeSet?) :
        RecyclerView(context, attrs) {
        fun setRecyclerAdapter(context: Context) {
            layoutManager = LinearLayoutManager(context)
           // adapter = CardRecyclerAdapter(context, context.getResources().getStringArray(array.dummy))
        }

        init {
            setRecyclerAdapter(context)
        }
    }

