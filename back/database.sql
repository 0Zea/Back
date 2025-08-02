ALTER TABLE Goal ADD COLUMN save_amount BIGINT NOT NULL AFTER target_amount;

delete from user;