package B_Datos.Persistencia;

import A_Modelos.Pojo;

import java.util.List;

public interface Persistencia<PK, T extends Pojo<PK>>
{
	public List<T> cargar();

	public void guardar(List<T> guardado);
}
