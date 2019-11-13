package jp.ac.sinet.ui.notifications;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import jp.ac.sinet.R;
import jp.ad.sinet.stream.android.AndroidMessageReaderFactory;
import jp.ad.sinet.stream.api.Message;
import jp.ad.sinet.stream.api.async.AsyncMessageReader;
import jp.ad.sinet.stream.api.async.MessageCallback;


public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    private AsyncMessageReader<String> reader;
    private TextView reader_iot;
    private Button button_iot;

    String url = "https://vcc3056.vcp-handson.org/kibana/goto/2659afdf4ab21eca910236d2cc013203";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);

        reader_iot = root.findViewById(R.id.textView_iot);
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        button_iot = root.findViewById(R.id.button_iot);
        button_iot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                // Note the Chooser below. If no applications match,
                // Android displays a system message.So here there is no need for try-catch.
                startActivity(Intent.createChooser(intent, "Browse with"));
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (reader == null) {
            reader = new AndroidMessageReaderFactory.Builder<String>().service("service-1").context(activity).build().getAsyncReader();
            final Handler handler = new Handler();
            reader.addCallback(new MessageCallback<String>() {
                @Override
                public void onMessage(final Message<String> message) {
                    Log.d("message", message.getValue());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            reader_iot.setText(message.getValue());
                            Log.d("message", message.getValue());
                        }
                    });
                }
            });
                }


//        final Handler handler = new Handler();
//        final Runnable r = new Runnable() {
//            int count = 0;
//            @Override
//            public void run() {
//                // UIスレッド
//                count++;
//                if (count > 5) { // 5回実行したら終了
//                    return;
//                }
//                Log.d("handler",""+count);
//                handler.postDelayed(this, 10000);
//            }
//        };
//        handler.post(r);


    }


}