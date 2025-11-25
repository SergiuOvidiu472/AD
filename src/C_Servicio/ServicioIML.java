package C_Servicio;

import A_Modelos.Pojo;
import B_Datos.Repositorio.Repositorio;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ServicioIML<PK, T extends Pojo<PK>> implements Servicio<PK, T>
{
	private final Repositorio<PK, T> repo;

	private final List<Servicio<?, ?>> entidadesDebiles;
	private final List<Servicio<?, ?>> entidadesFuertes;

	private final Map<Predicate<T>, String> constraints;
	private final Map<BiFunction<T, T, Boolean>, String> uniques;

	public ServicioIML(Repositorio<PK, T> repo)
	{
		this.repo = repo;
		entidadesDebiles = new ArrayList<>();
		entidadesFuertes = new ArrayList<>();
		constraints = new HashMap<>();
		uniques = new HashMap<>();
	}

	private void pojoNotNull(T pojo)
	{
		if (pojo == null)
		{
			throw new ConstraintException("No se pueden insertar registros nulos");
		}
	}

	private void pkNotNull(T pojo)
	{
		if (pojo.getPK() == null)
		{
			throw new ConstraintException("La clave primaria no puede ser null");
		}
	}

	private void pkUnique(T pojo)
	{
		List<T> equalPK = repo.filtrar(x -> x.getPK().equals(pojo.getPK()));

		if (!equalPK.isEmpty())
		{
			StringBuilder stb = new StringBuilder();
			stb.append(pojo);
			stb.append(": DUPLICATE PRIMARY KEY -> ");
			stb.append(equalPK);
			stb.append("\n");
			throw new ConstraintException(stb.toString());
		}
	}

	private void verificarConstraints(T pojo)
	{
		if (constraints.keySet().parallelStream().anyMatch(x -> x.test(pojo)))
		{
			Stream<Predicate<T>> stream = constraints.keySet().parallelStream();
			List<Predicate<T>> constraintsEncontrados = stream.filter(x -> x.test(pojo)).toList();

			StringBuilder stb = new StringBuilder();
			stb.append(pojo);
			stb.append(": CONSTRAINTS ENCONTRADOS\n");

			for (Predicate<T> encontrado : constraintsEncontrados)
			{
				stb.append(constraints.get(encontrado));
				stb.append("\n");
			}

			throw new ConstraintException(stb.toString());
		}
	}

	private void verificarUnicidad(T pojo)
	{
		List<T> allPojos = repo.filtrar(Objects::nonNull);
		Set<BiFunction<T, T, Boolean>> uniquesConstraints = uniques.keySet();

		for (T pojoIT : allPojos)
		{
			Stream<BiFunction<T, T, Boolean>> stream = uniquesConstraints.parallelStream();
			List<BiFunction<T, T, Boolean>> uniquesEncontrados = stream.filter(x -> x.apply(pojo, pojoIT)).toList();
			List<T> equalPK = repo.filtrar(x -> x.getPK().equals(pojo.getPK()));

			boolean mismoPojo = (equalPK.size() == 1)
				&&
				equalPK.parallelStream().anyMatch(x -> x.getPK() == pojo.getPK());

			if (!uniquesEncontrados.isEmpty() && !mismoPojo)
			{
				StringBuilder stb = new StringBuilder();
				stb.append(pojo);
				stb.append(": DUPLICIDAD\n");

				for (BiFunction<T, T, Boolean> uniqueEncontrado : uniquesEncontrados)
				{
					stb.append(uniques.get(uniqueEncontrado));
					stb.append("\n");
				}

				throw new ConstraintException(stb.toString());
			}
		}
	}

	private void verificarRelaciones(T pojo)
	{
		List<Object> fks = pojo.getFK();

		for (Servicio<?, ?> srv : entidadesFuertes)
		{
			List<?> entidadFuerte = srv.select(x -> fks.contains(x.getClass()));
			Class<?> claseEntidadFuerte = entidadFuerte.getFirst().getClass();

			int indexEntidadFuerte = fks.indexOf(claseEntidadFuerte);
			Object claveForanea = fks.get(indexEntidadFuerte + 1);

			if (srv.select(x -> x.getPK().equals(claveForanea)).isEmpty())
			{
				StringBuilder stb = new StringBuilder();
				stb.append(pojo);
				stb.append(": No tiene asociación con ninguna entidad fuerte\n");
				throw new ConstraintException(stb.toString());
			}

		}
	}

	@Override
	public void addEntidadDebil(Servicio<?, ?> entidad)
	{
		entidadesDebiles.add(entidad);
	}

	@Override
	public void addEntidadFuerte(Servicio<?, ?> entidad)
	{
		entidadesFuertes.add(entidad);
	}

	@Override
	public void addConstraint(String nombre, Predicate<T> throwsExceptionIfTrue)
	{
		constraints.put(throwsExceptionIfTrue, nombre);
	}

	@Override
	public void addConstraint(String nombre, BiFunction<T, T, Boolean> throwsExceptionIfTrue)
	{
		uniques.put(throwsExceptionIfTrue, nombre);
	}

	@Override
	public void abrir()
	{
		List<T> all = repo.abrir();
		for (T i : all)
		{
			insert(i);
		}
	}

	@Override
	public void insert(T pojo) throws ConstraintException
	{
		pojoNotNull(pojo);
		pkNotNull(pojo);
		pkUnique(pojo);
		verificarConstraints(pojo);
		verificarUnicidad(pojo);
		verificarRelaciones(pojo);
		repo.insertar(pojo);
	}

	@Override
	public List<T> select(Predicate<T> select)
	{
		return repo.filtrar(select);
	}

	@Override
	public int update(Predicate<T> query, Consumer<T> updateStatement) throws ConstraintException, IOException
	{
		List<T> queryList = repo.filtrar(query);
		int updates = 0;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		     ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			for (T pojo : queryList)
			{
				oos.writeObject(pojo);
				byte[] pojoEnBytes = baos.toByteArray();
				T copia = null;

				try (ByteArrayInputStream bais = new ByteArrayInputStream(pojoEnBytes);
				     ObjectInputStream ois = new ObjectInputStream(bais))
				{
					copia = (T) ois.readObject();
				} catch (IOException | ClassNotFoundException e)
				{
					throw new IOException(pojo + ": No se pudo actualizar");
				}


				updateStatement.accept(copia);
				verificarConstraints(copia);
				verificarUnicidad(copia);
				verificarRelaciones(copia);

				updateStatement.accept(pojo);
				++updates;
			}
		} catch (IOException e)
		{
			throw new IOException(e.getMessage());
		}

		return updates;
	}

	@Override
	public void delete(Predicate<T> delete)
	{
		List<T> deleteOnCascade = repo.filtrar(delete);
		for (T i : deleteOnCascade)
		{
			for (Servicio<?, ?> srv : entidadesDebiles)
			{
				srv.delete(x -> x.getFK().contains(i.getPK()));
			}
			repo.delete(i.getPK());
		}
	}

	@Override
	public void cerrar()
	{
		repo.cerrar();
	}
}
