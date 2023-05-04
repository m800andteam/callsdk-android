package com.m800.sdk.core.demo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionHelper {

    private Activity mActivity;
    private Fragment mFragment;
    private final SparseArray<Callback> mCallbackMap = new SparseArray<>();

    public PermissionHelper(Fragment fragment) {
        mFragment = fragment;
    }

    public PermissionHelper(Activity activity) {
        mActivity = activity;
    }

    public boolean isPermissionGranted(String permission) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission);
    }

    public void requestPermissions(int requestCode, @NonNull Callback callback, @NonNull String... permissions) {

        String[] missingPermissions = getMissingPermissions(permissions);

        if (missingPermissions.length > 0) {
            mCallbackMap.put(requestCode, callback);
            requestPermissions(missingPermissions, requestCode);
        } else {
            callback.onPermissionsGranted(requestCode);
        }
    }

    public String[] getMissingPermissions(@NonNull String... permissions) {

        if (permissions.length == 0) {
            return new String[0];
        }

        List<String> missingPermissions = new ArrayList<>();
        List<String> permissionsRequested = new ArrayList<>(Arrays.asList(permissions));

        for (String permission1 : permissionsRequested) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission1) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission1);
            }
        }

        return missingPermissions.toArray(new String[0]);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mCallbackMap.get(requestCode) != null && permissions.length > 0) {

            if (grantResults.length <= 0) {
                return;
            }

            for (int grantResult: grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    mCallbackMap.get(requestCode).onPermissionsDenied(requestCode);
                    mCallbackMap.remove(requestCode);
                    return;
                }
            }

            mCallbackMap.get(requestCode).onPermissionsGranted(requestCode);
            mCallbackMap.remove(requestCode);
        }
    }

    private void requestPermissions(@NonNull String[] permissions, int requestCode) {
        if (mFragment != null) {
            mFragment.requestPermissions(permissions, requestCode);
        } else {
            ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
        }
    }

    private Activity getActivity() {
        if (mFragment != null) {
            return mFragment.getActivity();
        } else {
            return mActivity;
        }
    }

    /**
     * Check if the app has a particular permission.
     *
     * @param permission permission to check. Should be one from {@link Manifest.permission}.
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public interface Callback {
        void onPermissionsGranted(int requestCode);

        void onPermissionsDenied(int requestCode);
    }

    public boolean hasBTPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            return true;
        }
    }
}
