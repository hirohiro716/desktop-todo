
/* Drop Tables */

DROP TABLE [setting];
DROP TABLE [todo];




/* Create Tables */

CREATE TABLE [setting]
(
	[name] text NOT NULL UNIQUE,
	[value] text,
	PRIMARY KEY ([name])
);


CREATE TABLE [todo]
(
	[id] integer NOT NULL UNIQUE,
	[description] text,
	[directory] text,
	[limit_of_item_count] integer NOT NULL,
	PRIMARY KEY ([id])
);



