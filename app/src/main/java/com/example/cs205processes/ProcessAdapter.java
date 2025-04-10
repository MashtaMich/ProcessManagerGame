package com.example.cs205processes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    /**
     * Updates the process list efficiently
     */
    public void updateProcesses(List<Process> newProcesses) {
        // Simple approach: clear and add all
        this.processes.clear();
        this.processes.addAll(newProcesses);

        // Notify adapter that all items may have changed
        notifyDataSetChanged();
    }

    class ProcessViewHolder extends RecyclerView.ViewHolder {
        private TextView processNameTextView;
        private TextView timeRemainingTextView;
        private ProgressBar timeProgressBar;
        private Button completeButton;
        private LinearLayout ingredientsContainer;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            processNameTextView = itemView.findViewById(R.id.processNameTextView);
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeProgressBar = itemView.findViewById(R.id.timeProgressBar);
            completeButton = itemView.findViewById(R.id.completeButton);
            ingredientsContainer = itemView.findViewById(R.id.ingredientsContainer);
        }

        public void bind(final Process process) {
            // Set compact display data
            processNameTextView.setText(process.getName());

            // Update time display
            int timeRemaining = process.getTimeRemaining();
            timeRemainingTextView.setText(timeRemaining + "s");

            // Update progress bar
            timeProgressBar.setMax(process.getTimeLimit());
            timeProgressBar.setProgress(timeRemaining);

            // Set progress bar color based on time remaining
            updateProgressBarColor(process);

            // Set button state
            completeButton.setEnabled(!process.isComplete() && !process.isDead());

            if (process.isComplete()) {
                completeButton.setText("✓"); // Checkmark for completed
                completeButton.setBackgroundResource(R.drawable.button_completed);
            } else if (process.isDead()) {
                completeButton.setText("✗"); // X mark for failed
                completeButton.setBackgroundResource(R.drawable.button_failed);
            } else {
                completeButton.setText("◉"); // Circle for active
                completeButton.setBackgroundResource(R.drawable.button_normal);
            }

            completeButton.setOnClickListener(v -> {
                if (listener != null && !process.isComplete() && !process.isDead()) {
                    listener.onCompleteButtonClicked(process);
                }
            });

            // Display recipe ingredients in compact form
            displayIngredients(process);
        }

        private void updateProgressBarColor(Process process) {
            if (process.isAboutToDie()) {
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

        private void displayIngredients(Process process) {
            ingredientsContainer.removeAllViews();

            // Display only icons in compact form
            for (Ingredient ingredient : process.getRecipe().getIngredients()) {
                ImageView ingredientIcon = new ImageView(context);
                ingredientIcon.setImageResource(ingredient.getIconResourceId());
                ingredientIcon.setContentDescription(ingredient.getName());

                // Set a small fixed size
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        40, 40 // Smaller fixed size in pixels
                );
                params.setMargins(2, 0, 2, 0);
                ingredientIcon.setLayoutParams(params);

                ingredientsContainer.addView(ingredientIcon);
            }
        }
    }
}