package jp.tmhouse.android.elgoog.elgoog;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mutoh on 16/9/22.
 */
public class BookMarkActivity extends ListActivity {
    private Prefs   m_prefs;
    private Handler m_handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list_main);
        m_prefs = new Prefs(this);
        setupList();
    }

    private void setupList() {
        LocalDB.Bookmark[] arr = m_prefs.getAllBookmarks();
        List<LocalDB.Bookmark> list = Arrays.asList(arr);

        setListAdapter(new ListViewAdapter(this, R.layout.bookmark_list_items, list));
    }

    // ArrayAdapterを継承したカスタムのアダプタークラス
    public class ListViewAdapter extends ArrayAdapter<LocalDB.Bookmark> {
        private LayoutInflater inflater;
        private int itemLayout;
        LocalDB.Bookmark data;

        private class ViewHolder {
            TextView urlText;
            TextView titleText;
            ListViewAdapter.GoUrlCallback goUrlCb;
            ListViewAdapter.DelBookmarkCallback delCb;
        }

        public ListViewAdapter(
                Context context, int itemLayout,
                List<LocalDB.Bookmark> list) {
            super(context, 0, list);
            this.inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            this.itemLayout = itemLayout;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder holder;
            if (v == null) {
                v = inflater.inflate(itemLayout, parent, false);
                TextView urlText = (TextView)v.findViewById(R.id.bookmarkUrl);
                //urlText.setPaintFlags(urlText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                TextView titleText = (TextView) v.findViewById(R.id.bookmarkTitle);
                titleText.setPaintFlags(urlText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                Button delBtn = (Button) v.findViewById(R.id.bookmarkDelBtn);
                DelBookmarkCallback delCb = new DelBookmarkCallback();
                delBtn.setOnClickListener(delCb);

                GoUrlCallback goUrlCb = new GoUrlCallback();
                urlText.setOnClickListener(goUrlCb);
                titleText.setOnClickListener(goUrlCb);

                holder = new ViewHolder();
                holder.urlText = urlText;
                holder.titleText = titleText;
                holder.goUrlCb = goUrlCb;
                holder.delCb = delCb;

                v.setTag(holder);
            } else {
                holder = (ViewHolder)v.getTag();
            }

            data = getItem(position);
            holder.titleText.setText(data.m_title);
            holder.urlText.setText(data.m_url);
            holder.goUrlCb.setUrl(data.m_url);
            holder.delCb.setBookmarkId(data.m_id);

            //holder.imageView.setImageResource(data.imageDrawableId);
            return v;
        }

        private class GoUrlCallback implements View.OnClickListener {
            private String m_url;
            private void setUrl(String url) {
                m_url = url;
            }
            @Override
            public void onClick(View v) {
                //Log.d("go url", m_url);
                Intent data = new Intent();
                data.putExtra("url", m_url);
                setResult(RESULT_OK, data);
                finish();
            }
        }

        private class DelBookmarkCallback implements View.OnClickListener {
            private int bookmark_id;
            private void setBookmarkId(int id) {
                bookmark_id = id;
            }
            @Override
            public void onClick(View v) {
                //Log.d("del bookmark", "id=" + bookmark_id);
                m_prefs.deleteBookmark(bookmark_id);

                m_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setupList();
                    }
                });
            }
        }
    }
}
