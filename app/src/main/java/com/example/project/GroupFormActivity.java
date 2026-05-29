package com.example.project;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.database.AppDatabase;
import com.example.project.model.Group;

public class GroupFormActivity extends AppCompatActivity {
    private AppDatabase db;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_group);

        db = AppDatabase.getInstance(this);
        editId = getIntent().getIntExtra("id", -1);

        android.widget.EditText etNum = findViewById(R.id.etNumber);
        android.widget.EditText etFac = findViewById(R.id.etFaculty);
        android.widget.Button btnSave = findViewById(R.id.btnSave);
        android.widget.Button btnCancel = findViewById(R.id.btnCancel);

        if(editId != -1) {
            etNum.setText(getIntent().getStringExtra("number"));
            etFac.setText(getIntent().getStringExtra("faculty"));
            setTitle("Редактирование группы");
        } else {
            setTitle("Добавление группы");
        }

        btnSave.setOnClickListener(v -> {
            String num = etNum.getText().toString().trim();
            String fac = etFac.getText().toString().trim();

            if(num.isEmpty()) { etNum.setError("Обязательно"); return; }
            if(fac.isEmpty()) { etFac.setError("Обязательно"); return; }

            new Thread(() -> {
                try {
                    // Проверка дубликата (исключаем текущую группу при редактировании)
                    int dup = db.groupDao().checkDuplicate(num, editId);
                    if(dup > 0) {
                        runOnUiThread(() -> new AlertDialog.Builder(this)
                                .setTitle("⚠️ Дубликат")
                                .setMessage("Группа \"" + num + "\" уже существует!")
                                .setPositiveButton("OK", null).show());
                        return;
                    }

                    Group group = new Group(num, fac);
                    if(editId == -1) {
                        db.groupDao().insert(group);
                    } else {
                        group.setId(editId);
                        db.groupDao().update(group);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Сохранено", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "❌ Ошибка БД: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}