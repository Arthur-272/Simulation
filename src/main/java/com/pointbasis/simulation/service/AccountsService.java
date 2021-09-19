package com.pointbasis.simulation.service;

import com.pointbasis.simulation.domain.Accounts;
import com.pointbasis.simulation.domain.User;

public interface AccountsService {
    Accounts createAccount(User user);
}
