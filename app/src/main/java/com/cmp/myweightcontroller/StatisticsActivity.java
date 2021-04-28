package com.cmp.myweightcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.cmp.myweightcontroller.app.WeightControllerApp;
import com.cmp.myweightcontroller.model.WeightRecord;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    Spinner graphModeSpinner;
    GraphView lineGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        lineGraphView = findViewById(R.id.lineGraphView);
        graphModeSpinner = findViewById(R.id.graphModeSpinner);

        ArrayList<String> graphModes = new ArrayList<>();
        graphModes.add(getString(R.string.month_progress));
        graphModes.add(getString(R.string.year_progress));
        graphModes.add(getString(R.string.all_progress));

        ArrayAdapter<String> graphModeAdapter = new ArrayAdapter<>(this, R.layout.graph_mode_spinner_item, graphModes);
        graphModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphModeSpinner.setAdapter(graphModeAdapter);

        graphModeSpinner.setSelection(0);

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
                        calendar.set(Calendar.YEAR, 2021);
                        calendar.set(Calendar.MONTH, 1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        break;
                }
                loadGraphData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

                    DataPoint[] dataPoints = new DataPoint[weightRecordList.size()];

                    Date firstDate = new Date();
                    Date lastDate = new Date();

                    for (int i = 0; i < weightRecordList.size(); i++) {

                        WeightRecord record = weightRecordList.get(i);

                        Date recordDate = new Date();

                        try {
                            recordDate = dateFormat.parse("" + record.getYear() + "-"
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

                        dataPoints[i] = new DataPoint(recordDate, record.getWeight());

                        if (i == 0) {
                            firstDate = recordDate;
                        } else if (i == weightRecordList.size() - 1) {
                            lastDate = recordDate;
                        }

                    }

                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);

                    Date finalFirstDate = firstDate;
                    Date finalLastDate = lastDate;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lineGraphView.removeAllSeries();
                            lineGraphView.addSeries(series);
                            lineGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(StatisticsActivity.this, dateFormat));
                            lineGraphView.getGridLabelRenderer().setNumHorizontalLabels(weightRecordList.size());
                            lineGraphView.getViewport().setXAxisBoundsManual(true);
                            lineGraphView.getViewport().setMinX(finalFirstDate.getTime());
                            lineGraphView.getViewport().setMaxX(finalLastDate.getTime());
                            lineGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);
                            lineGraphView.getGridLabelRenderer().setHumanRounding(false);
                        }
                    });

                }

            }
        }).start();
    }
}