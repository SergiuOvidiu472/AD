package A_Modelos.Entidades;

import A_Modelos.Pojo;

import java.util.List;

public class Estudiante implements Pojo<Integer>
{
	private int ID;
	private String nombre;
	private String primerApellido;
	private String segundoApellido;

	public Estudiante()
	{
	}

	public Estudiante(int ID, String nombre, String primerApellido, String segundoApellido)
	{
		this.ID = ID;
		this.nombre = nombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
	}

	public void setNombre(String nombre)
	{
		this.nombre = nombre;
	}

	public String getNombre()
	{
		return nombre;
	}

	public String getPrimerApellido()
	{
		return primerApellido;
	}

	public void setPrimerApellido(String primerApellido)
	{
		this.primerApellido = primerApellido;
	}

	public String getSegundoApellido()
	{
		return segundoApellido;
	}

	public void setSegundoApellido(String segundoApellido)
	{
		this.segundoApellido = segundoApellido;
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
		return ID + " | " + nombre + " | " + primerApellido + " | " + segundoApellido;
	}
}
