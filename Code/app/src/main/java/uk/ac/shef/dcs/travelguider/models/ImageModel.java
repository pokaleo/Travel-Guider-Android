package uk.ac.shef.dcs.travelguider.models;

import android.content.Context;

import java.util.List;

import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.database.UserDatabase;
import uk.ac.shef.dcs.travelguider.database.VisitBean;

public class ImageModel {
    private List<PhotoBean> images;

    public List<PhotoBean> getImages() {
        return images;
    }

    public ImageModel(Context context) {
        images = UserDatabase.getInstance(context).getPhotoDAO().queryAll();
    }
}
