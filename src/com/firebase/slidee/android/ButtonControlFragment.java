package com.firebase.slidee.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.firebase.slidee.android.slidee.R;

public class ButtonControlFragment extends Fragment {
    private OnControlButtonClickedListener onControlButtonClickedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.button_control_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button mNextButton = (Button) this.getActivity().findViewById(R.id.buttonNext);
        Button mPrevButton = (Button) this.getActivity().findViewById(R.id.buttonPrev);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlButtonClicked(SlideeCommands.Next);
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlButtonClicked(SlideeCommands.Previous);
            }
        });
    }

    private void controlButtonClicked(SlideeCommands command) {
        if(this.onControlButtonClickedListener != null) {
            onControlButtonClickedListener.onControlButtonClicked(command);
        }
    }

    public void setOnControlButtonClickedListener(OnControlButtonClickedListener listener) {
        this.onControlButtonClickedListener = listener;
    }


    public interface OnControlButtonClickedListener {
        public void onControlButtonClicked(SlideeCommands command);
    }
}
