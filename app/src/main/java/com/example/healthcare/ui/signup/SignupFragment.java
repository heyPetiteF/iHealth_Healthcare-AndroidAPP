package com.example.healthcare.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.ui.home.HomeFragment;
import com.example.healthcare.util.SharedPrefHelper;

import java.util.Calendar;

public class SignupFragment extends Fragment {

    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner sexSpinner;
    private Button doneButton;
    private Button cancelButton;
    private TextView loginLink;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_signup, container, false);

        firstNameEditText = root.findViewById(R.id.firstNameEditText);
        lastNameEditText = root.findViewById(R.id.lastNameEditText);
        dobEditText = root.findViewById(R.id.dobEditText);
        phoneEditText = root.findViewById(R.id.phoneEditText);
        emailEditText = root.findViewById(R.id.emailEditText);
        passwordEditText = root.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = root.findViewById(R.id.confirmPasswordEditText);
        sexSpinner = root.findViewById(R.id.sexSpinner);
        doneButton = root.findViewById(R.id.doneButton);
        cancelButton = root.findViewById(R.id.cancelButton);
        loginLink = root.findViewById(R.id.loginLink);


        // Set up the Spinner adapter with a hint
        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        HintSpinnerAdapter adapter = new HintSpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, sexOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);

        dobEditText.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        doneButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String dob = dobEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String sex = sexSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(dob) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            } else if (!isValidPassword(password)) {
                Toast.makeText(getContext(), "Password must be between 8 and 20 characters, and include at least one uppercase letter, one lowercase letter, one special character, and one number", Toast.LENGTH_LONG).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                AppDatabase db = AppDatabase.getInstance(getContext());
                UserInfo userInfo = new UserInfo();
                userInfo.firstName = firstName;
                userInfo.lastName = lastName;
                userInfo.dob = dob;
                userInfo.phone = phone;
                userInfo.email = email;
                userInfo.password = password;
                userInfo.sex = sex;
                new Thread(() -> {
                    db.userDao().insert(userInfo);

                    SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
                    sharedPrefHelper.saveCurrentUserEmail(email);

                    Log.d("SignupFragment", "User inserted: " + userInfo.email);

                }).start();
                Toast.makeText(getContext(), "Sign Up successful", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).navigate(R.id.action_signupFragment_to_loginFragment);
            }
        });

        cancelButton.setOnClickListener(v -> {
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
            sharedPrefHelper.clear();

            // Switch to pre-login layout
            HomeFragment.switchToPreLoginLayout();

            // Navigate back to HomeFragment
            Navigation.findNavController(v).navigate(R.id.action_signupFragment_to_homeFragment);
        });

        loginLink.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_signupFragment_to_loginFragment);
        });

        return root;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, month1, dayOfMonth) -> dobEditText.setText(year1 + "-" + (month1 + 1) + "-" + dayOfMonth),
                year, month, day);

        // Ensure the date picker dialog is displayed in English
        Context context = new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Light_Dialog);
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        datePickerDialog = new DatePickerDialog(context);
        datePickerDialog.setOnDateSetListener((view, year1, month1, dayOfMonth) -> dobEditText.setText(year1 + "-" + (month1 + 1) + "-" + dayOfMonth));

        datePickerDialog.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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
