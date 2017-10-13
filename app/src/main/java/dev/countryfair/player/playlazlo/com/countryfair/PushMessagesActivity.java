package dev.countryfair.player.playlazlo.com.countryfair;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import dev.countryfair.player.playlazlo.com.countryfair.database.AppDb;
import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessage;


public class PushMessagesActivity extends FragmentActivity {

    private static final String TAG = PushMessagesActivity.class.getSimpleName();

    private List<PushMessage> messageList = new ArrayList<>();
    private RecyclerView rvPushMessages;
    private MessageListAdapter mMessageListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushmessages);
        init();
    }

    private void init(){

        mMessageListAdapter = new MessageListAdapter();
        rvPushMessages = (RecyclerView) findViewById(R.id.rvPushMessages);
        rvPushMessages.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        rvPushMessages.setHasFixedSize(true);
        rvPushMessages.setAdapter(mMessageListAdapter);


        messageList.clear();
        getAllMessagsList();
    }

    private void getAllMessagsList() {
        try {
            AppDb mAppDb = AppDb.getAppDatabase(this.getApplicationContext());
            messageList = mAppDb.daoPushMessage().getAll();
            mMessageListAdapter.notifyDataSetChanged();
    } catch (Exception e) {
        }
    }

    public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ItemHolder>{

        public class ItemHolder extends RecyclerView.ViewHolder {
            public TextView tvData;

            public ItemHolder(View itemView) {
                super(itemView);
                tvData = (TextView) itemView.findViewById(R.id.tvData);
            }
        }


        public MessageListAdapter() {
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_push_message, parent, false);
            ItemHolder holder = new ItemHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, final int position) {
            final PushMessage pushMessage = messageList.get(position);
            holder.tvData.setText(pushMessage.getData());
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }


}
