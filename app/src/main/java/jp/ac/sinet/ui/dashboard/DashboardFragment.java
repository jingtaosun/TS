package jp.ac.sinet.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import jp.ac.sinet.MessageJson;
import jp.ac.sinet.R;
import jp.ad.sinet.stream.android.AndroidMessageWriterFactory;
import jp.ad.sinet.stream.api.async.AsyncMessageWriter;
import jp.ad.sinet.stream.api.async.FailureCallback;
import jp.ad.sinet.stream.api.async.SuccessCallback;

public class DashboardFragment extends Fragment implements SensorEventListener{

    private DashboardViewModel dashboardViewModel;

    private SensorManager sensorManager;

    private TextView textView;

    private Button button;
    private EditText editText;
    private AsyncMessageWriter<String> writer;
    private String message;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        button = root.findViewById(R.id.button);
        editText = root.findViewById(R.id.editText);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        // 照度センサーを指定してオブジェクトリストを取得する
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);

        // 照度センサーがサポートされているか確認してから登録する
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (writer == null) {
            writer = new AndroidMessageWriterFactory.Builder<String>().service("service-1").context(activity).build().getAsyncWriter();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(editText);
            }
        });
    }

    private void sendMessage(EditText editText) {
        if (editText == null) {
            return;
        }
        MessageJson info = new MessageJson();
        info.time = info.timezone("Asia/Tokyo");
        info.sensor = editText.getText().toString();

        Log.d("time",info.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            message = mapper.writeValueAsString(info);
            Log.d("time",message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.write(message).addCallback(new SuccessCallback<String>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable String s) {
                Log.d("debug", "PUBLISH SUCCESS");
            }
        }, new FailureCallback<String>() {
            @Override
            public void onFailure(String s, Throwable throwable) {
                Log.d("debug", "PUBLISH FAILURE", throwable);
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()){
            case Sensor.TYPE_LIGHT:
                // 現在の明るさを取得
                int light_value = (int)(sensorEvent.values[0]);
                editText.setText(String.valueOf(light_value));
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}