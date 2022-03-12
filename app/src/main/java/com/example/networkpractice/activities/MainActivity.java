package com.example.networkpractice.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.networkpractice.R;
import com.example.networkpractice.data.MovieAdapter;
import com.example.networkpractice.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movies;
    private RequestQueue requestQueue;
    private EditText titleEditText;
    private Button searchBtn;
    private ProgressBar progressBar;

    String URL = "http://www.omdbapi.com/?apikey=[API-KEY]&s=";
    /*
        Ссылка для поиска с помощью сервиса omdbapi
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movies = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        titleEditText = findViewById(R.id.titleEditText);
        searchBtn = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEditText.getText().toString();              //Получаем вводные данные
                URL = URL + title;     //Компонируем ВД с ссылкой для поиска
                movies.clear();
                GetInformationFromInternet getInformationFromInternet = new GetInformationFromInternet();
                getInformationFromInternet.execute();
            }
        });
    }

    // Ищем фильмы по названию в потоке заднего плана, если задерживается то выводим progressBar
    public class GetInformationFromInternet extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                    URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("Search");
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String title = jsonObject.getString("Title");
                            String year = jsonObject.getString("Year");
                            String posterURL = jsonObject.getString("Poster");

                            Movie movie = new Movie();
                            movie.setPosterURL(posterURL);
                            movie.setTitle(title);
                            movie.setYear(year);
                            movies.add(movie);
                        }

                        movieAdapter = new MovieAdapter(MainActivity.this, movies);
                        recyclerView.setAdapter(movieAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            requestQueue.add(request);
            return null;
        }
    }
}