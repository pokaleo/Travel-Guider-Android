package uk.ac.shef.dcs.travelguider.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import uk.ac.shef.dcs.travelguider.R;
import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.databinding.FragmentGalleryBinding;
import uk.ac.shef.dcs.travelguider.models.ImageModel;
import uk.ac.shef.dcs.travelguider.utils.ImageAdapter;
import uk.ac.shef.dcs.travelguider.viewModels.MapViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private MapViewModel mapViewModel;

    public GalleryFragment() {
        // Required empty public constructor
    }
    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        // Try to locate if location permission granted
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        ImageModel imageModel = new ImageModel(requireContext());
        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        ImageAdapter adapter= new ImageAdapter(requireContext(), imageModel.getImages());
        gridView.setAdapter(adapter);
        // Display the photo with details when it's clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                PhotoBean photoBean = (PhotoBean) adapter.getItem(position);
                String filePath = photoBean.getPath();
                double latitude = Double.parseDouble(photoBean.getLatitude());
                double longitude = Double.parseDouble(photoBean.getLongitude());
                String title = photoBean.getTitle();
                String date = photoBean.getDate();
                String path = photoBean.getPath();
                requireContext().
                        startActivity(new Intent(requireContext(), ImageDetailView.class)
                                .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude)
                                .putExtra("imagePath",filePath)
                        .putExtra("title", title)
                        .putExtra("date", date)
                        .putExtra("path", path));
            }
        });
        return view;
    }
}