package B_Datos.Persistencia;

import A_Modelos.Pojo;
import A_Modelos.PojoBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArchivoCSV<PK, T extends Pojo<PK>> implements Persistencia<PK, T>
{
	private final PojoBuilder<PK, T> constructorPojo;
	private final String cabecera;
	private final Path ruta;
	private final Field[] campos;

	public ArchivoCSV(Class<T> clase, String ruta)
	{
		constructorPojo = new PojoBuilder<>(clase);
		StringBuilder stb = new StringBuilder();

		this.campos = clase.getDeclaredFields();

		for (Field campo : campos)
		{
			stb.append(campo.getName());
			stb.append(',');
		}

		stb.deleteCharAt(stb.lastIndexOf(","));
		stb.append('\n');

		this.cabecera = stb.toString();
		this.ruta = Path.of(ruta);
	}

	@Override
	public List<T> cargar()
	{
		List<T> carga = new ArrayList<>();

		try
		{
			ArrayList<String> lines = new ArrayList<>(Files.readAllLines(ruta));

			for (int l = 1; l < lines.size(); ++l)
			{
				T tmp = constructorPojo.newPojo();
				String csvLine = lines.get(l);
				String[] csvAttb = csvLine.split(",");

				for (int i = 0; i < campos.length; ++i)
				{
					campos[i].setAccessible(true);
					campos[i].set(tmp, constructorPojo.parseField(campos[i].getType(), csvAttb[i]));
					campos[i].setAccessible(false);
				}

				carga.add(tmp);
			}

		} catch (IllegalAccessException | IOException e)
		{
			throw new RuntimeException(e);
		}
		return carga;
	}

	@Override
	public void guardar(List<T> guardado)
	{
		StringBuilder stb = new StringBuilder(cabecera);

		for (T i : guardado)
		{
			for (Field j : campos)
			{
				try
				{
					j.setAccessible(true);
					stb.append(j.get(i));
					j.setAccessible(false);
					stb.append(',');
				} catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}

			stb.deleteCharAt(stb.lastIndexOf(","));
			stb.append('\n');
		}

		try (FileWriter fw = new FileWriter(ruta.toFile()))
		{
			fw.write(stb.toString());
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
