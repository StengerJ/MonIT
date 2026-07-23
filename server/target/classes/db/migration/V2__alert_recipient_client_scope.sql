ALTER TABLE alert_recipients ADD COLUMN client_id UUID REFERENCES clients (id);
