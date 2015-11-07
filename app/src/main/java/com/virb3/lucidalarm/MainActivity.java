package com.virb3.lucidalarm;

import android.Manifest;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    SampleAlarmReceiver alarm = new SampleAlarmReceiver();

    public boolean Enabled()
    {
        return _chkEnable.isChecked();
    }

    public int Hours;
    public int Minutes;

    public String AlarmSoundPath()
    {
        return _txtCurrentAlarmSoundPath.getText().toString();
    }

    public int RingCount()
    {
        return Integer.parseInt(_editRingCount.getText().toString());
    }

    public int RingDuration()
    {
        return Integer.parseInt(_editRingDuration.getText().toString());
    }

    public int RingInterval()
    {
        return Integer.parseInt(_editRingInterval.getText().toString());
    }

    private CheckBox _chkEnable;
    private TextView _txtAlarmTimeActual;
    private TextView _txtCurrentAlarmSoundPath;
    private EditText _editRingCount;
    private EditText _editRingDuration;
    private EditText _editRingInterval;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _chkEnable = (CheckBox) findViewById(R.id.chkEnable);
        _txtAlarmTimeActual = (TextView) findViewById(R.id.txtAlarmTimeActual);
        _txtCurrentAlarmSoundPath = (TextView) findViewById(R.id.txtCurrentAlarmSoundPath);
        _editRingCount = (EditText) findViewById(R.id.editRingCount);
        _editRingDuration = (EditText) findViewById(R.id.editRingDuration);
        _editRingInterval = (EditText) findViewById(R.id.editRingInterval);

        Load();

        SetupEnableCheckbox();
        SetupNumberListeners();
        SetupTimeSelector();
        SetupSelectButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_preview:
                SampleSchedulingService.PlayAlarm(this, RingDuration() * 1000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void SetupEnableCheckbox()
    {
        _chkEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    SwitchControls(false);
                    StartAlarm();
                } else
                {
                    SwitchControls(true);
                    StopAlarm();
                }

                Save();
            }
        });
    }

    private void SetupNumberListeners()
    {
        final SharedPreferences settings = getSharedPreferences("PREFERENCES", 0);

        Map<EditText, String> editTexts = new HashMap<>();
        editTexts.put(_editRingCount, "ringCount");
        editTexts.put(_editRingDuration, "ringDuration");
        editTexts.put(_editRingInterval, "ringInterval");

        for (Object key : editTexts.keySet())
        {
            final EditText editText = (EditText) key;
            String preferenceName = editTexts.get(key);

            editText.setText(Integer.toString(settings.getInt(preferenceName, 0)));

            editText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    if (s.toString().equals(""))
                        editText.setText("0");

                    Save();
                }
            });
        }
    }

    private void SetupTimeSelector()
    {
        _txtAlarmTimeActual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
    }

    private void SetupSelectButton()
    {
        Button btnSelect = (Button) findViewById(R.id.btnCurrentAlarmSelect);
        btnSelect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int readPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                int writePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    return;
                }

                DialogChooseDirectory.Result result = new DialogChooseDirectory.Result()
                {

                    @Override
                    public void onChooseFile(File file)
                    {
                        SharedPreferences settings = getSharedPreferences("PREFERENCES", 0);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putString("alarmPath", file.getPath());
                        editor.apply();

                        TextView alarmPath = (TextView) findViewById(R.id.txtCurrentAlarmSoundPath);
                        alarmPath.setText(file.getPath());
                    }
                };

                new DialogChooseDirectory(MainActivity.this, result, null);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 0:
                if (grantResults.length != 2)
                    return;

                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    return;

                if (grantResults[1] != PackageManager.PERMISSION_GRANTED)
                    return;

                Button btnSelect = (Button) findViewById(R.id.btnCurrentAlarmSelect);
                btnSelect.callOnClick();

                break;
        }
    }

    private void StartAlarm()
    {
        int currentMinutes = Minutes;
        int currentSeconds = 0;

        for (int i = 1; i <= RingCount(); i++)
        {
            alarm.SetAlarm(this, Hours, currentMinutes, currentSeconds, i);
            currentSeconds += RingInterval() + RingDuration();

            while (currentSeconds >= 60)
            {
                currentMinutes++;
                currentSeconds -= 60;
            }
        }
    }

    private void StopAlarm()
    {
        alarm.CancelAlarm(this);
    }

    public void Load()
    {
        SharedPreferences settings = getSharedPreferences("PREFERENCES", 0);

        _chkEnable.setChecked(settings.getBoolean("enabled", false));
        SwitchControls(!Enabled());

        Hours = settings.getInt("hours", 3);
        Minutes = settings.getInt("minutes", 0);
        _txtAlarmTimeActual.setText(String.format("%1$02d:%2$02d", Hours, Minutes));

        _txtCurrentAlarmSoundPath.setText(settings.getString("alarmSoundPath", "none"));
        _editRingCount.setText(String.valueOf(settings.getInt("ringCount", 3)));
        _editRingDuration.setText(String.valueOf(settings.getInt("ringDuration", 4)));
        _editRingInterval.setText(String.valueOf(settings.getInt("ringInterval", 10)));

        TextView txtAlarmPath = (TextView) findViewById(R.id.txtCurrentAlarmSoundPath);
        String alarmPath = settings.getString("alarmPath", "");

        if (!new File(alarmPath).exists())
            alarmPath = "";

        txtAlarmPath.setText(alarmPath.equals("") ? "none" : alarmPath);
    }

    public void Save()
    {
        SharedPreferences settings = getSharedPreferences("PREFERENCES", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("enabled", Enabled());

        String[] data = _txtAlarmTimeActual.getText().toString().split(":");
        editor.putInt("hours", Integer.parseInt(data[0]));
        editor.putInt("minutes", Integer.parseInt(data[1]));

        editor.putString("alarmSoundPath", AlarmSoundPath());
        editor.putInt("ringCount", RingCount());
        editor.putInt("ringDuration", RingDuration());
        editor.putInt("ringInterval", RingInterval());

        editor.apply();
    }

    private void SwitchControls(boolean enabled)
    {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.controlsLayout);
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            View child = layout.getChildAt(i);
            child.setEnabled(enabled);
        }
    }
}