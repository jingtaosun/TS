package jp.ac.sinet;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class NIIMainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        initConfigFile();

    }


    private void initConfigFile() {
        InputStream src = getResources().openRawResource(R.raw.sinetstream_config);
        File dest = new File(getApplicationContext().getFilesDir(), "sinetstream_config.yml");
        Log.d("tag_file",dest.getAbsolutePath());
        try {
            Files.copy(src, dest.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            Log.d("DEBUG", "SINETStreamの設定ファイルの配置に失敗した", e);
        }
    }

}
