package com.example.letsgogolfing;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

public class AnimationUtils {

    // Disable animations on the device
    public static void disableAnimations() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        try {
            // Disable window animations
            uiDevice.executeShellCommand("settings put global window_animation_scale 0");
            // Disable transition animations
            uiDevice.executeShellCommand("settings put global transition_animation_scale 0");
            // Disable animator duration scale
            uiDevice.executeShellCommand("settings put global animator_duration_scale 0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Enable animations on the device
    public static void enableAnimations() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        try {
            // Enable window animations
            uiDevice.executeShellCommand("settings put global window_animation_scale 1");
            // Enable transition animations
            uiDevice.executeShellCommand("settings put global transition_animation_scale 1");
            // Enable animator duration scale
            uiDevice.executeShellCommand("settings put global animator_duration_scale 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
