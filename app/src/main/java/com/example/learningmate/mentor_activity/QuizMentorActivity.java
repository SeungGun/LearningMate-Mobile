package com.example.learningmate.mentor_activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.learningmate.mentee_activity.ProblemActivity;
import com.example.learningmate.R;
import com.example.learningmate.adapter.QuizRVAdapter;
import com.example.learningmate.model.Quiz;
import com.example.learningmate.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class QuizMentorActivity extends AppCompatActivity {

    private RecyclerView quizRecyclerView;
    private ArrayList<Quiz> quizArrayList;
    private QuizRVAdapter quizRVAdapter;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.arg2 == 1) {
                quizRVAdapter = new QuizRVAdapter(quizArrayList);
                quizRecyclerView.setAdapter(quizRVAdapter);
                quizRVAdapter.setOnItemClickListener(new QuizRVAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Intent intent = new Intent(getApplicationContext(), ProblemActivity.class);
                        intent.putExtra("data", quizArrayList.get(position));
                        startActivity(intent);
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_mentor);
        quizArrayList = new ArrayList<>();
        quizRecyclerView = findViewById(R.id.quiz_mentor_rv);
        quizRecyclerView.setHasFixedSize(true);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.back_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton homework_add_ib = findViewById(R.id.homework_add_ib);
        homework_add_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), QuizMakerActivity.class);
                startActivity(intent);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                getQuizList();
            }
        }).start();
    }

    public void getQuizList() {
        try {
            String url = "http://windry.dothome.co.kr/se_learning_mate/controller/quiz_controller.php";
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("get_quiz_uid", User.currentUser.getUserId())
                    .build();

            Request request = new Request
                    .Builder()
                    .url(url)
                    .post(formBody).build();

            Response response = client
                    .newCall(request)
                    .execute();

            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String data = body.string();
                    Log.d("data", data);
                    if (data.equals("None")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(QuizMentorActivity.this, "퀴즈가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        JSONArray jsonArray = new JSONArray(data);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                            quizArrayList.add(new Quiz(jsonObject.optString("quiz_id")
                                    , jsonObject.optString("quiz_owner")
                                    , jsonObject.optString("title")
                                    , Integer.parseInt(jsonObject.optString("time_limit"))
                                    , jsonObject.optString("quiz_grade").equals("null") ? -1 : Integer.parseInt(jsonObject.optString("quiz_grade"))
                                    , Integer.parseInt(jsonObject.optString("perfect_score"))));
                        }
                        Message message = handler.obtainMessage();
                        message.arg2 = 1;
                        handler.sendMessage(message);
                    }
                }
            } else {
                Log.d("response", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}