package com.example.moneta.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.moneta.DatabaseHelper;
import com.example.moneta.R;
import com.example.moneta.model.CategoryStat;
import com.example.moneta.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsActivity extends AppCompatActivity {

    private static final String TAG = "StatsActivity";

    private Toolbar toolbar;
    private Button startDateButton, endDateButton;
    private RadioGroup typeRadioGroup;
    private PieChart pieChart;
    private TextView totalTextView;

    private DatabaseHelper dbHelper;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Transaction.TransactionType currentType = Transaction.TransactionType.EXPENSE;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        toolbar = findViewById(R.id.toolbar_stats);
        startDateButton = findViewById(R.id.stats_start_date_button);
        endDateButton = findViewById(R.id.stats_end_date_button);
        typeRadioGroup = findViewById(R.id.stats_type_radio_group);
        pieChart = findViewById(R.id.stats_pie_chart);
        totalTextView = findViewById(R.id.stats_total_textview);

        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        setupInitialDates();
        setupDatePickers();
        setupTypeToggle();
        setupPieChart();
        loadStatsData();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistics");
        }
    }

    private void setupInitialDates() {
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        setCalendarTimeToStartOfDay(startCalendar);
        endCalendar = (Calendar) startCalendar.clone();
        endCalendar.add(Calendar.MONTH, 1);

        updateDateButtons();
    }

    private void setupDatePickers() {
        startDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendarToShow = isStartDate ? startCalendar : endCalendar;
        Calendar displayCalendar = (Calendar) calendarToShow.clone();
        if (!isStartDate) {
            displayCalendar.add(Calendar.DAY_OF_MONTH, -1);
        }


        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            Calendar selectedCalendar = isStartDate ? startCalendar : endCalendar;
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, monthOfYear);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (isStartDate) {
                setCalendarTimeToStartOfDay(selectedCalendar);
                if (endCalendar.before(startCalendar)) {
                    endCalendar = (Calendar) startCalendar.clone();
                    endCalendar.add(Calendar.MONTH, 1);
                }
            } else {
                setCalendarTimeToStartOfDay(selectedCalendar);
                selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            updateDateButtons();
            loadStatsData();
        };

        new DatePickerDialog(StatsActivity.this, dateSetListener,
                displayCalendar.get(Calendar.YEAR),
                displayCalendar.get(Calendar.MONTH),
                displayCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setupTypeToggle() {
        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.stats_income_radio_button) {
                currentType = Transaction.TransactionType.INCOME;
            } else {
                currentType = Transaction.TransactionType.EXPENSE;
            }
            loadStatsData();
        });
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }


    private void loadStatsData() {
        long startDateMillis = startCalendar.getTimeInMillis();
        long endDateMillis = endCalendar.getTimeInMillis();

        Log.d(TAG, "Loading stats for type: " + currentType + " from " + startDateMillis + " to " + endDateMillis);

        executor.execute(() -> {
            List<CategoryStat> stats = dbHelper.getCategoryStats(startDateMillis, endDateMillis, currentType);
            mainHandler.post(() -> {
                updatePieChart(stats);
            });
        });
    }

    private void updatePieChart(List<CategoryStat> stats) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        double totalAmount = 0;

        if (stats == null || stats.isEmpty()) {
            Log.d(TAG, "No stats data to display.");
            pieChart.clear();
            pieChart.setCenterText("No data available");
            pieChart.invalidate();
            totalTextView.setText(R.string.total_0_00);
            return;
        }

        for (CategoryStat stat : stats) {
            entries.add(new PieEntry((float) stat.getTotalAmount(), stat.getCategoryName()));
            totalAmount += stat.getTotalAmount();
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.setCenterText(currentType.toString());
        pieChart.highlightValues(null);
        pieChart.invalidate();

        String totalLabel = "Total " + currentType.toString().toLowerCase() + ":";
        totalTextView.setText(String.format(Locale.getDefault(), "%s %.2f", totalLabel, totalAmount));
    }


    private void updateDateButtons() {
        startDateButton.setText(dateFormatter.format(startCalendar.getTime()));
        Calendar displayEndCal = (Calendar) endCalendar.clone();
        displayEndCal.add(Calendar.DAY_OF_MONTH, -1);
        endDateButton.setText(dateFormatter.format(displayEndCal.getTime()));
    }

    private void setCalendarTimeToStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
