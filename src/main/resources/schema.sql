-- Runs on startup (spring.sql.init.mode=always). Works on both H2 (MySQL mode) and MySQL 8.
CREATE TABLE IF NOT EXISTS employees (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(50)   NOT NULL,
    last_name   VARCHAR(50)   NOT NULL,
    email       VARCHAR(100)  NOT NULL,
    department  VARCHAR(20)   NOT NULL,
    job_title   VARCHAR(100)  NOT NULL,
    salary      DECIMAL(12,2) NOT NULL,
    hire_date   DATE          NOT NULL,
    CONSTRAINT uq_employees_email UNIQUE (email),
    CONSTRAINT ck_employees_salary CHECK (salary >= 0),
    -- Speeds up the "filter by department" query; the unique constraint already indexes email.
    -- Declared inline because MySQL has no CREATE INDEX IF NOT EXISTS.
    INDEX idx_employees_department (department)
);
