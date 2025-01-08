DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='kraken_api_log_activity' and column_name='life_status')
  THEN
    ALTER TABLE kraken_api_log_activity DROP CONSTRAINT IF EXISTS kraken_api_log_activity_life_status_check;

    ALTER TABLE kraken_api_log_activity ADD CONSTRAINT kraken_api_log_activity_life_status_check CHECK (((life_status)::text = ANY ((ARRAY['LIVE'::character varying, 'ACHIEVED'::character varying, 'ARCHIVED'::character varying])::text[])));

  END IF;

END $$;