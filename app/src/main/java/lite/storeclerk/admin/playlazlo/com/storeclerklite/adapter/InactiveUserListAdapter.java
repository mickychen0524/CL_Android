package lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.R;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.model.User;

/**
 * Created by administrator on 10/20/17.
 */

public class InactiveUserListAdapter extends ArrayAdapter<User> {

    public InactiveUserListAdapter(Context context, List<User> users) {
        super(context, R.layout.item_inactive_user, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);

        UserViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_inactive_user, null, false);
            holder = new UserViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (UserViewHolder) convertView.getTag();
        }

        holder.nameTextView.setText(user.firstname + " " + user.lastname);

        return convertView;
    }

    private class UserViewHolder {
        TextView nameTextView;

        UserViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.name_textview);
        }
    }
}
