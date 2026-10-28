package com.ampy.openboot;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** All-Android UI. Real lean boot requires a separately built ROM component. */
public final class MainActivity extends Activity {
    private static final int BG = 0xFF070E18;
    private static final int CARD = 0xFF101C2C;
    private static final int AQUA = 0xFF46E4FF;
    private static final int WHITE = 0xFFE8F5FF;
    private static final int MUTED = 0xFF8DA7BC;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView memoryLabel;
    private TextView rootLabel;
    private boolean preview = false;
    private boolean destroyed = false;
    private final Runnable memoryUpdate = new Runnable() {
        @Override public void run() {
            if (destroyed) return;
            if (memoryLabel != null) memoryLabel.setText(getMemoryText());
            handler.postDelayed(this, 2000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        showCurrent();
        handler.post(memoryUpdate);
    }

    @Override protected void onResume() {
        super.onResume();
        if (!preview) showCurrent();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        handler.removeCallbacks(memoryUpdate);
        super.onDestroy();
    }

    private void immersive(boolean enabled) {
        getWindow().getDecorView().setSystemUiVisibility(enabled
                ? (View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                   | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                   | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                : View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable surface(int fill, int stroke, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fill);
        bg.setCornerRadius(dp(radius));
        if (stroke != 0) bg.setStroke(dp(1), stroke);
        return bg;
    }

    private LinearLayout column(int padding, int gravity) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(gravity);
        col.setPadding(dp(padding), dp(padding), dp(padding), dp(padding));
        return col;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(sp);
        label.setTextColor(color);
        label.setIncludeFontPadding(true);
        label.setGravity(Gravity.CENTER_HORIZONTAL);
        label.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return label;
    }

    private void add(LinearLayout layout, View view, int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(top);
        layout.addView(view, lp);
    }

    private Button button(String title, boolean primary, Runnable onTap) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(title);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? BG : AQUA);
        b.setPadding(dp(12), dp(11), dp(12), dp(11));
        b.setBackground(surface(primary ? AQUA : CARD, primary ? 0 : 0xFF2C566B, 14));
        b.setOnClickListener(v -> onTap.run());
        return b;
    }

    private LinearLayout page(boolean fullScreen) {
        immersive(fullScreen);
        LinearLayout layout = column(24, Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(BG);
        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setClipToPadding(false);
        scroller.addView(layout);
        setContentView(scroller);
        memoryLabel = null;
        rootLabel = null;
        return layout;
    }

    private void header(LinearLayout page) {
        TextView logo = text("◈  OPENBOOT  /  REV1", 15, AQUA, true);
        logo.setLetterSpacing(0.18f);
        add(page, logo, 28);
        TextView subtitle = text("AMPYBIOS EXPERIMENTAL BOOT INTERFACE", 10, MUTED, false);
        subtitle.setLetterSpacing(0.08f);
        add(page, subtitle, 8);
    }

    private void ramCard(LinearLayout page) {
        LinearLayout card = column(20, Gravity.CENTER_HORIZONTAL);
        card.setBackground(surface(CARD, 0xFF233E50, 18));
        add(card, text("MEMORIA DEL DISPOSITIVO", 12, MUTED, true), 0);
        memoryLabel = text(getMemoryText(), 19, WHITE, true);
        add(card, memoryLabel, 9);
        add(page, card, 25);
    }

    private String getMemoryText() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager == null) return "RAM non disponibile";
        manager.getMemoryInfo(info);
        return String.format(java.util.Locale.ITALY, "Libera: %.2f GB   /   Totale: %.2f GB",
                info.availMem / 1e9, info.totalMem / 1e9);
    }

    private void showCurrent() {
        if (BootState.active(this)) showLite(false);
        else showHome();
    }

    private void showHome() {
        LinearLayout page = page(false);
        header(page);
        add(page, text("BOOT MODE\nCONTROLLER", 30, WHITE, true), 44);
        add(page, text("Riavvio temporaneo e interfaccia minimalista per Android.",
                15, MUTED, false), 16);
        ramCard(page);
        rootLabel = text("ACCESSO ROOT: NON VERIFICATO", 12, MUTED, false);
        add(page, rootLabel, 22);
        add(page, button("VERIFICA ROOT", false, this::checkRoot), 12);
        add(page, button("RIAVVIA IN MODALITÀ MINIMAL", true, this::confirmLiteReboot), 18);
        add(page, button("ANTEPRIMA MINIMAL · SENZA RIAVVIO", false, () -> {
            preview = true;
            showLite(true);
        }), 12);
        add(page, button("ABILITA NOTIFICHE DI AVVIO", false, this::requestNotifications), 12);
        add(page, text("NOTA: la modalità di Rev1 non arresta Android o i servizi di sistema.\n" +
                "Per una vera ROM minimale serve un componente di avvio dedicato.",
                12, MUTED, false), 25);
    }

    private void showLite(boolean isPreview) {
        LinearLayout page = page(true);
        header(page);
        add(page, text("WELCOME TO\nMINIMAL BOOT!", 34, AQUA, true), 60);
        add(page, text(isPreview ? "PREVIEW MODE" : "ONE-SHOT BOOT SESSION", 14, WHITE, true), 16);
        add(page, text("L'interfaccia essenziale è attiva.\n" +
                "Android rimane caricato: nessuna riduzione di RAM è garantita.",
                14, MUTED, false), 20);
        ramCard(page);
        add(page, text("✓  Modalità temporanea\n✓  Nessuna modifica alle partizioni\n" +
                "✓  Ritorno al boot normale\n!  Framework Android ancora attivo",
                14, WHITE, false), 25);
        add(page, button("RIAVVIO NORMALE", true, this::confirmNormalReboot), 38);
        add(page, button("ESCI DALLA SCHERMATA", false, () -> {
            preview = false;
            BootState.clear(this);
            showHome();
        }), 12);
        add(page, text("OpenBoot rev1 · Pixel / Android · Experimental", 11, MUTED, false), 28);
    }

    private void checkRoot() {
        if (rootLabel != null) rootLabel.setText("VERIFICA ACCESSO ROOT...");
        new Thread(() -> {
            boolean granted = RootShell.hasRoot();
            runOnUiThread(() -> {
                if (rootLabel != null) rootLabel.setText(granted
                    ? "ACCESSO ROOT: CONCESSO" : "ACCESSO ROOT: NON DISPONIBILE");
            });
        }).start();
    }

    private void requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        } else {
            Toast.makeText(this, "Notifiche già autorizzate", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLiteReboot() {
        new AlertDialog.Builder(this)
                .setTitle("Riavviare in sessione OpenBoot?")
                .setMessage("OpenBoot Rev1 richiede root e opzionalmente il modulo Magisk " +
                        "per riaprire la schermata dopo il riavvio. Android resterà completo: " +
                        "NON è ancora un vero boot minimale. Continuare?")
                .setNegativeButton("Annulla", null)
                .setPositiveButton("Riavvia", (dialog, which) -> restartLite())
                .show();
    }

    private void restartLite() {
        Toast.makeText(this, "Richiesta accesso root...", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            if (!RootShell.hasRoot()) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("Root non disponibile")
                        .setMessage("Sul sistema attuale non è possibile richiedere un riavvio " +
                                "privilegiato. Puoi provare l'anteprima senza root.")
                        .setPositiveButton("OK", null).show());
                return;
            }
            BootState.request(this);
            if (!RootShell.requestLiteRestart()) {
                BootState.clear(this);
                runOnUiThread(() -> Toast.makeText(this,
                        "Riavvio non eseguito: verifica il gestore root.",
                        Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void confirmNormalReboot() {
        new AlertDialog.Builder(this)
                .setTitle("RIAVVIO NORMALE")
                .setMessage(preview ? "Sei nell'anteprima. Uscire dalla schermata? " +
                        "Puoi anche riavviare manualmente con il tasto accensione."
                        : "Vuoi riavviare il dispositivo? La sessione OpenBoot non " +
                          "verrà riattivata al prossimo avvio.")
                .setNegativeButton("Annulla", null)
                .setPositiveButton(preview ? "Esci" : "Riavvia", (d, w) -> {
                    if (preview) {
                        preview = false;
                        BootState.clear(this);
                        showHome();
                    } else restartNormal();
                }).show();
    }

    private void restartNormal() {
        BootState.clear(this);
        new Thread(() -> {
            if (!RootShell.hasRoot() || !RootShell.requestNormalRestart()) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("Riavvio manuale richiesto")
                        .setMessage("Non è stato possibile riavviare con root. " +
                                "La modalità one-shot è già disattivata: usa il menu " +
                                "di accensione di Android per il riavvio normale.")
                        .setPositiveButton("OK", (d,w) -> showHome()).show());
            }
        }).start();
    }
}
