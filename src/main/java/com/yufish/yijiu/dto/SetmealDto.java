package com.yufish.yijiu.dto;

import com.yufish.yijiu.entity.Setmeal;
import com.yufish.yijiu.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
