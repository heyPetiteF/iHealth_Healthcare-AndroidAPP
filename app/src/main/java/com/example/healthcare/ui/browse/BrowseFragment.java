package com.example.healthcare.ui.browse;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthcare.R;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.ui.map.MapActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final String TAG = "BrowseFragment";

    private LinearLayout activityDetails;
    private LinearLayout bodyMeasurementsDetails;
    private LinearLayout mentalWellbeingDetails;
    private LinearLayout otherDataDetails;

    private TextView stepsTextView;
    private TextView walkingDistanceTextView;
    private TextView runningSpeedTextView;
    private TextView bodyTemperatureTextView;
    private TextView bodyFatPercentageTextView;
    private TextView timeInBedTextView;
    private TextView anxietyRiskTextView;

    private EditText weight;
    private EditText height;
    private EditText allergyMedications;
    private EditText emergencyContact;

    private List<BluetoothDevice> pairedDeviceList;
    private BrowseViewModel browseViewModel;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard UUID for SPP

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browse, container, false);

        browseViewModel = new ViewModelProvider(this).get(BrowseViewModel.class);

        activityDetails = root.findViewById(R.id.activityDetails);
        bodyMeasurementsDetails = root.findViewById(R.id.bodyMeasurementsDetails);
        mentalWellbeingDetails = root.findViewById(R.id.mentalWellbeingDetails);
        otherDataDetails = root.findViewById(R.id.otherDataDetails);

        stepsTextView = root.findViewById(R.id.steps);
        walkingDistanceTextView = root.findViewById(R.id.walkingDistance);
        runningSpeedTextView = root.findViewById(R.id.runningSpeed);
        bodyTemperatureTextView = root.findViewById(R.id.bodyTemperature);
        bodyFatPercentageTextView = root.findViewById(R.id.BMI);
        timeInBedTextView = root.findViewById(R.id.timeInBed);
        anxietyRiskTextView = root.findViewById(R.id.anxietyRisk);

        weight = root.findViewById(R.id.weight);
        height = root.findViewById(R.id.height);
        allergyMedications = root.findViewById(R.id.allergyMedications);
        emergencyContact = root.findViewById(R.id.emergencyContact);

        pairedDeviceList = new ArrayList<>();

        root.findViewById(R.id.activityHeader).setOnClickListener(v -> toggleVisibility(activityDetails));
        root.findViewById(R.id.bodyMeasurementsHeader).setOnClickListener(v -> toggleVisibility(bodyMeasurementsDetails));
        root.findViewById(R.id.mentalWellbeingHeader).setOnClickListener(v -> toggleVisibility(mentalWellbeingDetails));
        root.findViewById(R.id.otherDataHeader).setOnClickListener(v -> toggleVisibility(otherDataDetails));
        root.findViewById(R.id.button_pharmacies_nearby).setOnClickListener(v -> showPharmaciesNearby());
        root.findViewById(R.id.button_search_bluetooth).setOnClickListener(v -> searchBluetoothDevices());

        setupEditTextListeners(weight, "weight");
        setupEditTextListeners(height, "height");
        setupEditTextListeners(allergyMedications, "allergyMedications");
        setupEditTextListeners(emergencyContact, "emergencyContact");

        browseViewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                weight.setText(userInfo.weight);
                height.setText(userInfo.height);
                allergyMedications.setText(userInfo.allergyMedications);
                emergencyContact.setText(userInfo.emergencyContact);
                calculateBMIAndUpdateUI();
            }
        });

        return root;
    }

    // EditText
    private void setupEditTextListeners(EditText editText, String dataType) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String data = editText.getText().toString();
                // Save the data to the database
                saveDataToDatabase(dataType, data);

                if (dataType.equals("weight") || dataType.equals("height")) {
                    calculateBMIAndUpdateUI();
                }
            }
        });
    }

    private void saveDataToDatabase(String dataType, String data) {
        browseViewModel.updateUserInfo(dataType, data);
    }

    private void calculateBMIAndUpdateUI() {
        BrowseViewModel viewModel = new ViewModelProvider(this).get(BrowseViewModel.class);
        UserInfo userInfo = viewModel.getUserInfo().getValue();

        if (userInfo != null && userInfo.weight != null && userInfo.height != null) {
            try {
                double weight = Double.parseDouble(userInfo.weight);
                double height = Double.parseDouble(userInfo.height) / 100; // assuming height is in cm
                if (height > 0) {
                    double bmi = weight / (height * height);
                    bodyFatPercentageTextView.setText(String.format("%.2f", bmi));
                } else {
                    bodyFatPercentageTextView.setText("No Data");
                }
            } catch (NumberFormatException e) {
                bodyFatPercentageTextView.setText("No Data");
            }
        } else {
            bodyFatPercentageTextView.setText("No Data");
        }
    }

    private void toggleVisibility(LinearLayout layout) {
        if (layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }
    }

    public void showPharmaciesNearby() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        startActivity(intent);
    }

    private void searchBluetoothDevices() {
        Log.d(TAG, "Starting Bluetooth device search");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting Bluetooth permissions");
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Bluetooth not supported");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bluetooth not enabled");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            showDeviceSelectionDialog(pairedDevices);
        } else {
            Toast.makeText(getContext(), "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No paired Bluetooth devices found");
        }
    }

    private void showDeviceSelectionDialog(Set<BluetoothDevice> pairedDevices) {
        List<String> deviceNames = new ArrayList<>();
        pairedDeviceList.clear();

        for (BluetoothDevice device : pairedDevices) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
            pairedDeviceList.add(device);
            deviceNames.add(device.getName() + "\n" + device.getAddress());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select a device")
                .setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceNames), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothDevice selectedDevice = pairedDeviceList.get(which);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.BLUETOOTH_CONNECT
                            }, REQUEST_BLUETOOTH_PERMISSION);
                            return;
                        }

                        connectToDevice(selectedDevice);
                    }
                });
        builder.create().show();
    }


    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                Log.d(TAG, "Connected to device: " + device.getName());

                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);
                    Log.d(TAG, "Received data: " + data);
                    getActivity().runOnUiThread(() -> updateSensorData(data));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Bluetooth device", e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error connecting to Bluetooth device", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateSensorData(String data) {
        Log.d(TAG, "Updating sensor data: " + data);
        // 假设数据是以某种方式分隔的，例如 "sensor1:1000,sensor2:5,sensor3:10"
        String[] dataPairs = data.split(",");
        for (String pair : dataPairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];

                switch (key) {
                    case "steps":
                        stepsTextView.setText(value);
                        break;
                    case "walkingDistance":
                        walkingDistanceTextView.setText(value);
                        break;
                    case "runningSpeed":
                        runningSpeedTextView.setText(value);
                        break;
                    case "bodyTemperature":
                        bodyTemperatureTextView.setText(value);
                        break;
                    case "timeInBed":
                        timeInBedTextView.setText(value);
                        break;
                    case "anxietyRisk":
                        anxietyRiskTextView.setText(value);
                        break;
                    default:
                        Log.w(TAG, "Unknown sensor data key: " + key);
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                searchBluetoothDevices();
            } else {
                Toast.makeText(getContext(), "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Bluetooth permissions not granted");
            }
        }
    }
}
