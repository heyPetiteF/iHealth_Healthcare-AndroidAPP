package com.example.healthcare.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.util.SharedPrefHelper;

public class EditProfileFragment extends Fragment {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private Button saveButton;
    private Button cancelButton;

    private UserInfo userInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        firstNameEditText = root.findViewById(R.id.firstNameEditText);
        lastNameEditText = root.findViewById(R.id.lastNameEditText);
        dobEditText = root.findViewById(R.id.dobEditText);
        phoneEditText = root.findViewById(R.id.phoneEditText);
        emailEditText = root.findViewById(R.id.emailEditText);
        saveButton = root.findViewById(R.id.saveButton);
        cancelButton = root.findViewById(R.id.cancelButton);

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
        String currentUserEmail = sharedPrefHelper.getCurrentUserEmail();

        //get users info
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            userInfo = db.userDao().getUserByEmail(currentUserEmail);
            getActivity().runOnUiThread(() -> {
                if (userInfo != null) {
                    firstNameEditText.setText(userInfo.firstName);
                    lastNameEditText.setText(userInfo.lastName);
                    dobEditText.setText(userInfo.dob);
                    phoneEditText.setText(userInfo.phone);
                    emailEditText.setText(userInfo.email);
                }
            });
        }).start();

        saveButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String dob = dobEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String email = emailEditText.getText().toString();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(dob) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            } else {
                //renew users info
                new Thread(() -> {
                    userInfo.firstName = firstName;
                    userInfo.lastName = lastName;
                    userInfo.dob = dob;
                    userInfo.phone = phone;
                    userInfo.email = email;
                    AppDatabase db = AppDatabase.getInstance(getContext());
                    db.userDao().update(userInfo);
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_homeFragment);
                    });
                }).start();
            }
        });

        cancelButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_editProfileFragment_to_homeFragment);
        });

        return root;
    }
}
