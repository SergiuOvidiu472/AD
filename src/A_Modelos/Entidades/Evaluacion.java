package A_Modelos.Entidades;

import A_Modelos.Pojo;

import java.util.List;

public class Evaluacion implements Pojo<Integer>
{
	private int ID;
	private String descripcion;

	public Evaluacion()
	{
	}

	public Evaluacion(int ID, String descripcion)
	{
		this.ID = ID;
		this.descripcion = descripcion;
	}

	public String getDescripcion()
	{
		return descripcion;
	}

	@Override
	public Integer getPK()
	{
		return ID;
	}

	@Override
	public List<Object> getFK()
	{
		return List.of();
	}

	@Override
	public String toString()
	{
		return ID + " | " + descripcion;
	}
}
