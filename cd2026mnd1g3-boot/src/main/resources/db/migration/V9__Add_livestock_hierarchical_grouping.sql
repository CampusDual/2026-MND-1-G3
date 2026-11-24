ALTER TABLE animals ADD COLUMN IF NOT EXISTS subtipo VARCHAR(40);
ALTER TABLE animals ADD COLUMN IF NOT EXISTS ubicacion VARCHAR(80);
ALTER TABLE animals ADD COLUMN IF NOT EXISTS cantidad_cabezas INTEGER DEFAULT 1 NOT NULL;
ALTER TABLE animals ADD COLUMN IF NOT EXISTS clinica_id BIGINT DEFAULT 1 NOT NULL;

UPDATE animals
SET clinica_id = (SELECT f.clinica_id FROM farms f WHERE f.id = animals.farm_id)
WHERE EXISTS (SELECT 1 FROM farms f WHERE f.id = animals.farm_id);

UPDATE animals
SET subtipo = 'Sin clasificar'
WHERE subtipo IS NULL OR TRIM(subtipo) = '';

UPDATE animals
SET ubicacion = 'Sin ubicacion'
WHERE ubicacion IS NULL OR TRIM(ubicacion) = '';

CREATE INDEX IF NOT EXISTS idx_animals_clinica_farm_subtipo_id
    ON animals (clinica_id, farm_id, subtipo, id);
CREATE INDEX IF NOT EXISTS idx_animals_clinica_farm_ubicacion_id
    ON animals (clinica_id, farm_id, ubicacion, id);
CREATE INDEX IF NOT EXISTS idx_animals_clinica_farm_estado_id
    ON animals (clinica_id, farm_id, estado_sanitario, id);

CREATE INDEX IF NOT EXISTS idx_alertas_clinica_explotacion_animal
    ON alertas_sistema (clinica_id, explotacion_id, animal_id);
CREATE INDEX IF NOT EXISTS idx_protocol_events_clinica_farm_animal_status
    ON protocol_events (farm_id, animal_id, status);
