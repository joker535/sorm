package com.guye.orm.testapp;

import java.util.Date;

import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.Table;

@Table(value="pojo_b",genCode=true)
//@PK({"name","st"})
public class PojoB {

    @Name
    public String name;
    @Column(isNotNull=true)
    public int something;
    @Column
    public Integer something2;
    @Column
    public int something3;
    @Column
    public String st;
    @Column
    public Date date;
}
