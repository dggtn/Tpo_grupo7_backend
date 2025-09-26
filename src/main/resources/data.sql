-- ===========================================
-- SEED H2 PARA ESQUEMA RITMOFIT (según Hibernate)
-- ===========================================

-- Limpieza suave (si corrés varias veces)
DELETE FROM teacher_course;
DELETE FROM headquarter_course;
DELETE FROM shift;
DELETE FROM course_attend;
DELETE FROM inscription;
DELETE FROM reservations;
DELETE FROM course;
DELETE FROM teacher;
DELETE FROM headquarter;
DELETE FROM location;
DELETE FROM sport;
DELETE FROM users;

-- =========================
-- USERS (demo)
-- =========================
--INSERT INTO users (id, first_name, last_name, email, username, password, role, age, address, url_avatar)
--VALUES
--  (1, 'Federico', 'Torlaschi', 'federicotorlaschi@yahoo.com.ar', 'federico', '123456', 'USER', 30, 'CABA', NULL),
--  (2, 'Laura',   'Perez',   'laura@example.com',   'laura',   '1234', 'USER', 28, 'CABA', NULL);

-- =========================
-- LOCATIONS + HEADQUARTERS
-- =========================
INSERT INTO location (id, latitude, lenght) VALUES
  (1, -34.5800, -58.4200), -- Palermo
  (2, -34.5620, -58.4560); -- Belgrano

INSERT INTO headquarter (id, name, address, email, phone, whattsapp, location_id)
VALUES
  (1, 'Palermo',  'Av. Santa Fe 1234', 'palermo@ritmofit.com',  '011-4000-1001', '011-11-1001', 1),
  (2, 'Belgrano', 'Av. Cabildo 2345',  'belgrano@ritmofit.com', '011-4000-1002', '011-11-1002', 2);

-- =========================
-- SPORTS (disciplinas)
-- =========================
INSERT INTO sport (id, sport_type_name) VALUES
  (1, 'Funcional'),
  (2, 'Spinning'),
  (3, 'Yoga'),
  (4, 'Natación'),
  (5, 'Boxeo');

-- =========================
-- TEACHERS
-- =========================
INSERT INTO teacher (id, name) VALUES
  (1, 'Juan Lopez'),
  (2, 'María Diaz'),
  (3, 'Carlos Pérez'),
  (4, 'Lucía Fernández');

-- =========================
-- COURSES (Inicial/Intermedio/Avanzado por deporte)
-- Campos: (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id)
-- length: duración en minutos; price: precio demo
-- =========================

-- Funcional (sport 1)
INSERT INTO course (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id) VALUES
  (101, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Funcional Inicial',      0, 1),
  (102, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Funcional Intermedio',   0, 1),
  (103, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Funcional Avanzado',     0, 1);

-- Spinning (sport 2)
INSERT INTO course (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id) VALUES
  (201, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Spinning Inicial',       0, 2),
  (202, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Spinning Intermedio',    0, 2),
  (203, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Spinning Avanzado',      0, 2);

-- Yoga (sport 3)
INSERT INTO course (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id) VALUES
  (301, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Yoga Inicial',           0, 3),
  (302, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Yoga Intermedio',        0, 3),
  (303, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Yoga Avanzado',          0, 3);

-- Natación (sport 4)
INSERT INTO course (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id) VALUES
  (401, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Natación Inicial',       0, 4),
  (402, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Natación Intermedio',    0, 4),
  (403, DATE '2025-12-31', DATE '2025-09-01', NULL, 45, 'Natación Avanzado',      0, 4);

-- Boxeo (sport 5)
INSERT INTO course (id, fecha_fin, fecha_inicio, img_course, length, name, price, sport_name_id) VALUES
  (501, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Boxeo Inicial',          0, 5),
  (502, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Boxeo Intermedio',       0, 5),
  (503, DATE '2025-12-31', DATE '2025-09-01', NULL, 60, 'Boxeo Avanzado',         0, 5);

-- =========================
-- teacher_course (mapeo simple)
-- =========================
INSERT INTO teacher_course (course, teacher_id) VALUES
  (101,1),(102,1),(103,2),
  (201,3),(202,4),(203,3),
  (301,4),(302,1),(303,1),
  (401,2),(402,2),(403,3),
  (501,1),(502,4),(503,1);

-- =========================
-- headquarter_course
-- Reglas:
--   - Algunas clases en AMBAS sedes (1 y 2)
--   - Otras SOLO en 1 sede
-- =========================

-- Funcional: Inicial en 1 y 2; Intermedio solo 1; Avanzado solo 2
INSERT INTO headquarter_course (course, headquarter_id) VALUES
  (101,1),(101,2),
  (102,1),
  (103,2);

-- Spinning: Inicial solo 1; Intermedio solo 2; Avanzado en 1 y 2
INSERT INTO headquarter_course (course, headquarter_id) VALUES
  (201,1),
  (202,2),
  (203,1),(203,2);

-- Yoga: Inicial solo 2; Intermedio en 1 y 2; Avanzado solo 1
INSERT INTO headquarter_course (course, headquarter_id) VALUES
  (301,2),
  (302,1),(302,2),
  (303,1);

-- Natación: Inicial en 1 y 2; Intermedio solo 1; Avanzado solo 2
INSERT INTO headquarter_course (course, headquarter_id) VALUES
  (401,1),(401,2),
  (402,1),
  (403,2);

-- Boxeo: Inicial solo 1; Intermedio solo 2; Avanzado en 1 y 2
INSERT INTO headquarter_course (course, headquarter_id) VALUES
  (501,1),
  (502,2),
  (503,1),(503,2);

-- =========================
-- SHIFTS
-- Campos: (id, dia_en_que_se_dicta, hora_fin, hora_inicio, vacancy, clase_id, headquarter_id, teacher_id)
-- Turnos: mañana (08:00), tarde (18:00), noche (21:00)
-- Asumo: 1=Lun, 2=Mar, 3=Mié, 4=Jue, 5=Vie, 6=Sáb, 7=Dom
-- =========================

-- Funcional (101 en ambas sedes)
INSERT INTO shift (id, dia_en_que_se_dicta, hora_fin, hora_inicio, vacancy, clase_id, headquarter_id, teacher_id) VALUES
  (11001, 2, '09:00', '08:00', 25, 101, 1, 1),
  (11002, 4, '19:00', '18:00', 25, 101, 2, 2),
  (11003, 6, '22:00', '21:00', 20, 101, 1, 1);

-- Funcional Intermedio (solo sede 1)
INSERT INTO shift VALUES
  (11010, 3, '19:00', '18:00', 20, 102, 1, 1),
  (11011, 5, '22:00', '21:00', 20, 102, 1, 1);

-- Funcional Avanzado (solo sede 2)
INSERT INTO shift VALUES
  (11020, 2, '09:00', '08:00', 20, 103, 2, 2),
  (11021, 4, '19:00', '18:00', 20, 103, 2, 2);

-- Spinning
INSERT INTO shift VALUES
  (12001, 2, '08:45', '08:00', 30, 201, 1, 3),
  (12002, 5, '18:45', '18:00', 30, 201, 1, 3),

  (12010, 3, '18:45', '18:00', 25, 202, 2, 4),
  (12011, 6, '21:45', '21:00', 25, 202, 2, 4),

  (12020, 2, '08:45', '08:00', 30, 203, 1, 3),
  (12021, 4, '18:45', '18:00', 30, 203, 2, 4),
  (12022, 7, '21:45', '21:00', 25, 203, 1, 3);

-- Yoga
INSERT INTO shift VALUES
  (13001, 1, '09:00', '08:00', 18, 301, 2, 4),
  (13002, 5, '19:00', '18:00', 18, 301, 2, 4),

  (13010, 3, '19:00', '18:00', 20, 302, 1, 1),
  (13011, 6, '22:00', '21:00', 20, 302, 2, 1),

  (13020, 2, '09:00', '08:00', 18, 303, 1, 1),
  (13021, 4, '19:00', '18:00', 18, 303, 1, 1);

-- Natación
INSERT INTO shift VALUES
  (14001, 2, '08:45', '08:00', 12, 401, 1, 2),
  (14002, 5, '18:45', '18:00', 12, 401, 2, 3),

  (14010, 3, '18:45', '18:00', 10, 402, 1, 2),
  (14011, 7, '21:45', '21:00', 10, 402, 1, 2),

  (14020, 4, '08:45', '08:00', 12, 403, 2, 3),
  (14021, 6, '18:45', '18:00', 12, 403, 2, 3);

-- Boxeo
INSERT INTO shift VALUES
  (15001, 1, '09:00', '08:00', 16, 501, 1, 1),
  (15002, 5, '19:00', '18:00', 16, 501, 1, 1),

  (15010, 3, '19:00', '18:00', 14, 502, 2, 4),
  (15011, 7, '22:00', '21:00', 14, 502, 2, 4),

  (15020, 2, '09:00', '08:00', 16, 503, 1, 1),
  (15021, 4, '19:00', '18:00', 16, 503, 2, 4),
  (15022, 6, '22:00', '21:00', 14, 503, 1, 1);

-- =========================
-- RESERVATIONS (opcional demo)
-- Campos: (id, expiry_date, id_shift, id_user, metodo_de_pago)
-- metodo_de_pago es tinyint(0=EFECTIVO?,1=TARJETA?) → ajustalo si usás Enum propio
-- =========================
-- INSERT INTO reservations (id, expiry_date, id_shift, id_user, metodo_de_pago)
-- VALUES (1, TIMESTAMP '2025-09-27 07:30:00', 11001, 1, 0);
