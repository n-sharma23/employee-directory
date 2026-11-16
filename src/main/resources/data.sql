-- Sample rows, only inserted into an empty table so restarts against MySQL don't duplicate them.
INSERT INTO employees (first_name, last_name, email, department, job_title, salary, hire_date)
SELECT * FROM (
    SELECT 'Asha',  'Verma',  'asha.verma@example.com',  'ENGINEERING', 'Backend Engineer',   85000.00, DATE '2022-04-11' UNION ALL
    SELECT 'Rahul', 'Mehta',  'rahul.mehta@example.com', 'PRODUCT',     'Product Manager',    92000.00, DATE '2021-09-01' UNION ALL
    SELECT 'Sara',  'Nilsen', 'sara.nilsen@example.com', 'HR',          'HR Partner',         64000.00, DATE '2023-01-16' UNION ALL
    SELECT 'Imran', 'Khan',   'imran.khan@example.com',  'ENGINEERING', 'Frontend Engineer',  78000.00, DATE '2024-06-03' UNION ALL
    SELECT 'Lena',  'Berg',   'lena.berg@example.com',   'SALES',       'Account Executive',  70000.00, DATE '2020-11-23'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM employees);
