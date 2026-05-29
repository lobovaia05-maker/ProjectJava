package com.example.project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.model.StudentWithGroup;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private List<StudentWithGroup> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(StudentWithGroup s);
        void onDelete(StudentWithGroup s);
    }

    public StudentAdapter(OnItemClickListener listener) { this.listener = listener; }
    public void setList(List<StudentWithGroup> list) { this.list = list; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentWithGroup swg = list.get(position);
        holder.tvName.setText(swg.student.getLastName() + " " + swg.student.getFirstName());
        holder.tvGroup.setText("Группа: " + (swg.group != null ? swg.group.getNumber() : "?"));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(swg));
        holder.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Удалить студента?")
                        .setPositiveButton("Да", (d, w) -> listener.onDelete(swg))
                        .show()
        );
    }

    @Override public int getItemCount() { return list == null ? 0 : list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGroup;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvGroup = itemView.findViewById(R.id.tvStudentGroup);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}