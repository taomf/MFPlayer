package com.ygzy.finance_elec.component.camera;

/**
 * @author : taomf
 * Date    : 2025/6/11 011/16:23
 * Desc    : 图片消息
 */
public class ImageMessage {
    private String message;
    private int type;

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_ERROR = 1;

    public ImageMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}
