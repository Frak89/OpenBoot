package com.ampy.openboot;

import java.util.concurrent.TimeUnit;

/** Run only fixed, audited commands; never interpolate external input into a root shell. */
final class RootShell {
    private RootShell() {}

    static boolean run(String command) {
        Process p = null;
        try {
            p = new ProcessBuilder("su", "-c", command)
                    .redirectErrorStream(true).start();
            boolean finished = p.waitFor(20, TimeUnit.SECONDS);
            if (!finished) return false;
            return p.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }

    static boolean hasRoot() {
        // "id -u" is 0 only for a privileged shell, not for ordinary Android apps.
        return run("test \"$(id -u)\" = 0");
    }

    static boolean requestLiteRestart() {
        // A Magisk boot-service module consumes this marker once and launches the UI.
        // This does NOT change Android into a barebones ROM.
        return run("mkdir -p /data/adb/openboot && chmod 700 /data/adb/openboot && touch /data/adb/openboot/nextboot && reboot");
    }

    static boolean requestNormalRestart() {
        return run("rm -f /data/adb/openboot/nextboot /data/adb/openboot/active; reboot");
    }
}
