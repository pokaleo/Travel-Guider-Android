package uk.ac.shef.dcs.travelguider.viewModels;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import pl.aprilapps.easyphotopicker.EasyImage;
import uk.ac.shef.dcs.travelguider.MainActivity;
import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.database.UserDatabase;
import uk.ac.shef.dcs.travelguider.database.VisitBean;
import uk.ac.shef.dcs.travelguider.utils.ImageHelper;
import uk.ac.shef.dcs.travelguider.utils.JsonHelper;
import uk.ac.shef.dcs.travelguider.utils.PermissionHelper;
import uk.ac.shef.dcs.travelguider.views.ImageView;

public class MapViewModel extends ViewModel {
    public EasyImage easyImage;
    private Context context;
    private GoogleMap map;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng defaultLocation = new LatLng(-34, 151);
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PermissionHelper permissionHelper;
    private Polyline line;
    private String duration;
    private VisitBean visitBean;
    private Handler weatherHandler;
    private Handler timerHandler;
    private Runnable runnable;
    private Runnable runnableTimer;
    private Long counter;

    public void setDuration(String duration) {
        this.duration = duration;
    }

    private MutableLiveData<Integer> locationPermissionStatus;
    private MutableLiveData<Boolean> visitStatus;
    private MutableLiveData<String> title;
    private MutableLiveData<String> temperature;
    private MutableLiveData<String> pressure;

    public void setTimer(MutableLiveData<String> timer) {
        this.timer = timer;
    }

    public MutableLiveData<String> getTimer() {
        return timer;
    }

    private MutableLiveData<String> timer;

    public MutableLiveData<String> getTemperature() {
        return temperature;
    }

    public MutableLiveData<String> getPressure() {
        return pressure;
    }

    public void setTemperature(String temperature) {
        this.temperature.setValue(temperature);
    }


    public void setPressure(String pressure) {
        this.pressure.setValue(pressure);
    }

    private FollowMeLocationSource followMeLocationSource;

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        followMeLocationSource = new FollowMeLocationSource();
        this.map = map;
        locate(map);
    }

    public void init(Context context) {
        permissionHelper = new PermissionHelper();
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
        this.context = context;
        locationPermissionStatus = new MutableLiveData<Integer>(0);
        visitStatus = new MutableLiveData<Boolean>(false);
        title = new MutableLiveData<>(" ");
        temperature = new MutableLiveData<>(" ");
        pressure = new MutableLiveData<>(" ");
        timer = new MutableLiveData<>(" ");
        easyImage = new EasyImage.Builder(context)
                .setCopyImagesToPublicGalleryFolder(true)
                .setFolderName("TravelGuider")
                .build();
    }

    public MutableLiveData<Integer> getLocationPermissionStatus() {
        return locationPermissionStatus;
    }

    public void setLocationPermissionStatus(int statusCode) {
        this.locationPermissionStatus.setValue(statusCode);
    }

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public MutableLiveData<Boolean> getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(boolean visitStatus) {
        this.visitStatus.setValue(visitStatus);
    }

    public void startVisit() {
        setTitle();
        // permissionHelper.requestBackgroundLocationPermission(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Please grant permission to proceed", Toast.LENGTH_SHORT).show();
        }
        visitBean = new VisitBean();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        visitBean.setDate(currentDate);
        locate(map);
        followMeLocationSource.locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        10, followMeLocationSource);
        // Retrieve data info
        if (lastKnownLocation != null) {
            retrieveWeatherData(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
        }
        weatherHandler= new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                retrieveWeatherData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                weatherHandler.postDelayed(this, 20000);
            }
        };
        weatherHandler.postDelayed(runnable, 20000);
        // Init timer
        counter = Long.valueOf(0);
        timerHandler = new Handler(Looper.getMainLooper());
        runnableTimer = new Runnable() {
            @Override
            public void run() {
                counter++;
                long day = (counter / (24 * 60 * 60));
                long hour = (counter / (60 * 60) - day * 24);
                long min = ((counter / (60)) - day * 24 * 60 - hour * 60);
                long s = (counter - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

                if (day > 0) {
                    timer.setValue(day + "day " + hour + ": " + min + ": " + s + " ");
                }else if (hour > 0)
                    timer.setValue(hour + ": " + min + ": " + s + " ");
                else if (min > 0)
                    timer.setValue("00:" + min + ": " + s);
                else if (s > 0)
                    timer.setValue("00:00:" + s);
                else
                    timer.setValue("00:");
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(runnableTimer, 1000);
    }

    public void stopVisit() {
        visitBean.setTitle(title.getValue());
        visitBean.setDuration(duration);
        UserDatabase.getInstance(context).getVisitDAO().insertData(visitBean);
        if (line != null) {
            line.remove();
        }
        try {
            followMeLocationSource.deactivate();
            timerHandler.removeCallbacks(runnableTimer);
            weatherHandler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.clear();
        title.setValue(" ");
    }

    public void openCamera() {
        easyImage.openCameraForImage((Activity) context);
    }

    public void openGallery() {
        easyImage.openGallery((Activity) context);
    }

    public void locate(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionHelper.requestLocationPermission(context);
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener((Activity) context, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.getResult();
                    if (lastKnownLocation != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    }
                } else {
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });
    }

    // Prompt user to enter a title
    public void setTitle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please enter a title for this visit");

        final EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText() != null) {
                    setTitle(input.getText().toString());
                } else {
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Display image on map and store it
    public void setMediaFiles(String path) {
        if (path!=null&&path.length()>0){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(path,bmOptions);



            MarkerOptions markerOptions=new MarkerOptions();
            BitmapDescriptor bitmapDescriptor= BitmapDescriptorFactory.fromBitmap(ImageHelper.changeBitmapSize(bitmap));
            PhotoBean photoBean = new PhotoBean();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            // Insert to DB
            photoBean.setDate(currentDate);
            photoBean.setPath(path);
            if (visitBean != null) {
                visitBean.setPath(path);
            }
            photoBean.setLatitude(String.valueOf(lastKnownLocation.getLatitude()));
            photoBean.setLongitude(String.valueOf(lastKnownLocation.getLongitude()));
            if (title.getValue() != null) {
                photoBean.setTitle(title.getValue());
            } else {
                photoBean.setTitle(" ");
            }
            UserDatabase.getInstance(context).getPhotoDAO().insertData(photoBean);
            // Display it on the map
            markerOptions.icon(bitmapDescriptor);
            markerOptions.position(new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude()));
            Marker marker=  map.addMarker(markerOptions);
            assert marker != null;
            marker.setTag(path);
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    Log.e("tag",marker.getTag().toString());
                    context.startActivity(new Intent(context, ImageView.class)
                            .putExtra("imagePath",marker.getTag().toString())
                    );
                    return true;
                }

            });

        }
    }

    // Retrieve temperature * pressure from OpenWeather API
    private void retrieveWeatherData(double latitude, double longitude) {
        String api = "https://api.openweathermap.org/data/2.5/weather?lat=" +
                latitude + "&lon=" + longitude +
                "&appid=1281ebc612ad849bb4f547f95fa9c92d";
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                InputStream ips = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null){
                    result.append(line);
                }
                Map<String, Object> apiResult = JsonHelper.jsonToMap(result.toString());
                Map<String, Object> weatherData = JsonHelper.jsonToMap(apiResult.get("main").toString());
                setTemperature(weatherData.get("feels_like").toString() + " °F");
                setPressure(weatherData.get("pressure").toString() + " Pa");
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // A class handling the position tracking
    private class FollowMeLocationSource implements LocationSource, LocationListener {

        private LocationSource.OnLocationChangedListener mListener;
        private LocationManager locationManager;
        private final Criteria criteria = new Criteria();
        private String bestAvailableProvider;
        /* Updates are restricted to one every 10 seconds, and only when
         * movement of more than 10 meters has been detected.*/
        private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
        private final int minDistance = 10;    // minimum distance between location updates, in meters

        private FollowMeLocationSource() {
            // Get reference to Location Manager
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // Specify Location Provider criteria
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);
        }

        private void getBestAvailableProvider() {
            /* The preferred way of specifying the location provider (e.g. GPS, NETWORK) to use
             * is to ask the Location Manager for the one that best satisfies our criteria.
             * By passing the 'true' boolean we ask for the best available (enabled) provider. */
            bestAvailableProvider = locationManager.getBestProvider(criteria, true);
        }

        /* Activates this provider. This provider will notify the supplied listener
         * periodically, until you call deactivate().
         * This method is automatically invoked by enabling my-location layer. */
        @Override
        public void activate(@NonNull OnLocationChangedListener listener) {
            // We need to keep a reference to my-location layer's listener so we can push forward
            // location updates to it when we receive them from Location Manager.
            mListener = listener;

            // Request location updates from Location Manager
            if (bestAvailableProvider != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(bestAvailableProvider, minTime, minDistance, this);
            } else {
                return;
            }
        }

        /* Deactivates this provider.
         * This method is automatically invoked by disabling my-location layer. */
        @Override
        public void deactivate() {
            // Remove location updates from Location Manager
            locationManager.removeUpdates(this);

            mListener = null;
        }

        @Override
        public void onLocationChanged(Location location) {
            /* Push location updates to the registered listener..
             * (this ensures that my-location layer will set the blue dot at the new/received location) */
            if (mListener != null) {
                mListener.onLocationChanged(location);
            }

            /* ..and Animate camera to center on that location !
             * (the reason for we created this custom Location Source !) */
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                    location.getLongitude()), DEFAULT_ZOOM));
            line = map.addPolyline(new PolylineOptions()
                    .add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())
                            , new LatLng(location.getLatitude(), location.getLongitude()))
                    .width(5)
                    .color(Color.RED));
            lastKnownLocation = location;
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}