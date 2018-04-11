キャンペーンの登録及びJSONによるレスポンスとインプレッションのトラッキングをするアプリ


###環境
- sbt(activator)
- java version "1.8.0_45"
- MySQL(local)
- Play2.4.3
- Slick3.0.0
- Mac OS X El Capitan


###テーブルの作成
```
mysql> create database deliver2;
mysql> create table campaigns (id integer primary key auto_increment, name varchar(64), genre_id integer, count integer);
mysql> create table genres(id integer primary key auto_increment, name varchar(64));
mysql> show columns from campaigns;
```

```
+----------+-------------+------+-----+---------+----------------+
| Field    | Type        | Null | Key | Default | Extra          |
+----------+-------------+------+-----+---------+----------------+
| id       | int(11)     | NO   | PRI | NULL    | auto_increment |
| name     | varchar(64) | YES  |     | NULL    |                |
| genre_id | int(11)     | YES  |     | NULL    |                |
| count    | int(11)     | YES  |     | NULL    |                |
+----------+-------------+------+-----+---------+----------------+
4 rows in set (0.00 sec)
```

```
mysql> show columns from genres;
+-------+-------------+------+-----+---------+----------------+
| Field | Type        | Null | Key | Default | Extra          |
+-------+-------------+------+-----+---------+----------------+
| id    | int(11)     | NO   | PRI | NULL    | auto_increment |
| name  | varchar(64) | YES  |     | NULL    |                |
+-------+-------------+------+-----+---------+----------------+
2 rows in set (0.00 sec)
```
