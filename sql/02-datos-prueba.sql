-- 02-datos-prueba.sql  ·  CREA 3  ·  datos de prueba (requiere 01-crear-tablas.sql)
-- Usuarios: 2 docentes y 6 estudiantes. TODAS las contraseñas son: admin123 (guardadas con BCrypt).
-- Ingreso rápido:  docente@utu.edu.uy / admin123   y   estudiante@utu.edu.uy / admin123

SET NAMES utf8mb4;

INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena, rol) VALUES
(1, 'Matías', 'Alfaro', 'docente@utu.edu.uy', '$2a$10$mkq6pXkD4PGbsh4oxv70qeKyyx.7FXp1dEOLeKzq0To6csqWR7LQC', 'DOCENTE'),
(2, 'Lucía', 'Pereira', 'estudiante@utu.edu.uy', '$2a$10$A55cleXatTiEtDDtFEr50OFflDJ.odqjLBhHYmGUI8.AD/r3L7cUy', 'ESTUDIANTE'),
(3, 'Valentina', 'Techera', 'valentina.techera@utu.edu.uy', '$2a$10$1qEZkx676pIquJikiiROsOAwi6gzTd33VUk9qRXDcVDGzJDgCTNLK', 'ESTUDIANTE'),
(4, 'Joaquín', 'Rodríguez', 'joaquin.rodriguez@utu.edu.uy', '$2a$10$XCyijFtHOO.hYrb7vS3rhODuw9ahXpgMR5IN7h/PKlfjwL225RoDa', 'ESTUDIANTE'),
(5, 'Camila', 'Silva', 'camila.silva@utu.edu.uy', '$2a$10$TuY4guDynAhkbapZ86N.OOg0QTLHFT9RvIqx741PVSROZagJNRKmG', 'ESTUDIANTE'),
(6, 'Santiago', 'Martínez', 'santiago.martinez@utu.edu.uy', '$2a$10$XOs9iK/4bqHud2P4Kex6Ae6qilv84EeiWMLViVKeSH2GyrWbGpWGS', 'ESTUDIANTE'),
(7, 'Sofía', 'Núñez', 'sofia.nunez@utu.edu.uy', '$2a$10$27grWw5Sg74ZU166IoI9meEsmKcLgy5KnR5rS0YDH8td/A8a2oFn.', 'ESTUDIANTE'),
(8, 'Gabriela', 'Costa', 'gabriela.costa@utu.edu.uy', '$2a$10$AoZuTZahGyy.4X.ljBS6eOcYqtkW8biQb9.pJn9bLRzGHHXnDCqNK', 'DOCENTE');

INSERT INTO materia (id_materia, nombre) VALUES
(1, 'Programación Avanzada'),
(2, 'Base de Datos'),
(3, 'Sistemas Operativos'),
(4, 'Redes de Datos'),
(5, 'Inglés Técnico');

INSERT INTO calificacion (id_calificacion, id_estudiante, id_materia, nota, fecha, tipo) VALUES
(1, 2, 1, 8, '2026-08-20', 'Parcial'),
(2, 3, 2, 10, '2026-09-03', 'Examen'),
(3, 4, 3, 6, '2026-09-17', 'Tarea'),
(4, 5, 4, 11, '2026-10-01', 'Parcial'),
(5, 6, 5, 9, '2026-10-15', 'Examen'),
(6, 7, 1, 7, '2026-10-29', 'Tarea'),
(7, 2, 2, 12, '2026-11-05', 'Parcial'),
(8, 3, 3, 5, '2026-08-20', 'Examen'),
(9, 4, 4, 9, '2026-09-03', 'Tarea'),
(10, 5, 5, 10, '2026-09-17', 'Parcial'),
(11, 6, 1, 8, '2026-10-01', 'Examen'),
(12, 7, 2, 4, '2026-10-15', 'Tarea'),
(13, 2, 3, 11, '2026-10-29', 'Parcial'),
(14, 3, 4, 7, '2026-11-05', 'Examen'),
(15, 4, 5, 9, '2026-08-20', 'Tarea'),
(16, 5, 1, 6, '2026-09-03', 'Parcial'),
(17, 6, 2, 10, '2026-09-17', 'Examen'),
(18, 7, 3, 8, '2026-10-01', 'Tarea'),
(19, 2, 4, 12, '2026-10-15', 'Parcial'),
(20, 3, 5, 3, '2026-10-29', 'Examen'),
(21, 4, 1, 9, '2026-11-05', 'Tarea'),
(22, 5, 2, 7, '2026-08-20', 'Parcial'),
(23, 6, 3, 10, '2026-09-03', 'Examen'),
(24, 7, 4, 8, '2026-09-17', 'Tarea');

INSERT INTO asistencia (id_asistencia, id_estudiante, id_materia, fecha, estado) VALUES
(1, 2, 1, '2026-10-01', 'PRESENTE'),
(2, 3, 3, '2026-10-15', 'JUSTIFICADO'),
(3, 4, 5, '2026-10-29', 'PRESENTE'),
(4, 5, 2, '2026-11-05', 'AUSENTE'),
(5, 6, 4, '2026-08-20', 'PRESENTE'),
(6, 7, 1, '2026-09-03', 'PRESENTE'),
(7, 2, 3, '2026-09-17', 'PRESENTE'),
(8, 3, 5, '2026-10-01', 'JUSTIFICADO'),
(9, 4, 2, '2026-10-15', 'PRESENTE'),
(10, 5, 4, '2026-10-29', 'AUSENTE'),
(11, 6, 1, '2026-11-05', 'PRESENTE'),
(12, 7, 3, '2026-08-20', 'PRESENTE'),
(13, 2, 5, '2026-09-03', 'PRESENTE'),
(14, 3, 2, '2026-09-17', 'JUSTIFICADO'),
(15, 4, 4, '2026-10-01', 'PRESENTE'),
(16, 5, 1, '2026-10-15', 'AUSENTE'),
(17, 6, 3, '2026-10-29', 'PRESENTE'),
(18, 7, 5, '2026-11-05', 'PRESENTE'),
(19, 2, 2, '2026-08-20', 'PRESENTE'),
(20, 3, 4, '2026-09-03', 'JUSTIFICADO'),
(21, 4, 1, '2026-09-17', 'PRESENTE'),
(22, 5, 3, '2026-10-01', 'AUSENTE'),
(23, 6, 5, '2026-10-15', 'PRESENTE'),
(24, 7, 2, '2026-10-29', 'PRESENTE');

INSERT INTO tarea (id_tarea, id_materia, titulo, descripcion, fecha_limite) VALUES
(1, 1, 'TP1 - Capas y DAO', 'Implementar las capas modelo, DAO, servicio y presentación con interfaces.', '2026-11-20 23:59:00'),
(2, 1, 'TP2 - Docker', 'Contenerizar la aplicación y levantarla con docker compose.', '2026-11-27 23:59:00'),
(3, 2, 'Consultas con JOIN', 'Resolver los ejercicios de JOIN y GROUP BY de la guía 4.', '2026-11-18 18:00:00'),
(4, 2, 'Diagrama entidad-relación', 'Entregar el DER del proyecto final en formato PDF.', '2026-11-25 23:59:00'),
(5, 3, 'Procesos y hilos', 'Informe sobre planificación de procesos en Linux.', '2026-11-22 20:00:00'),
(6, 4, 'Direccionamiento IP', 'Subnetting de la red del laboratorio.', '2026-11-19 12:00:00'),
(7, 4, 'Configuración de VLAN', 'Simulación en Packet Tracer con tres VLAN.', '2026-12-02 23:59:00'),
(8, 5, 'Reading comprehension', 'Leer el artículo técnico y responder el cuestionario.', '2026-11-16 10:00:00');
