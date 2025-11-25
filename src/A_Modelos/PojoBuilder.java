package A_Modelos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

public class PojoBuilder<PK, T extends Pojo<PK>>
{
	private final Constructor<T> constructor;

	private static class Verificacion<PK, T extends Pojo<PK>>
	{
		private final String classname;

		private Verificacion(String classname)
		{
			this.classname = classname;
		}

		private void verificarTotalCampos(int totalCampos)
		{
			if (totalCampos < 2)
			{
				throw new IllegalArgumentException(classname + ": La clase necesita al menos dos atributos");
			}
		}

		private Constructor<T> verificarConstructorVacio(Class<T> clase)
		{
			Constructor<T> constructor = null;

			try
			{
				constructor = clase.getDeclaredConstructor();
			} catch (NoSuchMethodException e)
			{
				throw new RuntimeException(classname + ": La clase necesita un constructor vacío");
			}
			return constructor;
		}

		private void verificarNullFK(T test)
		{
			if (test.getFK() == null)
			{
				throw new RuntimeException(classname + ": Devuelve null en getFK, podría devolver una lista vacía");
			}
		}

		private void verificarFormatoClavesForáneas(T test)
		{
			List<Object> clavesForaneas = test.getFK();

			int totalElemsFK = clavesForaneas.size();

			int classI = 1;
			int objctI = 0;

			for (Object i : test.getFK())
			{
				if ((classI == 1) && i.getClass().equals(Class.class))
				{
					classI = 0;
					objctI = 1;
				} else if ((objctI == 1) && !i.getClass().equals(Class.class))
				{
					classI = 1;
					objctI = 0;
				} else
				{
					throw new RuntimeException(classname + ": El formato de la llave foránea debe de ser de pares clase refenciada a clave forenea de la misma clase\n");
				}
			}

			if (classI != 1)
			{
				throw new RuntimeException(classname + ": El final de la clave foránea no encaja con el formato\n");
			}
		}
	}

	public PojoBuilder(Class<T> clase)
	{
		String nombreClase = clase.getSimpleName();
		Field[] campos = clase.getDeclaredFields();
		int totalCampos = campos.length;

		Verificacion<PK, T> verificacion = new Verificacion<>(nombreClase);
		verificacion.verificarTotalCampos(totalCampos);
		this.constructor = verificacion.verificarConstructorVacio(clase);

		try
		{
			T test = constructor.newInstance();
			verificacion.verificarNullFK(test);
			verificacion.verificarFormatoClavesForáneas(test);

		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	public T newPojo()
	{
		try
		{
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	// Inspired from: https://github.com/sdc224/Generic-CSV-Reader/blob/master/src/main/java/com/sdcworld/utility/ConvertUtility.java
	public Object parseField(Type type, String x)
	{
		String typeName = type.getTypeName();
		if (typeName.equalsIgnoreCase("int") || typeName.equalsIgnoreCase("integer"))
		{
			return Integer.parseInt(x);
		} else if (typeName.equalsIgnoreCase("byte"))
		{
			return Byte.parseByte(x);
		} else if (typeName.equalsIgnoreCase("short"))
		{
			return Short.parseShort(x);
		} else if (typeName.equalsIgnoreCase("float"))
		{
			return Float.parseFloat(x);
		} else if (typeName.equals("A_Modelos.BlobHEX"))
		{
			return new BlobHEX(x);
		} else
		{
			return x;
		}
	}
}
