package uk.ac.shef.dcs.travelguider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import uk.ac.shef.dcs.travelguider.databinding.MainActivityBinding;
import uk.ac.shef.dcs.travelguider.utils.PermissionHelper;
import uk.ac.shef.dcs.travelguider.viewModels.MapViewModel;
import uk.ac.shef.dcs.travelguider.views.GalleryFragment;
import uk.ac.shef.dcs.travelguider.views.MapFragment;

public class MainActivity extends AppCompatActivity {
    private MainActivityBinding binding;
    private MapViewModel viewModel;
    BottomNavigationView bottomNavigationView;
    private LiveData<NavController> currentNavController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // For network connection
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        // Init the viewModel and request permissions
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.init(this);
        PermissionHelper permissionHelper = new PermissionHelper();
        permissionHelper.requestAllPermission(this);
        setContentView(R.layout.main_activity);

        // Init bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        NavController currentController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(bottomNavigationView, currentController);

        // Night mode button init
        FloatingActionButton themeChanger = (FloatingActionButton) this.findViewById(R.id.theme_changer);
        themeChanger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int nightModeFlags =
                        getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;

                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        break;
                }
            }
        });
        viewModel.getVisitStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean) {
                    bottomNavigationView.setEnabled(true);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    themeChanger.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setEnabled(false);
                    bottomNavigationView.setVisibility(View.INVISIBLE);
                    themeChanger.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "You may not be able to view " +
                            "gallery/history until stop the current visit", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String[] locationPermission = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        for (String permission : locationPermission) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                viewModel.setLocationPermissionStatus(-1);
                return;
            }
            viewModel.setLocationPermissionStatus(1);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please start the visit before upload photos" +
                    " otherwise you may not be able to track your visit", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableBottomBar(boolean enable){
        bottomNavigationView.setEnabled(enable);
    }

    // Handle image picked
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        viewModel.easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                if (viewModel.getVisitStatus().getValue()) {
                    File file = null;
                    try {
                        String dir = MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString();
                        File fileDir = new File(dir);
                        if (!fileDir.exists()) {
                            fileDir.mkdir();
                        }
                        String filename = "TG" + imageFiles[0].getFile().getName();
                        file = new File(dir, filename);
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[0].getFile().getAbsolutePath(),bmOptions);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    viewModel.setMediaFiles(file.getPath());
                    System.out.println(file.getPath());
                } else {
                    Toast.makeText(MainActivity.this, "Please start the visit before upload photos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }
}