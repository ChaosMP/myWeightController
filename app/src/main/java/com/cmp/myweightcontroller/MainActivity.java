package com.cmp.myweightcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cmp.myweightcontroller.app.WeightControllerApp;
import com.cmp.myweightcontroller.model.WeightRecord;
import com.cmp.myweightcontroller.utils.WeightControllerUtils;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText weightEditText;
    private EditText dateEditText;
    private TableLayout weightTableLayout;
    private ImageView goalAchievedImageView;
    private ImageView goalAchievedBackground;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private AlertDialog goalAlertDialog;
    private AlertDialog newGoalAlertDialog;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weightEditText = findViewById(R.id.weightEditText);
        dateEditText = findViewById(R.id.dateEditText);
        weightTableLayout = findViewById(R.id.weightTableLayout);
        goalAchievedImageView = findViewById(R.id.goalAchievedImageView);
        goalAchievedBackground = findViewById(R.id.goalAchievedBackground);

        preferences = getSharedPreferences(WeightControllerUtils.APP_PREFERENCES, Context.MODE_PRIVATE);

        goalAlertDialog = new AlertDialog.Builder(MainActivity.this).create();
        goalAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window win = goalAlertDialog.getWindow();
                win.setContentView(R.layout.goal_layout);
                EditText goalValueEditText = win.findViewById(R.id.goalValueEditText);
                if (preferences != null) {
                    double weightGoal = preferences.getFloat(WeightControllerUtils.WEIGHT_GOAL_FIELD, 0.0f);
                    if (weightGoal != 0.0) {
                        goalValueEditText.setText(String.valueOf(weightGoal));
                    }
                }
                goalValueEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                Button saveWeightGoalButton = win.findViewById(R.id.saveWeightGoalButton);
                saveWeightGoalButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String goalStr = goalValueEditText.getText().toString().trim();
                        if (goalStr != null && !goalStr.equals("")) {
                            try {
                                double weightGoal = Double.parseDouble(goalStr);
                                if (weightGoal != 0.0) {
                                    preferences.edit()
                                            .putFloat(WeightControllerUtils.WEIGHT_GOAL_FIELD, (float) weightGoal)
                                            .apply();
                                    Toast.makeText(MainActivity.this, getString(R.string.goal_saved), Toast.LENGTH_SHORT).show();
                                } else {
                                    preferences.edit()
                                            .remove(WeightControllerUtils.WEIGHT_GOAL_FIELD)
                                            .apply();
                                }
                            } catch(NumberFormatException e) {
                                Toast.makeText(MainActivity.this, getString(R.string.goal_not_number), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            preferences.edit()
                                    .remove(WeightControllerUtils.WEIGHT_GOAL_FIELD)
                                    .apply();
                        }
                        goalAlertDialog.dismiss();
                    }
                });
            }
        });

        AlertDialog.Builder newGoalAlertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        newGoalAlertDialogBuilder.setTitle(R.string.goal);
        newGoalAlertDialogBuilder.setMessage(R.string.set_a_new_goal);
        newGoalAlertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goalAlertDialog.show();
            }
        });
        newGoalAlertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences.edit()
                        .remove(WeightControllerUtils.WEIGHT_GOAL_FIELD)
                        .apply();
            }
        });
        newGoalAlertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                preferences.edit()
                        .remove(WeightControllerUtils.WEIGHT_GOAL_FIELD)
                        .apply();
            }
        });
        newGoalAlertDialog = newGoalAlertDialogBuilder.create();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = dateEditText.getText().toString();
                int tmpYear;
                int tmpMonth;
                int tmpDay;
                if (date != null && !date.equals("")) {
                    tmpDay = selectedDay;
                    tmpMonth = selectedMonth;
                    tmpYear = selectedYear;
                } else {
                    Calendar dateNow = Calendar.getInstance();
                    tmpYear = dateNow.get(Calendar.YEAR);
                    tmpMonth = dateNow.get(Calendar.MONTH);
                    tmpDay = dateNow.get(Calendar.DAY_OF_MONTH);
                }
                new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                selectedYear = year;
                                selectedMonth = month;
                                selectedDay = dayOfMonth;
                                String selectedDate = "" + (dayOfMonth > 9 ? dayOfMonth : "0" + dayOfMonth) + "."
                                        + (month > 8 ? (month + 1) : "0" + (month + 1)) + "." + year;
                                dateEditText.setText(selectedDate);
                            }
                        }, tmpYear, tmpMonth, tmpDay).show();
            }
        });

        weightEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (focused)
                    keyboard.showSoftInput(weightEditText, 0);
                else
                    keyboard.hideSoftInputFromWindow(weightEditText.getWindowToken(), 0);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadWeightRecords();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // выводим меню для отображения кнопки настроек
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_goal) {
            goalAlertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkValue() {
        String weightStr = weightEditText.getText().toString().trim();

        if (weightStr == null || weightStr.equals("")) {
            Toast.makeText(this, getString(R.string.empty_weight), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            double weightDouble;
            try {
                weightDouble = Double.parseDouble(weightStr);
            } catch(NumberFormatException e) {
                Toast.makeText(this, getString(R.string.weight_not_number), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (weightDouble <= 0.0) {
                Toast.makeText(this, getString(R.string.weight_too_small), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    public void saveWeight(View view) {
        if (checkValue()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (selectedYear == 0 || selectedMonth == 0 || selectedDay == 0) {
                        Calendar dateNow = Calendar.getInstance();
                        selectedYear = dateNow.get(Calendar.YEAR);
                        selectedMonth = dateNow.get(Calendar.MONTH);
                        selectedDay = dateNow.get(Calendar.DAY_OF_MONTH);
                    }
                    WeightControllerApp
                            .getInstance()
                            .getDatabase()
                            .weightRecordDAO()
                            .addWeightRecord(new WeightRecord(0L,
                                    selectedYear,
                                    selectedMonth,
                                    selectedDay,
                                    Double.parseDouble(weightEditText.getText().toString().trim())));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weightEditText.setText("");
                            weightEditText.clearFocus();
                            dateEditText.setText("");
                            selectedYear = 0;
                            selectedMonth = 0;
                            selectedDay = 0;
                        }
                    });
                    loadWeightRecords();

                    double weightGoal = preferences.getFloat(WeightControllerUtils.WEIGHT_GOAL_FIELD, 0.0f);
                    if (weightGoal != 0.0) {
                        if (Double.parseDouble(weightEditText.getText().toString().trim()) <= weightGoal) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    goalAchievedBackground.setVisibility(View.VISIBLE);
                                    goalAchievedImageView.setVisibility(View.VISIBLE);
                                    goalAchievedImageView.animate().alpha(1.0f).setDuration(1000);
                                }
                            });
                        }
                    }

                }
            }).start();
        }
    }

    private void loadWeightRecords() {

        List<WeightRecord> weightRecordList = WeightControllerApp.getInstance().getDatabase().weightRecordDAO().getAllWeightRecords();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                weightTableLayout.removeAllViews();

                for (WeightRecord record : weightRecordList) {
                    View rowView = getLayoutInflater().inflate(R.layout.weight_row, null);
                    TableRow newRow = (TableRow) rowView.findViewById(R.id.weightTableRow);
                    TextView dateTextView = newRow.findViewById(R.id.dateTextView);
                    TextView weightTextView = newRow.findViewById(R.id.weightTextView);
                    ImageView deleteWeightRecordImageView = newRow.findViewById(R.id.deleteWeightRecordImageView);
                    String dateStr = "" + (record.getDay() > 9 ? record.getDay() : "0" + record.getDay()) + "."
                            + (record.getMonth() > 8 ? (record.getMonth() + 1) : "0" + (record.getMonth() + 1)) + "."
                            + record.getYear();
                    dateTextView.setText(dateStr);
                    weightTextView.setText(String.valueOf(record.getWeight()));
                    long id = record.getId();
                    deleteWeightRecordImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    WeightControllerApp
                                            .getInstance()
                                            .getDatabase()
                                            .weightRecordDAO()
                                            .deleteWeightRecord(id);
                                    loadWeightRecords();
                                }
                            }).start();
                        }
                    });

                    weightTableLayout.addView(rowView);
                }

            }
        });

    }

    public void goalAchievedClick(View view) {
        goalAchievedImageView.setAlpha(0.0f);
        goalAchievedImageView.setVisibility(View.GONE);
        goalAchievedBackground.setVisibility(View.GONE);
        newGoalAlertDialog.show();
    }
}