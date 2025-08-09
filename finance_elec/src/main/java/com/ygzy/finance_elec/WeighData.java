package com.ygzy.finance_elec;

/**
 * @author : taomf
 * Date    : 2024/11/17/13:48
 * Desc    : 称重数据
 */
public class WeighData {
    private String netWeight;
    private String grossWeight;
    private int flag;
    private String tareWeight;


    /**
     * @param flagStatus 稳定状态
     * @param netWeight 净重
     * @param tareWeight  皮重
     * @param grossWeight 毛重
     */
    public WeighData(int flagStatus, String netWeight, String tareWeight, String grossWeight) {
        this.flag = flagStatus;
        this.netWeight = netWeight;
        this.tareWeight = tareWeight;
        this.grossWeight = grossWeight;
    }

    /**
     *
     * @return 净重
     */
    public String getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(String netWeight) {
        this.netWeight = netWeight;
    }

    /**
     *
     * @return 总重（毛重）
     */
    public String getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(String grossWeight) {
        this.grossWeight = grossWeight;
    }
    /**
     * @return 稳定状态
     */
    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     *
     * @return 皮重
     */
    public String getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(String tareWeight) {
        this.tareWeight = tareWeight;
    }
}
