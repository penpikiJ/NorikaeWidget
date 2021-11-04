package com.example.norikaewidget

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.content.SharedPreferences
import android.provider.ContactsContract

import androidx.recyclerview.widget.LinearLayoutManager

import android.util.AttributeSet
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout


class TimeListFragment : Fragment() {
    lateinit var mAdapter: CustomAdapter
    lateinit var mTrainList: ArrayList<DataForSchedule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_time_schedule, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val rCont = requireContext()
        rCont.getSharedPreferences("savedata", 0)
        val prefs: SharedPreferences = rCont.getSharedPreferences("savedata",
            AppCompatActivity.MODE_PRIVATE
        )
        // データの作成
        val horse = DataForSchedule("ウマ", "下り")
        val lion = DataForSchedule("ライオン", "下り")
        mTrainList = arrayListOf(horse, lion)

        // RecyclerViewの取得
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView) as RecyclerView

        // LayoutManagerの設定
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // CustomAdapterの生成と設定
        mAdapter = CustomAdapter(mTrainList)
        recyclerView?.adapter = mAdapter
//ここの実装recyclerview的にこれでいいのかはわからない
        /*
        val routeName = prefs.getString("RouteSpinner",null)
        val direction = prefs.getString("UpDownSpinner",null)
        v.findViewById<TextView>(R.id.textView5).text = routeName
        v.findViewById<TextView>(R.id.textView7).text = "ニニに"
*/
    }
}

data class DataForSchedule(
    val route :String,
    val updown :String
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
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.timelistcard, viewGroup, false)
        return ViewHolder(v)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val trainTime = trainList[position]

        viewHolder.route.text = trainTime.route
        //viewHolder.updown.text = trainTime.updown 確認用にコメントアウト。動作したら戻す。
        viewHolder.updown.text = ""
    }

    // 表示数を返す
    override fun getItemCount() = trainList.size
}