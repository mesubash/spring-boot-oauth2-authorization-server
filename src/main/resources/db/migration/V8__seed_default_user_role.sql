INSERT INTO roles (id, name, description)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'USER',
           'Default application user'
       )
ON CONFLICT (name) DO NOTHING;