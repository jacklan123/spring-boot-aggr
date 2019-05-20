package com.enjoylearning.lison.mongodb;


import com.cjs.example.config.MongoConfig;
import com.cjs.example.mongo.entity.Comment;
import com.cjs.example.mongo.entity.MyOrder;
import com.cjs.example.mongo.entity.User;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoConfig.class)
public class SpringUpdateObjArray {

    private static final Logger logger = LoggerFactory.getLogger(SpringUpdateObjArray.class);

    @Resource
    private MongoOperations tempelate;


    //--------------------------------------insert demo--------------------------------------------------------------

    //给james老师增加一条评论（$push）
    //db.users.updateOne({"username":"james"},
//                       {"$push":{"comments":{"author":"lison23","content":"ydddyyytttt"}}})
    @Test
    public void addOneComment() {
        Query query = query(Criteria.where("username").is("james"));
        Comment comment = new Comment();
        comment.setAuthor("lison23");
        comment.setContent("ydddyyytttt");
        Update push = new Update().push("comments", comment);
        UpdateResult updateResult = tempelate.updateFirst(query, push, User.class);
        System.out.println(updateResult.getModifiedCount());
    }


    //    给james老师批量新增两条评论（$push,$each）
//    db.users.updateOne({"username":"james"},     
//           {"$push":{"comments":
//                      {"$each":[{"author":"lison33","content":"lison33lison33"},
//                                      {"author":"lison44","content":"lison44lison44"}]}}})
    @Test
    public void addManyComment() {
        Query query = query(Criteria.where("username").is("james"));
        Comment comment1 = new Comment();
        comment1.setAuthor("lison55");
        comment1.setContent("lison55lison55");
        Comment comment2 = new Comment();
        comment2.setAuthor("lison66");
        comment2.setContent("lison66lison66");
        Update push = new Update().pushAll("comments", new Comment[]{comment1, comment2});
        UpdateResult updateFirst = tempelate.updateFirst(query, push, User.class);
        System.out.println(updateFirst.getModifiedCount());
    }

    //    给james老师批量新增两条评论并对数组进行排序（$push,$eachm,$sort）
//    db.users.updateOne({"username":"james"}, 
//          {"$push": {"comments":
//                    {"$each":[ {"author":"lison22","content":"yyyytttt"},
//                                    {"author":"lison23","content":"ydddyyytttt"} ], 
//                      $sort: {"author":1} } } })
    @Test
    public void addManySortComment() {
        Query query = query(Criteria.where("username").is("james"));
        Comment comment1 = new Comment();
        comment1.setAuthor("lison77");
        comment1.setContent("lison55lison55");
        Comment comment2 = new Comment();
        comment2.setAuthor("lison88");
        comment2.setContent("lison66lison66");


        Update update = new Update();
        PushOperatorBuilder pob = update.push("comments");
        pob.each(comment1, comment2);
        pob.sort(new Sort(new Sort.Order(Direction.DESC, "author")));

        System.out.println("---------------");
        UpdateResult updateFirst = tempelate.updateFirst(query, update, User.class);
        System.out.println(updateFirst.getModifiedCount());
    }

    //--------------------------------------delete demo--------------------------------------------------------------

//    删除lison1对james的所有评论 （批量删除）
//    db.users.update({"username":“james"},
//                               {"$pull":{"comments":{"author":"lison23"}}})

    @Test
    public void deleteByAuthorComment() {
        Query query = query(Criteria.where("username").is("james"));

        Comment comment1 = new Comment();
        comment1.setAuthor("lison55");
        Update pull = new Update().pull("comments", comment1);
        UpdateResult updateFirst = tempelate.updateFirst(query, pull, User.class);
        System.out.println(updateFirst.getModifiedCount());
    }


    //    删除lison5对lison评语为“lison是苍老师的小迷弟”的评论（精确删除）
//    db.users.update({"username":"lison"},
//            {"$pull":{"comments":{"author":"lison5",
//                                  "content":"lison是苍老师的小迷弟"}}})
    @Test
    public void deleteByAuthorContentComment() {
        Query query = query(Criteria.where("username").is("lison"));
        Comment comment1 = new Comment();
        comment1.setAuthor("lison5");
        comment1.setContent("lison是苍老师的小迷弟");
        Update pull = new Update().pull("comments", comment1);
        UpdateResult updateFirst = tempelate.updateFirst(query, pull, User.class);
        System.out.println(updateFirst.getModifiedCount());
    }

    //--------------------------------------update demo--------------------------------------------------------------
//    db.users.updateMany({"username":"james","comments.author":"lison1"},
//            {"$set":{"comments.$.content":"xxoo",
//                        "comments.$.author":"lison10" }})
//    	含义：精确修改某人某一条精确的评论，如果有多个符合条件的数据，则修改最后一条数据。无法批量修改数组元素
    @Test
    public void updateOneComment() {
        Query query = query(where("username").is("lison").and("comments.author").is("lison4"));
        Update update = update("comments.$.content", "xxoo").set("comments.$.author", "lison11");
        UpdateResult updateFirst = tempelate.updateFirst(query, update, User.class);
        System.out.println(updateFirst.getModifiedCount());
    }

    //--------------------------------------findandModify demo--------------------------------------------------------------
//使用findandModify方法在修改数据同时返回更新前的数据或更新后的数据
//db.num.findAndModify({
//      	"query":{"_id":ObjectId("5a58cef99506c50abaeb4384")},
//		  	"update":{"$inc":{"saleOrder":1}},
//		  	"new":true
//	     })
    @Test
    public void findAndModifyTest() {
        Query query = query(where("_id").is(new ObjectId("5a5a3a87d7598152855d758e")));
        Update update = new Update().inc("saleOrder", 1);
        FindAndModifyOptions famo = FindAndModifyOptions.options().returnNew(true);

        MyOrder ret = tempelate.findAndModify(query, update, famo, MyOrder.class);
        System.out.println(ret.getSaleOrder());
    }


}
