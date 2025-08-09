package com.ygzy.finance_elec.scale

import android.app.Activity
import android.content.Context
import com.blankj.utilcode.util.ThreadUtils
import com.qhscale.QHJNIScale
import com.qhscale.data.QHADCallback
import com.qhscale.data.WeightV2
import com.qhscale.qhlog.QhLog

class ScaleManager(context: Context, var port: String) : Activity(), QHADCallback {
    private val context: Context = context
    private var scaleCallback: ScaleCallback? = null
    private var netWeight: String? = null
    private lateinit var scale: QHJNIScale
    private fun init() {
        scale = QHJNIScale.getScale(port)
        scale.setCallback(this)
//        scale.setMaxRangeCao(30)
        ThreadUtils.runOnUiThreadDelayed({
            scale.setMaxRangeCao(150)
        },3000)
    }

    fun stop() {
        scale.deleteScale()
    }

    fun setScaleCallback(cb: ScaleCallback?) {
        scaleCallback = cb
    }

    fun tare() {
        scale.tare()
    }

    fun preTare(tareValue: String) {
        scale.preTareWeight(tareValue)
    }

    fun zero() {
        scale.zero()
    }

    fun forceZero() {
        scale.forceZero()
    }

    fun calibrationZero() {
        scale.setZeroPoint()
    }

    fun calibrationHighPoint(value: String) {
        scale.setCalibration(value)
    }

    fun getLibVer(): String {
        return scale.getLibraryVersion()
    }

    fun getADVer(): String {
        return scale.getADVersion()
    }

    fun getISN(): String {
        return scale.getISN()!!
    }

    fun saveAllSettings() {
        scale.setAllSetting()
    }

    fun test(index: Int) {
        scale.setGravity(index)
    }

    fun initialize() {
        //Initialize Status Bar
        scaleCallback!!.onTaredStateChanged(scale.isTaredStatus())
        scaleCallback!!.onStableStateChanged(scale.isStableStatus())
        scaleCallback!!.onZeroStateChanged(scale.isZeroStatus())
    }

    override fun onWeightUpdate(
        weight: WeightV2,
        isStable: Boolean,
        isTared: Boolean,
        isZero: Boolean
    ) {
        QhLog.console(
            QhLog.d,
            "tadWeight:" + weight.weight
                    + " : tareValue: " + weight.tare
        )

        scaleCallback?.onStableStateChanged(isStable)


        scaleCallback?.onTaredStateChanged(isTared)


        scaleCallback?.onZeroStateChanged(isZero)
        //("isstable:$isStable iszero:$isZero istare: $isTared")


        netWeight = weight.weight.toString()
        scaleCallback!!.onWeightUpdate(weight, netWeight!!)
        weight.recycle()
    }

    override fun onCalibrationSwitchEvent() {
        scaleCallback!!.onCalibrationSwitchEvent()
    }

    init {
        init()
    }
}