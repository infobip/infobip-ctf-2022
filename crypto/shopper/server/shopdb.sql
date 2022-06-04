DROP TABLE IF EXISTS content;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS wallet;

CREATE TABLE IF NOT EXISTS items (
	id INTEGER PRIMARY KEY, 
	cost INTEGER NOT NULL, 
	name VARCHAR(50) NOT NULL
);
CREATE TABLE IF NOT EXISTS content (
	id INTEGER PRIMARY KEY, 
	data VARCHAR(150) NOT NULL, 
	item_id INTEGER,
	FOREIGN KEY(item_id) REFERENCES items(id)
);
CREATE TABLE wallet(
	id INTEGER PRIMARY KEY,
	hash VARCHAR(32) NOT NULL,
	amount INTEGER
);

INSERT INTO items (id, cost, name) VALUES
	(1, 0, "Canned air"),
	(2, 0, "River water"),
	(3, 0, "Dirt"),
	(4, 500, "flag.txt");

INSERT INTO content (id, data, item_id) VALUES
	(1, "BREATHE IN - BREATHE OUT - BREATHE IN - BREATHE OUT", 1),
	(2, "Dried out - OUT OF STOCK", 2),
	(3, "GRAB IT - PRESS IT - ITS FREE", 3),
	(4, "ibctf{w3_g4m3-b1t-fl1pp3r_w1th_un1nt3nd3d-s1d3-3ff3ctZ}", 4);

INSERT INTO wallet (id, hash, amount) VALUES
	(1, "4c9184f37cff01bcdc32dc486ec36961", 0);
