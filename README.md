# OpenBoot — Rev1

An ultra-lightweight *Android app* that offers a full-screen **WELCOME TO MINIMAL BOOT!**
experience, an explicit **RIAVVIO NORMALE** action, RAM measurements, and a
one-shot rooted reboot request.

**Important:** This revision is a functional UI / boot flow prototype. It is
**not a real minimal Android boot**. Android System Server, ART/Zygote, display
services, and background OS components still run. The app does not claim RAM
savings that it does not produce.

## Pixel 7 Pro test

1. Open this repository's **Actions > Android APK** workflow.
2. Download the **OpenBoot-Rev1-APK** artifact and extract **app-debug.apk**.
3. Install the APK, launch it once and select **ANTEPRIMA MINIMAL** for a safe
   test without root. The screen displays "WELCOME TO MINIMAL BOOT!"
4. Press **RIAVVIO NORMALE**. With no root in Preview mode, it exits Preview;
   it does not pretend to reboot the device.
5. For rooted phones, install **OpenBoot-OneShot-Module-rev1.zip** from the
   Actions artifacts through Magisk, reboot, open OpenBoot, and use
   **RIAVVIA IN MODALITÀ MINIMAL**. The module requests a one-time reopening
   of the UI after a *normal Android boot*.

The module requires Magisk-compatible root. Do **not** flash it on an
unrooted device; it does nothing there. No bootloader unlock / flashing
instructions are required for preview mode.

## One-shot behavior

The APK stores a device-protected pending flag and, with root, creates
/data/adb/openboot/nextboot. The optional Magisk service removes that flag on
next boot and opens the app after Android startup. With no module,
BOOT_COMPLETED may create an app notification instead (grant notification
permission using the app's setting). Background activity launch restrictions
on modern Android can prevent forced foreground opening.

The next reboot without a new request is always a normal Android boot.
Root operations do not disable system services, change slot A/B, patch
boot.img, or alter the kernel.

## What would be required for a real minimal boot?

A separate, device-specific firmware component running early in init must
switch to a pared-down userspace, with verified display/input support and an
explicit one-shot fallback path. Standard APKs still require the Android
runtime; a no-Android alternative would need a native app and appropriate
graphics/driver stack. This is not implemented in Rev1.

## Build

Requires JDK 17, Android SDK platform 36, Gradle 8.13, Android Gradle Plugin
8.11.1. Run from project root:

    gradle :app:assembleDebug

APK output: app/build/outputs/apk/debug/app-debug.apk

## Permissions

- RECEIVE_BOOT_COMPLETED: arm the one-shot state after reboot.
- POST_NOTIFICATIONS: optional fallback notification (Android 13+).
- Root (Magisk/su): only if explicitly requested by the user for reboot.
- No contacts, location, audio, camera, INTERNET or Accessibility permission.
