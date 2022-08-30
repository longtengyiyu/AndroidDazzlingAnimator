package com.tangtang.dazzlinganimator.bean;


import com.tangtang.dazzlinganimator.R;

public class Ball implements Displayable{
    private String url;
    private String local;

    @Override
    public int resource() {
        return R.mipmap.ic_default;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String local() {
        return local;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
