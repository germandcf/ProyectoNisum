# Evidencia de Pruebas Unitarias - API de Registro de Usuarios

## Archivos de Pruebas

### Archivos de Test
1. `src/test/java/com/nisum/userservice/service/UserServiceTest.java`
   - Contiene las pruebas del servicio de usuario
   - 7 métodos de prueba
   - Verifica operaciones CRUD y validaciones

2. `src/test/java/com/nisum/userservice/service/validator/UserValidatorTest.java`
   - Contiene las pruebas del validador de usuario
   - 6 métodos de prueba
   - Verifica todas las reglas de validación

3. `src/test/java/com/nisum/userservice/controller/UserControllerTest.java`
   - Contiene las pruebas del controlador
   - 10 métodos de prueba
   - Verifica endpoints y respuestas HTTP

### Archivos de Configuración de Test
1. `src/test/resources/application-test.properties`
   - Configuración específica para pruebas
   - Configuración de base de datos H2
   - Configuraciones de validación

2. `src/test/resources/data.sql`
   - Datos iniciales para pruebas
   - Configuraciones de validación
   - Datos de prueba

### Archivos Generados por las Pruebas
1. `target/test-classes/`
   - Clases compiladas para pruebas
   - Recursos de prueba

2. `target/surefire-reports/`
   - Reportes de ejecución de pruebas
   - Archivos XML con resultados
   - Archivos de texto con logs

3. `target/jacoco/`
   - Reportes de cobertura de código
   - Archivos HTML con detalles de cobertura
   - Archivos XML con métricas

## 1. Pruebas del Servicio de Usuario (UserServiceTest)

### 1.1 Creación de Usuario Exitosa
```
DEBUG SERVICE: Iniciando validación de usuario
DEBUG SERVICE: Lista de errores creada
DEBUG SERVICE: Regex del email: ^[A-Za-z0-9+_.-]+@(.+)$
DEBUG SERVICE: Email a validar: test@example.com
DEBUG SERVICE: Iniciando validación de contraseña
DEBUG SERVICE: Longitud mínima requerida: 8
DEBUG SERVICE: Longitud de la contraseña: 12
DEBUG SERVICE: Patrón de contraseña: ^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{8,}$
DEBUG SERVICE: Todas las validaciones pasaron
```
**Resultado**: ✅ Prueba exitosa
- Se validó correctamente el formato del email
- Se verificó la longitud mínima de la contraseña (8 caracteres)
- Se validó el patrón de la contraseña (números, mayúsculas, minúsculas y caracteres especiales)
- Todas las validaciones pasaron exitosamente

### 1.2 Otras Pruebas del Servicio
- getAllUsers_Success: ✅ Prueba exitosa
- getUserById_Success: ✅ Prueba exitosa
- getUserByEmail_Success: ✅ Prueba exitosa
- updateUser_Success: ✅ Prueba exitosa
- deleteUser_Success: ✅ Prueba exitosa
- updateLastLogin_Success: ✅ Prueba exitosa

## 2. Pruebas del Validador de Usuario (UserValidatorTest)

### 2.1 Validación de Campos Requeridos
```
DEBUG VALIDATOR: Iniciando validación de usuario
DEBUG VALIDATOR: Faltan campos requeridos
```
**Resultado**: ✅ Prueba exitosa
- Se verifica que el sistema detecta cuando faltan campos obligatorios

### 2.2 Validación de Longitud de Contraseña
```
DEBUG VALIDATOR: Iniciando validación de usuario
DEBUG VALIDATOR: Regex del email: ^[A-Za-z0-9+_.-]+@(.+)$
DEBUG VALIDATOR: Email a validar: test@example.com
DEBUG VALIDATOR: Iniciando validación de contraseña
DEBUG VALIDATOR: Longitud mínima requerida: 8
DEBUG VALIDATOR: Longitud de la contraseña: 3
DEBUG VALIDATOR: Contraseña no cumple con la longitud mínima
```
**Resultado**: ✅ Prueba exitosa
- Se verifica que el sistema detecta contraseñas demasiado cortas
- La contraseña debe tener al menos 8 caracteres

### 2.3 Validación de Longitud del Nombre
```
DEBUG VALIDATOR: Iniciando validación de usuario
DEBUG VALIDATOR: Regex del email: ^[A-Za-z0-9+_.-]+@(.+)$
DEBUG VALIDATOR: Email a validar: test@example.com
DEBUG VALIDATOR: Iniciando validación de contraseña
DEBUG VALIDATOR: Longitud mínima requerida: 8
DEBUG VALIDATOR: Longitud de la contraseña: 11
DEBUG VALIDATOR: Nombre no cumple con la longitud mínima
```
**Resultado**: ✅ Prueba exitosa
- Se verifica que el sistema valida la longitud mínima del nombre

### 2.4 Validación de Formato de Email
```
DEBUG VALIDATOR: Iniciando validación de usuario
DEBUG VALIDATOR: Regex del email: ^[A-Za-z0-9+_.-]+@(.+)$
DEBUG VALIDATOR: Email a validar: invalid-email
DEBUG VALIDATOR: Email no cumple con el formato
```
**Resultado**: ✅ Prueba exitosa
- Se verifica que el sistema detecta emails con formato inválido
- El email debe seguir el patrón: ^[A-Za-z0-9+_.-]+@(.+)$

### 2.5 Validación de Email Duplicado
```
DEBUG VALIDATOR: Iniciando validación de usuario
DEBUG VALIDATOR: Regex del email: ^[A-Za-z0-9+_.-]+@(.+)$
DEBUG VALIDATOR: Email a validar: test@example.com
DEBUG VALIDATOR: Email ya está registrado
```
**Resultado**: ✅ Prueba exitosa
- Se verifica que el sistema detecta emails duplicados

## Resumen de Resultados

### UserServiceTest
- Total de pruebas: 7
- Pruebas exitosas: 7
- Fallos: 0
- Errores: 0
- Tiempo de ejecución: 0.109s

### UserValidatorTest
- Total de pruebas: 6
- Pruebas exitosas: 6
- Fallos: 0
- Errores: 0

## Conclusión
Todas las pruebas unitarias han sido ejecutadas exitosamente, verificando:
- Validación de campos requeridos
- Validación de formato de email
- Validación de longitud de contraseña
- Validación de patrón de contraseña
- Validación de longitud de nombre
- Validación de unicidad de email
- Operaciones CRUD básicas

El sistema cumple con todos los requisitos de validación establecidos. 