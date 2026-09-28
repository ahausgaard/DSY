CREATE SCHEMA IF NOT EXISTS slaughterhouse;
SET search_path TO slaughterhouse;

DROP TABLE IF EXISTS product_tray, product, product_component, product_type,
                     part, tray, part_type, animal, delivery, farm CASCADE;

CREATE TABLE farm
(
  cvr     INTEGER PRIMARY KEY CHECK (cvr BETWEEN 10000000 AND 99999999),
  name    VARCHAR(100) NOT NULL,
  address VARCHAR(200) NOT NULL
);

CREATE TABLE part_type
(
  code             INTEGER PRIMARY KEY,
  name             VARCHAR(100) NOT NULL UNIQUE,
  species          VARCHAR(20)  NOT NULL
    CHECK (species IN ('Pig', 'Chicken', 'Cow', 'Lamb', 'Turkey', 'Ostrich')),
  count_per_animal INTEGER      NOT NULL CHECK (count_per_animal > 0)
);

CREATE TABLE product_type
(
  code INTEGER PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,

  kind VARCHAR(20)  NOT NULL CHECK (kind IN ('SAME_PART', 'HALF_ANIMAL'))
);

CREATE TABLE product_component
(
  product_type_code INTEGER NOT NULL REFERENCES product_type (code),
  part_type_code    INTEGER NOT NULL REFERENCES part_type (code),
  quantity          INTEGER NOT NULL CHECK (quantity > 0),
  PRIMARY KEY (product_type_code, part_type_code)
);


CREATE TABLE delivery
(
  delivery_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  farm_cvr      INTEGER     NOT NULL REFERENCES farm (cvr),
  arrived_at    TIMESTAMP   NOT NULL,
  transport_reg VARCHAR(20) NOT NULL
);

CREATE TABLE animal
(
  animal_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  delivery_id    UUID          NOT NULL REFERENCES delivery (delivery_id),
  species        VARCHAR(20)   NOT NULL
    CHECK (species IN ('Pig', 'Chicken', 'Cow', 'Lamb', 'Turkey', 'Ostrich')),
  live_weight_kg NUMERIC(8, 3) NOT NULL CHECK (live_weight_kg > 0),
  registered_at  TIMESTAMP     NOT NULL
);


CREATE TABLE tray
(
  tray_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  part_type_code INTEGER       NOT NULL REFERENCES part_type (code),
  max_weight_kg  NUMERIC(8, 3) NOT NULL CHECK (max_weight_kg > 0),
  net_weight_kg  NUMERIC(8, 3) NOT NULL DEFAULT 0 CHECK (net_weight_kg >= 0),
  opened_at      TIMESTAMP     NOT NULL,
  closed_at      TIMESTAMP,
  CHECK (net_weight_kg <= max_weight_kg),
  CHECK (closed_at IS NULL OR closed_at >= opened_at),

  UNIQUE (tray_id, part_type_code)
);

CREATE TABLE part
(
  part_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  animal_id      UUID          NOT NULL REFERENCES animal (animal_id),
  part_type_code INTEGER       NOT NULL REFERENCES part_type (code),
  tray_id        UUID,
  weight_kg      NUMERIC(8, 3) NOT NULL CHECK (weight_kg > 0),
  cut_at         TIMESTAMP     NOT NULL,
  FOREIGN KEY (tray_id, part_type_code) REFERENCES tray (tray_id, part_type_code)
);

CREATE INDEX part_animal_idx ON part (animal_id);
CREATE INDEX part_tray_idx ON part (tray_id);


CREATE TABLE product
(
  product_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_type_code INTEGER       NOT NULL REFERENCES product_type (code),
  total_weight_kg   NUMERIC(8, 3) NOT NULL CHECK (total_weight_kg > 0),
  packed_at         TIMESTAMP     NOT NULL
);

CREATE TABLE product_tray
(
  product_id UUID NOT NULL REFERENCES product (product_id),
  tray_id    UUID NOT NULL REFERENCES tray (tray_id),
  PRIMARY KEY (product_id, tray_id)
);

CREATE INDEX product_tray_tray_idx ON product_tray (tray_id);
