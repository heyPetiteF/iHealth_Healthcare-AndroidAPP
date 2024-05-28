package com.example.healthcare.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.Alert;
import com.example.healthcare.databinding.FragmentNotificationBinding;

import java.util.Date;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private NotificationViewModel notificationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationViewModel =
                new ViewModelProvider(this).get(NotificationViewModel.class);

        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
                        showAlertDialog(alert.message);
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

    private void showAlertDialog(String alertTitle) {
        new AlertDialog.Builder(getContext())
                .setTitle("Abnormal Alert")
                .setMessage(alertTitle)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
