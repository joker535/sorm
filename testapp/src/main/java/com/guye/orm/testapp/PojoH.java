package com.guye.orm.testapp;

import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Table;

public class PojoH {

    public static class PojoHIn1{
        
        @Table(value="pojo_hinner",genCode=true)
        public static class PojoHInner{
            @Column
            public String sa;
            @Column
            public Integer ia;
            @Column
            public Long la;
            @Column
            public Short sha;
            @Column
            public Byte ba;
            
            @Column
            public byte[] bs;
            @Column(useGetAndSet=true)
            public Boolean bc;
            @Column
            public Double da;
            @Column
            public Float fa;
            @Column
            public Character ca;
            
            public String getSa() {
                return sa;
            }
            public void setSa( String sa ) {
                this.sa = sa;
            }
            public Integer getIa() {
                return ia;
            }
            public void setIa( Integer ia ) {
                this.ia = ia;
            }
            public Long getLa() {
                return la;
            }
            public void setLa( Long la ) {
                this.la = la;
            }
            public Short getSha() {
                return sha;
            }
            public void setSha( Short sha ) {
                this.sha = sha;
            }
            public Byte getBa() {
                return ba;
            }
            public void setBa( Byte ba ) {
                this.ba = ba;
            }
            public byte[] getBs() {
                return bs;
            }
            public void setBs( byte[] bs ) {
                this.bs = bs;
            }
            public Boolean getBc() {
                return bc;
            }
            public void setBc( Boolean bc ) {
                this.bc = bc;
            }
            public Double getDa() {
                return da;
            }
            public void setDa( Double da ) {
                this.da = da;
            }
            public Float getFa() {
                return fa;
            }
            public void setFa( Float fa ) {
                this.fa = fa;
            }
            public Character getCa() {
                return ca;
            }
            public void setCa( Character ca ) {
                this.ca = ca;
            }
        }
    }
}
