package A_Modelos.Entidades;

import A_Modelos.Pojo;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

public class Matricula implements Pojo<Integer>
{
	private int ID;
	private int idEstudiante;
	private int idAsignatura;
	private String fecha;

	public Matricula()
	{
	}

	public Matricula(int ID, int idEstudiante, int idAsignatura)
	{
		this.ID = ID;
		this.idEstudiante = idEstudiante;
		this.idAsignatura = idAsignatura;
		this.fecha = Date.from(Instant.now()).toString();
	}

	@Override
	public Integer getPK()
	{
		return ID;
	}

	public int getIdEstudiante()
	{
		return idEstudiante;
	}

	public int getIdAsignatura()
	{
		return idAsignatura;
	}

	@Override
	public List<Object> getFK()
	{
		return List.of(Estudiante.class, idEstudiante, Asignatura.class, idAsignatura);
	}

	@Override
	public String toString()
	{
		return ID + " | " + idEstudiante + " | " + idAsignatura + " | " + fecha;
	}
}
