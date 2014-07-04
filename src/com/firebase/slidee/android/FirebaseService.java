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

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static final String SCOPES = "https://www.googleapis.com/auth/plus.login";

    private static final String CONTROL_REF = "control";

    private static Firebase rootRef = new Firebase("https://slidef.firebaseio.com");

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private SimpleLogin mSimpleLoginClient;
    private LoginState mCurrentLoginState = LoginState.Default;
    private FirebaseSimpleLoginUser mSimpleLoginUser = null;

    private OnLoginStateChangedListener mLoginStateChangedListener = null;

    public enum LoginState {
        Default,
        LoggingIn,
        LoggedIn
    }

    public interface OnLoginStateChangedListener {
        public void onLoginStateChanged(LoginState state);
    }

    /**
     * Creates a new FirebaseService
     * @param client the google API client
     * @param context the Android context
     */
    public FirebaseService(GoogleApiClient client, Context context) {
        this.mGoogleApiClient = client;
        this.mContext = context;

        mSimpleLoginClient = new SimpleLogin(rootRef, this.mContext);
    }

    public void setOnLoginStateChangedListener(OnLoginStateChangedListener listener) {
        mLoginStateChangedListener = listener;
    }


    /**
     * Raises a login state changed event if the listener is not null.
     * @param state the state to raise
     */
    private void loginStateChanged(final LoginState state) {
        mCurrentLoginState = state;
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

    /**
     * Pushes a command to Firebase to control the slide deck.
     * @param command the command to push
     */
    public void pushCommand(Commands command) {
        switch (command) {
            case Next:
                pushControlCommand("next");
                break;
            case Previous:
                pushControlCommand("prev");
                break;
        }
    }

    /**
     * Pushes a string command to Firebase to control the slide deck.
     * @param command the command to push
     */
    private void pushControlCommand(String command) {
        if(mCurrentLoginState == LoginState.LoggedIn) {
            String uid = mSimpleLoginUser.getUid();
            rootRef.child(uid).child(CONTROL_REF).push().setValue(command);
        }
    }

    /**
     * Logs in using Firebase Simple Login
     */
    public void login() {
        loginStateChanged(LoginState.LoggingIn);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String token = fetchToken();
                    if (token != null) {
                        simpleLogin(token);
                    } else {
                        Log.e(TAG, "No token");
                        loginStateChanged(LoginState.Default);
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    loginStateChanged(LoginState.Default);
                }
            }
        }).start();
    }


    /**
     * Logs the user out of Firebase
     */
    public void logout() {
        mSimpleLoginClient.logout();
        mSimpleLoginUser = null;

        //TODO: do this right, listen to .info/authenticated
        loginStateChanged(LoginState.Default);

    }

    /**
     * Given an access token initiates a firebase simple login
     *
     * @param accessToken the access token as provided by the G+ API
     */
    private void simpleLogin(String accessToken) {
        mSimpleLoginClient.loginWithGoogle(accessToken, new SimpleLoginAuthenticatedHandler() {
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error != null) {
                    Log.e(TAG, error.toString());
                    loginStateChanged(LoginState.Default);
                } else {
                    Log.d(TAG, "logged in!");
                    mSimpleLoginUser = user;
                    loginStateChanged(LoginState.LoggedIn);
                }
            }
        });
    }

    /**
     * Fetches an access token from the G+ API
     * @return the access token
     * @throws IOException
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(
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
