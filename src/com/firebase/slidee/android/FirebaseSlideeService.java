package com.firebase.slidee.android;

import com.firebase.client.Firebase;

public class FirebaseSlideeService {
    public enum Commands {
        Next,
        Previous
    }


    private static Firebase controlRef = new Firebase("https://slidee.firebaseio.com/control");
    private static Firebase currentRef = new Firebase("https://slidee.firebaseio.com/current");

    public FirebaseSlideeService() {

    }

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

    private void pushControlCommand(String command) {
        controlRef.push().setValue(command);
    }
}
