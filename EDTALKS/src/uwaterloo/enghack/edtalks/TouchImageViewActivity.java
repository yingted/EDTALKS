package uwaterloo.enghack.edtalks;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

public class TouchImageViewActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.map_screen);
        
        Bundle extras = getIntent().getExtras();
        String picName="";
        if (extras != null) {
            picName = extras.getString("picName");
        }
                
        TouchImageView img = (TouchImageView) findViewById(R.id.touchImageView);
        int resID;
        if (picName.equalsIgnoreCase("Sorry, path not found!") == false)
        	resID = getResources().getIdentifier(picName , "drawable", getPackageName());
        else
        	resID = R.drawable.lotr;
        img.setImageResource(resID);
        img.setMaxZoom(4f);
    }
}