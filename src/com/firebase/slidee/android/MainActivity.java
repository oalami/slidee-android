package com.firebase.slidee.android;

import android.app.Activity;
import android.os.Bundle;
import com.firebase.slidee.android.slidee.R;

public class MainActivity extends Activity implements ButtonControlFragment.OnControlButtonClickedListener, PebbleSlideeService.OnPebbleCommandReceivedListener {
    private FirebaseSlideeService mFirebaseService = new FirebaseSlideeService();
    private PebbleSlideeService mPebbleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButtonControlFragment buttonControlFragment = (ButtonControlFragment) getFragmentManager().findFragmentById(R.id.button_control_fragment);
        buttonControlFragment.setOnControlButtonClickedListener(this);

        mPebbleService = new PebbleSlideeService(getApplicationContext());
        mPebbleService.setOnPebbleCommandReceivedListener(this);
    }

    @Override
    public void onControlButtonClicked(SlideeCommands command) {
        mFirebaseService.pushCommand(command);
    }

    @Override
    public void onPebbleCommandReceivedListener(SlideeCommands command) {
        mFirebaseService.pushCommand(command);
    }
}
