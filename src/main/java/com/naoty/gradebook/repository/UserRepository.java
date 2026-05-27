package com.naoty.gradebook.repository;

import com.naoty.gradebook.model.Role;
import com.naoty.gradebook.model.User;

import java.util.List;

public interface UserRepository extends Repository<User, String> {
    List<User> findByRole(Role role);
}
