package com.firebase.slidee.android;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class PebbleSlideeService {
    private final static String TAG = "PebbleSlideeService";
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("bf928738-7ffb-44a0-a1b7-61309c1d69c8");
    private OnPebbleCommandReceivedListener listener;

    public interface OnPebbleCommandReceivedListener {
        void onPebbleCommandReceivedListener(SlideeCommands command);
    }

    public void setOnPebbleCommandReceivedListener(OnPebbleCommandReceivedListener listener) {
        this.listener = listener;
    }

    public PebbleSlideeService(Context context) {
        if (PebbleKit.isWatchConnected(context)) {
            PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);
        }


        final Handler handler = new Handler();
        PebbleKit.registerReceivedDataHandler(context, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                final int command = data.getInteger(0).intValue();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (command) {
                            case 0:
                                commandReceived(SlideeCommands.Next);
                                break;
                            case 1:
                                break;
                            case 2:
                                commandReceived(SlideeCommands.Previous);
                                break;
                        }
                    }
                });

                PebbleKit.sendAckToPebble(context, transactionId);
            }
        });
    }

    private void commandReceived(SlideeCommands command) {
        if (listener != null) {
            listener.onPebbleCommandReceivedListener(command);
        }
    }
}
