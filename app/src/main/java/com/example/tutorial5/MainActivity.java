package com.example.tutorial5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Version> versions = new ArrayList<Version>();
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        queue = Volley.newRequestQueue(this);
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://com.example.tutorial5.provider"),
                new String[]{"name","description","icon"}, null, null, "name");
        if(cursor.getCount() == 0) {
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    " https://raw.githubusercontent.com/kenobicjj/android/main/tutorial4.json ",
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            versions.clear();
                            for(int i = 0;i < response.length();i++) {
                                try {
                                    JSONObject item = response.getJSONObject(i);
                                    String name = item.getString("name");
                                    String description = item.getString("description");
                                    String icon = item.getString("icon");
                                    Version version =new Version(name, description, icon);
                                    versions.add(version);
                                    ContentValues values = new ContentValues(3);
                                    values.put("name", name);
                                    values.put("description", description);
                                    values.put("icon", icon);
                                    getContentResolver().insert(Uri.parse("content://com.example.tutorial5.provider"), values);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Toast.makeText(MainActivity.this,"Load from Repo",Toast.LENGTH_SHORT).show();
                            myAdapter.addElements(versions);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            );
            queue.add(request);
        } else {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                String name = cursor.getString(0);
                String description = cursor.getString(1);
                String icon = cursor.getString(2);
                Version version = new Version(name, description, icon);
                versions.add(version);
                cursor.moveToNext();
            }
            cursor.close();
            Toast.makeText(MainActivity.this,"Load from database",Toast.LENGTH_SHORT).show();
            myAdapter.addElements(versions);
        }

        queue = Volley.newRequestQueue(this);

        /*
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Attendance for Lecture")
                .setMessage("My name is Willy Liew")
                .setNeutralButton("Check ID", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"My ID is 1171101719",Toast.LENGTH_LONG).show();
                    }
                }).show();*/

    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<Version> elements = new ArrayList<Version>();
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rowView = getLayoutInflater().inflate(R.layout.row, parent, false);
            return new MyViewHolder(rowView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.rTitle.setText(elements.get(position).getName());
            holder.rDesc.setText(elements.get(position).getDescription());
            //holder.icon.setImageResource(elements.get(position).getIcon());
            String iconUrl = "https://raw.githubusercontent.com/kenobicjj/android/main/"+elements.get(position).getIcon();
            final LruCache<String, Bitmap> cache =new LruCache<String, Bitmap>(20);
            holder.icon.setImageUrl(iconUrl, new ImageLoader(queue, new ImageLoader.ImageCache() {
                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }
                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            }));
//

        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        public void addElements(ArrayList<Version> versions) {
            elements.clear();
            elements.addAll(versions);
            notifyDataSetChanged();
        }


        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView rTitle;
            public TextView rDesc;
            public NetworkImageView icon;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                rTitle = itemView.findViewById(R.id.title);
                rDesc = itemView.findViewById(R.id.desc);
                icon = itemView.findViewById(R.id.imageView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                String name = elements.get(getAdapterPosition()).getName();
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        }
    }
}
