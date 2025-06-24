-- DROP TABLE IF EXISTS symptoms;
-- DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE,
  email VARCHAR(255) , -- UNIQUE
  full_name VARCHAR(255),
  password VARCHAR(255)
);

CREATE TABLE symptoms (
  id SERIAL PRIMARY KEY,
  pulse INTEGER,
  steps INTEGER,
  sleep INTEGER,
  sleep_session TEXT,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_symptom_user FOREIGN KEY (user_id)
      REFERENCES users (id)
      ON DELETE CASCADE
);

-- GRANT SELECT, INSERT, UPDATE, DELETE
--   ON TABLE
--     public.users,
--     public.users_id_seq,
--     public.symptoms_id,
--     public.symptoms_id_seq
--   TO eray;