package com.apxpert.weien.utils;

import com.apxpert.weien.entity.Customer;

public class UserHolder {
    private static final ThreadLocal<Customer> threadLocal = new ThreadLocal<>();

    public static void saveUser(Customer customer) {
        threadLocal.set(customer);
    }

    public static Customer getUser() {
        return threadLocal.get();
    }

    public static Integer getId() {
        Customer customer = getUser();
        return customer == null? null : customer.getId();
    }

    public static void removeUser(){
        threadLocal.remove();
    }
}