package A_Modelos;

import java.io.Serializable;
import java.util.List;

public interface Pojo<PK> extends Serializable
{
	public PK getPK();

	public List<Object> getFK();
}
