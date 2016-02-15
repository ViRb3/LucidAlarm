package com.virb3.lucidalarm;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private AlarmReceiver _alarmReceiver = new AlarmReceiver();

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

        Settings.Initialize(this);

        LoadSettings();

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
                AlarmPlayer.Play(this, Settings.RingDuration() * 1000);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        Intent intent = new Intent();
                        String packageName = getApplicationContext().getPackageName();
                        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);

                        if (!pm.isIgnoringBatteryOptimizations(packageName))
                        {
                            buttonView.setChecked(false);

                            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);

                            return;
                        }
                    }

                    SwitchControls(false);
                    _alarmReceiver.SetAlarms(MainActivity.this, Settings.Hours(), Settings.Minutes());
                }
                else
                {
                    SwitchControls(true);
                    StopAlarms();
                }

                Save();
            }
        });
    }

    private void SetupNumberListeners()
    {
        Map<EditText, String> editTexts = new HashMap<>();
        editTexts.put(_editRingCount, "ringCount");
        editTexts.put(_editRingDuration, "ringDuration");
        editTexts.put(_editRingInterval, "ringInterval");

        for (Object key : editTexts.keySet())
        {
            final EditText editText = (EditText) key;

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus && editText.getText().toString().equals(""))
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
                        Settings.SaveString("alarmSoundPath", file.getPath());

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

    private void StopAlarms()
    {
        _alarmReceiver.CancelAlarms(this);
    }

    public void LoadSettings()
    {
        _chkEnable.setChecked(Settings.Enabled());
        SwitchControls(!Settings.Enabled());

        _txtAlarmTimeActual.setText(String.format("%1$02d:%2$02d", Settings.Hours(), Settings.Minutes()));

        _txtCurrentAlarmSoundPath.setText(Settings.AlarmSoundPath());
        _editRingCount.setText(String.valueOf(Settings.RingCount()));
        _editRingDuration.setText(String.valueOf(Settings.RingDuration()));
        _editRingInterval.setText(String.valueOf(Settings.RingInterval()));

        TextView txtAlarmPath = (TextView) findViewById(R.id.txtCurrentAlarmSoundPath);
        String alarmPath = Settings.AlarmSoundPath();

        if (!new File(alarmPath).exists())
            alarmPath = "none";

        txtAlarmPath.setText(alarmPath);
    }

    public void Save()
    {
        SharedPreferences settings = getSharedPreferences("PREFERENCES", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("enabled", _chkEnable.isChecked());

        String[] data = _txtAlarmTimeActual.getText().toString().split(":");
        editor.putInt("hours", Integer.parseInt(data[0]));
        editor.putInt("minutes", Integer.parseInt(data[1]));

        editor.putString("alarmSoundPath", _txtCurrentAlarmSoundPath.getText().toString());
        editor.putInt("ringCount", Integer.parseInt(_editRingCount.getText().toString()));
        editor.putInt("ringDuration", Integer.parseInt(_editRingDuration.getText().toString()));
        editor.putInt("ringInterval", Integer.parseInt(_editRingInterval.getText().toString()));

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