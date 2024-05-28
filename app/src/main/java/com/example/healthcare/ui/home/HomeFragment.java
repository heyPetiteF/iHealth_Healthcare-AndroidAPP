package com.example.healthcare.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.healthcare.MainActivity;
import com.example.healthcare.R;
import com.example.healthcare.data.AppDatabase;
import com.example.healthcare.data.UserInfo;
import com.example.healthcare.util.SharedPrefHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ViewPager2 viewPagerImages;
    private ViewPager2 viewPagerTexts;
    private ImagePagerAdapter imagePagerAdapter;
    private TextPagerAdapter textPagerAdapter;
    private Handler handler = new Handler();
    private int textIndex = 0;
    private int imageIndex = 0;
    private static ConstraintLayout preLoginLayout;
    private static ConstraintLayout postLoginLayout;
    private static TextView userInfo;

    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView sexTextView;
    private TextView dobTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerImages = root.findViewById(R.id.viewPagerImages);
        viewPagerTexts = root.findViewById(R.id.viewPagerTexts);
        Button buttonLogin = root.findViewById(R.id.buttonLogin);
        Button buttonSignUp = root.findViewById(R.id.buttonSignUp);
        preLoginLayout = root.findViewById(R.id.pre_login_layout);
        postLoginLayout = root.findViewById(R.id.post_login_layout);

        firstNameTextView = root.findViewById(R.id.firstNameTextView);
        lastNameTextView = root.findViewById(R.id.lastNameTextView);
        sexTextView = root.findViewById(R.id.sexTextView);
        dobTextView = root.findViewById(R.id.dobTextView);

        Button buttonUpdateProfile = root.findViewById(R.id.buttonUpdateProfile);
        Button buttonSignOut = root.findViewById(R.id.buttonSignOut);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getImageUrls().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> imageUrls) {
                imagePagerAdapter = new ImagePagerAdapter(imageUrls);
                viewPagerImages.setAdapter(imagePagerAdapter);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int nextIndex = (imageIndex + 1) % imageUrls.size();
                        viewPagerImages.setCurrentItem(nextIndex, true);
                        imageIndex = nextIndex;
                        handler.postDelayed(this, 3000);
                    }
                }, 3000);
            }
        });

        homeViewModel.getTexts().observe(getViewLifecycleOwner(), new Observer<List<String[]>>() {
            @Override
            public void onChanged(List<String[]> texts) {
                textPagerAdapter = new TextPagerAdapter(texts);
                viewPagerTexts.setAdapter(textPagerAdapter);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int nextIndex = (textIndex + 1) % texts.size();
                        viewPagerTexts.setCurrentItem(nextIndex, true);
                        textIndex = nextIndex;
                        handler.postDelayed(this, 3000);
                    }
                }, 3000);
            }
        });

        buttonSignUp.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_signupFragment);
        });
        buttonLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_loginFragment);
        });

        // Check if user is logged in
        boolean isLoggedIn = checkUserLoggedIn();
        if (isLoggedIn) {
            preLoginLayout.setVisibility(View.GONE);
            postLoginLayout.setVisibility(View.VISIBLE);
            fetchAndDisplayUserInfo();
        } else {
            preLoginLayout.setVisibility(View.VISIBLE);
            postLoginLayout.setVisibility(View.GONE);
        }

        buttonUpdateProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_editProfileFragment);
        });

        buttonSignOut.setOnClickListener(v -> {
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
            sharedPrefHelper.clear();

            // Switch to pre-login layout
            preLoginLayout.setVisibility(View.VISIBLE);
            postLoginLayout.setVisibility(View.GONE);
        });
        return root;
    }

    public static void switchToPreLoginLayout() {
        if (preLoginLayout != null && postLoginLayout != null && userInfo != null) {
            preLoginLayout.setVisibility(View.VISIBLE);
            postLoginLayout.setVisibility(View.GONE);
            userInfo.setText("");
        }
    }

    private boolean checkUserLoggedIn() {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
        return sharedPrefHelper.isLoggedIn();
    }


    private void fetchAndDisplayUserInfo() {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getContext());
        String currentUserEmail = sharedPrefHelper.getCurrentUserEmail();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            UserInfo userInfo = db.userDao().getUserByEmail(currentUserEmail);
            getActivity().runOnUiThread(() -> {
                if (userInfo != null) {
                    firstNameTextView.setText("Firstname: " + userInfo.firstName);
                    lastNameTextView.setText("Lastname: " + userInfo.lastName);
                    sexTextView.setText("Sex: " + userInfo.sex);
                    dobTextView.setText("Date of Birth: " + userInfo.dob);
                }
            });
        }).start();
    }
}
