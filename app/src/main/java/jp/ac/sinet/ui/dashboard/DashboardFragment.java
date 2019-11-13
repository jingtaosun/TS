package jp.ac.sinet.ui.dashboard;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import jp.ac.sinet.MessageJson;
import jp.ac.sinet.NIIMainActivity;
import jp.ac.sinet.R;
import jp.ac.sinet.sensormodel.AddNewSensorItemDialogFragment;
import jp.ac.sinet.sensormodel.SensorItem;
import jp.ac.sinet.sensormodel.SensorItemAdapter;
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

    private FloatingActionButton addSensorButton,publishButton;
    private ListView listView;
    private List<SensorItem> sensorItemList = new ArrayList<>();
    private SensorItem sensorItem;

    protected FragmentActivity mActivity;
    private List<Sensor> sensor_light,sensor_accle,sensor_gravity;



    public static final int DIALOG_FRAGMENT = 200;
    public static final int RESULT_OK = -1;



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

        addSensorButton = root.findViewById(R.id.add_fab);
        publishButton = root.findViewById(R.id.publish_fab);
        listView = root.findViewById(R.id.sensor_list);

        SensorItemAdapter adapter = new SensorItemAdapter(getActivity(), sensorItemList);
        listView.setAdapter(adapter);

        addSensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewSensorItemDialogFragment newListenerDialogFragment = new AddNewSensorItemDialogFragment();
                Log.d("fragment", "onClick: " + DashboardFragment.this.getTag());
                Log.d("fragment", "onClick: " + getActivity().getSupportFragmentManager().findFragmentByTag("SettingFragment"));
                newListenerDialogFragment.setTargetFragment(DashboardFragment.this, DIALOG_FRAGMENT);
                newListenerDialogFragment.show(getFragmentManager().beginTransaction(), "add new listener dialog");
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor_light = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        sensor_accle = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        sensor_gravity = sensorManager.getSensorList(Sensor.TYPE_GRAVITY);

        if (sensor_light.size() > 0) {
            Sensor s = sensor_light.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
        }

        if (sensor_accle.size() > 0) {
            Sensor s = sensor_accle.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (sensor_gravity.size() > 0) {
            Sensor s = sensor_gravity.get(0);
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
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

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("tagf",String.valueOf(sensorItemList.get(0).getValue()));


//                sendMessage(String.valueOf(sensorItemList.get(0).getValue()));
                int i = 0;
                StringBuilder stringBuilder = new StringBuilder();
                for (SensorItem item: sensorItemList){
                    if (item.checkbox){
                        stringBuilder.append(i);
                        stringBuilder.append(",");
                        stringBuilder.append(sensorItemList.get(i).getValue());
                        stringBuilder.append(",");
                    }
                    i++;
                }
//                sendMessage(stringBuilder.toString());

                Log.d("tagg", stringBuilder.toString());
                Toast.makeText(getContext(),stringBuilder.toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMessage(String text) {
        MessageJson info = new MessageJson();
        info.time = info.timezone("Asia/Tokyo");
        info.value = Double.parseDouble(text);

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
                String light_value = String.valueOf(sensorEvent.values[0]);
                if (!sensorItemList.isEmpty()){
                    for (SensorItem item : sensorItemList) {
                        if (item.getSensorTopic().equals("mqtt-android-light")){
                            item.setValue(light_value);
                        }
                        if (getActivity()!= null) {
                            updateListView();
                        }
                    }
                }
                break;
//            case Sensor.TYPE_ACCELEROMETER:
////                String str = sensorEvent.values[SensorManager.DATA_X] + "," + sensorEvent.values[SensorManager.DATA_Y] + "," + sensorEvent.values[SensorManager.DATA_Z];
//                double accel_value_x = sensorEvent.values[SensorManager.DATA_X];
//                double accel_value_y = sensorEvent.values[SensorManager.DATA_Y];
//                double accel_value_z = sensorEvent.values[SensorManager.DATA_Z];
//
//                if (!sensorItemList.isEmpty()){
//                    for (SensorItem item : sensorItemList) {
//                        if (item.getSensorTopic().equals("mqtt-android-accelerometer")){
//                            DecimalFormat fmt = new DecimalFormat("##0.0");
//
//                            item.setValue("x:"+fmt.format(accel_value_x)+",y:"+fmt.format(accel_value_y)+",z:"+fmt.format(accel_value_z));
//                        }
//                        if (getActivity()!= null) {
//                            updateListView();
//                        }
//                    }
//                }
//                break;
//            case Sensor.TYPE_GRAVITY:
////                String str = sensorEvent.values[SensorManager.DATA_X] + "," + sensorEvent.values[SensorManager.DATA_Y] + "," + sensorEvent.values[SensorManager.DATA_Z];
//                String gravity = String.valueOf(sensorEvent.values[0]);
//
//                if (!sensorItemList.isEmpty()){
//                    for (SensorItem item : sensorItemList) {
//                        if (item.getSensorTopic().equals("mqtt-android-gravity")){
//
//                            item.setValue(""+gravity);
//                        }
//                        if (getActivity()!= null) {
//                            updateListView();
//                        }
//                    }
//                }
//                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == DIALOG_FRAGMENT) {
            if (resultCode == RESULT_OK) {
                if (intent.getStringExtra("action").equals("add")) {
                    sensorItem = new SensorItem(intent.getStringExtra("name"),
                            intent.getStringExtra("topic"),
                            intent.getStringExtra("type"),
                            intent.getIntExtra("qos", 0),
                            intent.getBooleanExtra("retained", true));
                    Log.d("sensor", "onActivityResult: " + sensorItem.toString());
                    sensorItemList.add(sensorItem);
                    updateListView();
                } else if (intent.getStringExtra("action").equals("edit")) {
                    sensorItem = sensorItemList.get(intent.getIntExtra("position", 0));
                    sensorItem.setSensorName(intent.getStringExtra("name"));
                    sensorItem.setSensorTopic(intent.getStringExtra("topic"));
                    sensorItem.setSensorType(intent.getStringExtra("type"));
                    sensorItem.setQos(intent.getIntExtra("qos", 0));
                    sensorItem.setRetained(intent.getBooleanExtra("retained", true));
                    sensorItemList.set(intent.getIntExtra("position", 0), sensorItem);
                    updateListView();
                } else if (intent.getStringExtra("action").equals("delete")) {
                    sensorItemList.remove(intent.getIntExtra("position", 0));
                    updateListView();
                }
            }
        }
    }

    private void updateListView() {
        listView.setAdapter(new SensorItemAdapter(getActivity(), sensorItemList));
        listView.invalidate();
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity =(FragmentActivity) context;
        }
    }

}