package com.ygzy.finance_elec.service;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * @author : taomf
 * Date    : 2024/12/7/16:11
 * Desc    : 扫码枪监听服务
 */
public class ScanService extends AccessibilityService {
    private final String TAG = "ScanService";
    private final StringBuilder buffer = new StringBuilder();
    private boolean hasShift = false;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        LogUtils.d(TAG,event.getEventType());
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getDeviceId() != -1){
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    String result = buffer.toString();
                    LogUtils.e(TAG, "扫码枪扫描结果：" + result);
//                    ToastUtils.showShort(result);
                    buffer.setLength(0);
                    hasShift = false;
                    EventBus.getDefault().postSticky(result);
                    return true;
                }
                LogUtils.d(TAG, KeyEvent.keyCodeToString(event.getKeyCode()) + "code = " + event.getKeyCode() +  "按键名称" +((char)event.getUnicodeChar()));
                buffer.append(keyCodeToChar(event.getKeyCode(),hasShift));
                hasShift = event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_LEFT;
            }
            return true;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {}

    /** keyCode转换为字符 */
    public String keyCodeToChar(int code, boolean isShift){
        LogUtils.e(TAG, "扫码枪结果：" + code +"   "+isShift);
        switch(code){
            case KeyEvent.KEYCODE_SHIFT_LEFT: return "";
            //数字键10个 + 符号10个
            case KeyEvent.KEYCODE_0: return isShift ? ")" : "0";
            case KeyEvent.KEYCODE_1: return isShift ? "!" : "1";
            case KeyEvent.KEYCODE_2: return isShift ? "@" : "2";
            case KeyEvent.KEYCODE_3: return isShift ? "#" : "3";
            case KeyEvent.KEYCODE_4: return isShift ? "$" : "4";
            case KeyEvent.KEYCODE_5: return isShift ? "%" : "5";
            case KeyEvent.KEYCODE_6: return isShift ? "^" : "6";
            case KeyEvent.KEYCODE_7: return isShift ? "&" : "7";
            case KeyEvent.KEYCODE_8: return isShift ? "*" : "8";
            case KeyEvent.KEYCODE_9: return isShift ? "(" : "9";

            //字母键26个小写 + 26个大写
            case KeyEvent.KEYCODE_A: return isShift ? "A" : "a";
            case KeyEvent.KEYCODE_B: return isShift ? "B" : "b";
            case KeyEvent.KEYCODE_C: return isShift ? "C" : "c";
            case KeyEvent.KEYCODE_D: return isShift ? "D" : "d";
            case KeyEvent.KEYCODE_E: return isShift ? "E" : "e";
            case KeyEvent.KEYCODE_F: return isShift ? "F" : "f";
            case KeyEvent.KEYCODE_G: return isShift ? "G" : "g";
            case KeyEvent.KEYCODE_H: return isShift ? "H" : "h";
            case KeyEvent.KEYCODE_I: return isShift ? "I" : "i";
            case KeyEvent.KEYCODE_J: return isShift ? "J" : "j";
            case KeyEvent.KEYCODE_K: return isShift ? "K" : "k";
            case KeyEvent.KEYCODE_L: return isShift ? "L" : "l";
            case KeyEvent.KEYCODE_M: return isShift ? "M" : "m";
            case KeyEvent.KEYCODE_N: return isShift ? "N" : "n";
            case KeyEvent.KEYCODE_O: return isShift ? "O" : "o";
            case KeyEvent.KEYCODE_P: return isShift ? "P" : "p";
            case KeyEvent.KEYCODE_Q: return isShift ? "Q" : "q";
            case KeyEvent.KEYCODE_R: return isShift ? "R" : "r";
            case KeyEvent.KEYCODE_S: return isShift ? "S" : "s";
            case KeyEvent.KEYCODE_T: return isShift ? "T" : "t";
            case KeyEvent.KEYCODE_U: return isShift ? "U" : "u";
            case KeyEvent.KEYCODE_V: return isShift ? "V" : "v";
            case KeyEvent.KEYCODE_W: return isShift ? "W" : "w";
            case KeyEvent.KEYCODE_X: return isShift ? "X" : "x";
            case KeyEvent.KEYCODE_Y: return isShift ? "Y" : "y";
            case KeyEvent.KEYCODE_Z: return isShift ? "Z" : "z";

            //符号键11个 + 11个
            case KeyEvent.KEYCODE_COMMA: return isShift ? "<" : ",";
            case KeyEvent.KEYCODE_PERIOD: return isShift ? ">" : ".";
            case KeyEvent.KEYCODE_SLASH: return isShift ? "?" : "/";
            case KeyEvent.KEYCODE_BACKSLASH: return isShift ? "|" : "\\";
            case KeyEvent.KEYCODE_APOSTROPHE: return isShift ? "\"" : "'";
            case KeyEvent.KEYCODE_SEMICOLON: return isShift ? ":" : ";";
            case KeyEvent.KEYCODE_LEFT_BRACKET: return isShift ? "{" : "[";
            case KeyEvent.KEYCODE_RIGHT_BRACKET: return isShift ? "}" : "]";
            case KeyEvent.KEYCODE_GRAVE: return isShift ? "~" : "`";
            case KeyEvent.KEYCODE_EQUALS: return isShift ? "+" : "=";
            case KeyEvent.KEYCODE_MINUS: return isShift ? "_" : "-";
            default: return "?";
        }
    }
}
