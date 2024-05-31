package com.example.healthcare.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.ui.signup.HintSpinnerAdapter;
import com.example.healthcare.util.SharedPrefHelper;

import java.util.Calendar;

public class EditProfileFragment extends Fragment {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText dobEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private Spinner sexSpinner;
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
        sexSpinner = root.findViewById(R.id.sexSpinner);
        saveButton = root.findViewById(R.id.saveButton);
        cancelButton = root.findViewById(R.id.cancelButton);

        String[] sexOptions = getResources().getStringArray(R.array.sex_options);
        HintSpinnerAdapter adapter = new HintSpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, sexOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);

        dobEditText.setOnClickListener(v -> {
            showDatePickerDialog();
        });

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
                    setSpinnerSelection(sexSpinner, sexOptions, userInfo.sex);
                }
            });
        }).start();

        saveButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String dob = dobEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String sex = sexSpinner.getSelectedItem().toString();

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
                    userInfo.sex = sex;
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
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, month1, dayOfMonth) -> dobEditText.setText(year1 + "-" + (month1 + 1) + "-" + dayOfMonth),
                year, month, day);
        datePickerDialog.show();
    }

    private void setSpinnerSelection(Spinner spinner, String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
