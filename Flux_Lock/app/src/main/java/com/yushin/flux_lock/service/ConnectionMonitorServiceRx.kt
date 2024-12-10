package com.yushin.flux_lock.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import android.util.Log
import com.jakewharton.rxrelay3.PublishRelay
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.utils.Utils.addTo
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionMonitorServiceRx : Service() {

    private val networkStateSubject = BehaviorSubject.createDefault<Boolean>(true)
    private val bluetoothStateSubject = BehaviorSubject.createDefault<Boolean>(true)
    private val disposables = CompositeDisposable()
    @Inject
    lateinit var bleActionCreator: BLEActionCreator
    private lateinit var bluetoothReceiver:BroadcastReceiver


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // サービスが開始されたときの処理
        startMonitoring()
        return START_STICKY // サービスが終了しても再起動する場合
    }

    override fun onDestroy() {
        // Unregister Bluetooth receiver
        unregisterReceiver(bluetoothReceiver)
        disposables.clear()
        super.onDestroy()
    }

    private fun startMonitoring() {
        monitorNetworkState()
        monitorBluetoothState()

        // Combine the state of network and Bluetooth
        Observable.combineLatest(
            networkStateSubject.distinctUntilChanged(),
            bluetoothStateSubject.distinctUntilChanged()
        ) { isNetworkConnected, isBluetoothConnected ->
            isNetworkConnected to isBluetoothConnected
        }
            .distinctUntilChanged()
            .filter { (isNetworkConnected, isBluetoothConnected) ->
                !isNetworkConnected || !isBluetoothConnected
            }
            .subscribe { (_, _) ->
                bleActionCreator.handleNetworkAndBluetoothState()
                Log.d("BLE", "Network or Bluetooth is unavailable.")
            }
            .addTo(disposables)
    }

    private fun monitorNetworkState() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkStateSubject.onNext(true)
            }

            override fun onLost(network: Network) {
                networkStateSubject.onNext(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun monitorBluetoothState() {

         bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    val isBluetoothEnabled = state == BluetoothAdapter.STATE_ON
                    bluetoothStateSubject.onNext(isBluetoothEnabled)
                }
            }
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filter)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
