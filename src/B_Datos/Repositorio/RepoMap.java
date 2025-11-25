package B_Datos.Repositorio;

import A_Modelos.Pojo;
import B_Datos.Persistencia.Persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

public class RepoMap<PK, T extends Pojo<PK>> implements Repositorio<PK, T>
{
	private final LinkedHashMap<PK, T> ram;
	private final Persistencia<PK, T> IO;

	public RepoMap(Persistencia<PK, T> IO)
	{
		this.IO = IO;
		ram = new LinkedHashMap<>();
	}

	@Override
	public List<T> abrir()
	{
		return new ArrayList<>(IO.cargar());
	}

	@Override
	public void insertar(T pojo)
	{
		ram.putLast(pojo.getPK(), pojo);
	}

	@Override
	public T buscar(PK pk)
	{
		return ram.get(pk);
	}

	@Override
	public void update(T pojo)
	{
		ram.replace(pojo.getPK(), pojo);
	}

	@Override
	public T delete(PK pk)
	{
		return ram.remove(pk);
	}

	@Override
	public List<T> filtrar(Predicate<T> filtro)
	{
		return new ArrayList<>(ram.values().parallelStream().filter(filtro).toList());
	}

	@Override
	public void cerrar()
	{
		IO.guardar(new ArrayList<>(ram.values()));
	}
}
