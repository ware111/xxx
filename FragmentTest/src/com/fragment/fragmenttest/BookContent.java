package com.fragment.fragmenttest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookContent {
	public static List<Book> items = new ArrayList<Book>();
	public static Map<Integer, Book> map = new HashMap<Integer, Book>();
static{
		addItem(new Book(1, "小明", "善良的孩子"));
		addItem(new Book(2, "小红", "漂亮的孩子"));
	}
	public static void addItem(Book book){
		items.add(book);
		map.put(book.id, book);
	}
	public static class Book{
		public Integer id;
		public String title;
		public String desc;
		public Book(Integer id, String title, String desc){
			this.id = id;
			this.title = title;
			this.desc = desc;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return title;
		}
	}
	
}
