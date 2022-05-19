INSERT INTO profiles (id, profile_id, tenant_id, preferences, bio, phone_number, dob, first_release)
VALUES  ('17e813a2-bb28-11ec-8422-0242ac120001',1, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'My preferences for 1',  'My Bio for 1', '07879 899101', '1979-01-01', '1979-01-01'),
        ('17e813a2-bb28-11ec-8422-0242ac120002',2, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'My preferences for 2', 'My Bio for 2', '07879 899102', '1973-06-12', '1979-01-01'),
        ('17e813a2-bb28-11ec-8422-0242ac120003',3, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'My preferences for 3', 'My Bio for 3', '07879 899103', '1976-11-21', '1979-01-01'),
        ('1a7c7da4-bb29-11ec-8422-0242ac120004',4, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'My preferences for 4', 'My Bio for 4', '07879 899104', '1979-04-12', '1976-01-01'),
        ('17a7c7da4-bb29-11ec-8422-0242ac12005',5, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'My preferences for 5', 'My Bio for 5', '07879 899105', '1972-06-19', '1979-01-01'),
        ('1a7c7da4-bb29-11ec-8422-0242ac120006',6, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'My preferences for 6', 'My Bio for 6', '07879 899106', '1974-01-01', '1972-01-01');

INSERT INTO artists (id, artist_id, profile_id, tenant_id, performance_name)
VALUES  ('27e813a2-bb28-11ec-8422-0242ac120001',1, 1, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Beautiful South'),
        ('27e813a2-bb28-11ec-8422-0242ac120002',2, 2, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Oasis'),
        ('27e813a2-bb28-11ec-8422-0242ac120003',3, 3, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Pink'),
        ('2a7c7da4-bb29-11ec-8422-0242ac120004',4, 4, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'Adele'),
        ('2a7c7da4-bb29-11ec-8422-0242ac120005',5, 5, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'Beach Boys'),
        ('2a7c7da4-bb29-11ec-8422-0242ac120006',6, 6, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'Queen');

INSERT INTO concerts (id, concert_id, tenant_id, concert_name) VALUES
    ('37e813a2-bb28-11ec-8422-0242ac120001',1, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Live Aid 2020'),
    ('3a7c7da4-bb29-11ec-8422-0242ac120002',2, '7a7c7da4-bb29-11ec-8422-0242ac120002', 'Live Aid 2021'),
    ('37e813a2-bb28-11ec-8422-0242ac120003',3, 'b7e813a2-bb28-11ec-8422-0242ac120002', 'Live Aid 2020');

INSERT INTO concert_artists (concert_id, artist_id) VALUES
    (1, 1),
    (1, 2),
    (2, 4),
    (2, 5),
    (2, 6);
    
INSERT INTO records (id, record_id, tenant_id, artist_id, record_name) VALUES
('47e813a2-bb28-11ec-8422-0242ac120001',1, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'Song for Whoever'),
('47e813a2-bb28-11ec-8422-0242ac120002',2, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'You Keep It All In'),
('47e813a2-bb28-11ec-8422-0242ac120003',3, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'A Little Time'),
('47e813a2-bb28-11ec-8422-0242ac120004',4, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'My Book'),
('47e813a2-bb28-11ec-8422-0242ac120005',5, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'Let Love Speak Up Itself'),
('47e813a2-bb28-11ec-8422-0242ac120006',6, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'Old Red Eyes Is Back'),
('47e813a2-bb28-11ec-8422-0242ac120007',7, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'We Are Each Other'),
('47e813a2-bb28-11ec-8422-0242ac120008',8, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, 'Bell Bottomed Tear'),
('47e813a2-bb28-11ec-8422-0242ac120009',9, 'b7e813a2-bb28-11ec-8422-0242ac120002',1, '36D'),
('47e813a2-bb28-11ec-8422-0242ac120010',10,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Champagne Supernova'),
('47e813a2-bb28-11ec-8422-0242ac120011',11,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Whatever'),
('47e813a2-bb28-11ec-8422-0242ac120012',12,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Morning Glory'),
('47e813a2-bb28-11ec-8422-0242ac120013',13,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Live Foreve'),
('47e813a2-bb28-11ec-8422-0242ac120014',14,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Wonderwall'),
('47e813a2-bb28-11ec-8422-0242ac120015',15,'b7e813a2-bb28-11ec-8422-0242ac120002',2, 'Cast No Shadow'),
('47e813a2-bb28-11ec-8422-0242ac120016',16,'b7e813a2-bb28-11ec-8422-0242ac120002',3, 'Try'),
('47e813a2-bb28-11ec-8422-0242ac120017',17,'b7e813a2-bb28-11ec-8422-0242ac120002',3, 'So What'),
('47e813a2-bb28-11ec-8422-0242ac120018',18,'b7e813a2-bb28-11ec-8422-0242ac120002',4, 'Hello'),
('47e813a2-bb28-11ec-8422-0242ac120019',19,'b7e813a2-bb28-11ec-8422-0242ac120002',4, 'Easy on Me'),
('47e813a2-bb28-11ec-8422-0242ac120020',20,'b7e813a2-bb28-11ec-8422-0242ac120002',4, 'Set Fire To The Rain'),
('47e813a2-bb28-11ec-8422-0242ac120021',21,'b7e813a2-bb28-11ec-8422-0242ac120002',4, 'Someone Like You'),
('47e813a2-bb28-11ec-8422-0242ac120022',22,'b7e813a2-bb28-11ec-8422-0242ac120002',5, 'I Get Around'),
('47e813a2-bb28-11ec-8422-0242ac120023',23,'b7e813a2-bb28-11ec-8422-0242ac120002',5, 'Sloop John B'),
('47e813a2-bb28-11ec-8422-0242ac120024',24,'b7e813a2-bb28-11ec-8422-0242ac120002',5, 'Help Me Rhonda'),
('47e813a2-bb28-11ec-8422-0242ac120025',25,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'We Will Rock You'),
('47e813a2-bb28-11ec-8422-0242ac120026',26,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'Bohemian Rhapsody'),
('47e813a2-bb28-11ec-8422-0242ac120027',27,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'Killer Queen'),
('47e813a2-bb28-11ec-8422-0242ac120028',28,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'Bicycle Race'),
('47e813a2-bb28-11ec-8422-0242ac120029',29,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'Fat Bottomed Girls'),
('47e813a2-bb28-11ec-8422-0242ac120030',30,'b7e813a2-bb28-11ec-8422-0242ac120002',6, 'Crazy Little Thing Called Love');