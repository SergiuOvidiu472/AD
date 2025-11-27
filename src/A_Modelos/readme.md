### Modelos
Para que un modelo sea válido, la clase debe de tener:
- Al menos un constructor vacío, siempre
- Mínimo dos atributos
- Una lista de claves foráneas con una secuencia de pares tipo (clase padre, clave primaria de esa clase, ...)
- Ningún atributo constante (final).
- getFK nunca puede devolver null, aunque no tenga claves foráneas, al menos devolver una lista vacía

En la capa de datos, para persistir y tratar los modelos, la API de java reflection deberá de acceder a los campos de cada objeto.<br>
Podrá acceder a ellos o cambiarlos aunque sean privados, pero si son constantes, no.<br>
Deben de implementar la interfaz pojo para que puedan devolver su clave primaria y foráneas.<br>

### Limitaciones
- Se asume que las claves primarias son inmutables, (no existen setters para estos), será imposible hacer update porque la capa de servicios lo rechazará siempre
- Una vez exportados en persistencia, no se podrán cambiar los campos o reordenarlos dentro de la clase sin borrar todos los datos anteriores 
- Solo hay una implementación para mysql, que requiere tener la clave primaria como el primer atributo de la clase llamado "ID"

### Recomendaciones
- Modelar la base de datos o sistema y normalizar antes de crear las clases.
