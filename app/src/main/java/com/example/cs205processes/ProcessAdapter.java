package com.example.cs205processes;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder> {
    private static final String TAG = "ProcessAdapter";

    private List<Process> processes;
    private Context context;
    private OnProcessInteractionListener listener;

    public interface OnProcessInteractionListener {
        void onCompleteButtonClicked(Process process);
    }

    public ProcessAdapter(Context context, List<Process> processes, OnProcessInteractionListener listener) {
        this.context = context;
        this.processes = new ArrayList<>(processes);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_process_compact, parent, false);
        return new ProcessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcessViewHolder holder, int position) {
        Process process = processes.get(position);
        holder.bind(process);
    }

    @Override
    public int getItemCount() {
        return processes.size();
    }

    public void updateProcesses(List<Process> newProcesses) {
        this.processes.clear();
        this.processes.addAll(newProcesses);
        notifyDataSetChanged();
    }

    class ProcessViewHolder extends RecyclerView.ViewHolder {
        private TextView timeRemainingTextView;
        private TextView recipeName;
        private ProgressBar timeProgressBar;
        private LinearLayout ingredientsContainer;
        private CardView cardView;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeProgressBar = itemView.findViewById(R.id.timeProgressBar);
            ingredientsContainer = itemView.findViewById(R.id.ingredientsContainer);
            recipeName = itemView.findViewById(R.id.recipeName);
        }

        public void bind(final Process process) {
            // Update time display
            int timeRemaining = process.getTimeRemaining();
            timeRemainingTextView.setText(timeRemaining + "s");

            // Update progress bar
            timeProgressBar.setMax(process.getTimeLimit());
            timeProgressBar.setProgress(timeRemaining);

            // Update card color based on process state
            updateCardBackgroundColor(process);

            // Set progress bar color based on time remaining
            updateProgressBarColor(process);

            // Display tiny ingredient icons
            displayTinyIngredientIcons(process);

            // Display recipe name
            recipeName.setText(process.getRecipe().getName());


        }

        private void updateCardBackgroundColor(Process process) {
            if (process.isComplete()) {
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            } else if (process.isDead()) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        private void updateProgressBarColor(Process process) {
            if (process.getTimeRemaining() < 5) {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_critical));
                timeRemainingTextView.setTextColor(context.getResources().getColor(R.color.progressCritical));
            } else if (process.getTimeRemaining() < process.getTimeLimit() / 2) {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_warning));
                timeRemainingTextView.setTextColor(context.getResources().getColor(R.color.timeRemainingColor));
            } else {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_normal));
                timeRemainingTextView.setTextColor(context.getResources().getColor(R.color.timeRemainingColor));
            }
        }

        private void displayTinyIngredientIcons(Process process) {
            ingredientsContainer.removeAllViews();

            // Show tiny icons in a row
            for (Ingredient ingredient : process.getRecipe().getIngredients()) {
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