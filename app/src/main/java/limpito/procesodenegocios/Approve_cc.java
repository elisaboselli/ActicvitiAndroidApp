package limpito.procesodenegocios;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Approve_cc extends AppCompatActivity {

    private String TAG = Approve_cc.class.getSimpleName();

    private final int GET_TASKS = 1;
    private final int GET_PINSTANCE = 2;
    private final int POST_TASK = 3;

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";

    private static String ip;
    private static String url_get;
    private static String url_post;
    private static String user;
    private static String password;

    private ProgressDialog pDialog;

    private ListView listView;
    private ArrayList<ActivitiTask> taskList = new ArrayList<>();
    private ActivitiTaskAdapter adapter;

    private JsonObjectRequest jsonRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_cc);

        getProperties();

        listView = findViewById(R.id.id_task_list);
        adapter = new ActivitiTaskAdapter(Approve_cc.this,taskList);
        listView.setAdapter(adapter);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        getTaskRequest();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final ActivitiTask at = taskList.get(position);

                AlertDialog alertDialog = new AlertDialog.Builder(Approve_cc.this).create();
                alertDialog.setTitle(at.getPrintableName());
                alertDialog.setMessage(at.getDescription());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Approve",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //PostTaskParams params = new PostTaskParams(at.getId(), position, false);
                                //new PostTask().execute(params);
                                JSONObject body = new JSONObject();
                                try {
                                    body.put("action","complete");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                postTaskRequest(body,at.getId(),position);
                                clearList();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Deny",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PostTaskParams params = new PostTaskParams(at.getId(), position, false);
                                new PostTask().execute(params);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alertDialog.show();
            }
        });
    }

    private void getProperties(){
        try {
            ip = PropertiesUtil.getProperty("http", getApplicationContext()) + PropertiesUtil.getProperty("ip", getApplicationContext());
            url_get = ip + PropertiesUtil.getProperty("get", getApplicationContext()) + PropertiesUtil.getProperty("user", getApplicationContext());
            url_post = ip + PropertiesUtil.getProperty("post", getApplicationContext()); // + taskId

            user = PropertiesUtil.getProperty("user", getApplicationContext());
            password = PropertiesUtil.getProperty("pass", getApplicationContext());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void parseJSONResponse(JSONObject jsonResponse){
        if (jsonResponse != null) {
            try {
                JSONArray tasks = jsonResponse.getJSONArray("data");
                for (int i = 0; i < tasks.length(); i++) {
                    JSONObject obj = tasks.getJSONObject(i);

                    ActivitiTask activitiTask = new ActivitiTask();
                    activitiTask.setId(obj.getString("id"));
                    activitiTask.setName(obj.getString("name"));
                    activitiTask.setDesc(obj.getString("description"));
                    activitiTask.setCreated(obj.getString("createTime"));
                    activitiTask.setPInstance(obj.getString("processInstanceId"));

                    taskList.add(activitiTask);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

        }
    }

    private void getTaskRequest() {
        JsonObjectRequest getTaskReq =
                new JsonObjectRequest(Request.Method.GET,url_get,null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());

                        parseJSONResponse(response);

                        adapter.notifyDataSetChanged();
                        if (pDialog != null) {
                            pDialog.dismiss();
                            pDialog = null;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                            pDialog = null;
                        }
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders(){
                        HashMap<String, String> params = new HashMap<>();
                        String userCredentials = user + ":" + password;
                        String basicAuth = "Basic " + android.util.Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
                        params.put("Authorization", basicAuth);
                        return params;
                    }
                };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getTaskReq);
    }

    private void postTaskRequest(JSONObject body, String atId, final int position) {
        JsonObjectRequest postTaskReq =
                new JsonObjectRequest(Request.Method.POST,url_post + atId, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //taskList.remove(position);
                        //adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders(){
                        HashMap<String, String> params = new HashMap<>();
                        String userCredentials = user + ":" + password;
                        String basicAuth = "Basic " + android.util.Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
                        params.put("Authorization", basicAuth);
                        params.put("Content-Type", "application/json; charset=utf-8");
                        return params;
                    }
                };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(postTaskReq);
    }

    private class PostTask extends AsyncTask<PostTaskParams, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Approve_cc.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(PostTaskParams... params) {
            String taskId = params[0].taskId;
            boolean approved = params[0].approved;
            String body = getBody(approved);

            HttpHandler handler = new HttpHandler();
            handler.makeServiceCallAuth(url_post + taskId,user,password,METHOD_POST, body);

            int size = taskList.size()-1;
            for(int i=size; i>=0; i--){
                taskList.remove(i);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            getTaskRequest();
        }

        private String getBody(boolean approved){
            String body;
            try {
                if (approved)
                    body = PropertiesUtil.getProperty("body_approve", getApplicationContext());
                else
                    body = PropertiesUtil.getProperty("body_deny", getApplicationContext());
                return body;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static class PostTaskParams {
        String taskId;
        int position;
        boolean approved;

        PostTaskParams(String taskId, int position, boolean approved) {
            this.taskId = taskId;
            this.position = position;
            this.approved = approved;
        }
    }

    private void clearList(){
        int size = taskList.size()-1;
        for(int i=size; i>=0; i--){
            taskList.remove(i);
        }
        getTaskRequest();
    }
}
