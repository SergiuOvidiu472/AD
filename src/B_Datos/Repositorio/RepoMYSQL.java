package B_Datos.Repositorio;

import A_Modelos.Pojo;
import A_Modelos.PojoBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class RepoMYSQL<PK, T extends Pojo<PK>> implements Repositorio<PK, T>
{
	private final PojoBuilder<PK, T> constructor;
	private final ArrayList<Field> camposJava;
	private final ArrayList<String> nombreCamposJava;
	private final ArrayList<Type> tiposCamposJava;
	private final String nombreClaseJava;

	private final Connection IO;

	private final StringBuilder insert;
	private final StringBuilder select;
	private final StringBuilder update;
	private final StringBuilder delete;
	private final StringBuilder all;

	public RepoMYSQL(Class<T> clase, Connection IO)
	{
		this.nombreClaseJava = clase.getSimpleName();
		this.constructor = new PojoBuilder<>(clase);
		this.camposJava = new ArrayList<>(Arrays.asList(clase.getDeclaredFields()));
		this.nombreCamposJava = new ArrayList<>();
		this.tiposCamposJava = new ArrayList<>();

		for (Field field : camposJava)
		{
			nombreCamposJava.add(field.getName());
			tiposCamposJava.add(field.getType());
		}

		if (!nombreCamposJava.getFirst().equals("ID"))
		{
			throw new IllegalArgumentException("Esta implementación asume que todas las clases tienen la llave primaria como primer atributo con nombre 'ID'\n");
		}

		insert = new StringBuilder();
		select = new StringBuilder();
		update = new StringBuilder();
		delete = new StringBuilder();
		all = new StringBuilder();
		this.IO = IO;
	}

	private void setInsertStatement()
	{
		insert.append("INSERT INTO ");
		insert.append(nombreClaseJava);
		insert.append(" (");

		for (String x : nombreCamposJava)
		{
			insert.append(x);
			insert.append(",");
		}

		int lastComma = insert.lastIndexOf(",");
		insert.replace(lastComma, lastComma + 1, ") VALUES (");
		insert.repeat("?,", camposJava.size());
		lastComma = insert.lastIndexOf(",");
		insert.replace(lastComma, lastComma + 1, ")");
	}

	private void setSelectStatement()
	{
		select.append("SELECT * FROM ");
		select.append(nombreClaseJava);
		select.append(" WHERE ID=?");
	}

	private void setUpdateStatement()
	{
		update.append("UPDATE ");
		update.append(nombreClaseJava);
		update.append(" SET ");

		for (int i = 1; i < nombreCamposJava.size(); ++i)
		{
			update.append(nombreCamposJava.get(i));
			update.append("=?, ");
		}

		int lastComma = update.lastIndexOf(", ");
		update.replace(lastComma, lastComma + 2, " ");
		update.append("WHERE ID=?");
	}

	private void setDeleteStatement()
	{
		delete.append("DELETE FROM ");
		delete.append(nombreClaseJava);
		delete.append(" WHERE ID=?");
	}

	private void setAllStatement()
	{
		all.append("SELECT * FROM ");
		all.append(nombreClaseJava);
	}

	@Override
	public List<T> abrir()
	{
		setInsertStatement();
		setSelectStatement();
		setUpdateStatement();
		setDeleteStatement();
		setAllStatement();
		return List.of();
	}

	@Override
	public void insertar(T pojo)
	{
		try (PreparedStatement ps = IO.prepareStatement(insert.toString()))
		{
			for (int i = 0, j = 1; i < camposJava.size(); ++i, ++j)
			{
				Field campo = camposJava.get(i);

				if (campo.getClass().getSuperclass().equals(java.sql.Blob.class))
				{
					campo.setAccessible(true);
					Blob b = (Blob) campo.get(pojo);
					campo.setAccessible(false);
					ps.setBlob(j, b.getBinaryStream());
				} else
				{
					campo.setAccessible(true);
					String value = String.valueOf(campo.get(pojo));
					campo.setAccessible(false);
					ps.setString(j, value);
				}
			}

			ps.executeUpdate();
		} catch (SQLException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public T buscar(PK pk)
	{
		T pojo = constructor.newPojo();
		int totalCampos = camposJava.size();

		try (PreparedStatement ps = IO.prepareStatement(select.toString()))
		{
			ps.setString(1, String.valueOf(pk));

			ResultSet rs = ps.executeQuery();
			rs.close();

			boolean found = rs.next();
			int i = 0;

			while (i < totalCampos && found)
			{
				String strVal = rs.getString(nombreCamposJava.get(i));
				Field campo = camposJava.get(i);
				Type type = tiposCamposJava.get(i);
				Object value = constructor.parseField(type, strVal);

				campo.setAccessible(true);
				campo.set(pojo, value);
				campo.setAccessible(false);

				++i;
			}

		} catch (SQLException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}

		return pojo;
	}

	@Override
	public void update(T pojo)
	{
		try (PreparedStatement ps = IO.prepareStatement(update.toString()))
		{
			int k;
			for (k = 1; k < camposJava.size(); ++k)
			{
				Field campo = camposJava.get(k);

				campo.setAccessible(true);
				String value = String.valueOf(campo.get(pojo));
				campo.setAccessible(false);

				ps.setString(k, String.valueOf(value));
			}

			ps.setString(k, String.valueOf(pojo.getPK()));
			ps.executeUpdate();

		} catch (SQLException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public T delete(PK pk)
	{
		T pojo = buscar(pk);

		try (PreparedStatement ps = IO.prepareStatement(delete.toString()))
		{
			ps.setString(1, String.valueOf(pk));
			ps.executeUpdate();
		} catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		return pojo;
	}

	@Override
	public List<T> filtrar(Predicate<T> filtro)
	{
		ArrayList<T> allPojos = new ArrayList<>();

		try (PreparedStatement ps = IO.prepareStatement(all.toString()))
		{
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				T pojo = constructor.newPojo();
				int i = 0;
				for (Field campo : camposJava)
				{
					Type type = campo.getType();
					String q = rs.getString(nombreCamposJava.get(i));
					Object value = constructor.parseField(type, q);

					campo.setAccessible(true);
					campo.set(pojo, value);
					campo.setAccessible(false);

					++i;
				}

				allPojos.add(pojo);
			}

			rs.close();

		} catch (SQLException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}

		return new ArrayList<>(allPojos.parallelStream().filter(filtro).toList());
	}

	@Override
	public void cerrar()
	{

	}
}
