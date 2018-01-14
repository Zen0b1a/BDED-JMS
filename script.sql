DROP TABLE action;
DROP TABLE action_1;
DROP TABLE action_2;
DROP TABLE action_3;

CREATE TABLE action (id INTEGER PRIMARY KEY, id_parent INTEGER, nom_action VARCHAR2(50));
INSERT INTO action VALUES(1, 0, 'action_1');
INSERT INTO action VALUES(2, 0, 'action_2');
INSERT INTO action VALUES(3, 1, 'action_3');

CREATE TABLE action_1 (id INTEGER PRIMARY KEY, cours NUMBER(6,2));
CREATE TABLE action_2 (id INTEGER PRIMARY KEY, cours NUMBER(6,2));
CREATE TABLE action_3 (id INTEGER PRIMARY KEY, cours NUMBER(6,2));

DROP TABLE jms_commande_produit;
DROP TABLE jms_commande;
DROP TABLE jms_produit;
DROP TABLE jms_client;

CREATE TABLE jms_produit(
	id INTEGER PRIMARY KEY, 
	nom VARCHAR2(50), 
	stock INTEGER,
	stock_pour_commandes INTEGER);
	
CREATE TABLE jms_client(
	id INTEGER PRIMARY KEY, 
	nom VARCHAR2(50), 
	prenom VARCHAR2(50), 
	adresse VARCHAR2(50), 
	code_postal INTEGER, 
	ville VARCHAR2(50));
	
CREATE TABLE jms_commande(
	id INTEGER PRIMARY KEY,
	id_client REFERENCES jms_client(id),
	etat VARCHAR2(10),
	CONSTRAINT check_etat CHECK(etat IN('initiee', 'validee', 'preparee', 'payee', 'expediee')));
	
CREATE TABLE jms_commande_produit(
	id_commande REFERENCES jms_commande(id),
	id_produit REFERENCES jms_produit(id),
	quantite INTEGER,
	CONSTRAINT pk_commande_produit PRIMARY KEY(id_commande, id_produit));

INSERT INTO jms_produit VALUES(1, 'produit1', 10, 0);
INSERT INTO jms_produit VALUES(2, 'produit2', 10, 0);

INSERT INTO jms_client VALUES(1, 'nom1', 'prenom1', 'adresse1', 21000, 'Dijon');

INSERT INTO jms_commande VALUES(1, 1, 'initiee');
INSERT INTO jms_commande VALUES(2, 1, 'initiee');
INSERT INTO jms_commande VALUES(3, 1, 'initiee');

INSERT INTO jms_commande_produit VALUES(1, 1, 1);
INSERT INTO jms_commande_produit VALUES(1, 2, 1);
INSERT INTO jms_commande_produit VALUES(2, 1, 2);
INSERT INTO jms_commande_produit VALUES(3, 2, 2);