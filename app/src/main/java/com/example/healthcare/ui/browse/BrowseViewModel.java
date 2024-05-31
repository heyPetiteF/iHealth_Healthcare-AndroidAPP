package com.example.healthcare.ui.browse;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrowseViewModel extends AndroidViewModel {

    private final MutableLiveData<String> sensorData = new MutableLiveData<>();
    private final AppDatabase database;
    private final MutableLiveData<UserInfo> userInfo = new MutableLiveData<>();
    private final ExecutorService executorService;


    public BrowseViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
        loadUserInfo();
    }

    public LiveData<String> getSensorData() {
        return sensorData;
    }

    public LiveData<UserInfo> getUserInfo() {
        return userInfo;
    }

    private void loadUserInfo() {
        executorService.execute(() -> {
            UserInfo info = database.userDao().getUserById(1); // 假设用户ID为1
            if (info == null) {
                info = new UserInfo();
                info.id = 1;
                database.userDao().insert(info);
            }
            userInfo.postValue(info);
        });
    }

    public void setUserInfo(UserInfo info) {
        userInfo.setValue(info);
    }

    public void updateUserInfo(String dataType, String data) {
        executorService.execute(() -> {
            UserInfo currentUserInfo = userInfo.getValue();
            if (currentUserInfo == null) {
                currentUserInfo = new UserInfo();
                currentUserInfo.id = 1;
            }
            switch (dataType) {
                case "weight":
                    currentUserInfo.weight = data;
                    break;
                case "height":
                    currentUserInfo.height = data;
                    break;
                case "allergyMedications":
                    currentUserInfo.allergyMedications = data;
                    break;
                case "emergencyContact":
                    currentUserInfo.emergencyContact = data;
                    break;
                case "steps":
                    currentUserInfo.steps = data;
                    break;
                case "bodyTemperature":
                    currentUserInfo.bodyTemperature = data;
                    break;
            }
            database.userDao().update(currentUserInfo);
            userInfo.postValue(currentUserInfo);
        });
    }


    public void setSensorData(String data) {
        sensorData.setValue(data);
    }
}
