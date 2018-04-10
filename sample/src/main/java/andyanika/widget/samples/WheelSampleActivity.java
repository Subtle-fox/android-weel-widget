package andyanika.widget.samples;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sample.ui.roller.R;

import java.lang.ref.WeakReference;
import java.util.Random;

import andyanika.widget.wheel.WheelView;

public class WheelSampleActivity extends AppCompatActivity {
    private static final int INCREMENT = 10;
    private static final int DECREMENT = 20;
    private static final int DEFAULT_DELAY = 1000;
    private static final String EXTRA_CURRENT_VALUE = "current_value";

    private UpdateHandler updateHandler;
    private WheelView rollerView;
    private TextView labelCurrentValue;
    private EditText editCurrentValue;
    private ToggleButton btnIncrement;
    private ToggleButton btnDecrement;
    private int delay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roller);
        bindControls();
        delay = Math.max(rollerView.getAnimationDuration() + 100, DEFAULT_DELAY);
        updateHandler = new UpdateHandler(this, delay);
        labelCurrentValue.setText(getString(R.string.label_current, 0));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int lastValue = savedInstanceState.getInt(EXTRA_CURRENT_VALUE);
        labelCurrentValue.setText(getString(R.string.label_current, lastValue));
        rollerView.setInitialValue(lastValue);

        if (btnIncrement.isChecked()) {
            updateHandler.sendEmptyMessageDelayed(INCREMENT, delay);
        } else if (btnDecrement.isChecked()) {
            updateHandler.sendEmptyMessageDelayed(DECREMENT, delay);
        }
    }

    private void bindControls() {
        rollerView = findViewById(R.id.roller);
        int rollersCount = rollerView.getRollersCount();

        labelCurrentValue = findViewById(R.id.label_current);
        editCurrentValue = findViewById(R.id.edit_value);
        editCurrentValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(rollersCount)});
        btnIncrement = findViewById(R.id.btn_increment);
        btnDecrement = findViewById(R.id.btn_decrement);

        btnIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateHandler.removeCallbacksAndMessages(null);
                if (btnDecrement.isChecked()) {
                    btnDecrement.setChecked(false);
                }
                if (btnIncrement.isChecked()) {
                    updateHandler.sendEmptyMessage(INCREMENT);
                }
            }
        });

        btnDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateHandler.removeCallbacksAndMessages(null);
                if (btnIncrement.isChecked()) {
                    btnIncrement.setChecked(false);
                }
                if (btnDecrement.isChecked()) {
                    updateHandler.sendEmptyMessage(DECREMENT);
                }
            }
        });

        findViewById(R.id.btn_set_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoUpdate();
                String txtValue = editCurrentValue.getText().toString();
                if (!txtValue.isEmpty()) {
                    setNewValue(Integer.parseInt(txtValue));
                }
            }
        });

        findViewById(R.id.btn_random).setOnClickListener(new View.OnClickListener() {
            final Random rnd = new Random();

            @Override
            public void onClick(View v) {
                stopAutoUpdate();
                setNewValue(rnd.nextInt(rollerView.getMax()));
            }
        });
    }

    @Override
    protected void onDestroy() {
        updateHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CURRENT_VALUE, rollerView.getValue());
    }

    private void setNewValue(int newValue) {
        rollerView.setValue(newValue);
        updateText();
    }

    private void decrement() {
        rollerView.previous();
        updateText();
    }

    private void increment() {
        rollerView.next();
        updateText();
    }

    private void updateText() {
        labelCurrentValue.setText(getString(R.string.label_current, rollerView.getValue()));
        editCurrentValue.setText(null);
    }

    private void stopAutoUpdate() {
        updateHandler.removeCallbacksAndMessages(null);
        btnIncrement.setChecked(false);
        btnDecrement.setChecked(false);
    }

    private static class UpdateHandler extends Handler {
        private final WeakReference<WheelSampleActivity> activityWeakReference;
        private final int delay;

        UpdateHandler(WheelSampleActivity wheelSampleActivity, int delay) {
            activityWeakReference = new WeakReference<>(wheelSampleActivity);
            this.delay = delay;
        }

        @Override
        public void handleMessage(Message msg) {
            WheelSampleActivity wheelSampleActivity = activityWeakReference.get();
            if (wheelSampleActivity == null) {
                return;
            }

            if (msg.what == INCREMENT) {
                wheelSampleActivity.increment();
                sendEmptyMessageDelayed(INCREMENT, delay);
            } else if (msg.what == DECREMENT) {
                wheelSampleActivity.decrement();
                sendEmptyMessageDelayed(DECREMENT, delay);
            }
        }
    }
}