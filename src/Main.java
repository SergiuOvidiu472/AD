import A_Modelos.Entidades.*;
import B_Datos.Persistencia.ArchivoBIN;
import B_Datos.Persistencia.ArchivoCSV;
import B_Datos.Persistencia.ArchivoXML;
import B_Datos.Repositorio.RepoMYSQL;
import B_Datos.Repositorio.RepoMap;
import B_Datos.Repositorio.Repositorio;
import C_Servicio.ConstraintException;
import C_Servicio.Servicio;
import C_Servicio.ServicioIML;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Main
{
	private static final int CLASES = 6;
	private static final int MODOS_ARCHIVOS = 3;

	static Servicio<Integer, Asignatura> srvA;
	static Servicio<Integer, Estudiante> srvE;
	static Servicio<Integer, Evaluacion> srvR;
	static Servicio<Integer, Examen> srvX;
	static Servicio<Integer, Matricula> srvM;
	static Servicio<Integer, Nota> srvN;

	static Repositorio<Integer, Estudiante> repoE;
	static Repositorio<Integer, Asignatura> repoA;
	static Repositorio<Integer, Evaluacion> repoR;
	static Repositorio<Integer, Examen> repoX;
	static Repositorio<Integer, Matricula> repoM;
	static Repositorio<Integer, Nota> repoN;

	static Properties cargarConfiguracion()
	{
		Properties appConf = new Properties();

		try
		{
			appConf.load(new FileInputStream("./conf/conf.props"));
		} catch (IOException e)
		{
			System.out.println("No se ha encontrado la configuración");
			throw new RuntimeException(e);
		}

		return appConf;
	}

	static Properties getPropsDB(String ruta)
	{
		Properties prop = new Properties();
		try
		{
			prop.load(new FileInputStream(ruta));
		} catch (IOException e)
		{
			throw new RuntimeException(ruta + ": " + e);
		}
		return prop;
	}

	static String[][] getRutas(String archivo)
	{
		String[][] rutas = new String[MODOS_ARCHIVOS][CLASES];
		Path path = Path.of(archivo);

		try
		{
			ArrayList<String> ln = new ArrayList<>(Files.readAllLines(path));
			ln.removeIf(x -> x.equals("-"));
			int k = 0;

			for (int i = 0; i < MODOS_ARCHIVOS; ++i)
			{
				for (int j = 0; j < CLASES; ++j)
				{
					rutas[i][j] = ln.get(k);
					++k;
				}
			}

		} catch (IOException e)
		{
			StringBuilder stb = new StringBuilder();

			stb.append(path);
			stb.append(": Archivo de rutas no encontrado\n");
			stb.append("Prueba a escribir 'RUTAS'=[ruta del archivo de rutas] en la configuracion\n");

			throw new RuntimeException(stb.toString());
		} catch (IndexOutOfBoundsException e)
		{
			StringBuilder stb = new StringBuilder();
			stb.append("El formato no es válido\n");
			stb.append("Consiste en escribir para cada modo de archivo la ruta de cada clase\n");
			stb.append("Cada modo se separa con salto de línea, un guión y otro salto de línea\n");
			stb.append("El orden de las entidades debe de ser el mismo en cada modo\n");
			// y el mismo que en iniciarServicios();
			throw new RuntimeException(stb.toString());
		}
		return rutas;
	}

	static ArrayList<String> getXmlRoots(String archivo)
	{
		ArrayList<String> xml = new ArrayList<>();
		Path ruta = Path.of(archivo);

		try
		{
			xml.addAll(Files.readAllLines(ruta));
		} catch (IOException e)
		{
			StringBuilder stb = new StringBuilder(e.getMessage());
			stb.append("\n");
			stb.append("Es posible que no se haya encontrado el archivo\n");
			stb.append("Prueba a escribir en la configuración 'XML_ROOTS'=[ruta del archivo con los xml roots]\n");
			stb.append("Los root del xml se consideran en el mismo orden que en las rutas de los archivos\n");
			throw new RuntimeException(stb.toString());
		}

		return xml;
	}

	static boolean crearSiNoExisten(String[] rutas)
	{
		boolean existe = true;
		int i = 0;

		while (i < CLASES)
		{
			Path path = Path.of(rutas[i]);

			try
			{
				Files.createDirectories(path.getParent());
				if (!Files.isRegularFile(path))
				{
					Files.createFile(path);
					existe = false;
				}
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			++i;
		}
		return existe;
	}

	static Connection iniciarConexionMYSQL(Properties props)
	{
		String databaseUser = props.get("DB_USER").toString();
		String databaseName = props.get("DB_NAME").toString();
		String databaseHost = props.get("DB_HOST").toString();

		String databasePass = System.getenv(props.get("DB_PASS").toString());           // variable de entorno

		StringBuilder url = new StringBuilder("jdbc:mysql://");
		url.append(databaseHost);
		url.append("/");
		url.append(databaseName);
		url.append("?allowMultiQueries=true");

		try
		{
			return DriverManager.getConnection(url.toString(), databaseUser, databasePass);
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	static void iniciarBaseDeDatosMYSQL(Connection mysqlConn, String rutaScript)
	{
		try (FileInputStream fis = new FileInputStream(rutaScript))
		{
			String file = new String(fis.readAllBytes());
			PreparedStatement ps = mysqlConn.prepareStatement(file);
			ps.executeLargeUpdate();
		} catch (IOException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	static void iniciarServicios()
	{
		Properties appConf = cargarConfiguracion();
		String[] atbs = {"MODO", "RUTAS", "XML_ROOTS", "MYSQL_CONF"};

		String modo = appConf.get(atbs[0]).toString();
		String archivoRutas = appConf.get(atbs[1]).toString();
		String archivoXMLRoots = appConf.get(atbs[2]).toString();
		String rutaDB_CONF = appConf.get(atbs[3]).toString();
		Properties dbConf = getPropsDB(rutaDB_CONF);

		Class[] clase = new Class[CLASES];
		clase[0] = Asignatura.class;
		clase[1] = Estudiante.class;
		clase[2] = Evaluacion.class;
		clase[3] = Examen.class;
		clase[4] = Matricula.class;
		clase[5] = Nota.class;

		ArrayList<String> xmlR = getXmlRoots(archivoXMLRoots);
		if (xmlR.size() != CLASES)
		{
			throw new IllegalArgumentException("No hay tantos xml roots como entidades");
		}

		String[][] rutasF = getRutas(archivoRutas);
		boolean abrirServicio = true;

		switch (modo.toLowerCase())
		{
			case "bin" ->
			{
				repoA = new RepoMap<>(new ArchivoBIN<>(rutasF[0][0]));
				repoE = new RepoMap<>(new ArchivoBIN<>(rutasF[0][1]));
				repoR = new RepoMap<>(new ArchivoBIN<>(rutasF[0][2]));
				repoX = new RepoMap<>(new ArchivoBIN<>(rutasF[0][3]));
				repoM = new RepoMap<>(new ArchivoBIN<>(rutasF[0][4]));
				repoN = new RepoMap<>(new ArchivoBIN<>(rutasF[0][5]));
				abrirServicio = crearSiNoExisten(rutasF[0]);
			}
			case "csv" ->
			{
				repoA = new RepoMap<>(new ArchivoCSV<>(clase[0], rutasF[1][0]));
				repoE = new RepoMap<>(new ArchivoCSV<>(clase[1], rutasF[1][1]));
				repoR = new RepoMap<>(new ArchivoCSV<>(clase[2], rutasF[1][2]));
				repoX = new RepoMap<>(new ArchivoCSV<>(clase[3], rutasF[1][3]));
				repoM = new RepoMap<>(new ArchivoCSV<>(clase[4], rutasF[1][4]));
				repoN = new RepoMap<>(new ArchivoCSV<>(clase[5], rutasF[1][5]));
				abrirServicio = crearSiNoExisten(rutasF[1]);
			}
			case "xml" ->
			{
				repoA = new RepoMap<>(new ArchivoXML<>(clase[0], rutasF[2][0], xmlR.get(0)));
				repoE = new RepoMap<>(new ArchivoXML<>(clase[1], rutasF[2][1], xmlR.get(1)));
				repoR = new RepoMap<>(new ArchivoXML<>(clase[2], rutasF[2][2], xmlR.get(2)));
				repoX = new RepoMap<>(new ArchivoXML<>(clase[3], rutasF[2][3], xmlR.get(3)));
				repoM = new RepoMap<>(new ArchivoXML<>(clase[4], rutasF[2][4], xmlR.get(4)));
				repoN = new RepoMap<>(new ArchivoXML<>(clase[5], rutasF[2][5], xmlR.get(5)));
				abrirServicio = crearSiNoExisten(rutasF[2]);
			}
			case "mysql" ->
			{
				Connection sqlConn = iniciarConexionMYSQL(dbConf);
				String rutaInitSQL = dbConf.get("DB_INIT").toString();
				iniciarBaseDeDatosMYSQL(sqlConn, rutaInitSQL);

				repoA = new RepoMYSQL<>(clase[0], sqlConn);
				repoE = new RepoMYSQL<>(clase[1], sqlConn);
				repoR = new RepoMYSQL<>(clase[2], sqlConn);
				repoX = new RepoMYSQL<>(clase[3], sqlConn);
				repoM = new RepoMYSQL<>(clase[4], sqlConn);
				repoN = new RepoMYSQL<>(clase[5], sqlConn);
			}
		}

		srvA = new ServicioIML<>(repoA);
		srvE = new ServicioIML<>(repoE);
		srvR = new ServicioIML<>(repoR);
		srvX = new ServicioIML<>(repoX);
		srvM = new ServicioIML<>(repoM);
		srvN = new ServicioIML<>(repoN);

		if (abrirServicio)
		{
			srvA.abrir();
			srvE.abrir();
			srvR.abrir();
			srvX.abrir();
			srvM.abrir();
			srvN.abrir();
		}
	}

	static void setConstraints()
	{
		srvA.addConstraint("Nombre obligatorio", x -> x.getNombre() == null || x.getNombre().isEmpty());
		srvA.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);
		srvA.addConstraint("Nombre único", (x, y) -> x.getNombre().equals(y.getNombre()));

		srvE.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);
		srvE.addConstraint("Nombre obligatorio", x -> x.getNombre() == null || x.getNombre().isEmpty());

		srvR.addConstraint("Descripción obligatoria", x -> x.getDescripcion() == null || x.getDescripcion().isEmpty());
		srvR.addConstraint("Descripción unica", (x, y) -> x.getDescripcion().equals(y.getDescripcion()));
		srvR.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);

		srvX.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);

		srvM.addConstraint("Un estudiante solo puede tener una matricula por asignatura", (x, y) -> x.getFK().equals(y.getFK()));
		srvM.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);

		srvN.addConstraint("Nota mayor a cero", x -> x.getNota() < 0.0f);
		srvN.addConstraint("Matricula-Evalución unica", (x, y) -> (x.getIdEvaluacion() == y.getIdEvaluacion()) && (x.getIdMatricula() == y.getIdMatricula()));
		srvN.addConstraint("Solo puede haber una nota por examen, evaluacion y por matricula", (x, y) -> x.getFK().equals(y.getFK()));
		srvN.addConstraint("Id mayor a cero", x -> x.getPK() <= 0);
	}

	static void setRelations()
	{
		// Asignatura -> Matricula
		srvA.addEntidadDebil(srvM);

		// Estudiante -> Matricula
		srvE.addEntidadDebil(srvM);

		// Evaluación -> Notas
		srvR.addEntidadDebil(srvN);

		// Examen -> Notas
		srvX.addEntidadDebil(srvN);

		// Matricula <- Asignatura, Estudiante
		srvM.addEntidadFuerte(srvA);
		srvM.addEntidadFuerte(srvE);
		// Matricula -> Notas
		srvM.addEntidadDebil(srvN);

		// Notas <- Evaluación, Matricula, Examen
		srvN.addEntidadFuerte(srvR);
		srvN.addEntidadFuerte(srvM);
		srvN.addEntidadFuerte(srvX);
	}

	static void salir()
	{
		srvA.cerrar();
		srvE.cerrar();
		srvR.cerrar();
		srvX.cerrar();
		srvM.cerrar();
		srvN.cerrar();
	}

	static void insertTest()
	{
		srvE.insert(new Estudiante(1, "Miguel", "Montero", "Vidal"));
		srvE.insert(new Estudiante(2, "Josu", "Xu", ""));
		srvE.insert(new Estudiante(3, "Héctor", "Torres", ""));
		srvE.insert(new Estudiante(4, "María", "Cristina", "Fernández"));
		srvE.insert(new Estudiante(5, "Beatriz", "Álvarez", ""));
		srvE.insert(new Estudiante(6, "Roberto", "Castro", ""));
		srvE.insert(new Estudiante(7, "Miriam", "Nieto", "Gómez"));
		srvE.insert(new Estudiante(8, "Josep", "Santos", ""));
		srvE.insert(new Estudiante(9, "Julio", "Saéz", "Sanchez"));
		srvE.insert(new Estudiante(10, "María", "Nieves", "Rubio"));
		srvE.insert(new Estudiante(11, "Alberto", "Lorenzo", "Velazquez"));

		srvA.insert(new Asignatura(1, "Sistemas de gestión empresarial", 0));
		srvA.insert(new Asignatura(2, "Desarrollo y despliegue de aplicaciones", 0));
		srvA.insert(new Asignatura(3, "Programación multimedia y dispositivos móviles", 0));

		srvR.insert(new Evaluacion(1, "Primer trimestre"));
		srvR.insert(new Evaluacion(2, "Segundo trimestre"));
		srvR.insert(new Evaluacion(3, "Tercer trimestre"));
		srvR.insert(new Evaluacion(4, "Junio 1"));
		srvR.insert(new Evaluacion(5, "Junio 2"));

		srvM.insert(new Matricula(1, 1, 1));
		srvM.insert(new Matricula(2, 1, 2));
		srvM.insert(new Matricula(3, 1, 3));

		srvM.insert(new Matricula(4, 2, 1));
		srvM.insert(new Matricula(5, 2, 2));
		srvM.insert(new Matricula(6, 2, 3));

		srvM.insert(new Matricula(7, 3, 1));
		srvM.insert(new Matricula(8, 3, 2));
		srvM.insert(new Matricula(9, 3, 3));

		srvM.insert(new Matricula(10, 4, 1));
		srvM.insert(new Matricula(11, 4, 2));
		srvM.insert(new Matricula(12, 4, 3));

		srvM.insert(new Matricula(13, 5, 1));
		srvM.insert(new Matricula(14, 5, 2));
		srvM.insert(new Matricula(15, 5, 3));

		srvM.insert(new Matricula(16, 6, 1));
		srvM.insert(new Matricula(17, 6, 2));
		srvM.insert(new Matricula(18, 6, 3));

		srvM.insert(new Matricula(19, 7, 1));
		srvM.insert(new Matricula(20, 7, 2));
		srvM.insert(new Matricula(21, 7, 3));

		srvM.insert(new Matricula(22, 8, 1));
		srvM.insert(new Matricula(23, 8, 2));
		srvM.insert(new Matricula(24, 8, 3));

		srvM.insert(new Matricula(25, 9, 1));
		srvM.insert(new Matricula(26, 9, 2));
		srvM.insert(new Matricula(27, 9, 3));

		srvM.insert(new Matricula(28, 10, 1));
		srvM.insert(new Matricula(29, 10, 2));
		srvM.insert(new Matricula(30, 10, 3));

		srvM.insert(new Matricula(31, 11, 1));
		srvM.insert(new Matricula(32, 11, 2));
		srvM.insert(new Matricula(33, 11, 3));

		srvX.insert(new Examen(1, Path.of("./examenes/examen.asc")));
		srvX.insert(new Examen(2, Path.of("./holaMundo")));

		for (int i = 1, j = 3; j <= 33; ++i, j += 3)
		{
			srvN.insert(new Nota(i, j, 1, 1));
		}

		try
		{
			srvN.update(Objects::nonNull, List.of(
				x -> x.setNota(5.9f)
			));
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

	static void clearTest()
	{
		srvA.delete(Objects::nonNull);
		srvE.delete(Objects::nonNull);
		srvR.delete(Objects::nonNull);
		srvX.delete(Objects::nonNull);
		srvM.delete(Objects::nonNull);
		srvN.delete(Objects::nonNull);
	}

	static void updateTest()
	{
		try
		{
			srvN.update(x -> x.getPK() == 1, List.of(
				x -> x.setIdMatricula(3),
				x -> x.setIdEvaluacion(1),
				x -> x.setIdExamen(3),
				x -> x.setNota(-2.0f)
			));
		} catch (IOException | ConstraintException e)
		{
			System.err.println(e.getMessage());
		}
	}

	static void multipleQueriesTest()
	{
		/*
		select descripción from evaluaciones where id = (select idEvaluacion from notas where id = 1)
		 */

		List<Nota> innerQueryResult = srvN.select(x -> x.getPK() == 1);

		List<Evaluacion> outterQuery = srvR.select(
			ev -> innerQueryResult.parallelStream().allMatch(
				nota -> ev.getPK().equals(nota.getIdEvaluacion())
			)
		);

		outterQuery.forEach(x -> System.out.println(x.getDescripcion()));
	}

	static void joinsTest()
	{
		// select * from estudiante
		// inner join matricula
		// on estudiante.getPK() == matricula.getIdEstudiante();

		List<Estudiante> estudiantes = srvE.select(Objects::nonNull);   // select * from estudiante
		List<Matricula> matriculas = srvM.select(Objects::nonNull);     // select * from matricula

		int maxE = estudiantes.size();
		int maxM = matriculas.size();
		int estudiante_I = 0;
		int matricula_M = 0;

		StringBuilder join = new StringBuilder();

		while (estudiante_I < maxE)
		{
			Estudiante estd = estudiantes.get(estudiante_I);
			while ((matricula_M < maxM) && (estd.getPK() == matriculas.get(matricula_M).getIdEstudiante()))
			{
				join.append(estd);
				join.append("\t\t");
				join.append(matriculas.get(matricula_M));
				join.append("\n");
				++matricula_M;
			}
			++estudiante_I;
		}

		System.out.println(join);
	}

	static void joins_2_Test()
	{
		// select * from estudiante
		// inner join matricula
		// on estudiante.getPK() == matricula.getIdEstudiante() (primerJoin)
		// inner join asignatura
		// on asignatura.getPK ==  primerJoin.idAsignatura

		// La entidad matrícula contiene los estudiantes y asignaturas
		List<Matricula> matriculas = srvM.select(Objects::nonNull);     // select * from matricula

		int i = 0;
		StringBuilder join = new StringBuilder();

		while (i < matriculas.size())
		{
			Matricula M = matriculas.get(i);

			List<Estudiante> estd = srvE.select(x -> x.getPK() == M.getIdEstudiante());
			List<Asignatura> asgn = srvA.select(x -> x.getPK() == M.getIdAsignatura());

			join.append(estd);
			join.append("\t\t\t\t");
			join.append(M);
			join.append("\t\t\t\t");
			join.append(asgn);
			join.append("\n");

			++i;
		}

		System.out.println(join);
	}

	public static void main(String[] args)
	{
		iniciarServicios();
		setConstraints();
		setRelations();

		// insertTest();
		// clearTest();
		// updateTest();
		// multipleQueriesTest();
		joins_2_Test();

		salir();
	}
}