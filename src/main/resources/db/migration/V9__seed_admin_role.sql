INSERT INTO roles (id, name, description)
VALUES (
           '00000000-0000-0000-0000-000000000002',
           'ADMIN',
           'Authorization server administrator'
       )
ON CONFLICT (name) DO NOTHING;
