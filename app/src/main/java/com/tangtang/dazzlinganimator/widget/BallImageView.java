package com.tangtang.dazzlinganimator.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tangtang.dazzlinganimator.R;

public class BallImageView extends FrameLayout {

    private ImageView img;

    public BallImageView(@NonNull Context context) {
        this(context, null);
    }

    public BallImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.widget_ball_view, this, true);
        img = view.findViewById(R.id.img);
    }

    public void bindData(int res){
        img.setImageResource(res);
    }
}
