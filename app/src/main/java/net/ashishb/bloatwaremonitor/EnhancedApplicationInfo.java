package net.ashishb.bloatwaremonitor;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public class EnhancedApplicationInfo {
    private final String mPackageName;
    private final CharSequence mApplicationName;
    private Drawable mIcon;
    private final boolean mEnabled;
    private final boolean mBloatware;
    private @Nullable PackageSizeObserver mSizeInfo;

    public EnhancedApplicationInfo(
            String packageName, CharSequence applicationName, Drawable drawable, boolean enabled,
            @Nullable PackageSizeObserver sizeInfo, boolean bloatware) {

        mPackageName = packageName;
        mApplicationName = applicationName;
        mIcon = drawable;
        mEnabled = enabled;
        mBloatware = bloatware;
        mSizeInfo = sizeInfo;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public CharSequence getApplicationName() {
        return mApplicationName;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public long getTotalSize() {
        return mSizeInfo != null ? mSizeInfo.getTotalSize() : -1;
    }

    public long getCodeSize() {
        return mSizeInfo != null ? mSizeInfo.getCodeSize() : -1;
    }

    public long getDataSize() {
        return mSizeInfo != null ? mSizeInfo.getDataSize() : -1;
    }

    public long getCacheSize() {
        return mSizeInfo != null ? mSizeInfo.getCacheSize() : -1;
    }

    public long getMediaSize() {
        return mSizeInfo != null ? mSizeInfo.getMediaSize() : -1;
    }


    public long getObbSize() {
        return mSizeInfo != null ? mSizeInfo.getObbSize() : -1;
    }

    public Drawable getApplicationIcon() {
        return mIcon;
    }

    public boolean isBloatware() {
        return mBloatware;
    }
}
