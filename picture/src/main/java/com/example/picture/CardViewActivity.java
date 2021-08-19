package com.example.picture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.picture.util.Utils;

public class CardViewActivity extends AppCompatActivity {
    private CardView cv_card; // 声明一个卡片视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        cv_card = findViewById(R.id.cv_card);
        initCardSpinner(); // 初始化卡片类型下拉框
    }

    // 初始化卡片类型下拉框
    private void initCardSpinner() {
        ArrayAdapter<String> cardAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, cardArray);
        Spinner sp_card = findViewById(R.id.sp_card);
        sp_card.setPrompt("请选择卡片类型");
        sp_card.setAdapter(cardAdapter);
        sp_card.setOnItemSelectedListener(new CardSelectedListener());
        sp_card.setSelection(0);
    }

    private String[] cardArray = {"圆角与阴影均为3", "圆角与阴影均为6", "圆角与阴影均为10",
            "圆角与阴影均为15", "圆角与阴影均为20"};
    private int[] radiusArray = {3, 6, 10, 15, 20};
    class CardSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            int radius = Utils.dip2px(CardViewActivity.this, radiusArray[arg2]);
            cv_card.setRadius(radius); // 设置卡片视图的圆角半径
            cv_card.setCardElevation(radius); // 设置卡片视图的阴影长度
            MarginLayoutParams params = (MarginLayoutParams) cv_card.getLayoutParams();
            // 设置布局参数的四周空白
            params.setMargins(radius, radius, radius, radius);
            cv_card.setLayoutParams(params); // 设置卡片视图的布局参数
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}