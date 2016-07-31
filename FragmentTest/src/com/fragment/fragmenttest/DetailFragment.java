package com.fragment.fragmenttest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailFragment extends Fragment {
	BookContent.Book book;
	public static final String ITEM_ID = "item_id";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey(ITEM_ID)){
			book = BookContent.map.get(getArguments().getInt(ITEM_ID));
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.detail_layout, container);
		if (book != null){
		      ((TextView) view.findViewById(R.id.name)).setText(book.title);
		      ((TextView)view.findViewById(R.id.desc)).setText(book.desc);
		}
		return view;
	}
}
