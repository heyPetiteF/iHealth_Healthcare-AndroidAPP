package com.example.healthcare.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthcare.R;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<String[]>> texts;
    private final MutableLiveData<List<Integer>> imageUrls;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        texts = new MutableLiveData<>();
        imageUrls = new MutableLiveData<>();
        loadTexts();
        loadImages();
    }

    public LiveData<List<String[]>> getTexts() {
        return texts;
    }

    public LiveData<List<Integer>> getImageUrls() {
        return imageUrls;
    }

    private void loadTexts() {
        List<String[]> textList = new ArrayList<>();
        textList.add(new String[]{"View your health data", "Check your health data at any time.\nBody temperature, Steps, Heart Rate, etc.\nTo help you fully understand your physical condition."});
        textList.add(new String[]{"Provide danger alerts", "Real-time monitoring of health risks.\nAbnormal Temperature, Sudden Falls, etc.\nSending timely alerts to ensure your safety."});
        textList.add(new String[]{"Find a nearby pharmacy", "Quickly locate pharmacies around you\nTo conveniently take care of your health."});

        texts.setValue(textList);
    }

    private void loadImages() {
        List<Integer> urls = new ArrayList<>();
        urls.add(R.drawable.image3);
        urls.add(R.drawable.image1);
        urls.add(R.drawable.image2);

        imageUrls.setValue(urls);
    }
}
