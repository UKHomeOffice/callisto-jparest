INSERT INTO dummyEntityA (id, tenant_id) VALUES
    (1, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (2, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (3, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (4, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (5, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (6, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (7, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (8, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (9, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (10, 'b7e813a2-bb28-11ec-8422-0242ac120002');

INSERT INTO dummyEntityB (id, tenant_id) VALUES
    (1, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (2, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (3, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (4, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (5, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (6, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (7, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (8, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (9, 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    (10, 'b7e813a2-bb28-11ec-8422-0242ac120002');

INSERT INTO dummyEntityC (id, tenant_id, description, index) VALUES
    (1, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 1' , 1),
    (2, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 2', 2);

INSERT INTO dummyEntityD (id, description) VALUES
    (1, 'Dummy Entity D number 1'),
    (2, 'Dummy Entity D number 2');

INSERT INTO dummyEntityA_dummyEntityB (dummyEntityA, dummyEntityB) VALUES
    (1, 1),
    (1, 2),
    (2, 2);
