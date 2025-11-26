package C_Servicio;

import A_Modelos.Pojo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Servicio<PK, T extends Pojo<PK>>
{
	public void abrir();

	public void addEntidadDebil(Servicio<?, ?> entidad);

	public void addEntidadFuerte(Servicio<?, ?> entidad);

	public void addConstraint(String nombre, Predicate<T> throwsExceptionIfTrue);

	public void addConstraint(String nombre, BiFunction<T, T, Boolean> throwsExceptionIfTrue);

	public void insert(T pojo) throws ConstraintException;

	public List<T> select(Predicate<T> select);

	public int update(Predicate<T> query, List<Consumer<T>> updateStatements)
		throws ConstraintException, IOException;

	public void delete(Predicate<T> delete);

	public void cerrar();
}
