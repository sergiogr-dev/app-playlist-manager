-- Initial data for testing authentication
-- Password for all users is: "password123" (encoded with BCrypt)

INSERT INTO users (username, email, password, first_name, last_name, is_enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2a$10$e65QGSYud9/7AKSVgW.v0O3LSlUAxcW5eWPPk5n6Aged7DXw48Qpu', 'Admin', 'User', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', 'user1@example.com', '$2a$10$e65QGSYud9/7AKSVgW.v0O3LSlUAxcW5eWPPk5n6Aged7DXw48Qpu', 'John', 'Doe', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2', 'user2@example.com', '$2a$10$e65QGSYud9/7AKSVgW.v0O3LSlUAxcW5eWPPk5n6Aged7DXw48Qpu', 'Jane', 'Smith', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(1, 'USER'),
(2, 'USER'),
(3, 'USER');
