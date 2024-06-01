package com.example.healthcare.ui.notification;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthcare.data.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationViewModel extends ViewModel {
    private final MutableLiveData<List<com.example.healthcare.data.Alert>> alerts;
    private final ExecutorService executorService;
    private final AppDatabase db;

    public NotificationViewModel(Context context) {
        alerts = new MutableLiveData<List<com.example.healthcare.data.Alert>>();
        executorService = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(context);
        loadAlerts();
    }

    public LiveData<List<com.example.healthcare.data.Alert>> getAlerts() {
        return alerts;
    }

    private void loadAlerts() {
        executorService.execute(() -> {
            List<com.example.healthcare.data.Alert> alertList = fetchDataFromDatabase();
            alerts.postValue(alertList);
        });
    }

    private List<com.example.healthcare.data.Alert> fetchDataFromDatabase() {
        long currentTime = System.currentTimeMillis();
        long threeDaysAgo = currentTime - (3 * 24 * 60 * 60 * 1000);
        return db.alertDao().getAlertsSince(threeDaysAgo);
    }

    public static class Alert {
        public String message;
        public long timestamp;
        public float temperature;

        public Alert(String message, long timestamp, float temperature) {
            this.message = message;
            this.timestamp = timestamp;
            this.temperature = temperature;
        }
    }
}
