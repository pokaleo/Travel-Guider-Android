package uk.ac.shef.dcs.travelguider.models;

import android.content.Context;

import java.util.List;

import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.database.UserDatabase;
import uk.ac.shef.dcs.travelguider.database.VisitBean;

public class VisitModel {
    private List<VisitBean> visit;

    public List<VisitBean> getVisit() {
        return visit;
    }

    public VisitModel(Context context) {
        visit = UserDatabase.getInstance(context).getVisitDAO().queryAll();
    }
}
