package limpito.procesodenegocios;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Approve_cc extends AppCompatActivity {

    private String TAG = Approve_cc.class.getSimpleName();

    private static String ip = "192.168.100.5";
    private static String url_get = "http://" + ip + ":8080/activiti-rest/service/runtime/tasks?assignee=kermit";
    private static String url_post = "http://" + ip + ":8080/activiti-rest/service/runtime/tasks/"; // + tasId

    private ProgressDialog pDialog;

    private ListView listView;
    private ArrayList<String> taskList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_cc);

        taskList = new ArrayList<>();
        adapter = new ArrayAdapter<>(Approve_cc.this,android.R.layout.simple_list_item_1,taskList);
        listView = findViewById(R.id.id_task_list);

        new GetTasks().execute();
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

            String jsonStr = handler.makeServiceCallAuth(url_get,"kermit","kermit","GET", null);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray tasks = jsonObj.getJSONArray("data");

                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject t = tasks.getJSONObject(i);

                        String id = t.getString("id");
                        String name = t.getString("name");

                        String taskString = "(" + id + ") "+ name;
                        taskList.add(taskString);

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
                    final String taskId = taskList.get(position).substring(1,7);

                    AlertDialog alertDialog = new AlertDialog.Builder(Approve_cc.this).create();
                    alertDialog.setTitle(taskId);
                    alertDialog.setMessage("Info de la task\n Bla bla bla\n");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Approve",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PostTaskParams params = new PostTaskParams(taskId, position,true);
                                    new PostTask().execute(params);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Deny",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PostTaskParams params = new PostTaskParams(taskId, position, false);
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
            handler.makeServiceCallAuth(url_post + taskId,"kermit","kermit","POST", body);

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
            if(approved)
                return "{\"action\":\"complete\",\"variables\":[{\"name\":\"id_boolvalue\",\"value\":\"true\"}]}";
            else
                return "{\"action\":\"complete\",\"variables\":[{\"name\":\"id_boolvalue\",\"value\":\"false\"}]}";
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
