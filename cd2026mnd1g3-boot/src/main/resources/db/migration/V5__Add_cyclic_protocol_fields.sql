ALTER TABLE farms ADD COLUMN veterinarian_assigned_phone VARCHAR(30);
ALTER TABLE animals ADD COLUMN estado_sanitario VARCHAR(40) DEFAULT 'SANO' NOT NULL;
ALTER TABLE animals ADD COLUMN fecha_fin_retirada DATE;
ALTER TABLE animals ADD COLUMN notas VARCHAR(1000);
ALTER TABLE protocol_events ADD COLUMN medication_batch VARCHAR(120);
ALTER TABLE protocol_events ADD COLUMN next_cycle_days INTEGER DEFAULT 0 NOT NULL;
