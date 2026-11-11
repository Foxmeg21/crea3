-- 01-crear-tablas.sql  ·  CREA 3  ·  MySQL 8 / Aiven MySQL
-- No incluye CREATE DATABASE ni USE: se ejecuta sobre la base ya seleccionada
-- (MYSQL_DATABASE en docker compose / Kubernetes, o defaultdb / crea3_db en Aiven).
-- Es re-ejecutable: borra y recrea las tablas (¡elimina los datos existentes!).

SET NAMES utf8mb4;

DROP TABLE IF EXISTS tarea;
DROP TABLE IF EXISTS asistencia;
DROP TABLE IF EXISTS calificacion;
DROP TABLE IF EXISTS materia;
DROP TABLE IF EXISTS usuario;

-- Usuarios del sistema (login). El hash es BCrypt ($2a$), ver PasswordUtil.
CREATE TABLE usuario (
    id_usuario  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    apellido    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    contrasena  VARCHAR(60)  NOT NULL,
    rol         ENUM('DOCENTE','ESTUDIANTE') NOT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE materia (
    id_materia  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(80)  NOT NULL,
    PRIMARY KEY (id_materia),
    UNIQUE KEY uq_materia_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Relación 1:N usuario(estudiante) -> calificacion y materia -> calificacion
CREATE TABLE calificacion (
    id_calificacion INT   NOT NULL AUTO_INCREMENT,
    id_estudiante   INT   NOT NULL,
    id_materia      INT   NOT NULL,
    nota            FLOAT NOT NULL,
    fecha           DATE  NOT NULL,
    tipo            ENUM('Parcial','Examen','Tarea') NOT NULL,
    PRIMARY KEY (id_calificacion),
    CONSTRAINT fk_calif_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_calif_materia    FOREIGN KEY (id_materia)    REFERENCES materia (id_materia),
    CONSTRAINT ck_calif_nota CHECK (nota BETWEEN 1 AND 12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE asistencia (
    id_asistencia INT  NOT NULL AUTO_INCREMENT,
    id_estudiante INT  NOT NULL,
    id_materia    INT  NOT NULL,
    fecha         DATE NOT NULL,
    estado        ENUM('PRESENTE','AUSENTE','JUSTIFICADO') NOT NULL,
    PRIMARY KEY (id_asistencia),
    CONSTRAINT fk_asis_estudiante FOREIGN KEY (id_estudiante) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_asis_materia    FOREIGN KEY (id_materia)    REFERENCES materia (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tarea (
    id_tarea     INT          NOT NULL AUTO_INCREMENT,
    id_materia   INT          NOT NULL,
    titulo       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(500) NOT NULL,
    fecha_limite DATETIME     NOT NULL,
    PRIMARY KEY (id_tarea),
    CONSTRAINT fk_tarea_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_calif_estudiante ON calificacion (id_estudiante);
CREATE INDEX idx_asis_estudiante  ON asistencia (id_estudiante);
