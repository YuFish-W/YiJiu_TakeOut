package com.yufish.yijiu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufish.yijiu.entity.AddressBookPO;
import com.yufish.yijiu.mapper.AddressBookMapper;
import com.yufish.yijiu.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBookPO> implements AddressBookService {

}
