package com.example.cs205processes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ProcessViewHolder> {
    //private static final String TAG = "ProcessAdapter";

    private final List<Order> orders;
    private final Context context;
    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = new ArrayList<>(orders);
    }

    @NonNull
    @Override
    public ProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_process_compact, parent, false);
        return new ProcessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcessViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateProcesses(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    public class ProcessViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeRemainingTextView;
        private final TextView recipeName;
        private final ProgressBar timeProgressBar;
        private final LinearLayout ingredientsContainer;
        private final CardView cardView;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeProgressBar = itemView.findViewById(R.id.timeProgressBar);
            ingredientsContainer = itemView.findViewById(R.id.ingredientsContainer);
            recipeName = itemView.findViewById(R.id.recipeName);
        }

        @SuppressLint("SetTextI18n")
        public void bind(final Order order) {
            // Update time display
            int timeRemaining = order.getTimeRemaining();
            timeRemainingTextView.setText(timeRemaining + "s");

            // Update progress bar
            timeProgressBar.setMax(order.getTimeLimit());
            timeProgressBar.setProgress(timeRemaining);

            // Update card color based on process state
            updateCardBackgroundColor(order);

            // Set progress bar color based on time remaining
            updateProgressBarColor(order);

            // Display tiny ingredient icons
            displayTinyIngredientIcons(order);

            // Display recipe name
            recipeName.setText(order.getRecipe().getName());
        }

        private void updateCardBackgroundColor(Order order) {
            if (order.isComplete()) {
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            } else if (order.isDead()) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        private void updateProgressBarColor(Order order) {
            if (order.getTimeRemaining() < 10) {
                timeProgressBar.setProgressDrawable(ResourcesCompat.getDrawable(context.getResources(),
                        R.drawable.progress_critical, null));
                timeRemainingTextView.setTextColor(ContextCompat.getColor(context, R.color.progressCritical));
            } else if (order.getTimeRemaining() < order.getTimeLimit() / 2) {
                timeProgressBar.setProgressDrawable(ResourcesCompat.getDrawable(
                        context.getResources(), R.drawable.progress_warning, null));
                timeRemainingTextView.setTextColor(ContextCompat.getColor(context, R.color.timeRemainingColor));
            } else {
                timeProgressBar.setProgressDrawable(androidx.core.content.res.ResourcesCompat.getDrawable(
                        context.getResources(), R.drawable.progress_normal, null));
                timeRemainingTextView.setTextColor(ContextCompat.getColor(context, R.color.timeRemainingColor));
            }
        }

        private void displayTinyIngredientIcons(Order order) {
            ingredientsContainer.removeAllViews();

            // Show tiny icons in a row
            for (Ingredient ingredient : order.getRecipe().getIngredients()) {
                ImageView ingredientIcon = new ImageView(context);
                ingredientIcon.setImageResource(ingredient.getIconResourceId());
                ingredientIcon.setContentDescription(ingredient.getName());

                // Set tiny size for the icons
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 80);
                params.setMargins(2, 0, 2, 0);
                ingredientIcon.setLayoutParams(params);

                ingredientsContainer.addView(ingredientIcon);
            }
        }
    }
}