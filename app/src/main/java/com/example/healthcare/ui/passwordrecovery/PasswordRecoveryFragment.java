package com.example.healthcare.ui.passwordrecovery;

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

public class PasswordRecoveryFragment extends Fragment {

    private EditText emailEditText;
    private EditText firstNameEditText;
    private EditText dobEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button doneButton;
    private Button cancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_password_recovery, container, false);

        emailEditText = root.findViewById(R.id.emailEditText);
        firstNameEditText = root.findViewById(R.id.firstNameEditText);
        dobEditText = root.findViewById(R.id.dobEditText);
        newPasswordEditText = root.findViewById(R.id.new_PasswordEditText);
        confirmNewPasswordEditText = root.findViewById(R.id.confirmNew_PasswordEditText);
        doneButton = root.findViewById(R.id.doneButton);
        cancelButton = root.findViewById(R.id.cancelButton);

        doneButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String firstName = firstNameEditText.getText().toString();
            String dob = dobEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(dob) ||
                    TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            } else if (!isValidPassword(newPassword)) {
                Toast.makeText(getContext(), "Password must be between 8 and 20 characters, and include at least one uppercase letter, one lowercase letter, one special character, and one number", Toast.LENGTH_LONG).show();
            } else if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(getContext());
                    UserInfo userInfo = db.userDao().getUserByEmailAndNameAndDob(email, firstName, dob);
                    if (userInfo != null) {
                        userInfo.password = newPassword;
                        db.userDao().update(userInfo);
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Password recovery successful", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).navigate(R.id.action_passwordRecoveryFragment_to_loginFragment);
                        });
                    } else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });

        cancelButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_passwordRecoveryFragment_to_loginFragment);
        });

        return root;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
