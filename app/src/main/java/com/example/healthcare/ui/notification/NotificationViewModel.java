package com.example.healthcare.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<List<Alert>> alerts;

    public NotificationViewModel() {
        alerts = new MutableLiveData<>();
        loadAlerts();
    }

    public LiveData<List<Alert>> getAlerts() {
        return alerts;
    }

    private void loadAlerts() {
        List<Alert> alertList = fetchDataFromDatabase();
        alerts.setValue(alertList);
    }

    private List<Alert> fetchDataFromDatabase() {
        List<Alert> alertList = new ArrayList<>();
        String url = "jdbc:mysql://your-xampp-server-address:3306/your-database-name";
        String user = "your-database-username";
        String password = "your-database-password";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String threeDaysAgo = sdf.format(new Date(now.getTime() - 3L * 24 * 60 * 60 * 1000));

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM alerts WHERE time >= '" + threeDaysAgo + "'");

            while (rs.next()) {
                String title = rs.getString("title");
                String time = rs.getString("time");
                String value = rs.getString("value");
                alertList.add(new Alert(title, time, value));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return alertList;
    }

    public static class Alert {
        public String title;
        public String time;
        public String value;

        public Alert(String title, String time, String value) {
            this.title = title;
            this.time = time;
            this.value = value;
        }
    }
}
