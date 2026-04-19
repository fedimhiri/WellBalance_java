ALTER TABLE rendez_vous
    MODIFY COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'en cours';

UPDATE rendez_vous
SET statut = 'en cours'
WHERE statut IS NULL OR TRIM(statut) = '';
