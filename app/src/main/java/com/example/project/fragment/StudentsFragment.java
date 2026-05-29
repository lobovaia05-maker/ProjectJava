package com.example.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.project.StudentFormActivity;
import com.example.project.adapter.StudentAdapter;
import com.example.project.database.AppDatabase;
import com.example.project.databinding.FragmentStudentsBinding;
import com.example.project.model.StudentWithGroup;
import java.util.List;

public class StudentsFragment extends Fragment implements StudentAdapter.OnItemClickListener {
    private FragmentStudentsBinding binding;
    private StudentAdapter adapter;
    private AppDatabase db;
    private int sortMode = 0;

    // ✅ 4 РЕЖИМА СОРТИРОВКИ
    String[] modes = {
            "🔤 По фамилии (А-Я)",
            "🔤 По фамилии (Я-А)",
            "📚 По группе (возрастание)",
            "📚 По группе (убывание)"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentsBinding.inflate(inflater, container, false);
        db = AppDatabase.getInstance(requireContext());

        ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, modes);
        binding.spinnerSort.setAdapter(aa);

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                sortMode = pos;
                loadStudents();
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        adapter = new StudentAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(v -> {
            new Thread(() -> {
                int groupCount = db.groupDao().getAllGroupsAsc().size();
                requireActivity().runOnUiThread(() -> {
                    if(groupCount == 0) {
                        Toast.makeText(getContext(), "Сначала создайте группы!", Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(requireContext(), StudentFormActivity.class);
                        startActivity(i);
                    }
                });
            }).start();
        });

        return binding.getRoot();
    }

    @Override public void onResume() { super.onResume(); loadStudents(); }

    private void loadStudents() {
        new Thread(() -> {
            List<StudentWithGroup> list;

            // ✅ 4 режима сортировки
            switch(sortMode) {
                case 0: // По фамилии (А-Я)
                    list = db.studentDao().getAllLastNameAsc();
                    break;
                case 1: // По фамилии (Я-А)
                    list = db.studentDao().getAllLastNameDesc();
                    break;
                case 2: // По группе (возрастание)
                    list = db.studentDao().getAllGroupAscLastNameAsc();
                    break;
                case 3: // По группе (убывание)
                    list = db.studentDao().getAllGroupDescLastNameDesc();
                    break;
                default:
                    list = db.studentDao().getAllLastNameAsc();
            }

            requireActivity().runOnUiThread(() -> adapter.setList(list));
        }).start();
    }

    @Override public void onEdit(StudentWithGroup s) {
        Intent i = new Intent(requireContext(), StudentFormActivity.class);
        i.putExtra("id", s.student.getId());
        i.putExtra("f", s.student.getFirstName());
        i.putExtra("l", s.student.getLastName());
        i.putExtra("m", s.student.getMiddleName());
        i.putExtra("d", s.student.getDateOfBirth());
        i.putExtra("g", s.student.getGroupId());
        startActivity(i);
    }

    @Override public void onDelete(StudentWithGroup s) {
        new AlertDialog.Builder(requireContext())
                .setTitle("🗑️ Удалить?")
                .setMessage(s.student.getLastName() + " " + s.student.getFirstName())
                .setPositiveButton("Удалить", (d, w) -> {
                    new Thread(() -> {
                        db.studentDao().delete(s.student);
                        requireActivity().runOnUiThread(this::loadStudents);
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}