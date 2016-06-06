package com.guye.orm.testapp;

import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.Table;

@Table(value="pojo_d",genCode=true)
public class PojoD {
    @Name
    public String name;
        
    @Column
    public boolean isB;
    
    @Column
    public long age;    
}
