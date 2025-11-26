package A_Modelos.Entidades;

import A_Modelos.Pojo;

import java.util.List;

public class Nota implements Pojo<Integer>
{
	private int ID;
	private int idMatricula;
	private int idEvaluacion;
	private int idExamen;
	private float nota;

	public Nota()
	{
	}

	public Nota(int ID, int idMatricula, int idEvaluacion, int idExamen)
	{
		this.ID = ID;
		this.idMatricula = idMatricula;
		this.idEvaluacion = idEvaluacion;
		this.idExamen = idExamen;
	}

	public Nota(int ID, int idMatricula, int idEvaluacion, int idExamen, float nota)
	{
		this.ID = ID;
		this.idMatricula = idMatricula;
		this.idEvaluacion = idEvaluacion;
		this.idExamen = idExamen;
		this.nota = nota;
	}

	public float getNota()
	{
		return nota;
	}

	public void setNota(float nota)
	{
		this.nota = nota;
	}

	@Override
	public Integer getPK()
	{
		return ID;
	}

	public int getIdMatricula()
	{
		return idMatricula;
	}

	public int getIdEvaluacion()
	{
		return idEvaluacion;
	}

	public void setIdExamen(int idExamen)
	{
		this.idExamen = idExamen;
	}

	public int getIdExamen()
	{
		return idExamen;
	}

	public void setIdMatricula(int idMatricula)
	{
		this.idMatricula = idMatricula;
	}

	public void setIdEvaluacion(int idEvaluacion)
	{
		this.idEvaluacion = idEvaluacion;
	}

	@Override
	public List<Object> getFK()
	{
		return List.of(
			Matricula.class, idMatricula,
			Evaluacion.class, idEvaluacion,
			Examen.class, idExamen
		);
	}

	@Override
	public String toString()
	{
		return "Calificacion{" +
			"ID=" + ID +
			", idMatricula=" + idMatricula +
			", idEvaluacion=" + idEvaluacion +
			", idExamen=" + idExamen +
			", nota=" + nota +
			'}';
	}
}
