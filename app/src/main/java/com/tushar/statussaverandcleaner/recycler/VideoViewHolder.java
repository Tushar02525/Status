package com.tushar.statussaverandcleaner.recycler;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.tushar.statussaverandcleaner.R;



public class VideoViewHolder extends RecyclerView.ViewHolder {


    public ImageView imageView,imageViewCheck,imageViewPlay;


    public VideoViewHolder(View view) {
        super(view);


        this.imageView = (ImageView) view.findViewById(R.id.imageView_wa_image);
        this.imageViewCheck = (ImageView) view.findViewById(R.id.imageView_wa_checked);
        this.imageViewPlay = (ImageView) view.findViewById(R.id.imageView_wa_play);

    }
}