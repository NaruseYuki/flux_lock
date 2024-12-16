package com.yushin.flux_lock.view.ble

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import co.candyhouse.sesame.open.CHResultState
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDevices
import com.bumptech.glide.Glide
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.FragmentControlDeviceBinding
import com.yushin.flux_lock.store.DummyDevice
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.utils.Utils.getFirZip
import com.yushin.flux_lock.view.BLEActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class ControlDeviceFragment : BaseFragment() {
    private lateinit var binding: FragmentControlDeviceBinding
    private var selectedIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentControlDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // subscribeTagVersion()
        addDevicesListener()
        editDeviceSetting()
        setAdapterListener()
    }

    override fun onResume() {
        super.onResume()
        subscribeDeviceStatus()
        createDeviceList()
        setLockImageListener()
    }

    private fun checkFirmwareVersion(device:CHDevices) {
        bleActionCreator.getVersionTag(device)
    }

    /**
     * デバイスの状態を監視し、画像を更新する
     */
    private fun subscribeDeviceStatus() {
       bleStore.getBleStatus()
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe { status ->
               Log.d("BLE","@@@"+ status.toString())
               when(status){
                   CHDeviceStatus.Locked -> {
                       binding.lockImage.setImageResource(R.drawable.ic_lock)
                       binding.connectButton.apply {
                           isEnabled = false
                           visibility = View.GONE
                       }
                   }

                   CHDeviceStatus.Unlocked -> {
                       binding.lockImage.setImageResource(R.drawable.ic_unlock)
                       binding.connectButton.apply {
                           isEnabled = false
                           visibility = View.GONE
                       }
                   }
                   CHDeviceStatus.NoBleSignal -> {
                       binding.lockImage.setImageResource(R.drawable.ic_no_signal)
                       binding.connectButton.apply {
                           isEnabled = true
                           visibility = View.VISIBLE
                       }}
                   else -> {
                       Glide.with(requireActivity())
                           .load(R.raw.cycle_move)
                           .into(binding.lockImage)
                       binding.connectButton.apply {
                           visibility = View.GONE
                       }
                   }
               }
           }
           .addTo(disposable)
    }

//    /**
//     * FWバージョン取得を購読する
//     */
//    private fun subscribeTagVersion() {
//        bleStore.getVersionTag()
//            .filter{
//                bleStore.getConnectedDevice().value !is DummyDevice
//            }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                    currentVersion ->
//                // 最新FWがあるなら案内表示する
//                displayUpdateGuideUI(currentVersion)
//            }
//            .addTo(disposable)
//    }

//    /**
//     *  最新バージョンかチェックし、
//     *  最新でないならアップデートの案内表示
//     */
//    private fun displayUpdateGuideUI(currentVersion: CHResultState<String>) {
//        val zipName = bleStore.getConnectedDevice().value?.getFirZip()
//            ?.let { it1 -> resources.getResourceEntryName(it1) }
//        val tailTag = currentVersion.data.split("-").last()
//        val isLatest = (zipName?.contains(tailTag)) ?: return
//        if (!isLatest) {
//            setFirmWareUpdateListener()
//        } else {
//            // 最新の場合はUIも消しておく
//            binding.noticeUpdate.setOnClickListener(null)
//            binding.noticeUpdate.visibility = View.GONE
//        }
//    }

//    /**
//     *  最新バージョン確認の処理を追加する
//     */
//    private fun setFirmWareUpdateListener() {
//        binding.noticeUpdate.visibility = View.VISIBLE
//        binding.noticeUpdate.setOnClickListener {
//            showUpdateConfirmDialog(
//                getString(R.string.delete_confirm_title),
//                getString(R.string.update_confirm_text),
//                getString(R.string.update_confirm_text_ok)
//            )
//        }
//    }

    /**
     *  アップデートダイアログの表示
     *  YESなら更新開始のUI
     *  NOならダイアログを閉じる
     */
//    private fun showUpdateConfirmDialog(title:String,message: String,button: String) {
//        val alertDialogBuilder = AlertDialog.Builder(requireContext())
//        alertDialogBuilder.setTitle(title)
//        alertDialogBuilder.setMessage(message)
//        alertDialogBuilder.setPositiveButton(button) { _: DialogInterface, _: Int ->
//            bleStore.getConnectedDevice().value?.let {
//                bleActionCreator.updateFirmware(it)
//            }
//        }
//        alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
//            dialogInterface.dismiss()
//        }
//        val alertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }

    private fun setLockImageListener() {
        bleStore.getConnectedDevice()
            .filter {
                it != null && it !is DummyDevice
            }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { device ->
                // 向き先変更
                binding.lockImage.setOnClickListener {
                    bleActionCreator.toggle(device)
                }
                // ファームウェアバージョン確認
                checkFirmwareVersion(device)
            }.addTo(disposable)
    }

    private fun setAdapterListener() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (selectedIndex == p2) return
                selectedIndex = p2
                connectDevice(selectedIndex)
                sharedPreferencesHelper.saveConnectIndex(selectedIndex)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // 何もしない
            }
        }
    }

    /**
     * リストのindexのデバイスに接続する
     */
    private fun connectDevice(index:Int) {
        // 接続済みならreturn
        if(isConnect(index)) {
            Log.d("BLE", "connect:接続済み")
            return
        }
        Log.d("BLE", "connect:接続開始")
        // 接続開始
         bleActionCreator.connectDevice(
         bleStore.getRegisteredDevices().value?.get(index) ?: return
         )
    }

    private fun createDeviceList() {
        bleStore.getRegisteredDevices().value?.let { devices ->
                binding.spinner.adapter =
                    ArrayAdapter(
                        requireContext(),
                        R.layout.item_device,
                        devices.map {
                            it.deviceId?.let { it1 ->
                                Log.d("BLE", "getDeviceName: $it")
                                sharedPreferencesHelper.getDeviceName(it1)
                            } ?: it.productModel.deviceModelName()
                        }
                    )
            //登録直後なら登録したデバイスを表示したい
            if(bleStore.registerFlgRelay.value == true) {
                if (devices != null) {
                    selectedIndex = devices.size -1
                }
                binding.spinner.setSelection(selectedIndex)
                bleStore.registerFlgRelay.accept(false)
            } else {
                binding.spinner.setSelection(sharedPreferencesHelper.getConnectIndex())
            }

        }
    }

    private fun addDevicesListener() {
        binding.addDevices.setOnClickListener {
            // UnregisterDevicesFragmentを表示する
            (activity as BLEActivity).navigateFragment(
                R.id.container_main_fragment,
                UnregisterDevicesFragment()
            )
        }

        // 手動再接続
        binding.connectButton.setOnClickListener {
            connectDevice(selectedIndex)
        }

        binding.deleteDevices.setOnClickListener {
            showDeleteDialog(
                getString(R.string.delete_confirm_title),
                getString(R.string.delete_confirm_text),
                getString(R.string.delete_confirm_ok)
            )
        }
    }

    private fun editDeviceSetting(){
        // EditDeviceSettingFragmentを表示する
        binding.settingButton.setOnClickListener {
            (activity as BLEActivity).navigateFragment(
                R.id.container_main_fragment,
                SettingDeviceFragment.create(
                    bleStore.getRegisteredDevices().value?.get(selectedIndex) ?:return@setOnClickListener
                )
            )
        }
    }

    /**
     * 削除確認ダイアログを出す
     *  OK:
     *      成功時、端末から情報削除する（未接続でも可能）
     *      セサミ初期化アクションを送る（接続していないとできない）
     *      端末情報をスマホから削除できたら、index -1(>0)のロックに接続しに行く
     *      端末未登録時は、勝手に画面がnoDevicesFragmentに移行するので気にしなくて良い
     *  NO:ダイアログを閉じる
     */
    private fun showDeleteDialog(title:String,
                                 message:String,
                                 button:String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(button) { _: DialogInterface, _: Int ->
            // selectedIndexを信頼する
            // connectedSubject.valueはnullのタイミングがあるので
            // 未接続時も削除できるようにbleStore.getRegisteredDevices().valueを毎回使う
            val devices = bleStore.getRegisteredDevices().value
            val device = devices?.get(selectedIndex)
            val deviceId = device?.deviceId
            device?.let { bleActionCreator.resetLock(it) }
            if (deviceId != null) {
                sharedPreferencesHelper.removeDevice(deviceId)
            }
            if(((devices?.size?.minus(1)) ?: 0) >= 0){
                selectedIndex = 0
                sharedPreferencesHelper.saveConnectIndex(selectedIndex)
            } else{
                return@setPositiveButton
            }

            createDeviceList()
        }
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}