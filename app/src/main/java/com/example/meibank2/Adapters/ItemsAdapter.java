package com.example.meibank2.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.example.meibank2.Models.Item;
import com.example.meibank2.R;

import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    private static final String TAG = "ItemsAdapter";

    public interface GetItem {
        void OnGettingItemResult(Item item);
    }

    private GetItem getItem;

    private ArrayList<Item> items = new ArrayList<>();
    private Context context;
    private DialogFragment dialogFragment;

    public ItemsAdapter(Context context, DialogFragment dialogFragment) {
        this.context = context;
        this.dialogFragment = dialogFragment;
    }

    public ItemsAdapter() {
    }

    @NonNull
    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.name.setText(items.get(position).getName());

        // load image using Glide
        String imageUrl = items.get(position).getImage_url();
        ImageView UIImageView = holder.image;
        if (null != imageUrl) {
            if (null != UIImageView) {
                try {
                    Glide.with(context).asBitmap().load(imageUrl).into(UIImageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "not found UIImageView", Toast.LENGTH_SHORT).show();
            }
            
        } else {
            Toast.makeText(context, "not have imageUrl to load", Toast.LENGTH_SHORT).show();
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getItem = (GetItem) dialogFragment;
                    getItem.OnGettingItemResult(items.get(position));
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(ArrayList<Item> items) {
        Log.d(TAG, "setItems: started");
        this.items = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemImage);
            name = (TextView) itemView.findViewById(R.id.itemName);
            parent = (CardView) itemView.findViewById(R.id.parent);
        }
    }
}
