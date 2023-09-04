package com.tung.travelthere;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemListHolder> {

    ArrayList<MenuItem> menuItemList;

    public MenuItemAdapter(ArrayList<MenuItem> menuItemList) {
        this.menuItemList = menuItemList;
    }

    @Override
    public MenuItemListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuItemListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemListHolder holder, int position) {
        MenuItem menuItem = menuItemList.get(position);
        if (menuItem == null)
        {
            return;
        }
        holder.userName.setText(menuItem.getUserName());
        holder.itemContent.setText(menuItem.getContent());
        Glide.with(holder.itemView.getContext()).load(menuItemList.get(position).getUserImageURL()).into(holder.userImage);
        Glide.with(holder.itemView.getContext()).load(menuItemList.get(position).getContentImageURL()).into(holder.itemImage);

        holder.btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), "Button Answer is clicked !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    class MenuItemListHolder extends RecyclerView.ViewHolder{
        TextView userName;
        TextView itemContent;
        ImageView userImage;
        ImageView itemImage;
        Button btnAnswer;

        public MenuItemListHolder(View itemView){
            super(itemView);

            userName = itemView.findViewById(R.id.text_menu_item_info);
            itemContent = itemView.findViewById(R.id.text_menu_item_content);
            userImage = itemView.findViewById(R.id.image_menu_item_info);
            itemImage = itemView.findViewById(R.id.image_menu_item_content);
            btnAnswer = itemView.findViewById(R.id.btn_menu_item_answer);
        }


    }

}
