package com.tangtang.dazzlinganimator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.tangtang.dazzlinganimator.bean.Ball;
import com.tangtang.dazzlinganimator.widget.PoolBallView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PoolBallView poolBallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        poolBallView = findViewById(R.id.view_pool_ball);
        loadData();
        rock();
    }

    private void loadData(){
        List<Ball> ballList = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            Ball ball = new Ball();
//            ball.setUrl("net url");
//            ball.setLocal("local path");
            ballList.add(ball);
        }
        poolBallView.init(ballList);
    }

    private void rock(){
        poolBallView.postDelayed(() -> {
            poolBallView.getBallView().rockBallByImpulse();
            rock();
        }, 10000L);
    }
}