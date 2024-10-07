package com.apxpert.weien.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomProductDao {
    Map<Integer, Integer> findProductStocksByIds(Set<Integer> productIds);
}
