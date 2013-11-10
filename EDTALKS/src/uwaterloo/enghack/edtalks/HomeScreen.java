package uwaterloo.enghack.edtalks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uwaterloo.enghack.edtalks.CampusNavigator.Building;
import uwaterloo.enghack.edtalks.CampusNavigator.Floor;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Spannable;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import uwaterloo.enghack.edtalks.CampusNavigator.Direction;
import uwaterloo.enghack.edtalks.CampusNavigator.Floor;
import uwaterloo.enghack.edtalks.R;
import uwaterloo.enghack.edtalks.CampusNavigator.Building;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.graphics.Color;


public class HomeScreen extends Activity implements OnItemSelectedListener {

	Spinner fromFloorSpinner;
	Spinner toFloorSpinner;
	Spinner fromSpinner;
	Spinner toSpinner;
	ListView directionsList;
	Button viewFromButton;
	Button viewToButton;
	private int nr_selected=0;

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Building building = (Building)parent.getItemAtPosition(pos);
		ArrayAdapter<Floor> adapter = new ArrayAdapter<Floor>(HomeScreen.this, android.R.layout.simple_spinner_item, building);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    final Spinner sp=parent.getId() == R.id.fromSpinner?fromFloorSpinner:toFloorSpinner;
		sp.setAdapter(adapter);
		if(nr_selected>1)
			sp.performClick();
		else
			++nr_selected;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		// Find Views
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		fromFloorSpinner = (Spinner) findViewById(R.id.fromFloorSpinner);
		toFloorSpinner = (Spinner) findViewById(R.id.toFloorSpinner);
		directionsList = (ListView) findViewById(R.id.directionListView);
		
		viewFromButton = (Button) findViewById(R.id.button1);
		viewToButton = (Button) findViewById (R.id.button2);

		// Set the Spinner Values
		final ArrayList<Building>items=new ArrayList<Building>(CampusNavigator.getBuildings());
		Collections.sort(items,new Comparator<Building>(){
			@Override public int compare(Building lhs,Building rhs){
				return lhs.toString().compareTo(rhs.toString());
			}
		});
		
		ArrayAdapter<Building> adapter = new ArrayAdapter<Building>(this, android.R.layout.simple_spinner_item, items);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		fromSpinner.setOnItemSelectedListener(this);
		toSpinner.setOnItemSelectedListener(this);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);

		final OnItemSelectedListener updateListener=new OnItemSelectedListener(){
			@Override public void onItemSelected(AdapterView<?> viewParent,View view,int pos,long id){
				Floor from=(Floor)fromFloorSpinner.getSelectedItem(),to=(Floor)toFloorSpinner.getSelectedItem();
				final List<Direction>path=CampusNavigator.getPath(from,to);
				if(path==null){
					Spannable sp=Spannable.Factory.getInstance().newSpannable("Sorry, path not found!");
					sp.setSpan(new ForegroundColorSpan(Color.RED), 0,sp.length(),0);
					directionsList.setAdapter(new ArrayAdapter<CharSequence>(HomeScreen.this,android.R.layout.simple_list_item_1,new Spannable[]{sp}));
					return;
				}
				final Spannable[]sp=new Spannable[path.size()];
				for(int i=0;i<path.size();++i){
					final Direction d=path.get(i);
					final Spannable s=Spannable.Factory.getInstance().newSpannable(d.toString());
					s.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),d.toString().indexOf(" to ")+4,d.toString().length(),0);
					sp[i]=s;
				}
				directionsList.setAdapter(new ArrayAdapter<Spannable>(HomeScreen.this,android.R.layout.simple_list_item_1,sp));
			}

			@Override public void onNothingSelected(AdapterView<?> arg0){}
		};
		fromFloorSpinner.setOnItemSelectedListener(updateListener);
		toFloorSpinner.setOnItemSelectedListener(updateListener);
		
		directionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	            String o = directionsList.getItemAtPosition(position).toString();
            	String str=o.contains(" to ")?o.substring(o.indexOf(" to ")+4).replace(" floor ", "_").toLowerCase():"Sorry, path not found!";//As you are using Default String Adapter
	            Intent intent = new Intent (HomeScreen.this, TouchImageViewActivity.class);
	            intent.putExtra("picName", str);
	            HomeScreen.this.startActivity(intent);
	            }
	    });
		
		viewFromButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Floor o = (Floor)fromFloorSpinner.getSelectedItem();
	            String str=o.getName().toLowerCase().replace("/", "_");
            	Intent intent = new Intent (HomeScreen.this, TouchImageViewActivity.class);
	            intent.putExtra("picName", str);
	            HomeScreen.this.startActivity(intent);
            }
        });
		
		viewToButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Floor o = (Floor)toFloorSpinner.getSelectedItem();
	            String str=o.getName().toLowerCase().replace("/", "_");
            	Intent intent = new Intent (HomeScreen.this, TouchImageViewActivity.class);
	            intent.putExtra("picName", str);
	            HomeScreen.this.startActivity(intent);
            }
        });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

}
