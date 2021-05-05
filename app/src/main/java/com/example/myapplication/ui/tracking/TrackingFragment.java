package com.example.myapplication.ui.tracking;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class TrackingFragment extends Fragment {
    TextView calendarDate;
    TextView visitedLocation;

    public TrackingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_tracking, container, false);
        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                showBottomSheetDialog(year, month, dayOfMonth);
            }
        });

        return view;
    }

    private void showBottomSheetDialog(int year, int monthNum, int dayOfMonth) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet);

        calendarDate = bottomSheetDialog.findViewById(R.id.calendar_date);
        visitedLocation = bottomSheetDialog.findViewById(R.id.visited_location);

        String month;
        switch(monthNum) {
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;
            default:
                month = "Unknown";
        }

        String date = String.format("%s %s %s", dayOfMonth, month, year);
        calendarDate.setText(date);
        visitedLocation.setText("Los Angeles");

        bottomSheetDialog.show();
    }
}