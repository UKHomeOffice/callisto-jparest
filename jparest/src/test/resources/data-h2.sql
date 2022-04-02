INSERT INTO dummyEntityA (id) VALUES
    (1),
    (2),
    (3),
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10);

INSERT INTO dummyEntityB (id) VALUES
    (1),
    (2),
    (3),
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10);

INSERT INTO dummyEntityC (id, description, index) VALUES
    (1, 'Dummy Entity C number 1' , 1),
    (2, 'Dummy Entity C number 2', 2);

INSERT INTO dummyEntityD (id, description) VALUES
    (1, 'Dummy Entity D number 1'),
    (2, 'Dummy Entity D number 2');

INSERT INTO dummyEntityA_dummyEntityB (dummyEntityA, dummyEntityB) VALUES
    (1, 1),
    (1, 2),
    (2, 2);
