package com.shakeApp;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

class ApplicationAdapter extends ArrayAdapter<ApplicationInfo>
{
    private List<ApplicationInfo> appsList = null;
    private Context context;
    private PackageManager packageManager;
    private Dialog dialog;
    private Button selButton;
    private Button recButton;

    ApplicationAdapter(Context context, int textViewResourceId,
                       List<ApplicationInfo> appsList, Dialog dialog, Button selButton, Button recButton)
    {
        super(context, textViewResourceId, appsList);
        this.context = context;
        this.appsList = appsList;
        this.dialog = dialog;
        this.selButton = selButton;
        this.recButton = recButton;
        packageManager = context.getPackageManager();
    }

    @Override
    public int getCount() {
        return ((null != appsList) ? appsList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return ((null != appsList) ? appsList.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        if (null == view)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, null);
        }

        final ApplicationInfo applicationInfo = appsList.get(position);
        if (null != applicationInfo)
        {
            TextView appName = (TextView) view.findViewById(R.id.app_name);
            ImageView iconView = (ImageView) view.findViewById(R.id.app_icon);

            appName.setText(applicationInfo.loadLabel(packageManager));
            iconView.setImageDrawable(applicationInfo.loadIcon(packageManager));
        }

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recButton.setVisibility(View.VISIBLE);
                assert applicationInfo != null;
                setDefaultsString(getDefaultsString("selectApp", getContext()) + "Action", applicationInfo.packageName, getContext());
                selButton.setBackground(applicationInfo.loadIcon(packageManager));
                setDefaultsString(getDefaultsString("selectApp", getContext()) + "image", bitmapToString(drawableToBitmap(applicationInfo.loadIcon(packageManager))), getContext());
                dialog.dismiss();
            }
        });

        return view;
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable)
            return ((BitmapDrawable)drawable).getBitmap();
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private String bitmapToString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] arr = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(arr, Base64.DEFAULT);
    }

    private void setDefaultsString(String key, String value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getDefaultsString(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}
