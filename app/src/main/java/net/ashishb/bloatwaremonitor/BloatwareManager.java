package net.ashishb.bloatwaremonitor;

import java.util.HashSet;
import java.util.Set;

public class BloatwareManager {
    private static final Set<String> BLOATWARE_SET = new HashSet<>();

    static {
        BLOATWARE_SET.add("com.google.android.talk");
        BLOATWARE_SET.add("com.google.android.play.games");
        BLOATWARE_SET.add("com.google.android.videos");
        BLOATWARE_SET.add("com.google.android.marvin.talkback");
    }

    static boolean isBloatware(String packageName) {
        return BLOATWARE_SET.contains(packageName);
    }
}
