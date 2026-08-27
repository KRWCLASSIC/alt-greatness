package com.mikolajkolek.fixaltgr;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyboardListener implements NativeKeyListener {
    public volatile boolean altKeyPressed = false;
    public volatile boolean controlKeyPressed = false;
    public volatile long lastStateChangeTime = 0;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            altKeyPressed = true;
            lastStateChangeTime = System.currentTimeMillis();
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            controlKeyPressed = true;
            lastStateChangeTime = System.currentTimeMillis();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            altKeyPressed = false;
            lastStateChangeTime = System.currentTimeMillis();
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            controlKeyPressed = false;
            lastStateChangeTime = System.currentTimeMillis();
        }
    }
}
