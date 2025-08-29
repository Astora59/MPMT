--tables
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  


-- ====================================
-- Table : users
-- ====================================

CREATE TABLE users (
    users_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- clé primaire qu'on utilise dans d'autres tables, gen_randim_uuid() pour générer un UUID
    usersname VARCHAR(32) UNIQUE NOT NULL,
    users_email VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
); 


-- ====================================
-- Table : project
-- ====================================

CREATE TABLE project (
    project_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- clé primaire qu'on utilise dans d'autres tables   
    project_name VARCHAR(50) NOT NULL,
    project_description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    users_email VARCHAR(50) NOT NULL REFERENCES "users"(users_email) ON DELETE CASCADE,
    project_admin UUID NOT NULL REFERENCES users(users_id) ON DELETE CASCADE
);



-- ====================================
-- Table : role
-- ====================================

CREATE TABLE role (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(14) NOT NULL CHECK (role_name IN ('admin', 'member', 'observer')),
    users_id UUID NOT NULL REFERENCES "users"(users_id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES "project"(project_id) ON DELETE CASCADE,
    CONSTRAINT unique_user_project UNIQUE (users_id, project_id)
    
);



-- ====================================
-- Table : task
-- ====================================

CREATE TABLE task (
    task_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_name VARCHAR(50) NOT NULL,
    task_title VARCHAR(32) NOT NULL,
    task_description TEXT,
    task_deadline TIMESTAMP,
    task_status VARCHAR(14) CHECK (task_status IN ('pending', 'in_progress', 'completed')) DEFAULT 'pending',
    task_priority VARCHAR(10) CHECK (task_priority IN ('low', 'medium', 'high')) DEFAULT 'medium',
    project_id UUID NOT NULL REFERENCES "project"(project_id) ON DELETE CASCADE
);

-- ====================================
-- Table : task_history
-- ====================================

CREATE TABLE task_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES "task"(task_id) ON DELETE CASCADE,
    modified_by UUID NOT NULL REFERENCES "users"(users_id) ON DELETE CASCADE,
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
    users_email VARCHAR(50) NOT NULL REFERENCES "users"(users_email) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES "task"(task_id) ON DELETE CASCADE,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);




-- ====================================
-- test des tables
-- ====================================

INSERT INTO "users" (usersname, users_email, password_hash) VALUES ('dave_dave', 'dave_dave@gmail.com', 'hashed_password_987');
INSERT INTO "users" (usersname, users_email, password_hash) VALUES ('bella_ciao', 'bella_ciao@gmail.com', 'hashed_password_654');

INSERT INTO project (project_name, project_description, users_email, project_admin)
VALUES (
    'Project Test trigger',
    'Projet pour test un trigger',
    'bella_ciao@gmail.com',  
    (SELECT users_id FROM users WHERE users_email='bella_ciao@gmail.com')
);

INSERT INTO "role" (role_name, users_id, project_id) VALUES ('observer', (SELECT users_id FROM "users" WHERE usersname='dave_dave'), (SELECT project_id FROM "project" WHERE project_name='Project Delta'));
INSERT INTO "role" (role_name, users_id, project_id) VALUES ('member', (SELECT users_id FROM "users" WHERE usersname='bella_ciao'), (SELECT project_id FROM "project" WHERE project_name='Project Sigma'));

INSERT INTO "task" (task_name, task_title, task_description, task_deadline, task_status, task_priority, project_id) VALUES ('Task 1', 'Initial Setup', 'Set up the initial project structure.', '2023-12-01 10:00:00', 'pending', 'high', (SELECT project_id FROM "project" WHERE project_name='Project Delta'));



-- trigger
CREATE OR REPLACE FUNCTION add_admin_role()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO role (role_name, users_id, project_id)
    VALUES ('admin', NEW.project_admin, NEW.project_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_add_admin_role
AFTER INSERT ON project
FOR EACH ROW
EXECUTE FUNCTION add_admin_role();


