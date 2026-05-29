package com.example.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.database.AppDatabase;
import com.example.project.model.Group;
import com.example.project.model.Student;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class StudentFormActivity extends AppCompatActivity {
    private AppDatabase db;
    private int editId = -1;
    private List<Group> groupsList = new ArrayList<>();
    private Spinner spGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_student);

        db = AppDatabase.getInstance(this);
        editId = getIntent().getIntExtra("id", -1);

        // Находим view
        android.widget.EditText etF = findViewById(R.id.etFirstName);
        android.widget.EditText etL = findViewById(R.id.etLastName);
        android.widget.EditText etM = findViewById(R.id.etMiddleName);
        android.widget.EditText etD = findViewById(R.id.etDate);
        spGroup = findViewById(R.id.spGroup);
        android.widget.Button btnSave = findViewById(R.id.btnSave);
        android.widget.Button btnCancel = findViewById(R.id.btnCancel);

        // Выбор даты
        etD.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = day + "." + (month+1) + "." + year;
                etD.setText(date);
                etD.setError(null);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Загрузка данных при редактировании
        if(editId != -1) {
            etF.setText(getIntent().getStringExtra("f"));
            etL.setText(getIntent().getStringExtra("l"));
            etM.setText(getIntent().getStringExtra("m"));
            etD.setText(getIntent().getStringExtra("d"));
            setTitle("Редактирование студента");
        } else {
            setTitle("Добавление студента");
        }

        // Загружаем список групп
        loadGroups();

        // Кнопка сохранения
        btnSave.setOnClickListener(v -> {
            String f = etF.getText().toString().trim();
            String l = etL.getText().toString().trim();
            String m = etM.getText().toString().trim();
            String d = etD.getText().toString().trim();

            // Валидация полей
            if(l.isEmpty()) { etL.setError("Обязательно"); etL.requestFocus(); return; }
            if(f.isEmpty()) { etF.setError("Обязательно"); etF.requestFocus(); return; }
            if(d.isEmpty()) { etD.setError("Выберите дату"); etD.requestFocus(); return; }

            int position = spGroup.getSelectedItemPosition();
            if(position < 0 || groupsList == null || position >= groupsList.size()) {
                Toast.makeText(this, "Выберите группу", Toast.LENGTH_SHORT).show();
                return;
            }

            int gid = groupsList.get(position).getId();

            // ✅ ВСЯ работа с БД — в фоновом потоке
            new Thread(() -> {
                try {
                    // 1. Проверка на дубликат (БД)
                    int dup = db.studentDao().checkDuplicate(l, f, m, editId);

                    if(dup > 0) {
                        runOnUiThread(() -> new AlertDialog.Builder(this)
                                .setTitle("⚠️ Дубликат")
                                .setMessage("Студент \"" + l + " " + f + "\" уже существует!")
                                .setPositiveButton("OK", null).show());
                        return;
                    }

                    // 2. Создаём/обновляем студента (БД)
                    Student student = new Student(f, l, m, d, gid);
                    if(editId == -1) {
                        db.studentDao().insert(student);
                    } else {
                        student.setId(editId);
                        db.studentDao().update(student);
                    }

                    // 3. Только UI — в главном потоке
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Студент сохранён", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
                // ← new Thread закрывается ЗДЕСЬ
            }).start(); // ← .start() вызываем СНАРУЖИ runOnUiThread
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    // Загрузка групп в Spinner
    private void loadGroups() {
        new Thread(() -> {
            try {
                groupsList = db.groupDao().getAllGroupsAsc();

                if(groupsList.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Сначала создайте группы!", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    String[] names = new String[groupsList.size()];
                    for(int i = 0; i < groupsList.size(); i++) {
                        names[i] = groupsList.get(i).getNumber();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spGroup.setAdapter(adapter);

                    // Выбираем текущую группу при редактировании
                    int currentG = getIntent().getIntExtra("g", -1);
                    if(currentG != -1) {
                        for(int i = 0; i < groupsList.size(); i++) {
                            if(groupsList.get(i).getId() == currentG) {
                                spGroup.setSelection(i);
                                break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}