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
import com.example.project.model.Group;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<Group> groups;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Group g);
        void onDelete(Group g);
        void onClick(Group g);
    }

    public GroupAdapter(OnItemClickListener listener) { this.listener = listener; }
    public void setGroups(List<Group> list) { this.groups = list; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group g = groups.get(position);
        holder.tvName.setText("Группа: " + g.getNumber());
        holder.tvFaculty.setText("Факультет: " + g.getFacultyName());
        holder.itemView.setOnClickListener(v -> listener.onClick(g));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(g));
        holder.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Удалить?")
                        .setPositiveButton("Да", (d, w) -> listener.onDelete(g))
                        .show()
        );
    }

    @Override public int getItemCount() { return groups == null ? 0 : groups.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFaculty;
        ImageButton btnEdit, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGroupName);
            tvFaculty = itemView.findViewById(R.id.tvGroupFaculty);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}