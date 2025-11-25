USE Colegio;

CREATE TABLE IF NOT EXISTS Asignatura (
    ID int,
    nombre VARCHAR(50) NOT NULL UNIQUE,

    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS Estudiante (
    ID int,
    nombre VARCHAR(50) NOT NULL,

    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS Evaluacion (
    ID int,
    descripcion VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS Examen (
    ID int,
    entrega BLOB,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS Matricula (
    ID int,
    idEstudiante int,
    idAsignatura int,
    fecha VARCHAR(50) NOT NULL,

    PRIMARY KEY(ID),
    FOREIGN KEY(idEstudiante) REFERENCES Estudiante(ID),
    FOREIGN KEY(idAsignatura) REFERENCES Asignatura(ID)
);

CREATE TABLE IF NOT EXISTS Nota (
    ID int,
    idMatricula  int,
    idEvaluacion int,
    idExamen int,
    nota float,

    PRIMARY KEY(ID),
    FOREIGN KEY(idMatricula) REFERENCES Matricula(ID),
    FOREIGN KEY(idEvaluacion) REFERENCES Evaluacion(ID),
    FOREIGN KEY(idExamen) REFERENCES Examen(ID)
);