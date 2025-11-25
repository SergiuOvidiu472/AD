package A_Modelos;

import java.io.*;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HexFormat;

public class BlobHEX implements Blob, Serializable
{
	private final byte[] bytes;

	public BlobHEX(Path ruta)
	{
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(ruta.toFile())))
		{
			this.bytes = bis.readAllBytes();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public BlobHEX(String hexString)
	{
		bytes = HexFormat.of().parseHex(hexString);
	}

	public byte[] getRawBytes()
	{
		return bytes;
	}

	@Override
	public String toString()
	{
		StringBuilder stb = new StringBuilder();
		for (byte b : bytes)
		{
			stb.append(String.format("%02x", b));
		}
		return stb.toString();
	}

	@Override
	public long length() throws SQLException
	{
		return bytes.length;
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException
	{
		return new byte[0];
	}

	@Override
	public InputStream getBinaryStream() throws SQLException
	{
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException
	{
		return 0;
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException
	{
		return 0;
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException
	{
		return 0;
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
	{
		return 0;
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException
	{
		return null;
	}

	@Override
	public void truncate(long len) throws SQLException
	{

	}

	@Override
	public void free() throws SQLException
	{

	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException
	{
		return new ByteArrayInputStream(bytes);
	}
}
