# Optional Magisk one-shot autostart

This module *only* starts the OpenBoot APK after Android has fully booted.
It does not stop Zygote, System Server, or other operating-system services.

Install the APK, then install the ZIP produced by the GitHub Actions workflow
through Magisk's "Modules > Install from storage" feature and reboot.

The app's "Enter Lite" action asks for root, writes
/data/adb/openboot/nextboot, and reboots. The module consumes the marker at
startup and attempts to launch OpenBoot. If Android blocks that launch,
the app offers a notification (permission needed); open it manually.

On the next restart the marker is absent, so Android boots normally.
If you change ROMs or lose root, no Android framework services are disabled
by this module.

For a genuinely stripped-down boot, a separate device-specific ROM
implementation would be needed; see the repository's README.
