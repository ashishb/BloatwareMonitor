package net.ashishb.bloatwaremonitor;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private TextView mProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mProgressIndicator = (TextView)findViewById(R.id.loading_progress);


        AsyncTask<Void, Integer, List<EnhancedApplicationInfo>> task = new AsyncTask<Void, Integer, List<EnhancedApplicationInfo>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            protected List<EnhancedApplicationInfo> doInBackground(Void... params) {
                return getSystemApps(new UpdateListener() {
                    @Override
                    public void update(int progress, int total) {
                        publishProgress(progress, total);
                    }
                });
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressIndicator.setText((values[1] > 0 ? 100 * values[0]/values[1] : -1) + "%");
            }

            @Override
            protected void onPostExecute(List<EnhancedApplicationInfo> packageInfos) {
                mProgressIndicator.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                configureRecyclerView(packageInfos);
            }
        };
        task.execute((Void) null);
    }

    private void configureRecyclerView(final List<EnhancedApplicationInfo> packageInfos) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<PackageInfoViewHolder>() {
            @Override
            public PackageInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.list_item_pkg_info, parent, false);
                return new PackageInfoViewHolder(view);
            }

            @Override
            public void onBindViewHolder(PackageInfoViewHolder holder, int position) {
                holder.bindPackageInfo(position, packageInfos.get(position));
            }

            @Override
            public int getItemCount() {
                return packageInfos.size();
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    private List<EnhancedApplicationInfo> getSystemApps(@Nullable UpdateListener listener) {
        List<EnhancedApplicationInfo> systemsApps = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> allApps = packageManager.getInstalledPackages(0);
        int i = 0;
        for (PackageInfo pkgInfo : allApps) {
            if (listener != null) {
                listener.update(i, allApps.size());
            }
            i++;
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                        pkgInfo.packageName, PackageManager.GET_META_DATA);
                if (isSystemApp(applicationInfo)) {
                    systemsApps.add(new EnhancedApplicationInfo(
                            applicationInfo.packageName,
                            applicationInfo.loadLabel(packageManager),
                            pkgInfo.applicationInfo.loadIcon(packageManager),
                            applicationInfo.enabled,
                            getPackageSize(applicationInfo.packageName),
                            BloatwareManager.isBloatware(applicationInfo.packageName))
                    );
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        putEnabledFirst(systemsApps);
        return systemsApps;
    }

    private boolean isSystemApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }


    private void putEnabledFirst(List<EnhancedApplicationInfo> apps) {
        Collections.sort(apps, new Comparator<EnhancedApplicationInfo>() {
            @Override
            public int compare(EnhancedApplicationInfo lhs, EnhancedApplicationInfo rhs) {
                int compare = (BloatwareManager.isBloatware(lhs.getPackageName()) ? 0 : 1) -
                        (BloatwareManager.isBloatware(rhs.getPackageName()) ? 0 : 1);
                if (compare != 0) {
                    return compare;
                }
                compare = (lhs.isEnabled() ? 0 : 1) - (rhs.isEnabled() ? 0 : 1);
                if (compare != 0) {
                    return compare;
                }
                long sizeDiff = rhs.getTotalSize() - lhs.getTotalSize();
                if (sizeDiff < Integer.MAX_VALUE && sizeDiff > Integer.MIN_VALUE) {
                    return (int) sizeDiff;
                } else {
                    return (int) Math.signum(sizeDiff);
                }
            }
        });
    }

    private PackageSizeObserver getPackageSize(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            Class<?> clz = pm.getClass();
            ConditionVariable receivedData = new ConditionVariable(false);
            PackageSizeObserver packageSizeObserver = new PackageSizeObserver(receivedData);
            if (Build.VERSION.SDK_INT > 16) {
                Method myUserId = UserHandle.class.getDeclaredMethod("myUserId");
                int userID = (Integer) myUserId.invoke(pm);
                Method getPackageSizeInfo = clz.getDeclaredMethod(
                        "getPackageSizeInfo", String.class, int.class,
                        IPackageStatsObserver.class);//remember add int.class into the params
                getPackageSizeInfo.invoke(pm, packageName, userID, packageSizeObserver);
            } else {//for old API
                Method getPackageSizeInfo = clz.getDeclaredMethod(
                        "getPackageSizeInfo", String.class,
                        IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm, packageName, packageSizeObserver);
            }
            receivedData.block();
            return packageSizeObserver;
        } catch (Exception ex) {
            Log.e("MainActivity", "NoSuchMethodException");
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private interface UpdateListener {
        void update(int progress, int total);
    }
}