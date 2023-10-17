--Flyway repeatable migration for populating one user
MERGE INTO user_t
(id, name, created_at, updated_at, email, password)
VALUES
(1,'Juan Bruno',now(),now(),'XXXXXXXXXXXXX','test');