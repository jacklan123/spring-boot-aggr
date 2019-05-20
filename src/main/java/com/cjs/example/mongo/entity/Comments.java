package com.cjs.example.mongo.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

//@Document(collection="comments")
public class Comments {
	
	private List<Comment> lists;

	public List<Comment> getLists() {
		return lists;
	}

	public void setLists(List<Comment> lists) {
		this.lists = lists;
	}
	
	

}
