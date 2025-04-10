package com.example.cs205processes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder> {

    private List<Process> processes;
    private Context context;
    private OnProcessInteractionListener listener;

    public interface OnProcessInteractionListener {
        void onCompleteButtonClicked(Process process);
    }

    public ProcessAdapter(Context context, List<Process> processes, OnProcessInteractionListener listener) {
        this.context = context;
        this.processes = processes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_process, parent, false);
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
        this.processes = newProcesses;
        notifyDataSetChanged();
    }

    class ProcessViewHolder extends RecyclerView.ViewHolder {
        private TextView processNameTextView;
        private TextView recipeNameTextView;
        private TextView timeRemainingTextView;
        private ProgressBar timeProgressBar;
        private Button completeButton;
        private ViewGroup ingredientsContainer;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            processNameTextView = itemView.findViewById(R.id.processNameTextView);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            timeRemainingTextView = itemView.findViewById(R.id.timeRemainingTextView);
            timeProgressBar = itemView.findViewById(R.id.timeProgressBar);
            completeButton = itemView.findViewById(R.id.completeButton);
            ingredientsContainer = itemView.findViewById(R.id.ingredientsContainer);
        }

        public void bind(final Process process) {
            processNameTextView.setText(process.getName());
            recipeNameTextView.setText("Needs: " + process.getRecipe().getName());
            timeRemainingTextView.setText(process.getTimeRemaining() + "s");

            // Update progress bar
            timeProgressBar.setMax(process.getTimeLimit());
            timeProgressBar.setProgress(process.getTimeRemaining());

            // Set progress bar color based on time remaining
            if (process.isAboutToDie()) {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_critical));
            } else if (process.getTimeRemaining() < process.getTimeLimit() / 2) {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_warning));
            } else {
                timeProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_normal));
            }

            // Set button state
            completeButton.setEnabled(!process.isComplete() && !process.isDead());

            if (process.isComplete()) {
                completeButton.setText("Completed");
                completeButton.setBackgroundResource(R.drawable.button_completed);
            } else if (process.isDead()) {
                completeButton.setText("Failed");
                completeButton.setBackgroundResource(R.drawable.button_failed);
            } else {
                completeButton.setText("Complete");
                completeButton.setBackgroundResource(R.drawable.button_normal);
            }

            completeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && !process.isComplete() && !process.isDead()) {
                        listener.onCompleteButtonClicked(process);
                    }
                }
            });

            // Display recipe ingredients
            ingredientsContainer.removeAllViews();
            for (Ingredient ingredient : process.getRecipe().getIngredients()) {
                ImageView ingredientIcon = new ImageView(context);
                ingredientIcon.setImageResource(ingredient.getIconResourceId());
                ingredientIcon.setContentDescription(ingredient.getName());

                // Set the size of the image
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                ingredientIcon.setLayoutParams(params);

                // Add some padding
                ingredientIcon.setPadding(8, 8, 8, 8);

                ingredientsContainer.addView(ingredientIcon);
            }
        }
    }
}