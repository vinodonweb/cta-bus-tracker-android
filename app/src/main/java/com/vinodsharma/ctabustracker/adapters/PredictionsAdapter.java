package com.vinodsharma.ctabustracker.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.ctabustracker.activities.PredictionsActivity;
import com.vinodsharma.ctabustracker.databinding.ActivityPredictionsEntryBinding;
import com.vinodsharma.ctabustracker.models.Predictions;
import com.vinodsharma.ctabustracker.view_holders.PredictionsViewHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsViewHolder> {
    private final ArrayList<Predictions> predictionsList;
    private static final String TAG = "PredictionsAdapter";
    private PredictionsActivity predictionsActivity;

    public PredictionsAdapter(ArrayList<Predictions> predictionsList, PredictionsActivity pa) {
        this.predictionsList = predictionsList;
        this.predictionsActivity = pa;
    }

    @NonNull
    @Override
    public PredictionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityPredictionsEntryBinding binding = ActivityPredictionsEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PredictionsViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PredictionsViewHolder holder, int position){
        Predictions prediction = predictionsList.get(position);

        holder.binding.predicBusNum.setText("Bus #" + prediction.getVid());
        holder.binding.predicBoudTDis.setText(prediction.getRtDir() + " to " + prediction.getDes());

        if(prediction.getPrdctdm().equals("DUE")){
            holder.binding.predicDueTxt.setText("Due in a minute at");
        } else {
            holder.binding.predicDueTxt.setText("Due in " + prediction.getPrdctdm() + " mins at");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(prediction.getPrdtm(), formatter);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        holder.binding.predicTime.setText(timeFormatter.format(dateTime));

        //click listner for the alert dialog
        holder.itemView.setOnClickListener(v -> {
            predictionsActivity.showVehicleDistanceDialog(
                    prediction.getVid(),
                    prediction.getPrdctdm()
            );
        });
    }

    @Override
    public int getItemCount() {
        return predictionsList.size();
    }

}
