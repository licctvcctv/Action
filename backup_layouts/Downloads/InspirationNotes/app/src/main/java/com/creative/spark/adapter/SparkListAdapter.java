package com.creative.spark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.creative.spark.R;
import com.creative.spark.data.model.TravelNote;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

public class SparkListAdapter extends RecyclerView.Adapter<SparkListAdapter.TravelNoteViewHolder> {
    private Context context;
    private List<TravelNote> noteList;
    private OnNoteClickListener noteClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(TravelNote note);
        void onNoteLongClick(TravelNote note);
    }

    public SparkListAdapter(Context context, List<TravelNote> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.noteClickListener = listener;
    }

    @NonNull
    @Override
    public TravelNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_spark_card, parent, false);
        return new TravelNoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelNoteViewHolder holder, int position) {
        TravelNote currentNote = noteList.get(position);
        holder.bindNote(currentNote);
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public void updateNoteList(List<TravelNote> newNoteList) {
        this.noteList = newNoteList;
        notifyDataSetChanged();
    }

    public void addNote(TravelNote note) {
        noteList.add(0, note);
        notifyItemInserted(0);
    }

    public void removeNote(int position) {
        if (position >= 0 && position < noteList.size()) {
            noteList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateNote(int position, TravelNote updatedNote) {
        if (position >= 0 && position < noteList.size()) {
            noteList.set(position, updatedNote);
            notifyItemChanged(position);
        }
    }

    class TravelNoteViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView noteCard;
        private ImageView travelImage;
        private TextView titleTextView;
        private TextView contentTextView;
        private TextView categoryChip;
        private TextView timestampTextView;

        public TravelNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteCard = itemView.findViewById(R.id.spark_card);
            travelImage = itemView.findViewById(R.id.travel_image);
            titleTextView = itemView.findViewById(R.id.spark_title);
            contentTextView = itemView.findViewById(R.id.spark_content_preview);
            categoryChip = itemView.findViewById(R.id.spark_collection_chip);
            timestampTextView = itemView.findViewById(R.id.spark_timestamp);
        }

        public void bindNote(final TravelNote note) {
            titleTextView.setText(note.getTitle());
            
            String contentPreview = note.getContent();
            if (contentPreview.length() > 80) {
                contentPreview = contentPreview.substring(0, 80) + "...";
            }
            contentTextView.setText(contentPreview);
            
            categoryChip.setText(note.getCategory());
            timestampTextView.setText(note.getFormattedDate());
            
            // 使用Glide加载真实图片 (图文并茂要求)
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_note) // 加载中显示
                    .error(android.R.drawable.ic_menu_gallery) // 加载失败显示
                    .transform(new RoundedCorners(16)); // 圆角效果
                    
            if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(note.getImageUrl())
                        .apply(requestOptions)
                        .into(travelImage);
            } else {
                // 如果没有图片URL，显示默认旅游图片
                Glide.with(context)
                        .load("https://via.placeholder.com/400x300/4CAF50/white?text=旅游")
                        .apply(requestOptions)
                        .into(travelImage);
            }

            noteCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (noteClickListener != null) {
                        noteClickListener.onNoteClick(note);
                    }
                }
            });

            noteCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (noteClickListener != null) {
                        noteClickListener.onNoteLongClick(note);
                    }
                    return true;
                }
            });
        }
    }
}