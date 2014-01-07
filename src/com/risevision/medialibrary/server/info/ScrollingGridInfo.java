package com.risevision.medialibrary.server.info;

import java.io.Serializable;

import com.risevision.core.api.attributes.CommonAttribute;

@SuppressWarnings("serial")
public class ScrollingGridInfo implements Serializable {
	public static final String NUMBER = "number";
	public static final String BOOKMARK = "bookmark";
	public static final String CHECKBOX = "#checkbox#";
	
	public static final String SORT_UP = "desc";
	public static final String SORT_DOWN = "";
	
	private static final String SORT_UP_CHAR = "\u25B2";
	private static final String SORT_DOWN_CHAR = "\u25BC";

	public static final int SORTACTION = 0;
	public static final int PAGEACTION = 1;
	
	public static final int SELECTACTION = 2;
	public static final int SEARCHACTION = 3;
//	public static final int DELETEACTION = 3;
	public static final int CHECKALLACTION = 4;
	public static final int CHECKACTION = 5;
	public static final int DELETEACTION = 6;

	public static final int DEFAULTPAGESIZE = 50;

	private String sortBy = CommonAttribute.CHANGE_DATE;
	private String sortByDefault = CommonAttribute.CHANGE_DATE;
	// NOTE: Default sort direction is UP - because default sort is by CHANGE_DATE (show largest date first)
	private String sortDirection = SORT_UP;
	private String searchFor;
	private int pageSize = DEFAULTPAGESIZE;
//	private Integer pageCount;
	private int page = 0;

//	public Integer getFirstPage() {
//		if (pageCount > 0 && this.getPrevPage() > 1)
//			return 1;
//		return 0;
//	}

//	public Integer getLastPage() {
//		if (pageCount > 0 && currentPage + 1 < pageCount)
//			return pageCount;
//		return 0;
//	}

//	public Integer getPrevPage() {
//		if (currentPage > 1)
//			return currentPage - 1;
//		return 0;
//	}

//	public Integer getNextPage() {
//		if (currentPage < pageCount)
//			return currentPage + 1;
//		return 0;
//	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public void setDefaultSort() {
		sortBy = sortByDefault;
	}

	public void setSortByDefault(String sortByDefault) {
		this.sortByDefault = sortByDefault;
		setDefaultSort();
	}

	public String getSortDirection() {
		return sortDirection;
	}
	
	public String getSortDirectionChar() {
		return sortDirection.equals(ScrollingGridInfo.SORT_UP) ? SORT_UP_CHAR : SORT_DOWN_CHAR;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	public String getSearchFor() {
		return searchFor;
	}

	public void setSearchFor(String searchFor) {
		this.searchFor = searchFor;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

//	public void setPageCount(Integer pageCount) {
//		this.pageCount = pageCount;
//	}

//	public Integer getPageCount() {
//		return pageCount;
//	}
	
	public int getPage() {
		return page;
	}

	public void nextPage() {
		page++;
	}
	
	public void resetPage() {
		page = 0;
	}

}
