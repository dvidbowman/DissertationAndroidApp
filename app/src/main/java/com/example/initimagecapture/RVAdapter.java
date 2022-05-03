package com.example.initimagecapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVViewHolder> {
    private ArrayList<UserImage> images;
    private Context context;

    public RVAdapter(Context ct, ArrayList<UserImage> images) {
        this.images = images;
        this.context = ct;
    }

    @NonNull
    @Override
    public RVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.analyses_row, parent, false);
        return new RVViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVViewHolder holder, int position) {
        holder.id.setText(images.get(position).getImgId());
        holder.date.setText(images.get(position).getImgDate());
        holder.avgRed.setText(images.get(position).getAvgRed());
        holder.pco2.setText(images.get(position).getPco2());

        //Converting Image String to Bitmap to show it in ImageView
        byte[] imgByteArray = Base64.decode(images.get(position).getImgValue(), Base64.DEFAULT);
        Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length);
        holder.img.setImageBitmap(imgBitmap);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class RVViewHolder extends RecyclerView.ViewHolder {

        TextView id, date, avgRed, pco2;
        ImageView img;

        public RVViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.textView_imageId);
            date = itemView.findViewById(R.id.textView_imageDate);
            img = itemView.findViewById(R.id.imageView_image);
            avgRed = itemView.findViewById(R.id.textView_avgRed);
            pco2 = itemView.findViewById(R.id.textView_PCO2);
        }

    }

}
