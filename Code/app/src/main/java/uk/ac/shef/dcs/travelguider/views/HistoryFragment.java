package uk.ac.shef.dcs.travelguider.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import uk.ac.shef.dcs.travelguider.R;
import uk.ac.shef.dcs.travelguider.database.VisitBean;
import uk.ac.shef.dcs.travelguider.databinding.FragmentHistoryBinding;
import uk.ac.shef.dcs.travelguider.models.VisitModel;
import uk.ac.shef.dcs.travelguider.utils.VisitAdapter;
import uk.ac.shef.dcs.travelguider.viewModels.MapViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private MapViewModel mapViewModel;

    public HistoryFragment() {
        // Required empty public constructor
    }
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        VisitModel visitModel = new VisitModel(requireContext());
        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        VisitAdapter adapter= new VisitAdapter(requireContext(), visitModel.getVisit());
        gridView.setAdapter(adapter);
        // Display the photo with details when it's clicked
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                
                VisitBean visitBean = (VisitBean) adapter.getItem(position);
                String filePath = visitBean.getPath();
                String title = visitBean.getTitle();
                String date = visitBean.getDate();
                String path = visitBean.getPath();
                String duration = visitBean.getDuration();
                requireContext().
                        startActivity(new Intent(requireContext(), VisitDetailView.class)
                                .putExtra("imagePath",filePath)
                        .putExtra("title", title)
                        .putExtra("date", date)
                        .putExtra("path", path)
                        .putExtra("duration", duration));
            }
        });
        return view;
    }
}