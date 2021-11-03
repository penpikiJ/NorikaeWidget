package com.example.norikaewidget

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.provider.ContactsContract

import android.widget.ImageView

import android.widget.LinearLayout

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager

import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout


class TimeListFragment : Fragment() {
    lateinit var mAdapter: CustomAdapter
    lateinit var mTrainList: ArrayList<DataForSchedule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// データの作成
        val horse = DataForSchedule("ウマ", 4)
        val lion = DataForSchedule("ライオン", 6)
        mTrainList = arrayListOf(horse, lion)

        // RecyclerViewの取得
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)

        // LayoutManagerの設定
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mTrainList)
        recyclerView?.adapter = mAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_time_schedule, container, false)
    }
}

data class DataForSchedule(
    val route :String,
    val updown :Int
)
class CustomAdapter(private val trainList: ArrayList<DataForSchedule>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
// Viewの初期化
class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val route: TextView
    val updown: TextView

    init {
        route = v.findViewById(R.id.textView5)
        updown = v.findViewById(R.id.textView7)
    }
}
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.timelistcard, viewGroup, false)
        return ViewHolder(view)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val trainTime = trainList[position]

        viewHolder.route.text = trainTime.route
        viewHolder.updown.text = trainTime.updown.toString()
    }

    // 表示数を返す
    override fun getItemCount() = trainList.size
}