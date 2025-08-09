package com.ygzy.finance_elec.scale

import com.qhscale.data.WeightV2


interface ScaleCallback {
    /**
     * 稳定标志
     *
     * @param state
     */
    fun onStableStateChanged(state: Boolean)

    /**
     * 扣重标志
     *
     * @param state
     */
    fun onTaredStateChanged(state: Boolean)

    /**
     * 零点标志
     *
     * @param state
     */
    fun onZeroStateChanged(state: Boolean)

    /**
     * 重量
     *
     * @param weight
     */
    fun onWeightUpdate(scaleWeight: WeightV2, weight: String)

    fun onCalibrationSwitchEvent()
}