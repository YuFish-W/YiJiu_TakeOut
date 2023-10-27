package com.yufish.yijiu.dto;

import com.yufish.yijiu.entity.SetmealDishPO;
import com.yufish.yijiu.entity.SetmealPO;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDTO extends SetmealPO {

    private List<SetmealDishPO> setmealDishPOS;

    private String categoryName;
}
