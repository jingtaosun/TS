package jp.ac.sinet.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private Button button;
    private EditText editText;
    private AsyncMessageWriter<String> writer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        final TextView textView = root.findViewById(R.id.text_home);

        button = root.findViewById(R.id.button);
        editText = root.findViewById(R.id.editText);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
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
        String message = editText.getText().toString();
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

}