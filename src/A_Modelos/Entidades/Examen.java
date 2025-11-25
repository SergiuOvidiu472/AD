package A_Modelos.Entidades;

import A_Modelos.BlobHEX;
import A_Modelos.Pojo;

import java.nio.file.Path;
import java.util.List;

public class Examen implements Pojo<Integer>
{
	private int ID;
	private BlobHEX entrega;

	public Examen()
	{
	}

	public Examen(int ID, Path ruta)
	{
		this.ID = ID;
		entrega = new BlobHEX(ruta);
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
		return ID + " | " + entrega;
	}
}
