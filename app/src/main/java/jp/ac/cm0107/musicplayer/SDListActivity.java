package jp.ac.cm0107.musicplayer;

import static android.text.TextUtils.concat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SDListActivity extends AppCompatActivity {
    private ArrayAdapter adapter;
    private File nowPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdlist);

        adapter = new RowModelAdapter(this);
        File path = Environment.getExternalStorageDirectory();
        final File[] files = path.listFiles();
        nowPath = path;

        // fixme　現在のPath表示？
        TextView txtNowPath = findViewById(R.id.txtNowPath);
        txtNowPath.setText(path.getPath());

        if (files != null){
            for (int i = 0; i < files.length;i++){
                adapter.add(new RowModel(files[i]));
            }
        }


        ListView list = (ListView) findViewById(R.id.sdList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView list = (ListView) parent;
                RowModel item = (RowModel) list.getItemAtPosition(position);
                if (item.getFile().isFile()) {
                    Toast.makeText(SDListActivity.this,
                            item.getFile().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    intent.putExtra("SELECT_FILE", item.getFile().getAbsolutePath());
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    final File[] subFiles =  item.getFile().listFiles();
                    txtNowPath.setText(item.getFile().getPath());
                    adapter.clear();
                    nowPath = item.getFile().getParentFile();
                    adapter.add(new RowModel((nowPath.getAbsoluteFile())));
                    Log.i(item.getFile().getPath(), "onItemClick: ");
                    if (subFiles != null){
                        for (int i = 0; i < subFiles.length;i++){
                            adapter.add(new RowModel(subFiles[i]));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    class RowModelAdapter extends ArrayAdapter<RowModel>{
        public RowModelAdapter(@NonNull Context context){
            super(context,R.layout.row_item);
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            RowModel item = getItem(position);
            if (convertView == null){
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_item,null);
            }

            if (item != null){
                TextView txt1 = (TextView) convertView.findViewById(R.id.txtListFileName);
                if (txt1 != null){
                    if (item.getFile().getPath() == nowPath.getAbsoluteFile().getPath()){
                        txt1.setTextColor(Color.rgb(255,0,255));
                        txt1.setText(". .");
                    } else if (item.getFile().isFile()){
                        txt1.setTextColor(Color.rgb(	147,112,219));
                        txt1.setText(item.getFileName());
                    }else {
                        txt1.setTextColor(Color.rgb(175,238,238));
                        txt1.setText(item.getFileName()+" /");
                    }

                }
                TextView txt2 = (TextView) convertView.findViewById(R.id.txtListFileSize);
                if (txt2 != null){
                    txt2.setText(String.valueOf(item.getFileSize()));
                }
                ImageView img = (ImageView) convertView.findViewById(R.id.imgFolderFile);
                if (img != null){
                    if (item.getFile().isFile()){
                        img.setImageResource(R.drawable.baseline_insert_drive_file_24);
                    }else {
                        img.setImageResource(R.drawable.baseline_folder_24);
                    }
                }
            }
            return convertView;
        }
    }
}
