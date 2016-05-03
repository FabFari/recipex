package com.recipex.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appspot.recipex_1281.recipexServerApi.model.MainAddMeasurementMessage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.recipex.AppConstants;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import com.recipex.R;

import java.util.Locale;

import me.angrybyte.numberpicker.view.ActualNumberPicker;

public class AddMeasurement extends AppCompatActivity
        implements me.angrybyte.numberpicker.listener.OnValueChangeListener {

    private SwipeNumberPicker picker1;
    private SwipeNumberPicker picker2;
    private ActualNumberPicker float_picker;
    private ImageView icon;
    private TextView title;
    private LinearLayout linearLayout;
    private TextView text_picker1;
    private TextView text_picker2;
    private EditText bio;
    private RelativeLayout relativeLayout2;

    TextView picker_res1;
    TextView picker_res2;
    TextView float_picker_res;

    CircularProgressView progressView;

    // Input
    Long user_id = 5705241014042624L;
    String measurement_kind = AppConstants.DOLORE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

        bindActivity();
        setupUI();

        picker1.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
                boolean isValueOk = (newValue & 1) == 0;
                if (isValueOk) {
                    if(measurement_kind.equals(AppConstants.TEMP_CORPOREA))
                        picker_res1.setText(Integer.toString(newValue)+"C°");
                }

                return isValueOk;
            }
        });

        picker2.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
                boolean isValueOk = (newValue & 1) == 0;
                if (isValueOk) {
                    if(measurement_kind.equals(AppConstants.SPO2))
                        picker_res2.setText(Integer.toString(newValue)+"%");
                }

                return isValueOk;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_measurement, menu);
        return true;
    }

    @Override
    public void onValueChanged(int oldValue, int newValue) {
        if(measurement_kind.equals(AppConstants.SPO2)) {
            double percent = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*100.0;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f%%", percent));
        }
        else if(measurement_kind.equals(AppConstants.GLUCOSIO)){
            double percent = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*600.0;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f", percent));
        }
        else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)){
            double percent = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*15.0;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1fC°", percent));
        }
        else {
            double percent = ((double) newValue / (double) (float_picker.getMaxValue() - float_picker.getMinValue()))*800.0;
            float_picker_res.setText(String.format(Locale.getDefault(), "%.1f°", percent));
        }
    }

    private void bindActivity() {
        picker1 = (SwipeNumberPicker) findViewById(R.id.measurement_number_picker_1);
        picker2 = (SwipeNumberPicker) findViewById((R.id.measurement_number_picker_2));
        picker_res1 = (TextView) findViewById(R.id.measurement_number_picker_res1);
        picker_res2 = (TextView) findViewById(R.id.measurement_number_picker_res2);
        float_picker_res = (TextView) findViewById(R.id.measurement_number_float_picker);
        if(measurement_kind.equals(AppConstants.SPO2))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_spo2_number_picker);
        if(measurement_kind.equals(AppConstants.GLUCOSIO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_hgt_number_picker);
        if(measurement_kind.equals(AppConstants.TEMP_CORPOREA))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_temp_number_picker);
        if(measurement_kind.equals(AppConstants.COLESTEROLO))
            float_picker = (ActualNumberPicker) findViewById(R.id.measurement_chl_number_picker);
        icon = (ImageView) findViewById(R.id.measurement_icon);
        title = (TextView) findViewById(R.id.measurement_title);
        linearLayout = (LinearLayout) findViewById(R.id.measurement_linearlayout);
        text_picker1 = (TextView) findViewById(R.id.measurement_number_picker_text1);
        text_picker2 = (TextView) findViewById(R.id.measurement_number_picker_text2);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.measurement_relative2);
        progressView = (CircularProgressView) findViewById(R.id.measurement_progress_view);
        bio = (EditText) findViewById((R.id.measurement_crgv_bio));
    }

    private void setupUI() {
        if(measurement_kind.equals(AppConstants.PRESSIONE)) {
            text_picker2.setVisibility(View.VISIBLE);
            relativeLayout2.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.FREQ_CARDIACA)) {
            text_picker1.setText("Numero battiti:");
            picker1.setMinValue(0);
            picker1.setValue(80, true);
            picker1.setMaxValue(400);
            picker_res1.setText("80");
            icon.setImageResource(R.drawable.ic_heart_rate_dark);
            title.setText("Frequenza cardiaca");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.FREQ_RESPIRAZIONE)) {
            text_picker1.setText("Numero Battiti:");
            picker1.setMinValue(0);
            picker1.setValue(16, true);
            picker1.setMaxValue(200);
            picker_res1.setText("16");
            icon.setImageResource(R.drawable.ic_respirations_dark);
            title.setText("Frequenza respiratoria");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.SPO2)) {
            text_picker1.setText("Concentrazione di ossigeno:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("98%");
            icon.setImageResource(R.drawable.ic_spo2_dark);
            title.setText("Ossigenazione sanguigna");
            title.setTextSize(20);
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.GLUCOSIO)) {
            text_picker1.setText("HGT");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("150");
            icon.setImageResource(R.drawable.ic_diabetes_dark);
            title.setText("Glicemia");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)) {
            text_picker1.setText("Temperatura:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("36.5C°");
            icon.setImageResource(R.drawable.ic_temperature_dark);
            title.setText("Temperatura corporea");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else if(measurement_kind.equals(AppConstants.DOLORE)) {
            text_picker1.setText("Dolore:");
            picker1.setMinValue(0);
            picker1.setValue(0, true);
            picker1.setMaxValue(10);
            picker_res1.setText("0");
            float_picker_res.setText("36.5C°");
            icon.setImageResource(R.drawable.ic_pain_dark);
            title.setText("Scala del dolore");
            linearLayout.setVisibility(View.VISIBLE);
        }
        else {
            text_picker1.setText("Colesterolo:");
            picker1.setVisibility(View.INVISIBLE);
            picker_res1.setVisibility(View.INVISIBLE);
            float_picker.setVisibility(View.VISIBLE);
            float_picker_res.setVisibility(View.VISIBLE);
            float_picker.setListener(this);
            float_picker_res.setText("150");
            icon.setImageResource(R.drawable.ic_cholesterol_dark);
            title.setText("Temperatura corporea");
            linearLayout.setVisibility(View.VISIBLE);
        }
        progressView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.measurement_confirm) {
            MainAddMeasurementMessage content = new MainAddMeasurementMessage();
            content.setKind(measurement_kind);
            if(measurement_kind.equals(AppConstants.PRESSIONE)) {
                content.setSystolic(Long.parseLong(picker_res1.getText().toString()));
                content.setDiastolic(Long.parseLong(picker_res2.getText().toString()));
            }
            else if(measurement_kind.equals(AppConstants.FREQ_CARDIACA))
                content.setBpm(Long.parseLong(picker_res1.getText().toString()));
            else if(measurement_kind.equals(AppConstants.FREQ_RESPIRAZIONE))
                content.setRespirations(Long.parseLong(picker_res1.getText().toString()));
            else if(measurement_kind.equals(AppConstants.SPO2)) {
                String spo2 = float_picker_res.getText().toString();
                content.setSpo2(Double.parseDouble(spo2.substring(0,spo2.length()-1)));
            }
            else if(measurement_kind.equals(AppConstants.GLUCOSIO)) {
                content.setHgt(Double.parseDouble(float_picker_res.getText().toString()));
            }
            else if(measurement_kind.equals(AppConstants.TEMP_CORPOREA)){
                content.setDegrees(Double.parseDouble(float_picker_res.getText().toString()));
            }
            else if(measurement_kind.equals(AppConstants.DOLORE)) {
                content.setNrs(Long.parseLong(picker_res1.getText().toString()));
            }
            else {
                content.setChlLevel(Double.parseDouble(float_picker_res.getText().toString()));
            }
            content.setNote(bio.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }

}
