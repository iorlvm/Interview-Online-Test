create table customer
(
    id             int auto_increment comment '顧客編號'
        primary key,
    username       varchar(255) not null comment '使用者帳號',
    password       varchar(255) not null comment '使用者密碼',
    name           varchar(255) not null comment '顧客姓名',
    auto_generated tinyint(1)   not null,
    constraint customer_pk_2
        unique (username)
)
    comment '顧客';

create table order_master
(
    id          int auto_increment comment '訂單編號'
        primary key,
    customer_id int           not null comment '使用者編號(FK)',
    total_price int default 0 not null comment '總價(交易快照)',
    success     tinyint(1)    null comment '交易成功(y/n)',
    create_date datetime      null comment '成交時間',
    constraint order_master_customer_id_fk
        foreign key (customer_id) references customer (id)
)
    comment '訂單主表';

create table order_detail
(
    id            int auto_increment comment '訂單細項編號'
        primary key,
    order_id      int          not null comment '訂單編號(FK)',
    count         int          not null comment '數量',
    product_name  varchar(255) not null comment '商品名稱(快照)',
    product_price int          not null comment '商品價格(快照)',
    product_unit  varchar(255) not null comment '商品單位(快照)',
    constraint order_detail_order_master_id_fk
        foreign key (order_id) references order_master (id)
            on delete cascade
)
    comment '訂單細項';

create table product
(
    id      int auto_increment comment '商品編號'
        primary key,
    name    varchar(255)              not null comment '商品名稱',
    price   int                       not null comment '商品價格',
    stock   int                       not null comment '庫存',
    unit    varchar(255) default '個' not null comment '單位',
    img_src varchar(255)              null comment '照片網址'
)
    comment '商品';

