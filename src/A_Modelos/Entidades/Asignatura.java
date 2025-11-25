package A_Modelos.Entidades;

import A_Modelos.Pojo;

import java.util.List;

public class Asignatura implements Pojo<Integer>
{
	private int ID;
	private String nombre;
	private int creditos;

	public Asignatura()
	{
	}

	public Asignatura(int ID, String nombre, int creditos)
	{
		this.ID = ID;
		this.nombre = nombre;
		this.creditos = creditos;
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

	public String getNombre()
	{
		return nombre;
	}

	@Override
	public String toString()
	{
		return ID + " | " + nombre;
	}
}