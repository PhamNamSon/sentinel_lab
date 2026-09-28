CREATE TABLE orders (
    uuid RAW(16) PRIMARY KEY,
    user_id RAW(16) NOT NULL,
    total_price NUMBER(10, 2) NOT NULL,
    status VARCHAR2(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(uuid),

    CONSTRAINT chk_orders_total_price
        CHECK (total_price >= 0),

    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'CANCELLED'))
);

CREATE TABLE order_items (
    uuid RAW(16) PRIMARY KEY,
    order_id RAW(16) NOT NULL,
    product_id RAW(16) NOT NULL,
    quantity NUMBER(10) NOT NULL,
    unit_price NUMBER(10, 2) NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(uuid),

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(uuid),

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT uq_order_items_order_product
        UNIQUE (order_id, product_id)
);