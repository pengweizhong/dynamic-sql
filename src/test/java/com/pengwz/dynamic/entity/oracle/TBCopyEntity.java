package com.pengwz.dynamic.entity.oracle;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.OracleDatabaseConfig;

@Table(value = "TB_copy666", dataSourceClass = OracleDatabaseConfig.class)
public class TBCopyEntity {
    @Column("ID")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column("TBCOLUMN0")
    private String tbColumn0;
    @Column("TBCOLUMN1")
    private String tbColumn1;
    @Column("TBCOLUMN2")
    private String tbColumn2;

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

    @Override
    public String toString() {
        return "TBCopyEntity{" +
                "id=" + id +
                ", tbColumn0='" + tbColumn0 + '\'' +
                ", tbColumn1='" + tbColumn1 + '\'' +
                ", tbColumn2='" + tbColumn2 + '\'' +
                '}';
    }
}
