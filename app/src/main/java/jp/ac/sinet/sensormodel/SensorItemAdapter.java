package jp.ac.sinet.sensormodel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import jp.ac.sinet.NIIMainActivity;
import jp.ac.sinet.R;
import jp.ac.sinet.ui.dashboard.DashboardFragment;


public class SensorItemAdapter extends ArrayAdapter<SensorItem> {

    private NIIMainActivity activity;
    private List<SensorItem> sensorItems;
    private LayoutInflater mInflater;


    public SensorItemAdapter(@NonNull Activity activity, List<SensorItem> sensorItems) {
        super(activity, R.layout.sensor_list_item, sensorItems);
        // TODO Auto-generated constructor stub

        this.activity = (NIIMainActivity) activity;
        this.sensorItems = sensorItems;
        mInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View mView;

        if (convertView != null) {
            mView = convertView;
        }
        else {
            mView = mInflater.inflate(R.layout.sensor_list_item, null);
        }

        // リストビューに表示する要素を取得
        final SensorItem item = sensorItems.get(position);

        TextView nameText = mView.findViewById(R.id.sensor_item_name);
        TextView topicText = mView.findViewById(R.id.sensor_item_topic);
        TextView typeText = mView.findViewById(R.id.sensor_item_type);
        TextView valueText = mView.findViewById(R.id.sensor_value);

        ImageButton detailButton = mView.findViewById(R.id.sensor_item_detail_button);

        Switch sendSwitch = mView.findViewById(R.id.check_switch);
        sendSwitch.setTag(position);
        sendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("check", "onClick: checkBox: "+ isChecked + " at No."+ buttonView.getTag());
                item.checkbox = isChecked;
            }
        });
        sendSwitch.setChecked(item.checkbox);

        nameText.setText(item.getSensorName());
        topicText.setText(item.getSensorTopic());
        typeText.setText(item.getSensorType());
        valueText.setText("" + item.getValue());

        Log.d("detail", "getView: "+ activity.getLocalClassName());

        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("detail", "onClick: clicked at No."+ position);
                AddNewSensorItemDialogFragment newListenerDialogFragment = new AddNewSensorItemDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("position",position);
                bundle.putString("name", item.getSensorName());
                bundle.putString("topic", item.getSensorTopic());
                bundle.putString("type", item.getSensorType());
                bundle.putInt("qos", item.getQos());
                bundle.putBoolean("retained", item.isRetained());
                newListenerDialogFragment.setArguments(bundle);
                Log.d("sensor", "onClick: "+ item.toString());
                //Log.d("fragment", "onClick: " + activity.getSupportFragmentManager().getFragments().get(0).getTag());
                newListenerDialogFragment.setTargetFragment(activity.getSupportFragmentManager().findFragmentByTag("SettingFragment"), DashboardFragment.DIALOG_FRAGMENT);
                newListenerDialogFragment.show(activity.getSupportFragmentManager().beginTransaction(), "add new listener dialog");
            }
        });

        return mView;
    }
}
