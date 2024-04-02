package com.example.garageapp.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.garageapp.Model.Slot;
import com.example.garageapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Random;

public class AdapterSlot extends BaseAdapter {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    private Context mContext;
    private List<Slot> slotList;

    public AdapterSlot(Context context, List<Slot> slots) {
        this.mContext = context;
        this.slotList = slots;
    }

    @Override
    public int getCount() {
        return slotList.size();
    }

    @Override
    public Object getItem(int position) {
        return slotList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItemView = convertView;
        if (gridItemView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            gridItemView = inflater.inflate(R.layout.item_slot_layout, parent, false);
        }

        LinearLayout linearLayout = gridItemView.findViewById(R.id.layout);
        TextView textView = gridItemView.findViewById(R.id.text);
        ImageView imageView = gridItemView.findViewById(R.id.image);

        Slot slot = (Slot) getItem(position);

        linearLayout.setLayoutDirection(position % 2 == 0 ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
        textView.setRotation(position % 2 == 0 ? 270 : 90);

        if (slot.isPrivate()) {
            imageView.setImageResource(position % 2 == 0 ? R.drawable.lock_left : R.drawable.lock_right);
            textView.setText("No Parking");
        } else {
            textView.setText("Parking\nAvailable");
            imageView.setImageResource(0);
        }

        if (slot.isReserved()) {
            textView.setText("");
            if (slot.getUserID().equals(currentUser.getUid())) {
                imageView.setImageResource(R.drawable.your_location);
            } else {
                Random random = new Random();
                int randomNumber = random.nextInt(5) + 1;
                switch (randomNumber) {
                    case 1:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left_1 : R.drawable.car_right_1);
                        break;
                    case 2:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left_2 : R.drawable.car_right_2);
                        break;
                    case 3:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left_3 : R.drawable.car_right_3);
                        break;
                    case 4:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left_4 : R.drawable.car_right_4);
                        break;
                    case 5:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left_5 : R.drawable.car_right_5);
                        break;
                    default:
                        imageView.setImageResource(position % 2 == 0 ? R.drawable.car_left : R.drawable.car_right);
                }
            }
        }
        return gridItemView;
    }
}