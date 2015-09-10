package frogermcs.io.flatbuffs;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import frogermcs.io.flatbuffs.model.flat.Repo;
import frogermcs.io.flatbuffs.model.flat.ReposList;
import frogermcs.io.flatbuffs.model.json.RepoJson;
import frogermcs.io.flatbuffs.model.json.ReposListJson;
import frogermcs.io.flatbuffs.utils.RawDataReader;
import frogermcs.io.flatbuffs.utils.SimpleObserver;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.tvFlat)
    TextView tvFlat;
    @Bind(R.id.tvJson)
    TextView tvJson;

    @Bind(R.id.tvJsonNo)
    TextView tvJsonNo;

    private RawDataReader rawDataReader;

    private ReposListJson reposListJson;
    private ReposList reposListFlat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        rawDataReader = new RawDataReader(this);
    }

    @OnClick(R.id.btnJson)
    public void onJsonClick() {
        rawDataReader.loadJsonString(R.raw.repos_json).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String reposStr) {
                parseReposListJson(reposStr);
            }
        });
    }

    private void parseReposListJson(String reposStr) {
        long startTime = System.currentTimeMillis();
        reposListJson = new Gson().fromJson(reposStr, ReposListJson.class);
        for (int i = 0; i < reposListJson.repos.size(); i++) {
            RepoJson repo = reposListJson.repos.get(i);
            Log.d("FlatBuffers", "Repo #" + i + ", id: " + repo.id);
        }
        long endTime = System.currentTimeMillis() - startTime;
        tvJson.setText("Elements: " + reposListJson.repos.size() + ": load time: " + endTime + "ms");
    }


    //使用Json解析 没有bean
    @OnClick(R.id.btnJsonNo)
    public void onJsonWithoutBean(){
        rawDataReader.loadJsonString(R.raw.repos_json).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String reposStr) {
                parseWithoutBeanJson(reposStr);

//                parseMapGson(reposStr);
            }
        });
    }

    // Map解析时
    private void parseMapGson(String reposStr){
        long startTime = System.currentTimeMillis();
        GsonBuilder gb = new GsonBuilder();
        Gson g = gb.create();
        LinkedTreeMap<String, Object> map = g.fromJson(reposStr, new TypeToken<LinkedTreeMap<String, Object>>() {
        }.getType());

        ArrayList<HashMap<String,Object>> repos = (ArrayList<HashMap<String, Object>>) map.get("repos");
        int length = repos.size();

        for (int i = 0; i < length; i++) {
//            Log.d("FlatBuffers", "Repo #" + i + ", id: " + repos.get(i).get("id"));
        }

        long endTime = System.currentTimeMillis() - startTime;
        tvJsonNo.setText("Elements: " + repos.size() + ": load time: " + endTime + "ms");
    }



    private void parseWithoutBeanJson(String reposStr){
        try{
            long startTime = System.currentTimeMillis();
            JSONArray repos = new JSONObject(reposStr).getJSONArray("repos");
            int length = repos.length();
            for (int i = 0; i < length; i++) {
                JSONObject repo = (JSONObject) repos.get(i);
                Log.d("FlatBuffers", "Repo #" + i + ", id: " + repo.getString("id"));
            }
            long endTime = System.currentTimeMillis() - startTime;
            tvJsonNo.setText("Elements: " + reposListJson.repos.size() + ": load time: " + endTime + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnFlatBuffers)
    public void onFlatBuffersClick() {
        rawDataReader.loadBytes(R.raw.repos_flat).subscribe(new SimpleObserver<byte[]>() {
            @Override
            public void onNext(byte[] bytes) {
                loadFlatBuffer(bytes);
            }
        });
    }

    private void loadFlatBuffer(byte[] bytes) {
        long startTime = System.currentTimeMillis();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        reposListFlat = frogermcs.io.flatbuffs.model.flat.ReposList.getRootAsReposList(bb);
        for (int i = 0; i < reposListFlat.reposLength(); i++) {
            Repo repos = reposListFlat.repos(i);
            Log.d("FlatBuffers", "Repo #" + i + ", id: " + repos.id());
        }
        long endTime = System.currentTimeMillis() - startTime;
        tvFlat.setText("Elements: " + reposListFlat.reposLength() + ": load time: " + endTime + "ms");

    }
}