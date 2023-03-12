package uk.ac.shef.dcs.travelguider.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import java.io.File;

import uk.ac.shef.dcs.travelguider.databinding.ActivityImageViewBinding;
import uk.ac.shef.dcs.travelguider.R;
import uk.ac.shef.dcs.travelguider.utils.ImageHelper;

public class ImageView extends AppCompatActivity {

    ActivityImageViewBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_view);
        binding.image.setImageBitmap(ImageHelper.getBitmapByFile(new File(getIntent().getStringExtra("imagePath"))));
    }
}