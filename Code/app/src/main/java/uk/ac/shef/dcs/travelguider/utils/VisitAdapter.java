package uk.ac.shef.dcs.travelguider.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import uk.ac.shef.dcs.travelguider.R;
import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.database.VisitBean;

public class VisitAdapter extends BaseAdapter {
    Context context;
    List<VisitBean> list;
    private String title;
    private String date;
    private String duration;

    public VisitAdapter(Context context, List<VisitBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public  View getView(int position, View convertView, ViewGroup parent) {
        if (convertView ==  null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.visit_list, parent, false);
        }
        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.visitImage);
        Bitmap bitmap = BitmapFactory.decodeFile(list.get(position).getPath());
        if (bitmap != null) {
            imageView.setImageBitmap(ImageHelper.changeBitmapSizeForGallery(bitmap));
        }
        TextView visitTitle = convertView.findViewById(R.id.visitTitleView);
        TextView visitDate = convertView.findViewById(R.id.visitDateView);
        title = list.get(position).getTitle();
        date = list.get(position).getDate();
        duration = list.get(position).getDuration();
        if (title != null) {
            visitTitle.setText(title);
        }
        if (date != null) {
            visitDate.setText(date);
        }
        return convertView;
    }
}
