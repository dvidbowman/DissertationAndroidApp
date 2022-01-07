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

    private ArrayList<String> imgIDs, imgDates, imgValues;
    private Context context;

    public RVAdapter(Context ct, ArrayList<String> imgIDs, ArrayList<String> imgDates, ArrayList<String> imgValues) {
        this.imgIDs = imgIDs;
        this.imgDates = imgDates;
        this.imgValues = imgValues;
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
        holder.id.setText(imgIDs.get(position));
        holder.date.setText(imgDates.get(position));

        //Converting Image String to Bitmap to show it in ImageView
        byte[] imgByteArray = Base64.decode(imgValues.get(position), Base64.DEFAULT);
        Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length);
        holder.img.setImageBitmap(imgBitmap);
    }

    @Override
    public int getItemCount() {
        return imgValues.size();
    }

    public class RVViewHolder extends RecyclerView.ViewHolder {

        TextView id, date;
        ImageView img;

        public RVViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.textView_imageId);
            date = itemView.findViewById(R.id.textView_imageDate);
            img = itemView.findViewById(R.id.imageView_image);

        }

    }

}
