package com.aaa.norikaewidget

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.BufferedReader
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import androidx.core.text.set
import androidx.core.view.isNotEmpty

import android.widget.Spinner
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.aaa.norikaewidget.R
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), MyListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blanklayout)

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
        //時刻表リストデータを定義しておく
        val filename = "schedule_files_merged.csv"
        var stationList = arrayListOf<String>()
        var givenEachStationSet = eachStationSet(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())


        @RequiresApi(Build.VERSION_CODES.O)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            //Fragment作成時に時刻表リストデータを先に読み込みしてする
            stationList = inputAssetsFile(filename)
            givenEachStationSet = getEachStationSet(stationList)

            //以前のデータを読み込み
            val registeredStationName = view.findViewById<EditText>(R.id.registeredStation)
            val rCont = requireContext()
            val prefs: SharedPreferences = rCont.getSharedPreferences("savedata", MODE_PRIVATE)
            registeredStationName.setText(prefs.getString("RegisteredStation",null))
            if(registeredStationName != null){
                if(prefs.getInt("FromPage",0) == 1 ){
                    val editor = prefs.edit()
                    editor.putInt("FromPage", 0)
                    editor.apply()
                    val intent = Intent(rCont, TimeSchedule::class.java)
                    startActivity(intent)
                }
                //駅名が空でなければ駅名から路線を検索して、得られたリストから選択肢を前回のものに設定
                addRoute()
                //addRouteが終了するまで2秒までなら待つ様に設定
                var routeName = view.findViewById<Spinner>(R.id.routespinner)
                var c = 0
                while(routeName.adapter == null){
                    Thread.sleep(100)  // wait for 0.5 second
                    c++
                    if(c > 5){
                        break
                    }
                }
                if(routeName.adapter != null){
                    val preselectedRoute = prefs.getString("RouteSpinner",null)
                    val routeNameList = retrieveAllItems(routeName)
                    val preselectedIndexOfRoute = getIndexofSelectedinSpinner(routeNameList!!,
                        preselectedRoute.toString()
                    )
                    routeName.setSelection(preselectedIndexOfRoute)
                }

                //駅名から方向を検索して、得られたリストから選択肢を前回のものに設定（路線は前回のデータがなければ１番上のデータが採用される様に設計）
                addDirection()
                //addDirectionが終了するまで2秒までなら待つ様に設定
                val direction = view.findViewById<Spinner>(R.id.UpDownSpinner)
                c = 0
                while(direction.adapter == null){
                    Thread.sleep(100)  // wait for 0.5 second
                    c++
                    if(c > 5){
                        break
                    }
                }
                if(direction.adapter != null){
                    val preselectedDirection = prefs.getString("UpDownSpinner",null)
                    val directionList = retrieveAllItems(direction)
                    val preselectedIndexOfDirection = getIndexofSelectedinSpinner(directionList!!,
                        preselectedDirection.toString()
                    )
                    direction.setSelection(preselectedIndexOfDirection)
                }
            }

            //検索でのkeyboardがEnterで閉じるように変更
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            registeredStationName.setOnKeyListener OnKeyListener@{ v, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    inputMethodManager.hideSoftInputFromWindow(registeredStationName.windowToken,
                        InputMethodManager.RESULT_UNCHANGED_SHOWN)
                    return@OnKeyListener true
                }
                false
            }
            //次ページへの遷移ボタンの処理
            view.findViewById<Button>(R.id.registButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    val rCont = requireContext()
                    val station = view.findViewById<EditText>(R.id.registeredStation).text
                    val route = view.findViewById<Spinner>(R.id.routespinner).selectedItem
                    val direction = view.findViewById<Spinner>(R.id.UpDownSpinner).selectedItem
                    if(station.toString() != ""){
                        if(route != null){
                            if(direction != null){
                                rCont.getSharedPreferences("savedata", 0)
                                val stationName = view.findViewById<EditText>(R.id.registeredStation).text.toString()
                                val routeName = view.findViewById<Spinner>(R.id.routespinner).selectedItem.toString()
                                val direction = view.findViewById<Spinner>(R.id.UpDownSpinner).selectedItem.toString()
                                val editor = prefs.edit()
                                editor.putString("RegisteredStation", stationName)
                                editor.putString("RouteSpinner", routeName)
                                editor.putString("UpDownSpinner", direction)
                                editor.putInt("FromPage", 1)
                                editor.apply()
                                val intent = Intent(requireContext(), TimeSchedule::class.java)
                                startActivity(intent)
                            }else{
                                Toast.makeText(rCont, "方向を入力してください", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(rCont, "路線を入力してください", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(rCont, "駅名を入力してください", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            //駅を登録してスピナーの準備をするボタンの処理
            view.findViewById<Button>(R.id.stationButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    addRoute()
                }
            })
            view.findViewById<Button>(R.id.directionButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    addDirection()
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

        fun addRoute() {// テーブル初期化してファイルの内容を追加


            var station = view?.findViewById<EditText>(R.id.registeredStation) as EditText
            var route = view?.findViewById<Spinner>(R.id.routespinner) as Spinner
            val numbersOfStation = getNumbersOfStation(station.text.toString().replace("駅",""))
            val gotlist = getRouteSet(numbersOfStation)
            //路線スピナーへの代入
            val routeSpinner = view?.findViewById<Spinner>(R.id.routespinner) as Spinner

            var adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                gotlist
            )
            activity?.runOnUiThread(java.lang.Runnable {routeSpinner.adapter = adapter})
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun addDirection() {

            var station = view?.findViewById<EditText>(R.id.registeredStation) as EditText
            var route = view?.findViewById<Spinner>(R.id.routespinner) as Spinner
            val numbersOfStation = getNumbersOfStation(station.text.toString().replace("駅",""))

            val rCont = requireContext()
            val prefs: SharedPreferences = rCont.getSharedPreferences("savedata", MODE_PRIVATE)
            var routeText = prefs.getString("UpDownSpinner","")
            if(route.isNotEmpty()){
                routeText = route.selectedItem.toString()
            }

            val gotlist = getDirectionSet(numbersOfStation, routeText.toString())

            //方向スピナーへの代入
            val destinationSpinner = view?.findViewById<Spinner>(R.id.UpDownSpinner) as Spinner
            var destinationadapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                gotlist
            )
            activity?.runOnUiThread(java.lang.Runnable {destinationSpinner.adapter = destinationadapter})
        }

        //スピナーの中のアイテムを全てリストで取り出す関数
        fun retrieveAllItems(theSpinner: Spinner): List<String>? {
            val adapter: Adapter = theSpinner.adapter
            val n = adapter.count
            val items: MutableList<String> = ArrayList<String>(n)
            for (i in 0 until n) {
                val item: String = adapter.getItem(i) as String
                items.add(item)
            }
            return items
        }
        //リストの番号と一致する場合、そのindexを返す関数
        private fun getIndexofSelectedinSpinner(list:List<String>,item:String): Int{
            var x = 0
            var index = 0
            while(x < list.size){
                if(item.equals(list[x])){
                    index = x
                    break
                }
                x++
            }
            return index
        }

        private fun inputAssetsFile (filename:String) : ArrayList<String> {
            val fileInputStream  = resources.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
            reader.readLine()
            var lineBuffer: String = ""
            var stationList : ArrayList<String> = arrayListOf()
            var k = 0
            //readLineは呼び出しごとに次の行にいくので、一回別の変数に格納して回避
            while (lineBuffer != null) {
                val tempLine = reader.readLine()
                if(tempLine != null){
                    lineBuffer = tempLine
                }else{
                    lineBuffer = "end"
                }
                if (lineBuffer != "end") {
                    stationList.add(lineBuffer) //これ１行で読み込まれる
                    k++
                } else {
                    break
                }
            }
            return stationList
        }

        private fun getEachStationSet(stationList: ArrayList<String>): eachStationSet{
            //読み込んだ行をそれぞれの変数ごとに格納するために、格納先を作成。
            val stationSet: ArrayList<String> = arrayListOf()  //駅名
            val routeSet: ArrayList<String> = arrayListOf()  //路線名
            val directionSet: ArrayList<String> = arrayListOf()  //方向
            val daytypeSet: ArrayList<String> = arrayListOf()  //休日・平日
            val csvSet: ArrayList<String> = arrayListOf()  //csvファイル名
            val last_updateSet: ArrayList<String> = arrayListOf()  //最終更新日
            val error_codeSet: ArrayList<String> = arrayListOf()  //エラーコード

            val rowNumber= stationList.size
            var i = 0
            while(i < rowNumber){
                //csv取得した行を「,」で変数ごとに分ける
                var temp = stationList[i].split(",")
                //変数ごとに格納
                stationSet.add(temp[0])
                routeSet.add(temp[1])
                directionSet.add(temp[2])
                daytypeSet.add(temp[3])
                csvSet.add(temp[4])
                last_updateSet.add(temp[5])
                error_codeSet.add(temp[6])
                i++
            }
            val eachStationSet = eachStationSet(stationSet,routeSet,directionSet,daytypeSet,csvSet,last_updateSet,error_codeSet)

            //それぞれの変数ごとに分離したArrayListを返す
            return eachStationSet
        }
        private fun getNumbersOfStation(stationName:String):ArrayList<Int>{
            val numbersOfStation:ArrayList<Int> = arrayListOf()
            var j = 0
            val stationList = givenEachStationSet.stationSet
            //駅名が検索したいものと一致すればその列番号を返す。
            while (j < stationList.size){
                if(stationList[j].equals(stationName)){
                    numbersOfStation.add(j)
                }
                j++
            }
            return numbersOfStation
        }
        private fun getRouteSet(numbersOfStation:ArrayList<Int>):ArrayList<String>{
            val givenRouteSet = arrayListOf<String>()
            var k = 0
            //路線のリストを作成する。
            while (k < numbersOfStation.size){
                //重複確認
                var uniqueFlag = 0
                var i = 0
                while(i<givenRouteSet.size){
                    if(givenRouteSet[i] == givenEachStationSet.routeSet[numbersOfStation[k]]){
                        uniqueFlag = 1
                    }
                    i++
                }
                //重複なしであれば追加
                if(uniqueFlag == 0){
                    givenRouteSet.add(givenEachStationSet.routeSet[numbersOfStation[k]])
                }else{
                    uniqueFlag = 0
                }
                k++
            }
            //検索駅に対する路線リストを変換
            return givenRouteSet
        }
        private fun getDirectionSet(numbersOfStation:ArrayList<Int>,routeName:String):ArrayList<String>{
            val givenDirectionSet = arrayListOf<String>()
            var k = 0
            //駅名で選択したデータから路線の合うデータ列を抜き出して、方向のリストを作成する
            while (k < numbersOfStation.size){
                //駅名・路線が定まったため重複はない前提
                if(routeName == givenEachStationSet.routeSet[numbersOfStation[k]]){
                    givenDirectionSet.add(givenEachStationSet.directionSet[numbersOfStation[k]])
                }
                k++
            }
            //検索駅に対する方向リストを返す
            return givenDirectionSet
        }
        private fun getCsvName(numbersOfStation:ArrayList<Int>,routeName:String,directionName:String):String{
            var csvName: String = ""
            var k = 0
            //駅名・路線・方向で選択したデータからcsvのデータ列を抜き出す。
            while (k < numbersOfStation.size){
                //駅名・路線が定まったため重複はない前提
                if(routeName == givenEachStationSet.routeSet[numbersOfStation[k]] && directionName == givenEachStationSet.directionSet[numbersOfStation[k]]){
                    csvName = givenEachStationSet.csvSet[numbersOfStation[k]]
                }
                k++
            }
            //csvの名前を返す
            return csvName
        }
    }
}

    interface MyListener {
        fun onClickButton()
    }

//まとめて返り値を返す用のDataClass
data class eachStationSet(
    val stationSet: ArrayList<String>,
    val routeSet: ArrayList<String>,
    val directionSet: ArrayList<String>,
    val daytypeSet: ArrayList<String>,
    val csvSet: ArrayList<String>,
    val last_updateSet: ArrayList<String>,
    val error_codeSet: ArrayList<String>
)
