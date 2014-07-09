package com.firebase.slidee.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends Activity implements ButtonControlFragment.OnControlButtonClickedListener, PebbleService.OnPebbleCommandReceivedListener {
    private static final String TAG = "MainActivity";

    private FirebaseService mFirebaseService;
    private PebbleService mPebbleService;
    private GoogleApiClient mGoogleApiClient;

    private MenuItem mSignInButton;
    private MenuItem mSignOutButton;

    private PendingIntent mSignInIntent;
    private static final int RC_SIGN_IN = 0;

    static final String ACTION_NEXTSLIDE = "com.firebase.slidee.action.NEXTSLIDE";

    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //             Default: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //              SignIn: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //          InProgress: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private GPlusSignInState mSignInProgress;

    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButtonControlFragment buttonControlFragment = (ButtonControlFragment) getFragmentManager().findFragmentById(R.id.button_control_fragment);
        buttonControlFragment.setOnControlButtonClickedListener(this);

        mSignInButton = (MenuItem) findViewById(R.id.action_signin);
        mSignOutButton = (MenuItem) findViewById(R.id.action_signout);

        mPebbleService = new PebbleService(getApplicationContext());
        mPebbleService.setOnPebbleCommandReceivedListener(this);

        mGoogleApiClient = buildGoogleApiClient();

        mFirebaseService = getFirebaseService();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if(action.equals(ACTION_NEXTSLIDE)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mFirebaseService.pushCommand(Commands.Next);
                }
            }).start();

        }

        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onControlButtonClicked(Commands command) {
        mFirebaseService.pushCommand(command);
    }

    @Override
    public void onPebbleCommandReceivedListener(Commands command) {
        mFirebaseService.pushCommand(command);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signin:
                gPlusSignIn();
                return true;
            case R.id.action_signout:
                gPlusSignOut();
                return true;
            case R.id.action_notification:
                notification();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void notification() {
        int notificationId = 001;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        PendingIntent nextPendingIntent = PendingIntent.getActivity(this, 2, new Intent(ACTION_NEXTSLIDE), 0);


        mBuilder.addAction(R.drawable.ic_launcher, getString(R.string.next_button_text), nextPendingIntent);



        // mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }


    private enum GPlusSignInState {
        Default,
        SignIn,
        InProgress
    }


    private void gPlusSignIn() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = GPlusSignInState.InProgress;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = GPlusSignInState.SignIn;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            showPlayServicesDialog();
        }
    }

    protected void showPlayServicesDialog() {
        if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
            GooglePlayServicesUtil.getErrorDialog(
                    mSignInError,
                    this,
                    RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.e(TAG, "Google Play services resolution cancelled");
                            mSignInProgress = GPlusSignInState.Default;
                        }
                    }).show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.play_services_error)
                    .setPositiveButton(R.string.close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.e(TAG, "Google Play services error could not be resovled: " + mSignInError);
                                    mSignInProgress = GPlusSignInState.Default;
                                }
                            }).create().show();
        }
    }


    private void gPlusSignOut() {
        mFirebaseService.logout();

        // We clear the default account on sign out so that Google Play
        // services will not return an onConnected callback without user
        // interaction.
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
    }

    private FirebaseService getFirebaseService() {
        if (mFirebaseService != null) {
            return mFirebaseService;
        }

        mFirebaseService = new FirebaseService(mGoogleApiClient, this);
        mFirebaseService.setOnLoginStateChangedListener(new FirebaseService.OnLoginStateChangedListener() {
            @Override
            public void onLoginStateChanged(FirebaseService.LoginState state) {
                Log.i(TAG, "State Changed " + state.toString());
                switch (state) {
                    case LoggedIn:
                        // Update the user interface to reflect that the user is signed in.
//                        mSignInButton.setEnabled(false);
//                        mSignOutButton.setEnabled(true);
                        break;
                    case Default:
                        // Update the user interface to reflect that the user is signed in.
//                        mSignInButton.setEnabled(true);
//                        mSignOutButton.setEnabled(false);
                        break;
                }
            }
        });

        return mFirebaseService;
    }

    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        // Reaching onConnected means we consider the user signed in.

                        Log.i(TAG, "onConnected");

                        // Retrieve some profile information to personalize our app for the user.
                        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                        // Indicate that the sign in process is complete.
//                        mSignInProgress = STATE_DEFAULT;

                        mFirebaseService.login();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "onConnectionSuspended");
                        // The connection to Google Play services was lost for some reason.
                        // We call connect() to attempt to re-establish the connection or get a
                        // ConnectionResult that we can attempt to resolve.
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i(TAG, "onConnectionFailed");

                        /* onConnectionFailed is called when our Activity could not connect to Google
                         * Play services.  onConnectionFailed indicates that the user needs to select
                         * an account, grant permissions or resolve an error in order to sign in.
                         */

                        // Refer to the javadoc for ConnectionResult to see what error codes might
                        // be returned in onConnectionFailed.
                        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                                + connectionResult.getErrorCode());

                        if (mSignInProgress != GPlusSignInState.InProgress) {
                            // We do not have an intent in progress so we should store the latest
                            // error resolution intent for use when the sign in button is clicked.
                            mSignInIntent = connectionResult.getResolution();
                            mSignInError = connectionResult.getErrorCode();

                            if (mSignInProgress == GPlusSignInState.SignIn) {
                                // STATE_SIGN_IN indicates the user already clicked the sign in button
                                // so we should continue processing errors until the user is signed in
                                // or they click cancel.
                                gPlusSignIn();
                            }
                        }

                        // In this sample we consider the user signed out whenever they do not have
                        // a connection to Google Play services.
//                        onSignedOut();

                    }
                })
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = GPlusSignInState.SignIn;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = GPlusSignInState.Default;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
        }
    }
}
