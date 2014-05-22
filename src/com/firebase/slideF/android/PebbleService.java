package com.firebase.slideF.android;

import android.content.Context;
import android.os.Handler;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class PebbleService {
    private final static String TAG = "PebbleService";
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("bf928738-7ffb-44a0-a1b7-61309c1d69c8");
    private OnPebbleCommandReceivedListener listener;

    public interface OnPebbleCommandReceivedListener {
        void onPebbleCommandReceivedListener(Commands command);
    }

    public void setOnPebbleCommandReceivedListener(OnPebbleCommandReceivedListener listener) {
        this.listener = listener;
    }

    public PebbleService(Context context) {
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
                                commandReceived(Commands.Next);
                                break;
                            case 1:
                                break;
                            case 2:
                                commandReceived(Commands.Previous);
                                break;
                        }
                    }
                });

                PebbleKit.sendAckToPebble(context, transactionId);
            }
        });
    }

    private void commandReceived(Commands command) {
        if (listener != null) {
            listener.onPebbleCommandReceivedListener(command);
        }
    }
}
