package com.firebase.slidee.android;

import android.app.Activity;
import android.os.Bundle;
import com.firebase.slidee.android.slidee.R;

public class MainActivity extends Activity implements ButtonControlFragment.OnControlButtonClickedListener {
    private FirebaseSlideeService mFirebaseService = new FirebaseSlideeService();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ButtonControlFragment buttonControlFragment = (ButtonControlFragment) getFragmentManager().findFragmentById(R.id.button_control_fragment);
        buttonControlFragment.setOnControlButtonClickedListener(this);
    }

    @Override
    public void onControlButtonClicked(FirebaseSlideeService.Commands command) {
        mFirebaseService.pushCommand(command);
    }
}
