package com.minimaldevelop.antistress;

import android.app.Activity;
import android.os.Bundle;

import com.socialize.Socialize;
import com.socialize.entity.Entity;
import android.view.View;

public class SocializeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Your entity key. May be passed as a Bundle parameter to your activity
		String entityKey = "http://www.getsocialize.com";

		// Create an entity object including a name
		// The Entity object is Serializable, so you could also store the whole
		// object in the Intent
		Entity entity = Entity.newInstance(entityKey, "Socialize");

		// Wrap your existing view with the action bar.
		// your_layout refers to the resource ID of your current layout.
		View actionBarWrapped = Socialize.getSocializeUI().showActionBar(this,
				R.layout.socialize, entity);

		// Now set the view for your activity to be the wrapped view.
		setContentView(actionBarWrapped);

	}
}
