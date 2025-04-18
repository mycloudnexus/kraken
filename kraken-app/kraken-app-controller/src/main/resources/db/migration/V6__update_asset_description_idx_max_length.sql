DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='kraken_asset' and column_name='description')
  THEN
    ALTER TABLE kraken_asset ALTER COLUMN description TYPE varchar(4096);
  END IF;
END $$;