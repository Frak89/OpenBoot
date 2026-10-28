package com.ampy.openboot;

import android.content.Context;
import android.content.SharedPreferences;

/** A one-shot request. Does not change kernel, ramdisk, Android services or ROM slots. */
final class BootState {
    private static final String NAME = "openboot_direct_boot";
    private static final String PENDING = "pending";
    private static final String ACTIVE = "active";
    private BootState() {}

    private static SharedPreferences prefs(Context context) {
        return context.createDeviceProtectedStorageContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    static boolean pending(Context context) {
        return prefs(context).getBoolean(PENDING, false);
    }

    static boolean active(Context context) {
        return prefs(context).getBoolean(ACTIVE, false);
    }

    static void request(Context context) {
        prefs(context).edit().putBoolean(PENDING, true).putBoolean(ACTIVE, false).commit();
    }

    static void onBoot(Context context) {
        SharedPreferences p = prefs(context);
        if (p.getBoolean(PENDING, false)) {
            p.edit().putBoolean(PENDING, false).putBoolean(ACTIVE, true).commit();
        } else if (p.getBoolean(ACTIVE, false)) {
            // Every subsequent boot is normal, even if the user couldn't press Exit.
            p.edit().putBoolean(ACTIVE, false).commit();
        }
    }

    static void clear(Context context) {
        prefs(context).edit().putBoolean(PENDING, false).putBoolean(ACTIVE, false).commit();
    }
}
