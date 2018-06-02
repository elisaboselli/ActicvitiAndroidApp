package limpito.procesodenegocios;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.content.DialogInterface;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Approve_cc extends AppCompatActivity {

    private String TAG = Approve_cc.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get tasks
    //private static String url = "192.168.1.120:8080";
    private static String url = "192.168.100.3:8080";

    private static String url_get = "http://" + url + "/activiti-rest/service/runtime/tasks?assignee=kermit";
    private static String url_post = "http://" + url + "/activiti-rest/service/runtime/tasks/"; // + tasId

    ArrayList<HashMap<String, String>> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_cc);
        //AsyncTaskRunner runner = new AsyncTaskRunner();
        //runner.execute();

        taskList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.id_task_list);
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
            HttpHandler sh = new HttpHandler();

            String jsonStr = sh.makeServiceCallAuth(url_get,"kermit","kermit","GET", null);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray tasks = jsonObj.getJSONArray("data");

                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject t = tasks.getJSONObject(i);

                        String id = t.getString("id");
                        String name = t.getString("name");
                        String description = t.getString("description");
                        /*String createTime = t.getString("createTime");
                        String executionId = t.getString("executionId");
                        String processInstanceId = t.getString("processInstanceId");
                        String processDefinitionId = t.getString("processDefinitionId");*/

                        HashMap<String, String> task = new HashMap<>();

                        task.put("id", id);
                        task.put("name", name);
                        task.put("email", id);
                        task.put("mobile", description);

                        taskList.add(task);
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
            final ListAdapter adapter = new SimpleAdapter(
                    Approve_cc.this, taskList,
                    R.layout.list_item, new String[]{"id", "name",
                    "otro"}, new int[]{R.id.task_id,
                    R.id.task_name, R.id.algo_mas});

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final String taskId = taskList.get(position).get("id");

                    AlertDialog alertDialog = new AlertDialog.Builder(Approve_cc.this).create();
                    alertDialog.setTitle(taskList.get(position).get("id"));
                    alertDialog.setMessage("Info de la task\n Bla bla bla\n");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Approve",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PostTaskParams params = new PostTaskParams(taskId,true);
                                    new PostTask().execute(params);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Deny",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PostTaskParams params = new PostTaskParams(taskId,false);
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

            taskList.remove(taskId);
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

            /* Ejemplo de body
            {
                "action" : "complete",
                "variables" : [
                    {
                        "name" : "id_boolvalue",
                        "value" : "false"
                    }
                ]
            }
            */

            JSONObject body = new JSONObject();
            JSONArray variables = new JSONArray();
            JSONObject status = new JSONObject();
            try {
                status.put("name", "id_boolvalue");
                if (approved)
                    status.put("value","true");
                else
                    status.put("value","false");
                variables.put(status);
                body.put("action","complete");
                body.put("variables",variables);
                return String.valueOf(body);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static class PostTaskParams {
        String taskId;
        boolean approved;

        PostTaskParams(String taskId, boolean approved) {
            this.taskId = taskId;
            this.approved = approved;
        }
    }
}
