package com.example.healthcare.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.healthcare.R;

import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.ui.home.HomeFragment;
import com.example.healthcare.util.SharedPrefHelper;

public class LoginFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button cancelButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = root.findViewById(R.id.emailEditText);
        passwordEditText = root.findViewById(R.id.passwordEditText);
        loginButton = root.findViewById(R.id.loginButton);
        cancelButton = root.findViewById(R.id.cancelButton);

        TextView signupLink = root.findViewById(R.id.signupLink);
        TextView retrieveLink = root.findViewById(R.id.retrieveLink);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getContext());
                        UserInfo userInfo = db.userDao().login(email, password);
                        if (userInfo != null) {
                            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
                            sharedPrefHelper.setLoggedIn(true);
                            sharedPrefHelper.saveCurrentUserEmail(email);

                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                if (userInfo.isAdmin) {
                                    Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_adminFragment);
                                } else {
                                    Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                }
            }
        });


        cancelButton.setOnClickListener(v -> {
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
            sharedPrefHelper.clear();

            // Switch to pre-login layout
            HomeFragment.switchToPreLoginLayout();

            // Navigate back to HomeFragment
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment);
        });




        signupLink.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signupFragment);
        });

        retrieveLink.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_passwordRecoveryFragment);
        });

        return root;
    }

}
