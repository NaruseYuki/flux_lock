package com.yushin.flux_lock.view

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.yushin.flux_lock.R
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.databinding.ActivityMainBinding
import com.yushin.flux_lock.exception.BaseException
import com.yushin.flux_lock.service.ConnectionMonitorServiceRx
import com.yushin.flux_lock.store.BLEStore
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.view.ble.ControlDeviceFragment
import com.yushin.flux_lock.view.ble.NoDevicesFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class BLEActivity : AppCompatActivity() {
    // Daggerでインジェクションされる
    @Inject
    lateinit var bleStore: BLEStore
    // Daggerでインジェクションされる
    @Inject
    lateinit var bleActionCreator: BLEActionCreator

    private lateinit var binding:ActivityMainBinding
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private var requestFlg = false
    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // サービスを開始
        val serviceIntent = Intent(this, ConnectionMonitorServiceRx::class.java)
        startService(serviceIntent)

    }


    override fun onResume() {
        super.onResume()
        if(disposables.isDisposed){
            disposables = CompositeDisposable()
        }
        if(!isNetworkConnected(this)|| !isBluetoothEnabled(this)){
            showAppEndDialog(getString(R.string.end_app_text))
        }
        subscribeNetworkBLUEStatus()

        // DB読み込みが完了したらフラグメントを表示するよう購読
        displayElements()
        if(!requestFlg){
            checkPermissions()
        }
        // 初期データをロードする
        bleActionCreator.loadRegisteredDevices()

    }
    override fun onPause() {
        super.onPause()
        // スキャン不可にする
        bleActionCreator.stopScanDevices()
        disposables.clear()
    }

    override fun onDestroy() {
        bleStore.getConnectedDevice().value?.let { bleActionCreator.disconnect(it) }
        // disposableを破棄
        // サービスを停止
        val serviceIntent = Intent(this, ConnectionMonitorServiceRx::class.java)
        stopService(serviceIntent)
        bleStore.onDestroy()
        disposables.dispose()
        super.onDestroy()
    }

    private fun hasBluetoothPermissions(): Boolean {
        return bluetoothPermissions.all {
            ActivityCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this, bluetoothPermissions, REQUEST_BLUETOOTH_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // ユーザーが権限を拒否した場合の処理
                // 一度表示されていたら繰り返し表示されないようにする
                if(!requestFlg){
                    showPermissionDeniedDialog(
                        getString(R.string.permission_title),
                        getString(R.string.permission_message),
                        getString(R.string.permission_positive_button))
                    requestFlg = true
                }else{
                    // 何も表示しない
                }

            }
        }
    }

    private fun showPermissionDeniedDialog(title:String,
                                           message:String,
                                           button:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(button) { dialogInterface: DialogInterface, _: Int ->
            // 設定画面を開くインテントを作成
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialogInterface.dismiss()
            requestFlg = false
        }
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            // アプリを終了する
            finish()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun checkPermissions() {
        if (hasBluetoothPermissions()) {
            // パーミッションが許可されている場合の処理
            // デバイスの購読処理を行う
            //subscribeDevices()
            // スキャン開始
            bleActionCreator.scanDevices()
        } else {
            // 権限がまだ許可されていない場合の処理
            requestBluetoothPermissions()
        }
    }

    private fun displayElements() {
        bleStore.getRegisteredDevices()
            .observeOn(AndroidSchedulers.mainThread())
            .delay(500, TimeUnit.MILLISECONDS)
            .subscribe{
                    if (it.isNotEmpty()) {
                        // ControlDeviceFragmentを表示する
                        // 全てのフラグメントをバックスタックから削除
                        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        navigateFragment(R.id.container_main_fragment,ControlDeviceFragment())
                    } else{
                        // NoDevicesFragmentを表示する
                        navigateFragment(R.id.container_main_fragment,NoDevicesFragment())
                    }
                }
                .addTo(disposables)
    }

     fun navigateFragment(fragmentId:Int,fragment: Fragment) {
         supportFragmentManager.beginTransaction().replace(
             fragmentId,
             fragment
         ).addToBackStack(fragment.getBackStackTag())
             .commit()
     }

    private fun Fragment.getBackStackTag(): String {
        return this.javaClass.simpleName
    }

    private fun showAppEndDialog(message:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(getString(R.string.caution_dialog_text))
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK"){ _: DialogInterface, _: Int ->
            finish()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun subscribeNetworkBLUEStatus(){
        bleStore.getError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when(it) {
                    is BaseException.NetworkBLEErrorException ->
                        showAppEndDialog(getString(R.string.end_app_text))
                    else -> {}
                }
            }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter =  bluetoothManager.adapter
        return bluetoothAdapter?.isEnabled == true
    }

    companion object{
        const val REQUEST_BLUETOOTH_PERMISSIONS = 100
    }
}
