# API de Registro de Usuarios
Esta es una API REST desarrollada con Spring Boot que permite el registro y gestión de usuarios.

## Requisitos Previos
- Java 8 o superior
- Maven

## Configuración
1. despues de haber clonado el repositorio y descargado:

2. Asegúrate de que no haya otros servicios usando el puerto 8080. Si es necesario, puedes cambiar el puerto en 'src/main/resources/application.properties'.

## Ejecución
### Spring Boot es bastante flexible y puedes usar el IDE que prefieras para ejecutarlo de manera grafica.
### Usando Maven Wrapper (Recomendado), ya que el proyecto lo tiene con el apache

### ejecucion in bash
./mvnw spring-boot:run

### Usando Maven (si está instalado)
mvn spring-boot:run

###ejecutando el archivo.jar generado
java -jar target/tu-aplicacion.jar

La aplicación se iniciará en http://localhost:8080

## Acceso a la Documentación

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints Disponibles

## se puede probar directamente desde swagger o Postman, un cliente REST

### Registro de Usuario

POST /users
Content-Type: application/json

{
  "name": "Roberto Jiménez",
  "email": "roberto@jimenez.com",
  "password": "MultiPhone1@",
  "phones": [
    {
      "number": "99887766",
      "cityCode": "9",
      "countryCode": "57"
    },
    {
      "number": "33445566",
      "cityCode": "2",
      "countryCode": "58"
    }
  ]
}

### Respuesta Exitosa (201 Created)
{
    "name": "Roberto Jiménez",
    "email": "roberto@jimenez.com",
    "password": "MultiPhone1@",
    "phones": [
        {
            "number": "99887766",
            "cityCode": "9",
            "countryCode": "57"
        },
        {
            "number": "33445566",
            "cityCode": "2",
            "countryCode": "58"
        }
    ],
    "created": "2025-05-28 15:55:01",
    "modified": "2025-05-28 15:55:01",
    "lastLogin": "2025-05-28 15:55:01",
    "token": "b188be37-7235-4fb3-a207-60f6dc285b58",
    "active": true
}


## Validaciones

- El email debe ser único y seguir el formato correcto  (aaaaaaa@dominio.cl)
- La contraseña debe cumplir con los requisitos de seguridad configurada en la base de datos
- Los números de teléfono son opcionales pero deben seguir el formato especificado

## Base de Datos

La aplicación utiliza HSQLDB en memoria. Puedes acceder a la consola de HSQLDB en:
Credenciales:
- JDBC URL: jdbc:hsqldb:mem:api_users_db
- Usuario: sa
- Contraseña: (vacía)

## Solución de Problemas

1. Si el puerto 8080 está en uso:
   - Cambia el puerto en application.properties
   - O identifica y cierra el proceso que está usando el puerto

2. Si Swagger UI no está accesible:
   - Verifica que la aplicación esté corriendo
   - Intenta acceder directamente a http://localhost:8080/swagger-ui/index.html

3. Si encuentras errores de compilación:
   - Asegúrate de tener Java 8 o superior instalado
   - Ejecuta ./mvnw clean install' para limpiar y recompilar el proyecto

## Notas Adicionales

- La aplicación incluye manejo de excepciones global
- Los logs detallados están habilitados para facilitar el debugging
- La base de datos se reinicia cada vez que se inicia la aplicación, no se requiere creacion de tablas.