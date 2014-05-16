package com.firebase.slidee.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class FirebaseSimpleLoginTask extends AsyncTask {
    private static String TAG = "FirebaseSimpleLoginTask";

    private Activity mActivity;
    private String mAccountName;
    private String mScope;

    FirebaseSimpleLoginTask(Activity activity, String accountName, String scope) {
        this.mActivity = activity;
        this.mAccountName = accountName;
        this.mScope = scope;
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            String token = fetchToken();
            if(token != null) {
                simpleLogin(token);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public void simpleLogin(String accessToken) {
        Firebase ref = new Firebase("https://slidee.firebaseio.com");
        SimpleLogin authClient = new SimpleLogin(ref, mActivity.getApplicationContext());

        authClient.loginWithGoogle(accessToken+'a', new SimpleLoginAuthenticatedHandler() {
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error != null) {
                    Log.e(TAG, error.toString());
                    // There was an error
                } else {
                    Log.d(TAG,"logged in!");
                }
            }
        });
    }

    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(
                    mActivity,
                    mAccountName,
                    "oauth2:" + mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
//            mActivity.handleException(userRecoverableException);
            Log.e(TAG, userRecoverableException.toString());
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.e(TAG, fatalException.toString());

        }
        return null;
    }
}
