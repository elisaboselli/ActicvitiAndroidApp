package limpito.procesodenegocios;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ActivitiTaskAdapter extends ArrayAdapter<ActivitiTask> {

    public ActivitiTaskAdapter(Context context, List<ActivitiTask> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (null == convertView) {
            convertView = inflater.inflate(
                    R.layout.list_item,
                    parent,
                    false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.task_id);
        TextView title = (TextView) convertView.findViewById(R.id.task_desc);
        TextView company = (TextView) convertView.findViewById(R.id.task_info);

        ActivitiTask task = getItem(position);

        name.setText(task.getPrintableName());
        title.setText(task.getDescription());
        company.setText(task.getPrintableInfo());

        return convertView;
    }
}
