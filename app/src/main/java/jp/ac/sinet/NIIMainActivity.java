package jp.ac.sinet;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import jp.ac.sinet.ui.dashboard.DashboardFragment;
import jp.ac.sinet.ui.home.HomeFragment;
import jp.ac.sinet.ui.notifications.NotificationsFragment;


import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class NIIMainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener{


    private HomeFragment homeFragment;
    private DashboardFragment settingFragment;
    private NotificationsFragment resultFragment;
    String time_information;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.navigation);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        homeFragment = new HomeFragment();
        settingFragment = new DashboardFragment();
        resultFragment = new NotificationsFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment,"HomeFragment").commit();
                        return true;
                    case R.id.navigation_dashboard:
                        if (time_information!=null){
                            Log.d("tttttt",time_information);
                            Bundle bundle = new Bundle();
                            bundle.putString("time",time_information);
                            settingFragment.setArguments(bundle);
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, settingFragment,"SettingFragment").commit();
                        return true;
                    case R.id.navigation_notifications:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, resultFragment,"ResultFragment").commit();
                        return true;
                }
                return false;
            }
        });

        initConfigFile();

    }


    private void initConfigFile() {
        InputStream src = getResources().openRawResource(R.raw.sinetstream_config);
        File dest = new File(getApplicationContext().getFilesDir(), "sinetstream_config.yml");
        Log.d("tag_file",dest.getAbsolutePath());
        try {
            Files.copy(src, dest.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            Log.d("DEBUG", "The configuration of SINETStream is failure", e);
        }
    }

    @Override
    public void onFragmentInteractionFromHomeToMain(String time) {
        time_information = time;
        Log.d("tagggggg", time_information);
    }
}
