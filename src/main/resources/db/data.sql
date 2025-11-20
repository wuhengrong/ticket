ALTER TABLE vip_customer ADD COLUMN customer_type VARCHAR(20) ;
UPDATE vip_customer SET customer_type = 'VIP' WHERE customer_type IS NULL;