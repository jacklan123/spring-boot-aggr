package com.cjs.example.entity;

import lombok.Data;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName = "product", type = "commodity", replicas = 2, shards = 5)
@Setting(settingPath = "/json/setting.json")
@Mapping(mappingPath = "/json/mapping.json")
public class Commodity implements Serializable {

    @Id
    private String skuId;

    private String name;

    private String category;

    private Integer price;

    private String brand;

    private Integer stock;

    private Date createdTime;


}