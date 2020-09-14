package com.websarva.wings.android.field_surveyy

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context as Context1


class MainActivity : AppCompatActivity(),LocationListener,SensorEventListener{
    private var _imageUri:Uri?=null


    //方位角、傾斜角、回転角の変数宣言
    private var azimuth: Double?=null
    private var pitch: Double?=null
    private var roll: Double?=null

    //緯度、経度の変数宣言
    private var latitude: Double?=null
    private var longitude: Double?=null

    //データベースヘルパーを継承
    private val _helper = DatabaseHelper(this@MainActivity)


    private lateinit var locationManager:LocationManager

    //センサーの値
    private val matrixSize=16
    private var mgValues=FloatArray(3)
    private var acValues=FloatArray(3)

    //gridviewの配列
    public var lstBitmap= arrayListOf<Bitmap>()
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //データベース内の画像の名前をselect
        val db=_helper.writableDatabase
        val sql="select name from saveImgTable order by date"
        val cursor=db.rawQuery(sql,null)
        var note=""


        var i=0


        if(cursor.count>0){
            while (cursor.moveToNext()){
                val idxNote=cursor.getColumnIndex("name")
                //noteに画像名を入れる
                note=cursor.getString(idxNote)
                val file=File("/storage/emulated/0/Pictures/genchii/$note")
                val inputStream0 = FileInputStream(file)
                val bitmap = BitmapFactory.decodeStream(inputStream0)  //bitmapに変換

                val imgview=ImageView(this)
                val params = GridLayout.LayoutParams()
                params.width = 256
                params.height = 256
                params.rowSpec = GridLayout.spec(i/4)
                params.columnSpec = GridLayout.spec(i%4)
                imgview.setLayoutParams(params)

                // ImageViewへ画像を設定
                imgview.setImageBitmap(bitmap)

                // GridLayoutへImageViewを割り当て
                gridLayout_img.addView(imgview)

                i+=1

            }
        }


        //位置情報のパーミッション
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000)
        } else {
            locationStart()

            if (::locationManager.isInitialized) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    50f,
                    this)
            }

        }



    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            //データベース内の画像の名前をselect
            gridLayout_img.removeAllViews()
            val db=_helper.writableDatabase
            val sql="select name from saveImgTable order by date"
            val cursor=db.rawQuery(sql,null)
            var note=""


            var i=0


            if(cursor.count>0){
                while (cursor.moveToNext()){
                    val idxNote=cursor.getColumnIndex("name")
                    //noteに画像名を入れる
                    note=cursor.getString(idxNote)
                    val file=File("/storage/emulated/0/Pictures/genchii/$note")
                    val inputStream0 = FileInputStream(file)
                    val bitmap = BitmapFactory.decodeStream(inputStream0)  //bitmapに変換

                    val imgview=ImageView(this)
                    val params = GridLayout.LayoutParams()
                    params.width = 256
                    params.height = 256
                    params.rowSpec = GridLayout.spec(i/4)
                    params.columnSpec = GridLayout.spec(i%4)
                    imgview.setLayoutParams(params)

                    // ImageViewへ画像を設定
                    imgview.setImageBitmap(bitmap)

                    // GridLayoutへImageViewを割り当て
                    gridLayout_img.addView(imgview)

                    i+=1

                }
            }
        }
    }

    override fun onDestroy() {

        _helper.close()
        super.onDestroy()
    }

    //カメラ画像クリック時処理
    fun onCameraImageClick(view: View){

        //現在の日付　時刻を取得する
        val dateFormat=SimpleDateFormat("yyyyMMddHHmmss")
        val now= Date()
        val nowStr=dateFormat.format(now)
        val outputFileName="genchii_Photo_${nowStr}.jpg"

        saveImage_infor(outputFileName)

        val values=ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,outputFileName)
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
        values.put(MediaStore.Audio.Media.RELATIVE_PATH,"Pictures/genchii")
        //Uriオブジェクトを作成する
        _imageUri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //imageURIに画像データを格納する
        intent.putExtra(MediaStore.EXTRA_OUTPUT,_imageUri)


        //アプリに戻る
        startActivityForResult(intent,200)


    }



    //位置情報メソッド
    private fun locationStart() {
        Log.d("debug", "locationStart()")

        // Instances of LocationManager class must be obtained using Context.getSystemService(Class)
        locationManager = getSystemService(Context1.LOCATION_SERVICE) as LocationManager

        val locationManager = getSystemService(Context1.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("debug", "location manager Enabled")
        } else {
            // to prompt setting up GPS
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
            Log.d("debug", "not gpsEnable, startActivity")
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)

            Log.d("debug", "checkSelfPermission false")
            return
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            50f,
            this)

    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true")

                locationStart()

            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(this,
                    "これ以上なにもできません", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        /* API 29以降非推奨
        when (status) {
            LocationProvider.AVAILABLE ->
                Log.d("debug", "LocationProvider.AVAILABLE")
            LocationProvider.OUT_OF_SERVICE ->
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE")
            LocationProvider.TEMPORARILY_UNAVAILABLE ->
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE")
        }
         */
    }

    override fun onLocationChanged(location: Location) {
        // 緯度

        latitude= location.getLatitude()


        // 経度
        longitude=  location.getLongitude()

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    //センサーの数値が更新されたとき
    override fun onSensorChanged(event: SensorEvent?) {
        val inR = FloatArray(matrixSize)
        val outR = FloatArray(matrixSize)
        val I =FloatArray(matrixSize)
        val orValues = FloatArray(3)

        if (event==null) return
        when (event.sensor.type){
            Sensor.TYPE_ACCELEROMETER->acValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD->mgValues = event.values.clone()
        }

        SensorManager.getRotationMatrix(inR,I,acValues,mgValues)

        SensorManager.remapCoordinateSystem(inR,SensorManager.AXIS_X,SensorManager.AXIS_Z,outR)

        SensorManager.getOrientation(outR,orValues)


        //方位角、傾斜角、回転角
        azimuth=rad2Deg(orValues[0])
        pitch=rad2Deg(orValues[1])
        roll=rad2Deg(orValues[2])



    }

    public override fun onResume(){
        super.onResume()
        val sensorManager = getSystemService(Context1.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magField=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,magField,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause(){
        super.onPause()
        val sensorManager=getSystemService(Context1.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    private fun rad2Deg(rad: Float): Double {
        return Math.floor(Math.toDegrees(rad.toDouble()))
    }

    //画像情報を保存する
    private fun saveImage_infor(imageNames: String){
        val imageName=imageNames
        val date = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val db=_helper.writableDatabase
        val sqlInsert="INSERT INTO saveImgTable(name, date, longitude, latitude, azimuth, pitch, roll) VALUES (?,?,?,?,?,?,?)"
        var stmt= db.compileStatement(sqlInsert)


        stmt.bindString(1,imageName)
        stmt.bindString(2,date.toString())
        longitude?.let { stmt.bindDouble(3, it) }
        latitude?.let { stmt.bindDouble(4, it) }
        azimuth?.let { stmt.bindDouble(5, it) }
        pitch?.let { stmt.bindDouble(6, it) }
        roll?.let { stmt.bindDouble(7, it) }


        stmt.executeInsert()

    }





}







