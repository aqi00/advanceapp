package com.example.event.constant;

import com.example.event.R;

import java.util.ArrayList;
import java.util.List;

public class ImageList {

    public static List<Integer> getDefault() {
        ArrayList<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.banner_1);
        imageList.add(R.drawable.banner_2);
        imageList.add(R.drawable.banner_3);
        imageList.add(R.drawable.banner_4);
        imageList.add(R.drawable.banner_5);
        return imageList;
    }
}
