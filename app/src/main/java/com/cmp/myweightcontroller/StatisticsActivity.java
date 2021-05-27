package com.cmp.myweightcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cmp.myweightcontroller.app.WeightControllerApp;
import com.cmp.myweightcontroller.model.WeightRecord;
import com.cmp.myweightcontroller.utils.WeightControllerUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private Spinner graphModeSpinner;
    private LineChart weightLineChart;
    private TextView weightChangeTextView;
    private TextView minWeightTextView;
    private TextView maxWeightTextView;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        weightLineChart = findViewById(R.id.weightLineChart);
        graphModeSpinner = findViewById(R.id.graphModeSpinner);
        weightChangeTextView = findViewById(R.id.weightChangeTextView);
        minWeightTextView = findViewById(R.id.minWeightTextView);
        maxWeightTextView = findViewById(R.id.maxWeightTextView);

        preferences = getSharedPreferences(WeightControllerUtils.APP_PREFERENCES, Context.MODE_PRIVATE);

        ArrayList<String> graphModes = new ArrayList<>();
        graphModes.add(getString(R.string.month_progress));
        graphModes.add(getString(R.string.year_progress));
        graphModes.add(getString(R.string.all_progress));

        ArrayAdapter<String> graphModeAdapter = new ArrayAdapter<>(this, R.layout.graph_mode_spinner_item, graphModes);
        graphModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphModeSpinner.setAdapter(graphModeAdapter);

        graphModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar calendar = Calendar.getInstance();
                switch (position) {
                    case 0:
                        calendar.add(Calendar.MONTH, -1);
                        break;
                    case 1:
                        calendar.add(Calendar.YEAR, -1);
                        break;
                    case 2:
                        calendar.set(Calendar.YEAR, 1970);
                        calendar.set(Calendar.MONTH, 0);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        break;
                }
                loadGraphData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        graphModeSpinner.setSelection(0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadGraphData(int year, int month, int day) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<WeightRecord> weightRecordList = WeightControllerApp
                        .getInstance()
                        .getDatabase()
                        .weightRecordDAO()
                        .getWeightRecordsAfter(year, month, day);

                if (weightRecordList.size() > 0) {

                    Date firstDate;
                    Date lastDate;
                    final double weightChange = Math.round((weightRecordList.get(weightRecordList.size() - 1).getWeight()
                            - weightRecordList.get(0).getWeight()) * 100.0) / 100.0;

                    try {
                        firstDate = dateFormat.parse("" + weightRecordList.get(0).getYear() + "-"
                                + (weightRecordList.get(0).getMonth() < 9
                                ? "0" + (weightRecordList.get(0).getMonth() + 1)
                                : (weightRecordList.get(0).getMonth() + 1)) + "-"
                                + (weightRecordList.get(0).getDay() < 10
                                ? "0" + weightRecordList.get(0).getDay()
                                : weightRecordList.get(0).getDay()));
                        lastDate = dateFormat.parse("" + weightRecordList.get(weightRecordList.size() - 1).getYear() + "-"
                                + (weightRecordList.get(weightRecordList.size() - 1).getMonth() < 9
                                ? "0" + (weightRecordList.get(weightRecordList.size() - 1).getMonth() + 1)
                                : (weightRecordList.get(weightRecordList.size() - 1).getMonth() + 1)) + "-"
                                + (weightRecordList.get(weightRecordList.size() - 1).getDay() < 10
                                ? "0" + weightRecordList.get(weightRecordList.size() - 1).getDay()
                                : weightRecordList.get(weightRecordList.size() - 1).getDay()));
                    } catch (ParseException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(StatisticsActivity.this, getString(R.string.error_load_stats_dates), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    int daysCount = (int) ((lastDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000)) + 1;
                    List<Date> dateLabels = new ArrayList<>();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(firstDate);

                    for (int i = 0; i < daysCount; i++) {
                        dateLabels.add(calendar.getTime());
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    List<Entry> entries = new ArrayList<>();

                    for (WeightRecord record : weightRecordList) {

                        Date tmpDate;
                        try {
                            tmpDate = dateFormat.parse("" + record.getYear() + "-"
                                    + (record.getMonth() < 9 ? "0" + (record.getMonth() + 1) : (record.getMonth() + 1)) + "-"
                                    + (record.getDay() < 10 ? "0" + record.getDay() : record.getDay()));
                        } catch (ParseException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(StatisticsActivity.this, getString(R.string.error_load_stats_dates), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        int pointIndex = dateLabels.indexOf(tmpDate);

                        if (pointIndex == -1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(StatisticsActivity.this, getString(R.string.error_load_stats_dates), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        } else {
                            entries.add(new Entry(pointIndex, (float) record.getWeight()));
                        }

                    }

                    LineDataSet dataSet = new LineDataSet(entries, getString(R.string.weight));
                    dataSet.setColor(ResourcesCompat.getColor(getResources(), R.color.chart_line_color, null));
                    dataSet.setCircleColor(ResourcesCompat.getColor(getResources(), R.color.chart_circle_color, null));
                    dataSet.setLineWidth(5f);
                    dataSet.setDrawCircleHole(false);
                    LineData lineData = new LineData(dataSet);
                    lineData.setDrawValues(false);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Description description = new Description();
                            description.setText("");
                            weightLineChart.setDescription(description);
                            weightLineChart.setNoDataText(getString(R.string.no_data_in_chart));
                            weightLineChart.setData(lineData);
                            weightLineChart.getLegend().setEnabled(false);

                            ValueFormatter formatter = new ValueFormatter() {
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    if ((int) value < dateLabels.size() && (int) value >= 0) {
                                        return dateFormat.format(dateLabels.get((int) value));
                                    } else {
                                        return "" + value;
                                    }
                                }
                            };

                            XAxis xAxis = weightLineChart.getXAxis();
                            YAxis yAxis = weightLineChart.getAxisLeft();
                            YAxis yAxisR = weightLineChart.getAxisRight();

                            yAxisR.setDrawLabels(false);

                            yAxis.setDrawAxisLine(true);
                            yAxis.setAxisLineWidth(3f);
                            yAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.light_gray, null));

                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setGranularity(1f);
                            xAxis.setDrawAxisLine(true);
                            xAxis.setAxisLineWidth(3f);
                            xAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.light_gray, null));
                            xAxis.setLabelCount(4);
                            xAxis.setValueFormatter(formatter);
//                            xAxis.setTextSize(11f);

                            weightLineChart.invalidate();

                            weightChangeTextView.setText(String.valueOf(weightChange));
                            double weightGoal = preferences.getFloat(WeightControllerUtils.WEIGHT_GOAL_FIELD, 0.0f);
                            if (weightGoal != 0.0f
                                    && Math.abs(weightGoal - dataSet.getValues().get(0).getY())
                                        >= Math.abs(weightGoal - (dataSet.getValues().get(0).getY() + weightChange))) {
                                weightChangeTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dark_green, null));
                            } else if (weightGoal != 0.0f
                                    && Math.abs(weightGoal - dataSet.getValues().get(0).getY())
                                    < Math.abs(weightGoal - (dataSet.getValues().get(0).getY() + weightChange))) {
                                weightChangeTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dark_red, null));
                            }
                            dataSet.getValues().get(0).getY();
                            minWeightTextView.setText(String.valueOf(dataSet.getYMin()));
                            maxWeightTextView.setText(String.valueOf(dataSet.getYMax()));
                        }
                    });

                }

            }
        }).start();
    }
}