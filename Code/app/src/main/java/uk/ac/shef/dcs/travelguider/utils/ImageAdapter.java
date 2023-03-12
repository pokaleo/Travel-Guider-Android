package uk.ac.shef.dcs.travelguider.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import java.util.List;

import uk.ac.shef.dcs.travelguider.database.PhotoBean;
import uk.ac.shef.dcs.travelguider.R;

public class ImageAdapter extends BaseAdapter {
    Context context;
    List<PhotoBean> list;
    public ImageAdapter(Context context, List<PhotoBean> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.image_list, parent, false);
        }
        ImageView imageView;
        imageView = (ImageView) convertView.findViewById(R.id.image);
        Bitmap bitmap = BitmapFactory.decodeFile(list.get(position).getPath());
        imageView.setImageBitmap(ImageHelper.changeBitmapSizeForGallery(bitmap));
        return convertView;
    }
}
