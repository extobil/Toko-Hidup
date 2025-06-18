package com.example.utsa22202303007.ui.product;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.utsa22202303007.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categories;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    // Method baru untuk set selected category dari luar
    public void setSelectedCategory(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getNama().equals(categoryName)) {
                selectedPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategory.setText(category.getNama());

        // Highlight background kategori yang selected
        if (position == selectedPosition) {
            holder.tvCategory.setBackgroundColor(Color.LTGRAY);
            holder.tvCategory.setTextColor(Color.BLACK);
        } else {
            holder.tvCategory.setBackgroundColor(Color.TRANSPARENT);
            holder.tvCategory.setTextColor(Color.DKGRAY);
        }

        holder.tvCategory.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onCategoryClick(category.getNama());
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
