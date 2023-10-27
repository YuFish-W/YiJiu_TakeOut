package com.yufish.yijiu.dto;

import com.yufish.yijiu.entity.DishPO;
import com.yufish.yijiu.entity.DishFlavorPO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDTO extends DishPO {

    //菜品对应的口味数据
    private List<DishFlavorPO> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
