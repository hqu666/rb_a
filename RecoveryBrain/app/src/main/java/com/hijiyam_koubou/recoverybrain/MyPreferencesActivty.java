package com.hijiyam_koubou.recoverybrain;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MyPreferencesActivty extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
	}
}