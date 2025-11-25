package B_Datos.Persistencia;

import A_Modelos.Pojo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoBIN<PK, T extends Pojo<PK>> implements Persistencia<PK, T>
{
	private final String ruta;

	public ArchivoBIN(String ruta)
	{
		this.ruta = ruta;
	}

	@Override
	public List<T> cargar()
	{
		ArrayList<T> carga = new ArrayList<>();

		try (
			FileInputStream in = new FileInputStream(ruta);
			ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in))
		)
		{
			while (true /* read until EOF */)
			{
				carga.add((T) oin.readObject());
			}
		} catch (EOFException exc)      // return
		{
			return carga;
		} catch (IOException | ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void guardar(List<T> guardado)
	{
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

		try (
			ObjectOutputStream oos = new ObjectOutputStream(byteArray);
			FileOutputStream fos = new FileOutputStream(ruta)
		)
		{
			for (T i : guardado)
				oos.writeObject(i);

			fos.write(byteArray.toByteArray());
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
