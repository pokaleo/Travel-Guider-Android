package uk.ac.shef.dcs.travelguider.views;

import static android.content.Context.LOCATION_SERVICE;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import kotlin.jvm.internal.Intrinsics;
import uk.ac.shef.dcs.travelguider.R;

import android.annotation.SuppressLint;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import uk.ac.shef.dcs.travelguider.databinding.FragmentMapsBinding;
import uk.ac.shef.dcs.travelguider.viewModels.MapViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapViewModel mapViewModel;
    private FragmentMapsBinding binding;
    private long timer = 0;
    private String timeElapsed;
    private TimerTask task;



    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Create data binding
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Init viewModel
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        // Try to locate if location permission granted
        mapViewModel.getLocationPermissionStatus().observe(requireActivity(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer.equals(1)) {
                    mapViewModel.getMap().setMyLocationEnabled(true);
                }
                if (integer.equals(-1)) {
                    Toast.makeText(requireContext(), "Location permission request failed, " +
                            "you may not be able to record your visit", Toast.LENGTH_LONG).show();
                }
            }
        });

        mapViewModel.getPressure().observe(requireActivity(), new Observer<String>() {

            @Override
            public void onChanged(String s) {
                if (s != null) {
                    binding.pressureValue.setText(s);
                }
            }
        });

        mapViewModel.getTimer().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null) {
                    binding.timer.setText(s);
                }
            }
        });

        mapViewModel.getTemperature().observe(requireActivity(), new Observer<String>() {

            @Override
            public void onChanged(String s) {
                if (s != null) {
                    binding.temperatureValue.setText(s);
                }
            }
        });

        mapViewModel.getTitle().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.visitTitle.setText(s);
            }
        });

        // Init buttons
        binding.cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                mapViewModel.openCamera();
            }
        });
        binding.photoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                mapViewModel.openGallery();
            }
        });
        binding.locateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                mapViewModel.locate(mapViewModel.getMap());
            }
        });
        binding.visitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapViewModel.getVisitStatus().getValue()) {
                    binding.visitButton.setText(R.string.new_visit);
                    binding.visitButton.setBackgroundTintList(ColorStateList.
                            valueOf(ContextCompat.getColor(requireContext(), R.color.primaryColor)));
                    mapViewModel.setVisitStatus(false);
                    mapViewModel.setDuration(binding.timer.getText().toString());
                    mapViewModel.stopVisit();
                    binding.timer.setText("00:00:00");
                    Toast.makeText(requireContext(), "Visit recorded", Toast.LENGTH_LONG).show();

                } else {
                    binding.visitButton.setText(R.string.stop_visit);
                    binding.visitButton.setBackgroundTintList(ColorStateList.
                            valueOf(ContextCompat.getColor(requireContext(), R.color.secondaryColor)));
                    mapViewModel.setVisitStatus(true);
                    mapViewModel.startVisit();
                }
            }
        });
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        Intrinsics.checkNotNullParameter(view, "view");
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager().
                findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapViewModel.setMap(googleMap);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapViewModel.getMap().setMyLocationEnabled(true);
    }

    public static MapFragment newInstance(){
        return new MapFragment();
    }




}