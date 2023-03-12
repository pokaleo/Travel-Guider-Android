package uk.ac.shef.dcs.travelguider.views;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import uk.ac.shef.dcs.travelguider.R;
import uk.ac.shef.dcs.travelguider.databinding.ActivityImageDetailViewBinding;
import uk.ac.shef.dcs.travelguider.databinding.ActivityImageViewBinding;
import uk.ac.shef.dcs.travelguider.utils.ImageHelper;

public class ImageDetailView extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    ActivityImageDetailViewBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_detail_view);
        binding.image.setImageBitmap(ImageHelper.getBitmapByFile(new File(getIntent().getStringExtra("imagePath"))));
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.imageMap));
        mapFragment.getMapAsync(this);

        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String path = getIntent().getStringExtra("path");
        binding.imageTitle.setText(title);
        binding.imageDate.setText(date);
        binding.imagePath.setText(path);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        MarkerOptions markerOptions = new MarkerOptions();
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        LatLng location = new LatLng(latitude, longitude);
        markerOptions.position(new LatLng(latitude,longitude));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
        Marker marker =  map.addMarker(markerOptions);
    }
}