package com.example.studentscoremanager.util;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    public interface OnPositionSelected {
        void onSelected(int position);
    }

    private final OnPositionSelected onPositionSelected;

    public SimpleItemSelectedListener(OnPositionSelected onPositionSelected) {
        this.onPositionSelected = onPositionSelected;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (onPositionSelected != null) {
            onPositionSelected.onSelected(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

