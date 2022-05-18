INSERT INTO dummy_EntityA (id, tenant_id) VALUES
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

INSERT INTO dummy_EntityB (id, tenant_id) VALUES
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

INSERT INTO dummy_EntityC (id, tenant_id, description, index) VALUES
    (1, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 1' , 1),
    (2, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 2', 2);

INSERT INTO dummy_EntityD (id, description) VALUES
    (1, 'Dummy Entity D number 1'),
    (2, 'Dummy Entity D number 2');

INSERT INTO dummy_EntityA_dummy_EntityB (dummy_EntityA, dummy_EntityB) VALUES
    (1, 1),
    (1, 2),
    (2, 2);
