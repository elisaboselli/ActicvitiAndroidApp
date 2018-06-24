package limpito.procesodenegocios;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Approve_cc extends AppCompatActivity {

    private String TAG = Approve_cc.class.getSimpleName();

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";

    private static String ip;
    private static String url_get;
    private static String url_post;
    private static String user;
    private static String password;

    private ProgressDialog pDialog;

    private ListView listView;
    private ArrayList<ActivitiTask> taskList;
    private ActivitiTaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_cc);

        try {
            ip = PropertiesUtil.getProperty("ip",getApplicationContext());
            url_get = ip + PropertiesUtil.getProperty("get",getApplicationContext()) + PropertiesUtil.getProperty("user",getApplicationContext());
            url_post = ip + PropertiesUtil.getProperty("post",getApplicationContext()); // + taskId

            user = PropertiesUtil.getProperty("user",getApplicationContext());
            password = PropertiesUtil.getProperty("pass",getApplicationContext());

            taskList = new ArrayList<>();

            adapter = new ActivitiTaskAdapter(Approve_cc.this,taskList);

            listView = findViewById(R.id.id_task_list);

            new GetTasks().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class GetTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Approve_cc.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            String jsonStr = handler.makeServiceCallAuth(url_get,user,password,METHOD_GET, null);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray tasks = jsonObj.getJSONArray("data");

                    if(tasks.length()==0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "No tasks found.",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    } else {

                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject t = tasks.getJSONObject(i);

                            ActivitiTask at = new ActivitiTask();

                            at.setId(t.getString("id"));
                            at.setName(t.getString("name"));
                            at.setDescription(t.getString("description"));
                            at.setCreated(t.getString("createTime"));
                            at.setPInstance(t.getString("processInstanceId"));

                            taskList.add(at);

                        }
                    }
                } catch (final JSONException e) {
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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            listView.setAdapter(adapter);

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
                                    PostTaskParams params = new PostTaskParams(at.getId(), position,true);
                                    new PostTask().execute(params);
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
            new GetTasks().execute();
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
}
