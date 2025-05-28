-- Insertar configuraciones de validación
INSERT INTO validation_config (id, config_key, config_value, description) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'password.min.length', '8', 'La contraseña debe tener al menos 8 caracteres'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'password.require.number', '.*[0-9].*', 'La contraseña debe contener al menos un número'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'password.require.lowercase', '.*[a-z].*', 'La contraseña debe contener al menos una letra minúscula'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'password.require.uppercase', '.*[A-Z].*', 'La contraseña debe contener al menos una letra mayúscula'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'password.require.special', '.*[@#$%^&+=!?].*', 'La contraseña debe contener al menos un carácter especial'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'password.no.spaces', 'true', 'La contraseña no debe contener espacios'); 