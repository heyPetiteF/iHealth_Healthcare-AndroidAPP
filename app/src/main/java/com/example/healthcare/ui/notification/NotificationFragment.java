package com.example.healthcare.ui.notification;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthcare.R;
import com.example.healthcare.data.Alert;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.databinding.FragmentNotificationBinding;

import java.util.Date;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private NotificationViewModel notificationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ViewModel with a factory to pass the context
        notificationViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
                    return (T) new NotificationViewModel(getContext());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(NotificationViewModel.class);

        final LinearLayout alertContainer = root.findViewById(R.id.alert_container);
        final TextView titleTextView = root.findViewById(R.id.text_notification_title);
        final TextView noAlertTextView = root.findViewById(R.id.no_alert_text_view);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            long currentTime = System.currentTimeMillis();
            long threeDaysAgo = currentTime - (3 * 24 * 60 * 60 * 1000); // 3天前的时间戳
            List<Alert> alerts = db.alertDao().getAlertsSince(threeDaysAgo);

            getActivity().runOnUiThread(() -> {
                alertContainer.removeAllViews();
                if (alerts.isEmpty()) {
                    noAlertTextView.setVisibility(View.VISIBLE);
                    titleTextView.setVisibility(View.GONE);
                } else {
                    noAlertTextView.setVisibility(View.GONE);
                    titleTextView.setVisibility(View.VISIBLE);
                    for (Alert alert : alerts) {
                        View alertView = getLayoutInflater().inflate(R.layout.item_alert, alertContainer, false);
                        TextView titleView = alertView.findViewById(R.id.alert_title);
                        TextView timeView = alertView.findViewById(R.id.alert_time);
                        TextView valueView = alertView.findViewById(R.id.alert_value);

                        titleView.setText(alert.message);
                        timeView.setText(new Date(alert.timestamp).toString());
                        valueView.setText(String.valueOf(alert.temperature));

                        alertContainer.addView(alertView);
                    }
                }
            });
        }).start();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static void showFallAlertNotification(Context context, String alertMessage) {
        new AlertDialog.Builder(context)
                .setTitle("Abnormal Alert")
                .setMessage(alertMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Alert alert = new Alert();
            alert.timestamp = System.currentTimeMillis();
            alert.message = alertMessage;
            db.alertDao().insert(alert);
        }).start();
    }
}
