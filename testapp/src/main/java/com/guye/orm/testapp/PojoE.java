package com.guye.orm.testapp;


import java.util.Calendar;
import java.util.Date;

import com.guye.orm.annotation.ColAdapter;
import com.guye.orm.annotation.ColType;
import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Table;

/**
 * Created by nieyu on 16/5/25.
 */
@Table(value="pojo_e" , genCode = true)
public class PojoE {
    
    @Column
    public String sa;
    @Column
    public int ia;
    @Column
    public long la;
    @Column
    public short sha;
    @Column
    public byte ba;
    @Column
    public PojoAType ea;
    @Column
    @ColAdapter(type=ColType.BLOB,value=SerAdapter.class)
    public Calendar calendar;
    @Column
    public Date date;
    @Column
    public byte[] bs;
    @Column
    public boolean bc;
    @Column
    public double da;
    @Column
    public float fa;
    @Column()
    public char ca;
    
    public String getSa() {
        return sa;
    }
    public void setSa( String sa ) {
        this.sa = sa;
        
    }
    public int getIa() {
        return ia;
    }
    public void setIa( int ia ) {
        this.ia = ia;
    }
    public long getLa() {
        return la;
    }
    public void setLa( long la ) {
        this.la = la;
    }
    public short getSha() {
        return sha;
    }
    public void setSha( short sha ) {
        this.sha = sha;
    }
    public byte getBa() {
        return ba;
    }
    public void setBa( byte ba ) {
        this.ba = ba;
    }
    public PojoAType getEa() {
        return ea;
    }
    public void setEa( PojoAType ea ) {
        this.ea = ea;
    }
    public Calendar getCalendar() {
        return calendar;
    }
    public void setCalendar( Calendar calendar ) {
        this.calendar = calendar;
    }
    public Date getDate() {
        return date;
    }
    public void setDate( Date date ) {
        this.date = date;
    }
    public byte[] getBs() {
        return bs;
    }
    public void setBs( byte[] bs ) {
        this.bs = bs;
    }
    public boolean isBc() {
        return bc;
    }
    public void setBc( boolean bc ) {
        this.bc = bc;
    }
    public double getDa() {
        return da;
    }
    public void setDa( double da ) {
        this.da = da;
    }
    public float getFa() {
        return fa;
    }
    public void setFa( float fa ) {
        this.fa = fa;
    }
    public char getCa() {
        return ca;
    }
    public void setCa( char ca ) {
        this.ca = ca;
    }
    
    
}
