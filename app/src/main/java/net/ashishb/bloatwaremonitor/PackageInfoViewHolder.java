package net.ashishb.bloatwaremonitor;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class PackageInfoViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private boolean mEnhancedView;

    public PackageInfoViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void bindPackageInfo(int position, final EnhancedApplicationInfo applicationInfo) {
        mEnhancedView = false;
        ((ImageView)mView.findViewById(R.id.application_icon)).setImageDrawable(applicationInfo.getApplicationIcon());

        final TextView packageSizeView = (TextView) mView.findViewById(R.id.package_size);
        packageSizeView.setText(getText(applicationInfo, false));

        TextView packageNameView = (TextView) mView.findViewById(R.id.package_name);
        packageNameView.setTextColor(applicationInfo.isBloatware() ? Color.RED : Color.BLACK);
        CharSequence appName = applicationInfo.getApplicationName();
        String title = String.format(Locale.getDefault(), "%d. %s", position + 1, appName);
        packageNameView.setText(title);

        setButtonVisibilityAndAction(applicationInfo);

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnhancedView = !mEnhancedView;
                packageSizeView.setText(getText(applicationInfo, mEnhancedView));
                setButtonVisibilityAndAction(applicationInfo);
            }
        });
    }

    private void setButtonVisibilityAndAction(final EnhancedApplicationInfo applicationInfo) {
        Button disableButton = (Button) mView.findViewById(R.id.disable_button);
        if (applicationInfo.isEnabled()) {
            ((TextView) mView.findViewById(R.id.enabled_status)).setText(R.string.enabled_status);
            disableButton.setVisibility(View.VISIBLE);
            disableButton.setText(mEnhancedView ? "App info" : "Disable");
            disableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri packageUri = Uri.parse("package:" + applicationInfo.getPackageName());
                    Intent intent =  new Intent(
                            mEnhancedView ?
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS :
                                    Intent.ACTION_DELETE, packageUri);
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            ((TextView) mView.findViewById(R.id.enabled_status)).setText(R.string.disabled_status);
            disableButton.setVisibility(View.GONE);
        }
    }

    private String getText(EnhancedApplicationInfo applicationInfo, boolean mEnhancedView) {
        if (mEnhancedView) {
            return String.format(Locale.getDefault(),
                    "Package name: %s\nSize: %s\nCode: %s\nData: %s\nCache: %s\nMedia: %s\nOBB: %s\n",
                    applicationInfo.getPackageName(),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getTotalSize()),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getCodeSize()),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getDataSize()),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getCacheSize()),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getMediaSize()),
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getObbSize())
            );
        } else {
            return String.format(Locale.getDefault(), "Size: %s",
                    Formatter.formatFileSize(mView.getContext(), applicationInfo.getTotalSize())
            );
        }
    }
}
