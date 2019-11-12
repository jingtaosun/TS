package jp.ac.sinet.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import jp.ac.sinet.R;
import jp.ad.sinet.stream.android.AndroidMessageWriterFactory;
import jp.ad.sinet.stream.api.async.AsyncMessageWriter;
import jp.ad.sinet.stream.api.async.FailureCallback;
import jp.ad.sinet.stream.api.async.SuccessCallback;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private TextView config, current_hour, current_min,current_sen, set_hour, set_min, set_bar;
    private Button saveButton,button;
    private TimePickerDialog dialog;
    private SeekBar bar;
    private EditText ip,port,username,passwd;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        config = root.findViewById(R.id.text_home);
        saveButton = root.findViewById(R.id.save01);

        ip = root.findViewById(R.id.ip_text);
        port = root.findViewById(R.id.port_text);
        username = root.findViewById(R.id.username_text);
        passwd = root.findViewById(R.id.passwd_text);

        current_hour = root.findViewById(R.id.hour01);
        current_min = root.findViewById(R.id.min01);
        current_sen= root.findViewById(R.id.second01);

        set_hour = root.findViewById(R.id.sethour01);
        set_min = root.findViewById(R.id.setmin01);

        button = root.findViewById(R.id.dialog01);

        bar = root.findViewById(R.id.seekbar01);
        set_bar = root.findViewById(R.id.set_bar01);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                config.setText(s);
            }
        });


        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        set_bar.setText("Setting Value(Second) : "+bar.getProgress());

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                set_bar.setText("Setting Value(Second) : "+bar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog = new TimePickerDialog(
                getContext(), AlertDialog.THEME_HOLO_LIGHT,
                new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String[] set_times = String.format("%02d:%02d", hourOfDay,minute).split(":",0);
                        set_hour.setText(set_times[0]);
                        set_min.setText(set_times[1]);
                    }
                },
                hour,minute,true);

        final Handler someHandler = new Handler(getActivity().getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String[] time = new SimpleDateFormat("HH:mm:ss", Locale.JAPAN).format(new Date()).split(":", 0);
                current_hour.setText(time[0]);
                current_min.setText(time[1]);
                current_sen.setText(time[2]);
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("button", saveButton.toString());
            }
        });

        return root;
    }


}