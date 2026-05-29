package com.example.project.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.project.fragment.GroupsFragment;
import com.example.project.fragment.StudentsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new GroupsFragment() : new StudentsFragment();
    }
    @Override public int getItemCount() { return 2; }
}