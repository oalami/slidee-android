package com.firebase.slidee.android;

import com.firebase.client.Firebase;

public class FirebaseSlideeService {


    private static Firebase controlRef = new Firebase("https://slidee.firebaseio.com/control");
    private static Firebase currentRef = new Firebase("https://slidee.firebaseio.com/current");

    public FirebaseSlideeService() {

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
}
