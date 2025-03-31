CREATE TABLE IF NOT EXISTS "swift_code" (
swift_code VARCHAR(11) PRIMARY KEY,
countryiso2 CHAR(2)  NOT NULL,
name VARCHAR(200) NOT NULL,
address VARCHAR(150),
country_name VARCHAR(100) NOT NULL,
is_headquarter BOOLEAN NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_swift_code ON swift_code(swift_code);
CREATE INDEX IF NOT EXISTS idx_country_iso2 ON swift_code(countryiso2);