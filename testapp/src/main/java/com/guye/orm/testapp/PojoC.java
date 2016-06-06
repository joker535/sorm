package com.guye.orm.testapp;

import java.util.List;

import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Id;
import com.guye.orm.annotation.Many;
import com.guye.orm.annotation.Table;

@Table(value="pojo_c",genCode=true)
public class PojoC {

    @Id
    public long id;
    
    @Column
    public String fristName;
    
    @Many(target=PojoD.class , field="age")
    public List<PojoD> list;
    
    
}
