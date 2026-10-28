#!/system/bin/sh
# Magisk late_start service: UI launcher only, not a lightweight OS.
DIR=/data/adb/openboot
mkdir -p "$DIR"
chmod 700 "$DIR"
# Old sessions always expire on a new boot.
rm -f "$DIR/active"
if [ ! -f "$DIR/nextboot" ]; then
    exit 0
fi
# Consume BEFORE launching so an unexpected reboot returns to normal.
rm -f "$DIR/nextboot"
touch "$DIR/active"
count=0
while [ "$(getprop sys.boot_completed)" != "1" ] && [ "$count" -lt 90 ]; do
    sleep 2
    count=$((count + 1))
done
if [ "$(getprop sys.boot_completed)" = "1" ]; then
    sleep 3
    am start --user 0 -n com.ampy.openboot/.MainActivity >/dev/null 2>&1
fi
