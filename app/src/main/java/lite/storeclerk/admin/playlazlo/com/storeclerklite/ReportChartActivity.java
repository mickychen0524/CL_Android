package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AppHelper;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.DayAxisValueFormatter;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.MyAxisValueFormatter;

/**
 * Created by mymac on 6/20/17.
 */

public class ReportChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {

    private ProgressDialog mProgressDialog;
    private JSONObject receivedObj;
    private double amount;

    protected BarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_chart_layout);

        mChart = (BarChart) findViewById(R.id.report_bar_chart);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);

        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mChart);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

        IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        getChartDataFromServer();
    }

    public void onBackAction(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("backView", "back");

        startActivity(i);
        finish();
    }

    private void getChartDataFromServer() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Getting data...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    receivedObj = APIInterface.getChartData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {

                                List<JSONObject> reportDataArr = new ArrayList<JSONObject>();

                                try {
                                    JSONArray jsonArr = receivedObj.getJSONArray("data");
                                    reportDataArr = AppHelper.parseFromJsonList(jsonArr);
                                    ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
                                    if (jsonArr.length() > 0) {
                                        for (JSONObject reportItem : reportDataArr) {
                                            yVals1.add(new BarEntry((float)reportDataArr.indexOf(reportItem), Float.parseFloat(reportItem.getString("value"))));
                                        }
                                    }

                                    BarDataSet set1;

                                    if (mChart.getData() != null &&
                                            mChart.getData().getDataSetCount() > 0) {
                                        set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
                                        set1.setValues(yVals1);
                                        mChart.getData().notifyDataChanged();
                                        mChart.notifyDataSetChanged();
                                    } else {
                                        set1 = new BarDataSet(yVals1, "Monthly Sales");

                                        set1.setDrawIcons(false);

                                        set1.setColors(ColorTemplate.MATERIAL_COLORS);

                                        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                                        dataSets.add(set1);

                                        BarData data = new BarData(dataSets);
                                        data.setValueTextSize(10f);
                                        data.setBarWidth(0.9f);

                                        mChart.setData(data);
                                    }


                                } catch (JSONException e) {
                                    Log.d("json_e-->", e.getMessage());
                                }
                            } else {
                                Log.e("game_list--->", "receivedObj is null");
                                new PromptDialog(ReportChartActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Get Report Error")
                                        .setContentText("Oops! Our services are not responding, try back later.")
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    });
                } catch (Exception e) {
                    Log.e("game_list--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            new PromptDialog(ReportChartActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Get Report Error")
                                    .setContentText("Oops! Our services are not responding, try back later.")
                                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });

                }
            }
        }).start();
    }

    protected RectF mOnValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;

        RectF bounds = mOnValueSelectedRectF;
        mChart.getBarBounds((BarEntry) e, bounds);
        MPPointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        Log.i("x-index",
                "low: " + mChart.getLowestVisibleX() + ", high: "
                        + mChart.getHighestVisibleX());

        MPPointF.recycleInstance(position);
    }

    @Override
    public void onNothingSelected() { }
}
