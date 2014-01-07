package com.risevision.medialibrary.server.info;

public interface SearchSortable {
	public boolean search(String query);
	public int compare(SearchSortable item, String column);
}
