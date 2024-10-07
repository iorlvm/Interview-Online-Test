package com.apxpert.weien.dao.impl;

import com.apxpert.weien.dao.CustomProductDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class CustomProductDaoImpl implements CustomProductDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<Integer, Integer> findProductStocksByIds(Set<Integer> productIds) {
        String sql = "SELECT id, stock FROM product WHERE id IN :productIds";
        Query query = entityManager.createNativeQuery(sql);
        ((Query) query).setParameter("productIds", productIds);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        Map<Integer, Integer> stockMap = new HashMap<>();
        for (Object[] row : results) {
            Integer productId = (Integer) row[0];
            Integer stock = (Integer) row[1];
            stockMap.put(productId, stock);
        }
        return stockMap;
    }
}
