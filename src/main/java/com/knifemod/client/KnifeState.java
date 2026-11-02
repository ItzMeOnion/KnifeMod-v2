package com.knifemod.client;

import com.knifemod.KnifeDef;

import java.util.ArrayList;
import java.util.List;

/** Client-side only: owned knives and the current hand animation. Nothing here is sent to the server. */
public final class KnifeState {
    public static final List<KnifeDef> OWNED = new ArrayList<>();
    public static KnifeDef equipped;

    /** 0 = idle, 1 = spin, 2 = inspect */
    public static int mode = 0;
    public static long start;

    private KnifeState() {
    }

    public static void play(int newMode) {
        if (equipped == null) return;
        mode = newMode;
        start = System.currentTimeMillis();
    }

    public static void equipNext() {
        if (OWNED.isEmpty()) return;
        int i = OWNED.indexOf(equipped);
        equipped = OWNED.get((i + 1) % OWNED.size());
    }
}
