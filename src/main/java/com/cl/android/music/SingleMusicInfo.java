package com.cl.android.music;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chenling on 2016/3/17.
 */
public class SingleMusicInfo extends PopupWindow {
    private View view;
    private TableLayout tableLayout;

    public SingleMusicInfo(Context context,Map<String, Object> map) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.singlemusicinfo, null);
        //取消按钮
      /*  view.findViewById(R.id.singlemusicinfo_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });*/

        tableLayout = (TableLayout) view.findViewById(R.id.tablelayout);

        map.remove("bitmap");//移除图片那个键值对
        for(String keys : map.keySet()){
            TableRow tableRow = new TableRow(context);
            TextView key = new TextView(context);
            TextView value = new TextView(context);
            Log.i("MusicPlayerService", "SingleMusicInfo..........." + tableRow.hashCode());
            key.setText("   " + keys + "    ");
            value.setText(map.get(keys).toString());
            tableRow.addView(key);
            tableRow.addView(value);
            tableLayout.addView(tableRow);
        }

        //设置SingleMusicInfo的View
        this.setContentView(view);
        //设置弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置S弹出窗体可点击
        this.setFocusable(true);
        //实例化一个ColorDrawable颜色为半透明
//        ColorDrawable dw = new ColorDrawable(0xb0000000);
        ColorDrawable dw = new ColorDrawable(Color.rgb(255,228,181));
        //设置弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //view添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        view.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.tablelayout).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });

    }
}
