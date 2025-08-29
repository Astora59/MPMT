--tables
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  
CREATE DATABASE pmt_db;
\c pmt_db;

-- ====================================
-- Table : User
-- ====================================

CREATE TABLE user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(32) UNIQUE NOT NULL,
    user_email VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
); 


-- ====================================
-- Table : role
-- ====================================

CREATE TABLE role (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(14) UNIQUE NOT NULL CHECK (role_name IN ('admin', 'member', 'observer'))
    user_id UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES "project"(project_id) ON DELETE CASCADE
    
);

-- ====================================
-- Table : project
-- ====================================

CREATE TABLE project (
    project_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_name VARCHAR(50) NOT NULL,
    project_description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_email VARCHAR(50) NOT NULL REFERENCES "user"(user_email) ON DELETE CASCADE
);

-- ====================================
-- Table : task
-- ====================================

CREATE TABLE task (
    task_id UUID PRIMARY KEY DEFAULT,
    task_name VARCHAR(50) NOT NULL,
    task_title VARCHAR(32) NOT NULL,
    task_description TEXT,
    task_deadline TIMESTAMP,
    task_status VARCHAR(14) CHECK (task_status IN ('pending', 'in_progress', 'completed')) DEFAULT 'pending',
    task_priority VARCHAR(10) CHECK (task_priority IN ('low', 'medium', 'high')) DEFAULT 'medium',
    project_id UUID NOT NULL REFERENCES "project"(project_id) ON DELETE CASCADE,
);

-- ====================================
-- Table : task_history
-- ====================================

CREATE TABLE task_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES "task"(task_id) ON DELETE CASCADE,
    modified_by UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    modification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_status VARCHAR(14) CHECK (old_status IN ('pending', 'in_progress', 'completed')),
    new_status VARCHAR(14) CHECK (new_status IN ('pending', 'in_progress', 'completed')),
    old_priority VARCHAR(10) CHECK (old_priority IN ('low', 'medium', 'high')),
    new_priority VARCHAR(10) CHECK (new_priority IN ('low', 'medium', 'high')),
    change_description TEXT
);

-- ====================================
-- Table : notification
-- ====================================

CREATE TABLE notification (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_email VARCHAR(50) NOT NULL REFERENCES "user"(email) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES "task"(task_id) ON DELETE CASCADE,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
)




-- ====================================
-- test des tables
-- ====================================

INSERT INTO "user" (username, user_email, password_hash) VALUES ('john_doe', 'john_doe@gmail.com', 'hashed_password_123');
INSERT INTO "user" (username, user_email, password_hash) VALUES ('jane_smith', 'jane_smith@gmail.com', 'hashed_password_456');

INSERT INTO "project" (project_name, project_description, user_email) VALUES ('Project Alpha', 'This is the first project.','15/05/2023', 'john_doe@gmail.com');
INSERT INTO "project" (project_name, project_description, user_email) VALUES ('Project Beta', 'This is the second project.','18/02/2025', 'jane_smith@gmail.com');

INSERT INTO "role" (role_name, user_id, project_id) VALUES ('admin', (SELECT user_id FROM "user" WHERE username='john_doe'), (SELECT project_id FROM "project" WHERE project_name='Project Alpha'));
INSERT INTO "role" (role_name, user_id, project_id) VALUES ('member', (SELECT user_id FROM "user" WHERE username='jane_smith'), (SELECT project_id FROM "project" WHERE project_name='Project Beta'));

INSERT INTO "task" (task_name, task_title, task_description, task_deadline, task_status, task_priority, project_id) VALUES ('Task 1', 'Initial Setup', 'Set up the initial project structure.', '2023-12-01 10:00:00', 'pending', 'high', (SELECT project_id FROM "project" WHERE project_name='Project Alpha'));