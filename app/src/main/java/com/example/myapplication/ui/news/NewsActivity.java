//package com.example.myapplication.ui.news;
//
//import android.app.SearchManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityOptionsCompat;
//import androidx.core.util.Pair;
//import androidx.core.view.ViewCompat;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.example.myapplication.ui.news.api.*;
//import com.example.myapplication.ui.news.models.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//import okhttp3.internal.Util;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import com.example.myapplication.R;
//
//public class NewsActivity extends AppCompatActivity
//        implements SwipeRefreshLayout.OnRefreshListener{
//
//    public static final String API_KEY = "4e6c5ec9-0c25-42f5-9738-5134600dd6b5";
//    private RecyclerView recyclerView;
//    private Article articles;
//    private List<Result> results = new ArrayList<>();
//    private Adapter adapter;
//    private String TAG = NewsActivity.class.getSimpleName();
//    private TextView topHeadline;
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_news);
//
//        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.setColorSchemeResources(R.color.loadingScroller);
//
//        topHeadline = findViewById(R.id.topheadlines);
//        recyclerView = findViewById(R.id.recyclerView);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NewsActivity.this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setNestedScrollingEnabled(false);
//
//        onLoadingSwipeRefresh();
//    }
//
//    @Override
//    public void onRefresh() {
//        LoadJson();
//    }
//
//    public void LoadJson() {
//
//        swipeRefreshLayout.setRefreshing(true);
//
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//
//        String[] tempKeywords = {"torrance", "coronavirus"};
//        String concept = "https://en.wikipedia.org/wiki/Coronavirus";
//
//        Call<News> call;
//        call = apiInterface.getLocalSearch(
//                tempKeywords,
//                "body",
//                "eng",
//                "articles",
//                concept,
//                "date",
//                "20",
//                "-1",
//                "skipDuplicates",
//                API_KEY
//        );
//
//        call.enqueue(new Callback<News>() {
//            @Override
//            public void onResponse(Call<News> call, Response<News> response) {
//                if (response.isSuccessful() && Objects.requireNonNull(response.body()).getArticles().getResults().size() != 0) {
//                    if (!results.isEmpty()) { results.clear(); }
//
//                    articles = response.body().getArticles();
//                    adapter = new Adapter(articles, NewsActivity.this);
//                    recyclerView.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
//
//                    initListener();
//
//                    topHeadline.setVisibility(View.VISIBLE);
//                } else {
//                    topHeadline.setVisibility(View.INVISIBLE);
//                }
//                swipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onFailure(Call<News> call, Throwable throwable) {
//                topHeadline.setVisibility(View.INVISIBLE);
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//    }
//
//    //Call NewsDetailActivity when item is selected
//    private void initListener() {
//        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                ImageView imageView = view.findViewById(R.id.img);
//                Intent intent = new Intent(NewsActivity.this, com.example.myapplication.ui.news.NewsDetailActivity.class);
//
//                Result result = articles.getResults().get(position);
//                intent.putExtra("url", result.getUrl());
//                intent.putExtra("title", result.getTitle());
//                intent.putExtra("img", result.getImage());
//                intent.putExtra("date", result.getPublishedAt());
//                intent.putExtra("source", result.getSource().getTitle());
//                intent.putExtra("author", result.getAuthor());
//
//                Pair<View, String> pair = Pair.create((View)imageView,
//                        ViewCompat.getTransitionName(imageView));
//                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
//                        .makeSceneTransitionAnimation(NewsActivity.this, pair);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    startActivity(intent, optionsCompat.toBundle());
//                } else {
//                    startActivity(intent);
//                }
//            }
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.news_menu_main, menu);
//        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
//
//        return true;
//    }
//
//    private void onLoadingSwipeRefresh() {
//        swipeRefreshLayout.post(
//                this::LoadJson
//        );
//    }
//}