package com.example.event.widget;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;

import com.example.event.R;
import com.example.event.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BannerPager extends RelativeLayout {
    private static final String TAG = "BannerPager";
    private Context mContext; // 声明一个上下文对象
    private ViewPager vp_banner; // 声明一个翻页视图对象
    private RadioGroup rg_indicator; // 声明一个单选组对象
    private List<ImageView> mViewList = new ArrayList<>(); // 声明一个图像视图列表
    private int mInterval = 2000; // 轮播的时间间隔，单位毫秒
    private float mOffsetX, mOffsetY; // 横纵方向上的偏移
    private PointF mLastPos; // 上次落点的位置

    public BannerPager(Context context) {
        this(context, null);
    }

    public BannerPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(); // 初始化视图
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result;
        if (event.getAction() == MotionEvent.ACTION_DOWN) { // 按下手指
            mOffsetX = 0.0F;
            mOffsetY = 0.0F;
            mLastPos = new PointF(event.getX(), event.getY());
            result = super.onTouchEvent(event);
        } else { // 其余动作，包括移动手指、提起手指等等
            PointF thisPos = new PointF(event.getX(), event.getY());
            mOffsetX += Math.abs(thisPos.x - mLastPos.x); // x轴偏差
            mOffsetY += Math.abs(thisPos.y - mLastPos.y); // y轴偏差
            mLastPos = thisPos;
            if (mOffsetX >= mOffsetY) { // 水平方向的滚动
                // 如果外层是普通的ScrollView，则此处不允许父容器的拦截动作
                // CustomScrollActivity通过自定义滚动视图来区分水平滑动还是垂直滑动
                // DisallowScrollActivity使用滚动视图，则此处需要下面代码禁止父容器拦截
                getParent().requestDisallowInterceptTouchEvent(true);
                result = true; // 返回true表示要继续处理
            } else { // 垂直方向的滚动
                result = false; // 返回false表示不处理了
            }
        }
        return result;
    }

    // 开始广告轮播
    public void start() {
        mHandler.postDelayed(mScroll, mInterval); // 延迟若干秒后启动滚动任务
    }

    // 停止广告轮播
    public void stop() {
        mHandler.removeCallbacks(mScroll); // 移除滚动任务
    }

    // 设置广告轮播的时间间隔
    public void setInterval(int interval) {
        mInterval = interval;
    }

    // 设置广告图片列表
    public void setImage(List<Integer> imageList) {
        int dip_15 = Utils.dip2px(mContext, 15);
        // 根据图片列表生成图像视图列表
        for (int i = 0; i < imageList.size(); i++) {
            Integer imageResId = imageList.get(i); // 获取图片的资源编号
            ImageView iv = new ImageView(mContext); // 创建一个图像视图对象
            iv.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageResource(imageResId); // 设置图像视图的资源图片
            iv.setOnClickListener(v -> {
                int position = vp_banner.getCurrentItem(); // 获取翻页视图当前页面项的序号
                mListener.onBannerClick(position); // 触发点击监听器的onBannerClick方法
            });
            mViewList.add(iv); // 往视图列表添加新的图像视图
        }
        // 设置翻页视图的图像适配器
        vp_banner.setAdapter(new ImageAdapater());
        // 给翻页视图添加简单的页面变更监听器，此时只需重写onPageSelected方法
        vp_banner.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setSelectedButton(position); // 高亮显示该位置的指示按钮
            }
        });
        // 根据图片列表生成指示按钮列表
        for (int i = 0; i < imageList.size(); i++) {
            RadioButton radio = new RadioButton(mContext); // 创建一个单选按钮对象
            radio.setLayoutParams(new RadioGroup.LayoutParams(dip_15, dip_15));
            radio.setButtonDrawable(R.drawable.indicator_selector); // 设置单选按钮的资源图片
            rg_indicator.addView(radio); // 往单选组添加新的单选按钮
        }
        vp_banner.setCurrentItem(0); // 设置翻页视图显示第一页
        setSelectedButton(0); // 默认高亮显示第一个指示按钮
    }

    // 设置选中单选组内部的哪个单选按钮
    private void setSelectedButton(int position) {
        ((RadioButton) rg_indicator.getChildAt(position)).setChecked(true);
    }

    // 初始化视图
    private void initView() {
        // 根据布局文件banner_pager.xml生成视图对象
        View view = LayoutInflater.from(mContext).inflate(R.layout.banner_pager, null);
        vp_banner = view.findViewById(R.id.vp_banner);
        rg_indicator = view.findViewById(R.id.rg_indicator);
        addView(view); // 将该布局视图添加到广告轮播条
    }

    private Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    // 定义一个广告滚动任务
    private Runnable mScroll = new Runnable() {
        @Override
        public void run() {
            int index = vp_banner.getCurrentItem() + 1; // 获得下一张广告图的位置
            if (index >= mViewList.size()) { // 已经到末尾了，准备重头开始
                index = 0;
            }
            vp_banner.setCurrentItem(index); // 设置翻页视图显示第几页
            mHandler.postDelayed(this, mInterval); // 延迟若干秒后继续启动滚动任务
        }
    };

    // 定义一个图像翻页适配器
    private class ImageAdapater extends PagerAdapter {

        // 获取页面项的个数
        @Override
        public int getCount() {
            return mViewList.size();
        }

        // 判断当前视图是否来自指定对象
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // 从容器中销毁指定位置的页面
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }

        // 实例化指定位置的页面，并将其添加到容器中
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }
    }

    // 设置广告图的点击监听器
    public void setOnBannerListener(BannerClickListener listener) {
        mListener = listener;
    }

    // 声明一个广告图点击的监听器对象
    private BannerClickListener mListener;

    // 定义一个广告图片的点击监听器接口
    public interface BannerClickListener {
        void onBannerClick(int position);
    }

}