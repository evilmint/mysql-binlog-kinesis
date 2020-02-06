## Setup

`binlog_format` must be set to `ROW` in db's parameter group in RDS
 
#### Database setup

```sql
CREATE TABLE users (
`id` int auto_increment uniqueusers,
first_name varchar(80),
last_name varchar(80)
);
```

Sample queries:

```
DELETE FROM users WHERE id > 0 LIMIT 1;
INSERT INTO users (id, first_name, last_name) VALUES (35, 'john', 'smith');

UPDATE users set last_name = 'Batch' WHERE id > 0;
```

Binlog checks:

```
SHOW GLOBAL VARIABLES LIKE 'log_bin';
SHOW GLOBAL VARIABLES LIKE 'binlog_format';
```