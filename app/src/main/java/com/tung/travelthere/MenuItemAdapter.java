package com.tung.travelthere;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.userName.setText(menuItem.getUserName());
        holder.itemContent.setText(menuItem.getContent());
        holder.userImage.setImageResource(R.drawable.email);
        holder.itemImage.setImageResource(R.drawable.benthanh);
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

        public MenuItemListHolder(View itemView){
            super(itemView);

            userName = itemView.findViewById(R.id.text_menu_item_info);
            itemContent = itemView.findViewById(R.id.text_menu_item_content);
            userImage = itemView.findViewById(R.id.image_menu_item_info);
            itemImage = itemView.findViewById(R.id.image_menu_item_content);
        }


    }

}
