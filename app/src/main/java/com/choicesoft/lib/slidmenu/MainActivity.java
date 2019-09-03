package com.choicesoft.lib.slidmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HorizontalExpandMenu expandMenu = findViewById(R.id.expandMenu);
        List list = new ArrayList();
        list.add("已结账");
        list.add("一点餐");
        list.add("正在");
        list.add("使用之");
        list.add("3哈哈哈");
        list.add("3");
        expandMenu.setLables(list, new HorizontalExpandMenu.onClickLabelListener() {
            @Override
            public void onClickLabel(String title, int index) {
                Toast.makeText(MainActivity.this,title,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
