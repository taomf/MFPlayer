package com.ygzy.finance_elec.utils;

import java.util.ArrayDeque;

/**
 * @author : taomf
 * Date    : 2024/4/21/17:44
 * Desc    : 检查重量是否稳定
 */
public class ScaleStabilityChecker {

    private static final int maxSampleQueueSize = 5;
    private static final double absoluteThreshold = 0.5; // 克，绝对差值阈值
    private static final double relativeThreshold = 0.05; // 百分比，相对变化率阈值

    private static final ArrayDeque<Double> weightSampleQueue = new ArrayDeque<>();
    private static double previousWeight = 0.0;

    public static boolean checkStability(double currentWeight) {
        // 将新重量值添加到队列，保持队列大小不超过 maxSampleQueueSize
        weightSampleQueue.addLast(currentWeight);
        if (weightSampleQueue.size() > maxSampleQueueSize) {
            weightSampleQueue.removeFirst();
        }
        previousWeight = currentWeight;

        // 检查所有队列中的样本是否都满足稳定条件
        if (weightSampleQueue.size() == maxSampleQueueSize) {
            for (double weight : weightSampleQueue) {
                if (!isSampleStable(weight, previousWeight)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSampleStable(double currentWeight, double previousWeight) {
        double delta = Math.abs(currentWeight - previousWeight);
        double changeRate = (previousWeight == 0.0) ? 0.0 : (currentWeight - previousWeight) / previousWeight;

        return delta <= absoluteThreshold && changeRate <= relativeThreshold;
    }
}
