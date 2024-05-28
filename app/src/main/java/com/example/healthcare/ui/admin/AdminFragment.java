package com.example.healthcare.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthcare.MainActivity;
import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.util.SharedPrefHelper;

import java.util.List;

public class AdminFragment extends Fragment {

    private LinearLayout userContainer;
    private Button signOutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin, container, false);

        userContainer = root.findViewById(R.id.user_container);
        signOutButton = root.findViewById(R.id.sign_out_button);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<UserInfo> users = db.userDao().getAllUsers();
            getActivity().runOnUiThread(() -> {
                View titleView = inflater.inflate(R.layout.item_user, userContainer, false);
                ((TextView) titleView.findViewById(R.id.name_text_view)).setText("First Name");
                ((TextView) titleView.findViewById(R.id.email_text_view)).setText("Last Name");
                ((TextView) titleView.findViewById(R.id.phone_text_view)).setText("Phone");
                userContainer.addView(titleView);

                for (UserInfo user : users) {
                    View userView = inflater.inflate(R.layout.item_user, userContainer, false);
                    ((TextView) userView.findViewById(R.id.name_text_view)).setText(user.firstName);
                    ((TextView) userView.findViewById(R.id.email_text_view)).setText(user.lastName);
                    ((TextView) userView.findViewById(R.id.phone_text_view)).setText(user.phone);

                    userContainer.addView(userView);
                }
            });
        }).start();

        signOutButton.setOnClickListener(v -> {
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
            sharedPrefHelper.clear();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        return root;
    }
}
