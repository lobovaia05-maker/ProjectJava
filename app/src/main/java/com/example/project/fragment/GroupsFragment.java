package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.project.GroupFormActivity;
import com.example.project.adapter.GroupAdapter;
import com.example.project.database.AppDatabase;
import com.example.project.databinding.FragmentGroupsBinding;
import com.example.project.model.Group;
import java.util.List;

public class GroupsFragment extends Fragment implements GroupAdapter.OnItemClickListener {
    private FragmentGroupsBinding binding;
    private GroupAdapter adapter;
    private AppDatabase db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        db = AppDatabase.getInstance(requireContext());

        adapter = new GroupAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), GroupFormActivity.class);
            startActivity(i);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGroups();
    }

    private void loadGroups() {
        if (!isAdded()) return;
        new Thread(() -> {
            try {
                List<Group> list = db.groupDao().getAllGroupsAsc();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> adapter.setGroups(list));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override public void onClick(Group g) {}

    @Override
    public void onEdit(Group g) {
        Intent i = new Intent(requireContext(), GroupFormActivity.class);
        i.putExtra("id", g.getId());
        i.putExtra("number", g.getNumber());
        i.putExtra("faculty", g.getFacultyName());
        startActivity(i);
    }

    @Override
    public void onDelete(Group g) {
        if (!isAdded() || getActivity() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("🗑️ Удалить группу?")
                .setMessage("Группа: " + g.getNumber() + "\nЭто действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // ✅ ВСЯ работа с БД — в фоновом потоке
                    new Thread(() -> {
                        try {
                            // 1. Проверяем студентов (БД)
                            int studentCount = db.groupDao().getStudentCount(g.getId());

                            if (studentCount > 0) {
                                // ✅ Только показ диалога — на UI потоке
                                if (getActivity() != null && isAdded()) {
                                    getActivity().runOnUiThread(() -> {
                                        if (isAdded()) {
                                            new AlertDialog.Builder(requireContext())
                                                    .setTitle("⚠️ Нельзя удалить")
                                                    .setMessage("В группе есть студенты (" + studentCount + ").\nСначала удалите их.")
                                                    .setPositiveButton("Понятно", null)
                                                    .show();
                                        }
                                    });
                                }
                            } else {
                                // 2. Удаляем группу (БД) — всё ещё в фоновом потоке!
                                db.groupDao().delete(g);

                                // ✅ Только UI обновления — на главном потоке
                                if (getActivity() != null && isAdded()) {
                                    getActivity().runOnUiThread(() -> {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(), "✅ Группа удалена", Toast.LENGTH_SHORT).show();
                                            loadGroups(); // ← это тоже запустит новый фон. поток внутри
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getActivity() != null && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).start(); // ← new Thread закрывается ЗДЕСЬ, а не внутри runOnUiThread
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}