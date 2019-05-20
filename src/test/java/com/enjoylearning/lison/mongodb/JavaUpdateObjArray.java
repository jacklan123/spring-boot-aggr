package com.enjoylearning.lison.mongodb;

import com.cjs.example.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class JavaUpdateObjArray {

	private static final Logger logger = LoggerFactory.getLogger(JavaUpdateObjArray.class);
	
    private MongoDatabase db;


    private MongoCollection<Document> collection;
    
    @Resource(name="mongo")
    private MongoClient client;
    
    
    @Before
    public void init(){
	    	db = client.getDatabase("lison");
	    	collection=db.getCollection("users");
    }
    
    //--------------------------------------insert demo--------------------------------------------------------------
    
    //给james老师增加一条评论（$push）
    //db.users.updateOne({"username":"james"},
//                       {"$push":{"comments":{"author":"lison23","content":"ydddyyytttt"}}})
    @Test
    public void addOneComment(){
    	Document comment = new Document().append("author", "lison23")
    			                        .append("content", "ydddyyytttt");
    	Bson filter = eq("username","james");
		Bson update = push("comments",comment);
		UpdateResult updateOne = collection.updateOne(filter, update);
    	System.out.println(updateOne.getModifiedCount());
    	
    }
    
    
//    给james老师批量新增两条评论（$push,$each）
//    db.users.updateOne({"username":"james"},     
//           {"$push":{"comments":
//                      {"$each":[{"author":"lison33","content":"lison33lison33"},
//                                      {"author":"lison44","content":"lison44lison44"}]}}})
    @Test
    public void addManyComment(){
    	Document comment1 = new Document().append("author", "lison33")
    			                        .append("content", "lison33lison33");
    	Document comment2 = new Document().append("author", "lison44")
                						.append("content", "lison44lison44");
    	
    	Bson filter = eq("username","james");
		Bson pushEach = pushEach("comments",Arrays.asList(comment1,comment2));
		UpdateResult updateOne = collection.updateOne(filter, pushEach);
    	System.out.println(updateOne.getModifiedCount());
    	
    }
    
    
//    给james老师批量新增两条评论并对数组进行排序（$push,$eachm,$sort）
//    db.users.updateOne({"username":"james"}, 
//          {"$push": {"comments":
//                    {"$each":[ {"author":"lison22","content":"yyyytttt"},
//                                    {"author":"lison23","content":"ydddyyytttt"} ], 
//                      $sort: {"author":1} } } })
    @Test
    public void addManySortComment(){
    	Document comment1 = new Document().append("author", "lison00")
    			                        .append("content", "lison00lison00");
    	Document comment2 = new Document().append("author", "lison01")
                						.append("content", "lison01lison01");
    	
    	Bson filter = eq("username","james");
    	
    	Document sortDoc = new Document().append("author", 1);
    	PushOptions pushOption = new PushOptions().sortDocument(sortDoc);
    	
		Bson pushEach = pushEach("comments",Arrays.asList(comment1,comment2),pushOption);
		
		UpdateResult updateOne = collection.updateOne(filter, pushEach);
    	System.out.println(updateOne.getModifiedCount());
    	
    }
 
    //--------------------------------------delete demo--------------------------------------------------------------
 
//    删除lison1对james的所有评论 （批量删除）
//    db.users.update({"username":“james"},
//                               {"$pull":{"comments":{"author":"lison23"}}})

    @Test
    public void deleteByAuthorComment(){
    	Document comment = new Document().append("author", "lison23");
		Bson filter = eq("username","james");
		Bson update = pull("comments",comment);
		UpdateResult updateOne = collection.updateOne(filter, update);
		System.out.println(updateOne.getModifiedCount());
    }
    
    
//    删除lison5对lison评语为“lison是苍老师的小迷弟”的评论（精确删除）
//    db.users.update({"username":"lison"},
//            {"$pull":{"comments":{"author":"lison5",
//                                  "content":"lison是苍老师的小迷弟"}}})
    @Test
    public void deleteByAuthorContentComment(){
    	Document comment = new Document().append("author", "lison5")
    			                         .append("content", "lison是苍老师的小迷弟");
		Bson filter = eq("username","lison");
		Bson update = pull("comments",comment);
		UpdateResult updateOne = collection.updateOne(filter, update);
		System.out.println(updateOne.getModifiedCount());
    }
    
    //--------------------------------------update demo--------------------------------------------------------------
//    db.users.updateMany({"username":"james","comments.author":"lison1"},
//            {"$set":{"comments.$.content":"xxoo",
//                        "comments.$.author":"lison10" }})
//    	含义：精确修改某人某一条精确的评论，如果有多个符合条件的数据，则修改最后一条数据。无法批量修改数组元素
  @Test
  public void updateOneComment(){
	  	Bson filter = and(eq("username","james"),eq("comments.author","lison01"));
	  	Bson updateContent = set("comments.$.content","xxoo");
	  	Bson updateAuthor = set("comments.$.author","lison10");
	  	Bson update = combine(updateContent,updateAuthor);
	  	UpdateResult updateOne = collection.updateOne(filter, update);
	  	System.out.println(updateOne.getModifiedCount());
	  
  }
  
  
//--------------------------------------findandModify demo--------------------------------------------------------------
//  使用findandModify方法在修改数据同时返回更新前的数据或更新后的数据
//  db.num.findAndModify({
//        	"query":{"_id":ObjectId("5a58cef99506c50abaeb4384")},
//		  	"update":{"$inc":{"saleOrder":1}},
//		  	"new":true
//	     })
  @Test
  public void findAndModifyTest(){
	  Bson filter = eq("_id",new ObjectId("5a5a3a87d7598152855d758e"));
	  Bson update = inc("saleOrder",1);
//	  //实例化findAndModify的配置选项
	  FindOneAndUpdateOptions fauo = new FindOneAndUpdateOptions();
//	  //配置"new":true
	  fauo.returnDocument(ReturnDocument.AFTER);//
	  MongoCollection<Document> numCollection = db.getCollection("myOrder");
	  Document ret = numCollection.findOneAndUpdate(filter, update,fauo);
	  System.out.println(ret.toJson());
  }
}
