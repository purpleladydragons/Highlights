package com.highlights;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.highlights.model.EntryItem;

import org.joda.time.YearMonth;

import java.util.HashMap;
import java.util.List;

/**
 * Created by austin on 12/22/14.
 */
public class EntryExpandableListAdapter extends BaseExpandableListAdapter{

    private static String MONTH_YEAR_FORMAT = "MMMM yyyy";

    private Context context;
    private List<YearMonth> listDataHeader;
    private HashMap<YearMonth, List<EntryItem>> listDataChild;

    public EntryExpandableListAdapter(Context context, List<YearMonth> listDataHeader,
                                      HashMap<YearMonth, List<EntryItem>> listDataChild) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    // TODO this might be wrong, sql id is not syncd to position
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        YearMonth headerYearMonth = (YearMonth) getGroup(groupPosition);
        String headerTitle = headerYearMonth.toString(MONTH_YEAR_FORMAT);

        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.entry_list_group, null);
        }

        TextView entryListHeader = (TextView) convertView
                .findViewById(R.id.entryListHeader);
        entryListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final EntryItem entry = (EntryItem) getChild(groupPosition, childPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.entry_list_item, null);
        }

        TextView entryItemDayOfMonth = (TextView) convertView
                .findViewById(R.id.entryItemDayOfMonth);
        TextView entryItemMonthAndYear = (TextView) convertView
                .findViewById(R.id.entryItemMonthAndYear);
        TextView entryItemText = (TextView) convertView
                .findViewById(R.id.entryItemText);

        // TODO set text
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
