ALTER TABLE animals ADD CONSTRAINT uq_animals_farm_ear_tag UNIQUE (farm_id, ear_tag);

CREATE INDEX IF NOT EXISTS idx_animals_farm_crotal
    ON animals (farm_id, ear_tag);
CREATE INDEX IF NOT EXISTS idx_animals_farm_subtipo_location
    ON animals (farm_id, subtipo, ubicacion);
