package com.firebase.slidee.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

public class FirebaseSlideeService {
    private static final String TAG = "FirebaseSlideeService";
    private static final String SCOPES = "https://www.googleapis.com/auth/plus.login";

    private static Firebase rootRef = new Firebase("https://slidee.firebaseio.com");
    private static Firebase controlRef = rootRef.child("control");
    //private static Firebase currentRef = rootRef.child("current");

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private SimpleLogin mSimpleLoginClient;

    private OnLoginStateChangedListener mLoginStateChangedListener = null;

    public enum LoginState {
        Default,
        LoggingIn,
        LoggedIn
    }

    public interface OnLoginStateChangedListener {
        public void onLoginStateChanged(LoginState state);
    }


    public FirebaseSlideeService(GoogleApiClient client, Context context) {
        this.mGoogleApiClient = client;
        this.mContext = context;

        mSimpleLoginClient = new SimpleLogin(rootRef, this.mContext);
    }

    public void setOnLoginStateChangedListener(OnLoginStateChangedListener listener) {
        mLoginStateChangedListener = listener;
    }

    private void loginStateChanged(final LoginState state) {
        if(mLoginStateChangedListener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mLoginStateChangedListener.onLoginStateChanged(state);
                }
            });
        }
    }

    public void pushCommand(SlideeCommands command) {
        switch (command) {
            case Next:
                pushControlCommand("next");
                break;
            case Previous:
                pushControlCommand("prev");
                break;
        }
    }

    private void pushControlCommand(String command) {
        controlRef.push().setValue(command);
    }

    public void login() {
        loginStateChanged(LoginState.LoggingIn);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String token = fetchToken();
                    if (token != null) {
                        simpleLogin(token);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
    }

    public void logout() {
        mSimpleLoginClient.logout();

        //TODO: do this right, listen to .info/authenticated
        loginStateChanged(LoginState.Default);

    }

    public void simpleLogin(String accessToken) {
        mSimpleLoginClient.loginWithGoogle(accessToken, new SimpleLoginAuthenticatedHandler() {
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error != null) {
                    Log.e(TAG, error.toString());
                    loginStateChanged(LoginState.Default);
                } else {
                    Log.d(TAG, "logged in!");
                    loginStateChanged(LoginState.LoggedIn);
                }
            }
        });
    }

    protected String fetchToken() throws IOException {
        try {
            GoogleAuthUtil.getToken(
                    mContext,
                    Plus.AccountApi.getAccountName(mGoogleApiClient),
                    "oauth2:" + SCOPES);
        } catch (UserRecoverableAuthException userRecoverableException) {

            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            Log.e(TAG, userRecoverableException.toString());
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.e(TAG, fatalException.toString());
        }
        return null;
    }
}
