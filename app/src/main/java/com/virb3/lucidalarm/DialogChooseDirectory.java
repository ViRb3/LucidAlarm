package com.virb3.lucidalarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DialogChooseDirectory implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener
{
    public interface Result
    {
        void onChooseFile(File file);
    }

    AlertDialog _alertDialog;
    List<File> _fileEntries = new ArrayList<>();
    List<String> _allowedExtensions = Arrays.asList("mp3", "ogg", "wav");
    File _currentDirectory;
    Context _context;
    ListView _listView;
    Result _result = null;

    public class DirAdapter extends ArrayAdapter<File>
    {
        public DirAdapter(int resourceID)
        {
            super(_context, resourceID, _fileEntries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView textView = (TextView) super.getView(position, convertView, parent);

            if (_fileEntries.get(position) == null)
                textView.setText("...");
            else
                textView.setText(_fileEntries.get(position).getName());

            return textView;
        }
    }

    private void ListDirectories()
    {
        _fileEntries.clear();

        File[] files = _currentDirectory.listFiles();

        if (files != null)
            for (File file : files)
            {
                if (file.isFile())
                {
                    if (!file.getName().contains(".") || file.getName().endsWith("."))
                        continue;

                    String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());

                    if (!_allowedExtensions.contains(extension))
                        continue;
                }

                _fileEntries.add(file);
            }

        Collections.sort(_fileEntries, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            }
        });

        if (_currentDirectory.getParent() != null)
            _fileEntries.add(0, new File("..."));
    }

    public DialogChooseDirectory(Context context, Result result, String startDirectory)
    {
        _context = context;
        _result = result;

        if (startDirectory != null)
            _currentDirectory = new File(startDirectory);
        else
            _currentDirectory = Environment.getExternalStorageDirectory();

        ListDirectories();
        DirAdapter adapter = new DirAdapter(R.layout.listitem_row_textview);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose a ringtone");
        builder.setAdapter(adapter, this);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        _alertDialog = builder.create();
        _listView = _alertDialog.getListView();
        _listView.setOnItemClickListener(this);
        _alertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View list, int pos, long id)
    {
        if (pos < 0 || pos >= _fileEntries.size())
            return;

        File clickedFile = _fileEntries.get(pos);

        if (clickedFile.isFile())
        {
            _alertDialog.cancel();
            _result.onChooseFile(clickedFile);
            return;
        }

        if (_fileEntries.get(pos).getName().equals("..."))
            _currentDirectory = _currentDirectory.getParentFile();
        else
            _currentDirectory = _fileEntries.get(pos);

        ListDirectories();
        DirAdapter adapter = new DirAdapter(R.layout.listitem_row_textview);
        _listView.setAdapter(adapter);
    }

    public void onClick(DialogInterface dialog, int which)
    {
    }
}