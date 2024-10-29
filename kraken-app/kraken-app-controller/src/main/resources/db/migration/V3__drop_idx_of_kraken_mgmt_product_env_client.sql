DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='kraken_mgmt_product_env_client' and column_name='client_ip')
  THEN
    ALTER TABLE IF EXISTS kraken_mgmt_product_env_client DROP CONSTRAINT IF EXISTS kraken_mgmt_product_env_client_uni_idx;
  END IF;
END $$;