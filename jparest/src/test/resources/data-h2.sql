INSERT INTO dummy_EntityA (id, tenant_id, index) VALUES
    ('b7e813a2-bb28-11ec-8422-0242ac110001', 'b7e813a2-bb28-11ec-8422-0242ac120002', 1),
    ('b7e813a2-bb28-11ec-8422-0242ac110002', 'b7e813a2-bb28-11ec-8422-0242ac120002', 2),
    ('b7e813a2-bb28-11ec-8422-0242ac110003', 'b7e813a2-bb28-11ec-8422-0242ac120002', 3),
    ('b7e813a2-bb28-11ec-8422-0242ac110004', 'b7e813a2-bb28-11ec-8422-0242ac120002', 4),
    ('b7e813a2-bb28-11ec-8422-0242ac110005', 'b7e813a2-bb28-11ec-8422-0242ac120002', 5),
    ('b7e813a2-bb28-11ec-8422-0242ac110006', 'b7e813a2-bb28-11ec-8422-0242ac120002', 6),
    ('b7e813a2-bb28-11ec-8422-0242ac110007', 'b7e813a2-bb28-11ec-8422-0242ac120002', 7),
    ('b7e813a2-bb28-11ec-8422-0242ac110008', 'b7e813a2-bb28-11ec-8422-0242ac120002', 8),
    ('b7e813a2-bb28-11ec-8422-0242ac110009', 'b7e813a2-bb28-11ec-8422-0242ac120002', 9),
    ('b7e813a2-bb28-11ec-8422-0242ac110010', 'b7e813a2-bb28-11ec-8422-0242ac120002', 10);

INSERT INTO dummy_EntityB (id, tenant_id) VALUES
    ('b7e813a2-bb28-11ec-8422-0242ac120001', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120002', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120003', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120004', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120005', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120006', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120007', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120008', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120009', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac120010', 'b7e813a2-bb28-11ec-8422-0242ac120002');

INSERT INTO dummy_EntityC (id, tenant_id, description, index, dob, instant) VALUES
    ('b7e813a2-bb28-11ec-8422-0242ac130001', 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 1' , 1, '2012-12-12', '2012-12-12T10:00:00.000+00:00'),
    ('b7e813a2-bb28-11ec-8422-0242ac130002', 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Dummy Entity C number 2', 2, '2012-12-13', '2012-12-13T10:00:00.000+00:00');

INSERT INTO dummy_EntityD (id, description) VALUES
    ('b7e813a2-bb28-11ec-8422-0242ac140001', 'Dummy Entity D number 1'),
    ('b7e813a2-bb28-11ec-8422-0242ac140002', 'Dummy Entity D number 2');

INSERT INTO dummy_EntityA_dummy_EntityB (dummy_EntityA, dummy_EntityB) VALUES
    ('b7e813a2-bb28-11ec-8422-0242ac110001', 'b7e813a2-bb28-11ec-8422-0242ac120001'),
    ('b7e813a2-bb28-11ec-8422-0242ac110001', 'b7e813a2-bb28-11ec-8422-0242ac120002'),
    ('b7e813a2-bb28-11ec-8422-0242ac110002', 'b7e813a2-bb28-11ec-8422-0242ac120002');

INSERT INTO dummy_EntityF (id, tenant_id, dummy_entityC_id) VALUES
    ('4424d0e2-e8f2-40b8-a564-f23d67e6f3a1', 'b7e813a2-bb28-11ec-8422-0242ac120002', 'b7e813a2-bb28-11ec-8422-0242ac130001'),
    ('4424d0e2-e8f2-40b8-a564-f23d67e6f3a1', 'b7e813a2-bb28-11ec-8422-0242ac120002', 'b7e813a2-bb28-11ec-8422-0242ac130001');