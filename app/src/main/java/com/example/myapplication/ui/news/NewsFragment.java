package com.example.myapplication.ui.news;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.ui.news.api.ApiClient;
import com.example.myapplication.ui.news.api.ApiInterface;
import com.example.myapplication.ui.news.models.Article;
import com.example.myapplication.ui.news.models.News;
import com.example.myapplication.ui.news.models.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String API_KEY = "4e6c5ec9-0c25-42f5-9738-5134600dd6b5";
    private RecyclerView recyclerView;
    private Article articles;
    private List<Result> results = new ArrayList<>();
    private Adapter adapter;
    private TextView topHeadline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_news, container, false);
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {

        super.onActivityCreated(savedInstance);

        swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.loadingScroller);

        topHeadline = getActivity().findViewById(R.id.topheadlines);
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        onLoadingSwipeRefresh();

        errorLayout = getActivity().findViewById(R.id.errorLayout);
        errorImage = getActivity().findViewById(R.id.errorImage);
        errorTitle = getActivity().findViewById(R.id.errorTitle);
        errorMessage = getActivity().findViewById(R.id.errorMessage);
        btnRetry = getActivity().findViewById(R.id.btnRetry);
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }

    public void LoadJson(String location) {

        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        if (location.isEmpty()) {
            location = "los angeles";
        }

        String[] tempKeywords = {location, "covid"};
        String concept = "https://en.wikipedia.org/wiki/Coronavirus";
        String country = "https://en.wikipedia.org/wiki/United_States";

        Call<News> call;
        call = apiInterface.getLocalSearch(
                tempKeywords,
                "body",
                "eng",
                "articles",
                country,
                concept,
                "date",
                "20",
                "-1",
                "skipDuplicates",
                API_KEY
        );

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && Objects.requireNonNull(response.body()).getArticles().getResults().size() != 0) {
                    if (!results.isEmpty()) {
                        results.clear();
                    }

                    articles = response.body().getArticles();
                    adapter = new Adapter(articles, getActivity());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    topHeadline.setVisibility(View.VISIBLE);
                } else {
                    topHeadline.setVisibility(View.INVISIBLE);

                    showErrorMessage(
                            R.drawable.no_result,
                            "No Result",
                            "Please Try Again!");
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<News> call, Throwable throwable) {
                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                showErrorMessage(
                        R.drawable.oops,
                        "Oops..",
                        "Network failure, Please Try Again\n"+
                                throwable.toString());
            }
        });
    }

    //Call NewsDetailActivity when item is selected
    private void initListener() {
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(getActivity(), com.example.myapplication.ui.news.NewsDetailActivity.class);

                Result result = articles.getResults().get(position);
                intent.putExtra("url", result.getUrl());
                intent.putExtra("title", result.getTitle());
                intent.putExtra("img", result.getImage());
                intent.putExtra("date", result.getPublishedAt());
                intent.putExtra("source", result.getSource().getTitle());
                intent.putExtra("author", result.getAuthor());

                Pair<View, String> pair = Pair.create((View) imageView,
                        ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(getActivity(), pair);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, optionsCompat.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });
    }

    private void onLoadingSwipeRefresh() {
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson("keyword");
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message){

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh();
            }
        });

    }
}