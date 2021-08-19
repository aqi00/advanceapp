package com.example.threed;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.threed.panorama.PanoramaView;

public class PanoramaActivity extends AppCompatActivity {
    private final static String TAG = "PanoramaActivity";
    private PanoramaView pv_content; // 声明一个全景视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕常亮
        pv_content = findViewById(R.id.pv_content);
        pv_content.initRender(resArray[0]); // 设置全景视图的全景图片
        initExampleSpinner(); // 初始化样例下拉框
    }

    // 初始化样例下拉框
    private void initExampleSpinner() {
        ArrayAdapter<String> exampleAdapter = new ArrayAdapter<>(this,
                R.layout.item_select, exampleArray);
        Spinner sp_example = findViewById(R.id.sp_example);
        sp_example.setPrompt("请选择全景照片例子");
        sp_example.setAdapter(exampleAdapter);
        sp_example.setOnItemSelectedListener(new ExampleSelectedListener());
        sp_example.setSelection(0);
    }

    private String[] exampleArray = {"现代客厅", "中式客厅", "故宫风光", "城市街景",
            "鸟瞰城市", "俯拍高校", "私人会所", "酒店大堂"};
    private int[] resArray = {R.drawable.panorama01, R.drawable.panorama02, R.drawable.panorama03, R.drawable.panorama04,
            R.drawable.panorama05, R.drawable.panorama06, R.drawable.panorama07, R.drawable.panorama08};

    class ExampleSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
            pv_content.setDrawableId(resArray[arg2]); // 传入全景图片的资源编号
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

}
