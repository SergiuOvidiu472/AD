package B_Datos.Persistencia;

import A_Modelos.Pojo;
import A_Modelos.PojoBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArchivoXML<PK, T extends Pojo<PK>> implements Persistencia<PK, T>
{
	private final PojoBuilder<PK, T> constructor;
	private final String nombreClase;
	private final String ruta;
	private final String rootname;
	private final Field[] campos;

	private Document docCargar;
	private Document docGuardar;

	public ArchivoXML(Class<T> clase, String ruta, String rootname)
	{
		this.constructor = new PojoBuilder<>(clase);
		this.nombreClase = clase.getSimpleName();
		this.campos = clase.getDeclaredFields();
		this.ruta = ruta;
		this.rootname = rootname;
	}

	private void abrirParaCargar()
	{
		DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder dBuilder = dbF.newDocumentBuilder();
			docCargar = dBuilder.parse(ruta);
		} catch (ParserConfigurationException | IOException | SAXException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<T> cargar()
	{
		List<T> carga = new ArrayList<>();
		abrirParaCargar();

		try
		{
			NodeList objetosXML = docCargar.getElementsByTagName(nombreClase);

			for (int i = 0; i < objetosXML.getLength(); ++i)
			{
				Element objXML = (Element) objetosXML.item(i);
				T objJava = constructor.newPojo();

				for (Field campoJava : campos)
				{
					Type tipoCampoJava = campoJava.getType();

					String tagXML = campoJava.getName();
					String valorXML = objXML.getElementsByTagName(tagXML).item(0).getTextContent();

					campoJava.setAccessible(true);
					campoJava.set(objJava, constructor.parseField(tipoCampoJava, valorXML));
					campoJava.setAccessible(false);
				}

				carga.add(objJava);
			}
		} catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}

		return carga;
	}

	private void abrirParaGuardar()
	{
		DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder dbB = dbF.newDocumentBuilder();
			docGuardar = dbB.newDocument();
		} catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void guardar(List<T> guardado)
	{
		abrirParaGuardar();
		Element rootXML = docGuardar.createElement(rootname);

		try
		{
			docGuardar.appendChild(rootXML);

			for (T obj : guardado)
			{
				Element objetoXML = docGuardar.createElement(nombreClase);

				for (Field campoJava : campos)
				{
					String nombreCampoXML = campoJava.getName();
					Element campoXML = docGuardar.createElement(nombreCampoXML);

					campoJava.setAccessible(true);
					String valorCampoJava = String.valueOf(campoJava.get(obj));
					campoJava.setAccessible(false);

					Text valorXML = docGuardar.createTextNode(valorCampoJava);
					campoXML.appendChild(valorXML);
					objetoXML.appendChild(campoXML);
				}

				rootXML.appendChild(objetoXML);
			}

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.transform(new DOMSource(docGuardar), new StreamResult(ruta));
		} catch (TransformerException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
