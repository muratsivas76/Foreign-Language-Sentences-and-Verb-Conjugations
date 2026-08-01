package net.murat.elang;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class FormarExpandableList {
  
  public BaseExpandableListAdapter getExpandableAdapter(
    final Context context,
    final String groupTitle,
    final String[] groupNames,
    final String[][] childData) {
    
    return new BaseExpandableListAdapter() {
      
      public int getGroupCount() {
        return groupNames.length;
      }
      
      public int getChildrenCount(int groupPosition) {
        return childData[groupPosition].length;
      }
      
      public Object getGroup(int groupPosition) {
        return groupNames[groupPosition];
      }
      
      public Object getChild(int groupPosition, int childPosition) {
        return childData[groupPosition][childPosition];
      }
      
      public long getGroupId(int groupPosition) {
        return groupPosition;
      }
      
      public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
      }
      
      public boolean hasStableIds() {
        return true;
      }
      
      public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
          tv = new TextView(context);
          tv.setPadding(40, 20, 20, 20);
          tv.setTextSize(16);
          tv.setTextColor(0xFF000000);
          } else {
          tv = (TextView) convertView;
        }
        tv.setText(groupNames[groupPosition]);
        return tv;
      }
      
	  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView == null) {
			tv = new TextView(context);
			tv.setPadding(60, 15, 20, 15);
			tv.setTextSize(15);
			tv.setTextColor(0xFF000000);
			tv.setBackgroundColor(0xFFF0F0F0);
		} else {
			tv = (TextView) convertView;
		}
		tv.setText(childData[groupPosition][childPosition]);
		return tv;
	  }
      
      public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
      }
    };
  }
  
}
