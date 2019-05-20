package com.cjs.example.mongo.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="myOrder")
public class MyOrder {
	
	private int saleOrder;

	public int getSaleOrder() {
		return saleOrder;
	}

	public void setSaleOrder(int saleOrder) {
		this.saleOrder = saleOrder;
	}

}
