package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter.InactiveUserListAdapter;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.model.User;

/**
 * Created by administrator on 10/20/17.
 */

public class InactiveUsersActivity extends AppCompatActivity {

    private Button mBackButton;
    private SwipeRefreshLayout mRefreshLayout;
    private ListView mInactiveUserListView;

    private InactiveUserListAdapter mInactiveUserListAdapter;

    private List<User> mInactiveUsers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.inactive_users_layout);

        initUI();
        fetchInactiveUsers();
    }

    private void initUI() {
        mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(mBackButtonClickListener);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(mRefreshLayoutRefreshListener);

        mInactiveUserListView = (ListView) findViewById(R.id.inactive_user_listview);
        mInactiveUserListView.setOnItemClickListener(mInactiveUserListViewItemClickListener);

        mInactiveUserListAdapter = new InactiveUserListAdapter(this, mInactiveUsers);
        mInactiveUserListView.setAdapter(mInactiveUserListAdapter);
    }

    private void fetchInactiveUsers() {
        mRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject responseObject = APIInterface.getInactiveUsers();
                    JSONArray userJSONArray = responseObject.getJSONArray("data");
                    mInactiveUsers.clear();
                    for (int i = 0; i < userJSONArray.length(); i ++) {
                        User user = new User(userJSONArray.getJSONObject(i));
                        mInactiveUsers.add(user);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.setRefreshing(false);
                            mInactiveUserListAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(InactiveUsersActivity.this, "Failed to fetch inactive users.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    private View.OnClickListener mBackButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InactiveUsersActivity.this, MainActivity.class);
            InactiveUsersActivity.this.startActivity(intent);
            finish();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener mRefreshLayoutRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            fetchInactiveUsers();
        }
    };

    private AdapterView.OnItemClickListener mInactiveUserListViewItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Dialog dialog = new Dialog(InactiveUsersActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_activation_code_layout);

            ImageView qrcodeImageView = (ImageView) dialog.findViewById(R.id.qrcode_imageview);
            byte[] data = Base64.decode(mInactiveUsers.get(position).base64QRCode, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bitmap != null) {
                qrcodeImageView.setImageBitmap(bitmap);
            }
            Button saveButton = (Button) dialog.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    };
}
