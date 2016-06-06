package com.guye.orm.testapp;

import java.util.Date;

import com.guye.orm.annotation.ColAdapter;
import com.guye.orm.annotation.ColType;
import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.One;
import com.guye.orm.annotation.Table;

@Table(value="pojo_a",genCode=true)
public class PojoA {
    @Name
    public String name;

    
    @Column(useGetAndSet = true)
    public String fristName;

    @Column(useGetAndSet = true)
    public Integer age;

    @Column
    public short sage;

    @Column
    public byte bage;

    public short noDB;

    @Column(useGetAndSet = true)
    public String firend;

    @Column(useGetAndSet = true)
    public PojoAType type;

    @Column(useGetAndSet = true)
    public byte[] data;

    @Column(value = "mom")
    public double memory;

    @Column
    public boolean isB;

    @One(target=PojoB.class , field="name")
    public PojoB pojoB;

    
    @Column
    @ColAdapter(value = GsonAdapter.class , type = ColType.TEXT)
    public PojoD pojod;

    public String getName() {
        return name;
    }
    public String getFristName() {
        return fristName;
    }
    public Integer getAge() {
        return age;
    }

    public String getFirend() {
        return firend;
    }

    public PojoAType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public double getMemory() {
        return memory;
    }

    public boolean isIsB() {
        return isB;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFristName(String fristName) {
        this.fristName = fristName;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setFirend(String firend) {
        this.firend = firend;
    }

    public void setType(PojoAType type) {
        this.type = type;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public void setIsB(boolean b) {
        isB = b;
    }

    public void setPojoB(PojoB pojoB) {
        this.pojoB = pojoB;
    }

    public void setPojod(PojoD pojod) {
        this.pojod = pojod;
    }


    public PojoB getPojoB() {
        return pojoB;
    }

    public PojoD getPojod() {
        return pojod;
    }
}
