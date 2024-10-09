local stockKey = KEYS[1]
local productIds = cjson.decode(ARGV[1])  -- 解析傳入的 JSON 字符串，獲取產品 ID
local requestedCounts = cjson.decode(ARGV[2])  -- 解析傳入的 JSON 字符串，獲取請求的數量

local insufficientStock = false

-- 檢查庫存
for i, productId in ipairs(productIds) do
    local currentStock = tonumber(redis.call('HGET', stockKey, productId))
    local requestedCount = requestedCounts[i]

    if currentStock == nil or currentStock < requestedCount then
        insufficientStock = true
        break
    end
end

if insufficientStock then
    return 0  -- 庫存不足，返回 0
else
    -- 扣減庫存
    for i, productId in ipairs(productIds) do
        redis.call('HINCRBY', stockKey, productId, -requestedCounts[i])
    end
    return 1  -- 返回成功
end
