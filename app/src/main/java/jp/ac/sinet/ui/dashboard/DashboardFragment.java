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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
import jp.ac.sinet.ui.home.HomeFragment;
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
    private List<Sensor> sensor_light,sensor_accle,sensor_pressure;
    private Bundle bundle;
    private int time_process;
    private int current_hour,current_min, setting_hour, setting_min;
    private int bar_value;

//    StringBuilder sendor_value = new StringBuilder();

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
//        if (sensorManager!=null) {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sensor_light = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
            sensor_accle = sensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER);
            sensor_pressure = sensorManager.getSensorList(Sensor.TYPE_PRESSURE);

            if (sensor_light.size() > 0) {
                Sensor s = sensor_light.get(0);
                sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (sensor_accle.size() > 0) {
                Sensor s = sensor_accle.get(0);
                sensorManager.registerListener(this, s, (int) 1e6);
            }

            if (sensor_pressure.size() > 0) {
                Sensor s = sensor_pressure.get(0);
                sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        String[] current_time = new SimpleDateFormat("HH:mm:ss", Locale.JAPAN).format(new Date()).split(":", 0);
        current_hour = Integer.parseInt(current_time[0]);
        current_min = Integer.parseInt(current_time[1]);

        bundle = getArguments();
        if (bundle!=null){
//            time_infor = bundle.getString("time");
//            Log.d("timessss", time_infor);
            String[] setting_time = bundle.getString("time").split(",",0);
            bar_value = Integer.parseInt(setting_time[0]);
            setting_hour = Integer.parseInt(setting_time[1]);
            setting_min = Integer.parseInt(setting_time[2]);
        }
        time_process = ((setting_hour-current_hour)*60+(setting_min-current_min)*60)/bar_value;

        Log.d("tagg", current_hour+":"+current_min+":"+setting_hour+":"+setting_min+":"+bar_value+":"+time_process);

        if (writer == null) {
            writer = new AndroidMessageWriterFactory.Builder<String>().service("service-1").context(activity).build().getAsyncWriter();
        }

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("tagf",String.valueOf(sensorItemList.get(0).getValue()));


//                sendMessage(String.valueOf(sensorItemList.get(0).getValue()));
                int i = 0;
                final StringBuilder stringBuilder = new StringBuilder();
                for (SensorItem item: sensorItemList){
                    if (item.checkbox){
                        stringBuilder.append(i);
                        stringBuilder.append(",");
                        stringBuilder.append(sensorItemList.get(i).getValue());
                        stringBuilder.append(",");
                    }
                    i++;
                }

                        if (time_process>0) {
                            for (int j = 0; j < time_process; j++) {
                                sendMessage(stringBuilder.toString());
                                Log.d("tagg", stringBuilder.toString()+":"+j);
                            }
                            Toast.makeText(getContext(),"Finish",Toast.LENGTH_LONG).show();
                        }else if(time_process == 0){
                            sendMessage(stringBuilder.toString());
//                            Toast.makeText(getContext(),"Finish",Toast.LENGTH_LONG).show();

                        }
//                Toast.makeText(getContext(),stringBuilder.toString()+":"+time_process,Toast.LENGTH_LONG).show();
//                sendMessage(stringBuilder.toString());

            }
        });
    }

    private void sendMessage(String text) {


        MessageJson info = new MessageJson();
        info.time = info.timezone("Asia/Tokyo");
        String[] strings = text.split(",");
        info.value = Double.parseDouble(strings[1]);

        Log.d("time",info.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            message = mapper.writeValueAsString(info);
            Log.d("time",message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
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
        };
        timer.schedule(task, bar_value*10000);

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
               final String light_value = String.valueOf(sensorEvent.values[0]);
                if (!sensorItemList.isEmpty()) {
                    for (SensorItem item : sensorItemList) {
                        if (item.getSensorTopic().equals("mqtt-android-light")) {
                            item.setValue(light_value);

//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        Thread.sleep(bar_value*100);
//                                        sendor_value.append(light_value);
//                                    }catch(InterruptedException e){
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }).start();
//                            Toast.makeText(getContext(),"Finish",Toast.LENGTH_LONG).show();
                        }
                        if (getActivity() != null) {
                            updateListView();
                        }
                    }
                }
                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                double accel_value_x = sensorEvent.values[SensorManager.DATA_X];
//                double accel_value_y = sensorEvent.values[SensorManager.DATA_Y];
//                double accel_value_z = sensorEvent.values[SensorManager.DATA_Z];
//
//                if (!sensorItemList.isEmpty()) {
//                    for (SensorItem item : sensorItemList) {
//                        if (item.getSensorTopic().equals("mqtt-android-accelerometer")) {
//                            DecimalFormat fmt = new DecimalFormat("##0.0");
//                            item.setValue("x:" + fmt.format(accel_value_x) + ",y:" + fmt.format(accel_value_y) + ",z:" + fmt.format(accel_value_z));
//                        }
//                        if (getActivity() != null) {
//                            updateListView();
//                        }
//                    }
//                }
//                break;
            case Sensor.TYPE_STEP_COUNTER:
                String step_counter = String.valueOf(sensorEvent.values[0]);
                if (!sensorItemList.isEmpty()) {
                    for (SensorItem item : sensorItemList) {
                        if (item.getSensorTopic().equals("mqtt-android-stepcounter")) {
                            item.setValue("" + step_counter);
                        }
                        if (getActivity() != null) {
                            updateListView();
                        }
                    }
                }
                break;

            case Sensor.TYPE_PRESSURE:
                String pressure = String.valueOf(sensorEvent.values[0]);
                if (!sensorItemList.isEmpty()) {
                    for (SensorItem item : sensorItemList) {
                        if (item.getSensorTopic().equals("mqtt-android-pressure")) {
                            item.setValue("" + pressure);
                        }
                        if (getActivity() != null) {
                            updateListView();
                        }
                    }
                }
                break;
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