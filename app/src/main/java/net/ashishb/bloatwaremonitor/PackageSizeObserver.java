package net.ashishb.bloatwaremonitor;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.RemoteException;
import android.util.Log;

class PackageSizeObserver extends IPackageStatsObserver.Stub {

    private long mTotalSize;
    private long mCodeSize;
    private long mDataSize;
    private long mCacheSize;
    private long mMediaSize;
    private long mObbSize;
    private ConditionVariable mReceivedData;

    public PackageSizeObserver(ConditionVariable receivedData) {

        mReceivedData = receivedData;
    }

    @Override
    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
            throws RemoteException {
        mTotalSize = pStats.codeSize + pStats.dataSize + pStats.cacheSize;
        mCodeSize = pStats.codeSize;
        mCodeSize = pStats.dataSize;
        mCodeSize = pStats.cacheSize;
        if (Build.VERSION.SDK_INT >= 14) {
            mTotalSize += pStats.externalCodeSize + pStats.externalDataSize +
                    pStats.externalCacheSize + pStats.externalMediaSize + pStats.externalObbSize;
            mCodeSize += pStats.externalCodeSize;
            mDataSize += pStats.externalDataSize;
            mCacheSize += pStats.externalCacheSize;
            mMediaSize = pStats.externalMediaSize;
            mObbSize = pStats.externalObbSize;

        }
        Log.d("PackageSizeObserver", "pStats: " + pStats.toString() + " success: " + succeeded);
        this.mReceivedData.open();
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public long getCodeSize() {
        return mCodeSize;
    }

    public long getDataSize() {
        return mDataSize;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    public long getMediaSize() {
        return mMediaSize;
    }

    public long getObbSize() {
        return mObbSize;
    }
}