package com.stonedot.todo.smartwalk;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.commons.lang.RandomStringUtils;

import jp.line.android.sdk.LineSdkContext;
import jp.line.android.sdk.LineSdkContextManager;
import jp.line.android.sdk.api.ApiClient;
import jp.line.android.sdk.api.ApiRequestFuture;
import jp.line.android.sdk.api.ApiRequestFutureListener;
import jp.line.android.sdk.login.LineAuthManager;
import jp.line.android.sdk.login.LineLoginFuture;
import jp.line.android.sdk.login.LineLoginFutureListener;
import jp.line.android.sdk.model.Profile;

/**
 * Created by komatsu on 2016/11/06.
 */

public class LINELoginPage {

    public static final String FILENAME_LINE_ID = "line_id";
    private AppCompatActivity mActivity;

    public LINELoginPage(AppCompatActivity activity) {
        mActivity = activity;
    }

    public void openLoginPage() {
        Log.d("LineFragment", "Clicked LINE login button.");
        LineSdkContext sdkContext = LineSdkContextManager.getSdkContext();
        LineAuthManager authManager = sdkContext.getAuthManager();
        LineLoginFuture loginFuture = authManager.login(mActivity);
        loginFuture.addFutureListener(loginComplete);
    }

    private LineLoginFutureListener loginComplete = new LineLoginFutureListener() {
        @Override
        public void loginComplete(LineLoginFuture future) {
            switch (future.getProgress()) {
                case SUCCESS:
                    Log.d("LineFragment", "Line login succeeded.");
                    LineSdkContext sdkContext = LineSdkContextManager.getSdkContext();
                    ApiClient apiClient = sdkContext.getApiClient();
                    apiClient.getMyProfile(myProfileComplete);
                    break;
                case CANCELED:
                    Log.d("LineFragment", "Line login canceled.");
                    break;
                default:
                    Log.d("LineFragment", "Line login go something wrong." + future.getProgress().toString());
                    Log.d("LineFragment", "We are trying fallback.");
                    if(UserDataStorage.getLineMid(mActivity).isEmpty()) {
                        String mid = RandomStringUtils.randomAlphanumeric(30);
                        UserDataStorage.putLineMid(mActivity, mid);
                    }
                    LINEFriendDialogFragment dialog = new LINEFriendDialogFragment();
                    dialog.show(mActivity.getSupportFragmentManager(), "line_dialog");
                    break;
            }
        }
    };

    private ApiRequestFutureListener myProfileComplete = new ApiRequestFutureListener<Profile>() {
        @Override
        public void requestComplete(ApiRequestFuture<Profile> future) {
            switch (future.getStatus()) {
                case SUCCESS:
                    Log.d("LineFragment", "Profile retrieving succeeded.");
                    Profile profile = future.getResponseObject();
                    UserDataStorage.putLineMid(mActivity, profile.mid);
                    LINEFriendDialogFragment dialog = new LINEFriendDialogFragment();
                    dialog.show(mActivity.getSupportFragmentManager(), "line_dialog");
                    break;
                default:
                    Log.d("LineFragment", "Profile retrieving failed.");
                    break;
            }
        }
    };
}
