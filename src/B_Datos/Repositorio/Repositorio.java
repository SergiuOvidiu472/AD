package B_Datos.Repositorio;

import A_Modelos.Pojo;

import java.util.List;
import java.util.function.Predicate;

public interface Repositorio<PK, T extends Pojo<PK>>
{
	public List<T> abrir();

	public void insertar(T pojo);

	public T buscar(PK pk);

	public void update(T pojo);

	public T delete(PK pk);

	public List<T> filtrar(Predicate<T> filtro);

	public void cerrar();
}
