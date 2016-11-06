package com.stonedot.todo.smartwalk;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class LINEFriendListFragment extends Fragment {

    private View mFragment;
    private TextView mFriendUrl;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_friend_list, container, false);
        findViews();
        makeList(getActivity());

        ColorDrawable sage = new ColorDrawable(Color.parseColor("#EEEEEE"));
        mListView.setDivider(sage);
        mListView.setDividerHeight(5);

        return mFragment;
    }

    private void findViews() {
        // mFriendUrl = (TextView) mFragment.findViewById(R.id.line_friend_url);
        mListView = (ListView) mFragment.findViewById(R.id.line_friend_list);
    }

    private void makeList(Activity activity) {
        friendList(new ItemAdder() {
            @Override
            public void ItemAdded(ArrayList<ItemPair> array) {
                LINEFriendListAdapter adapter = new LINEFriendListAdapter(activity.getApplicationContext());
                adapter.addAll(array);
                mListView.setAdapter(adapter);
            }
        });
    }

    public class ItemPair {
        public String mImageUrl;
        public String mDisplayName;
        public ItemPair(String displayName, String imageUrl) {
            mDisplayName = displayName;
            mImageUrl = imageUrl;
        }
    }

    public interface ItemAdder {
        void ItemAdded(ArrayList<ItemPair> array);
    }

    private static final String FriendListEndpoint = "https://smartwalk.stonedot.com/users/friend_list";
    private void friendList(ItemAdder callback) {
        HashMap<String, String> sendData = new HashMap<String, String>() {
            {
                put("mid", UserDataStorage.getLineMid(getActivity()));
            }
        };
        try {
            URL url = new URL(FriendListEndpoint);
            HttpJSONClient client = new HttpJSONClient(url, sendData);
            client.post(new HttpJSONClient.Responded() {
                @Override
                public void responded(int code, String statusMessage, String content) {
                    try {
                        JSONObject json = new JSONObject(content);
                        JSONArray friends = json.getJSONArray("friend_list");
                        ArrayList<ItemPair> array = new ArrayList<ItemPair>();
                        for(int i = 0; i < friends.length(); i++) {
                            JSONObject friend = friends.getJSONObject(i);
                            String displayName = friend.getString("display_name");
                            String pictureUrl = friend.getString("picture_url");
                            array.add(new ItemPair(displayName, pictureUrl));
                        }
                        if(callback != null) {
                            callback.ItemAdded(array);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("LineFriendList", "JSONの解析に失敗。");
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d("LineFriendList", "フレンドリストの取得に失敗。");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
