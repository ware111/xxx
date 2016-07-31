package com.fragment.fragmenttest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements BookListFragment.Callbacks{
    BookContent.Book book;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public void onItemSelected(Integer id) {
		// TODO Auto-generated method stub
		Bundle arguments = new Bundle();
		arguments.putInt("ITEM_ID", id);
		DetailFragment fragment = new DetailFragment();
		fragment.setArguments(arguments);
		getFragmentManager().beginTransaction().replace(R.id.book_detail_container, fragment);
	}
}
