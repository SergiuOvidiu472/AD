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
Es aconsejable modelar la base de datos o sistema y normalizar antes de crear las clases.<br>
