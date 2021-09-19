package com.pointbasis.simulation.service.impl;

import com.pointbasis.simulation.domain.Accounts;
import com.pointbasis.simulation.domain.User;
import com.pointbasis.simulation.repository.AccountsRepository;
import com.pointbasis.simulation.service.AccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsServiceImpl implements AccountsService {

    private final Logger log = LoggerFactory.getLogger(AccountsServiceImpl.class);

    @Autowired
    private AccountsRepository accountingRepository;

    @Override
    public Accounts createAccount(User user) {
        Accounts account = new Accounts(user);
        accountingRepository.save(account);
        System.out.println("ACCOUNT CREATION DONE");
        return account;
    }
}
