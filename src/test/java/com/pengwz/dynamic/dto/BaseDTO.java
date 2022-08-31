package com.pengwz.dynamic.dto;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.entity.SystemRoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

//@SuperBuilder
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
@Table(dataSourceClass = DatabaseConfig.class)
public class BaseDTO {
//    @Column(dependentTableClass = SystemRoleEntity.class)
//    private Integer id;

}
