package com.pengwz.dynamic.entity.oracle;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.OracleDatabaseConfig;

import java.math.BigInteger;

@Table(value = "C##TESTSYSTTEM.TB_copy666", dataSourceClass = OracleDatabaseConfig.class)
public class TBCopyEntity {
    @Column("ID")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, sequenceName = "TB_copy666_AutoID")
    private Integer id;
    @Column("TBCOLUMN0")
    private String tbColumn0;
    @Column("TBCOLUMN1")
    private String tbColumn1;
    @Column("TBCOLUMN2")
    private String tbColumn2;
    @Column("TBCOLUMN3")
    private BigInteger tbColumn3;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTbColumn0() {
        return tbColumn0;
    }

    public void setTbColumn0(String tbColumn0) {
        this.tbColumn0 = tbColumn0;
    }

    public String getTbColumn1() {
        return tbColumn1;
    }

    public void setTbColumn1(String tbColumn1) {
        this.tbColumn1 = tbColumn1;
    }

    public String getTbColumn2() {
        return tbColumn2;
    }

    public void setTbColumn2(String tbColumn2) {
        this.tbColumn2 = tbColumn2;
    }

    public BigInteger getTbColumn3() {
        return tbColumn3;
    }

    public void setTbColumn3(BigInteger tbColumn3) {
        this.tbColumn3 = tbColumn3;
    }

    @Override
    public String toString() {
        return "TBCopyEntity{" +
                "id=" + id +
                ", tbColumn0='" + tbColumn0 + '\'' +
                ", tbColumn1='" + tbColumn1 + '\'' +
                ", tbColumn2='" + tbColumn2 + '\'' +
                ", tbColumn3='" + tbColumn3 + '\'' +
                '}';
    }
}
