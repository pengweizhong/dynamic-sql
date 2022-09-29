package com.pengwz.dynamic.dto;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(dataSourceClass = DatabaseConfig.class)
public class BaseDTO {
    //    @Column(dependentTableClass = SystemRoleEntity.class)
    private Integer id;

}
