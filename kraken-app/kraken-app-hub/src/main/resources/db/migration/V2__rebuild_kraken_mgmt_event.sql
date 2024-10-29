DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='kraken_mgmt_event' and column_name='status' and "columns".data_type ='smallint')
  THEN
ALTER TABLE kraken_mgmt_event DROP CONSTRAINT kraken_mgmt_event_event_type_check;
ALTER TABLE kraken_mgmt_event DROP CONSTRAINT kraken_mgmt_event_status_check;
ALTER TABLE kraken_mgmt_event ALTER COLUMN status TYPE varchar(255) USING status::varchar(255);
END IF;
END $$;