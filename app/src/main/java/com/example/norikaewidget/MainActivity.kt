package com.example.norikaewidget

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.FileInputStream
import java.lang.Exception
import java.nio.BufferUnderflowException
import android.R.attr.fragment
import android.R.attr.fragmentEnterTransition
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextUtils.split
import android.view.ViewGroup

import android.view.LayoutInflater
import kotlinx.coroutines.Dispatchers.Main
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Paths
import android.widget.AutoCompleteTextView

import android.widget.ArrayAdapter





class MainActivity : AppCompatActivity(),MyListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blanklayout)
/*
        val registButton = findViewById<Button>(R.id.registButton)
        val registeredStationName = findViewById<TextView>(R.id.textView)
*/
        var stationName: String? = ""
/*
        registButton.setOnClickListener {  //登録ボタンの挙動
            getSharedPreferences("savedata", 0)
            val prefs: SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            stationName = registeredStationName.text.toString()
            val editor = prefs.edit()
            editor.putString("RegisteredStation", stationName)
            editor.apply()
        }
*/
        val fragment = MainFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            if (fragment != null && fragment is MainFragment) {
                //今はライフサイクルの場所的に呼べないっぽいので後で復活させる
            }
        }
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return super.getSharedPreferences(name, mode)
    }

    override fun onClickButton() {
    }

    class MainFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            // 先ほどのレイアウトをここでViewとして作成します
            return inflater.inflate(R.layout.activity_main, container, false)
        }

        private var mListener: MyListener? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val filename = "stationNameList.csv"
            val fileInputStream  = resources.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
            reader.readLine()
            var lineBuffer: String
            var stationNameList : ArrayList<String> = arrayListOf()
            var k = 0
            var temp :String? = ""
            while (temp != null) {
                lineBuffer = temp
                if (lineBuffer != null) {
                    if (temp != ""){
                        stationNameList.add(lineBuffer)
                    }
                    temp = reader.readLine()
                    k++
                } else {
                    break
                }
            }

            var autoCompleteAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, stationNameList
            )
            val stationtextList = view.findViewById(R.id.registeredStation) as AutoCompleteTextView
            stationtextList.setAdapter(autoCompleteAdapter)

            val UpDown:List<String> = listOf("上り","下り")
            var adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                UpDown
            )
            view.findViewById<Spinner>(R.id.UpDownSpinner).adapter = adapter

            view.findViewById<Button>(R.id.registButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    val rCont = requireContext()
                    val station = view.findViewById<EditText>(R.id.registeredStation).text
                    val route = view.findViewById<Spinner>(R.id.routespinner).selectedItem
                    if(station.toString() != ""){
                        if(route != null){
                            rCont.getSharedPreferences("savedata", 0)
                            val prefs: SharedPreferences = rCont.getSharedPreferences("savedata", MODE_PRIVATE)
                            val stationName = view.findViewById<EditText>(R.id.registeredStation).text.toString()
                            val routeName = view.findViewById<Spinner>(R.id.routespinner).selectedItem.toString()
                            val direction = view.findViewById<Spinner>(R.id.UpDownSpinner).selectedItem.toString()
                            val editor = prefs.edit()
                            editor.putString("RegisteredStation", stationName)
                            editor.putString("RouteSpinner", routeName)
                            editor.putString("UpDownSpinner", direction)
                            editor.apply()
                            val intent = Intent(requireContext(), TimeSchedule::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(rCont, "路線を入力してください", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(rCont, "駅名を入力してください", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            view.findViewById<Button>(R.id.stationButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    addItem()
                }
            })
        }

        // FragmentがActivityに追加されたら呼ばれるメソッド
        override fun onAttach(context: Context) {
            super.onAttach(context)
            // contextクラスがMyListenerを実装しているかをチェックする
            if (context is MyListener) {
                // リスナーをここでセットするようにします
                mListener = context
            }
        }

        // FragmentがActivityから離れたら呼ばれるメソッド
        override fun onDetach() {
            super.onDetach()
            // 画面からFragmentが離れたあとに処理が呼ばれることを避けるためにNullで初期化しておく
            mListener = null
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun addItem() {
            viewLifecycleOwner.lifecycleScope.launch {

                // ここからはIOスレッドで実行してもらう
                withContext(Dispatchers.IO) {
                    // テーブルに追加
                    val db = AppDatabase.getInstance(requireContext())
                    db.autocompletelistDao().delete()
                    db.autocompletelistDao().insert()

                    var station = view?.findViewById<EditText>(R.id.registeredStation) as EditText
                    db.StationRouteUpDownDaytypeDao().deleteStationRouteUpDownDaytype()

                    val fileInputStream  = resources.assets.open("StationRouteUpDownDaytype.csv")
                    val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
                    reader.readLine()
                    var lineBuffer: String
                    var stationList : ArrayList<String> = arrayListOf()
                    var k = 0
                    //readLineは呼び出しごとに次の行にいくみたいなので、この実装だと１行飛ばしで読み込んでしまう。Fragmentの感じでやると思うけど、コピペだとエラーあるので対処
                    while (reader.readLine() != null) {
                        lineBuffer = reader.readLine()
                        if (lineBuffer != null) {
                            stationList.add(lineBuffer) //これ１行で読み込まれる
                            k++
                        } else {
                            break
                        }
                    }

                    var j = 0
                    while (j < stationList.size) {
                        var temp = stationList[j].split(",")
                        db.StationRouteUpDownDaytypeDao().insertStationRouteUpDownDaytype(temp[0],temp[1],temp[2].toInt(),temp[3].toInt())
                        j++
                    }
                    val gotlist = db.StationRouteUpDownDaytypeDao().loadAllByStation(station.text.toString())
                    //以下、排他的になるように処理
                    var i = 0
                    var x = 0
                    var flag = 0
                    var routeList = mutableListOf<String>()
                    while (i < gotlist.size) {
                        while(x < routeList.size){
                            if(routeList[x] == gotlist[i].route.toString()){
                                flag = 1
                                break
                            }
                            x++
                        }
                        if(flag == 0){
                            routeList.add(gotlist[i].route.toString())
                        }
                        flag = 0
                        x=0
                        i++
                    }
                    val routeSpinner = view?.findViewById<Spinner>(R.id.routespinner) as Spinner

                    var adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        routeList
                    )
                    activity?.runOnUiThread(java.lang.Runnable {routeSpinner.adapter = adapter})
                }
            }
        }
    }
}

    interface MyListener {
        fun onClickButton()
    }

